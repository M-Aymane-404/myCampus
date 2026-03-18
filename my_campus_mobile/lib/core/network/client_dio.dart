import 'dart:async';
import 'dart:convert';

import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:flutter/foundation.dart';

import '../../features/authentification/data/service_authentification_api.dart';
import '../config/configuration_app.dart';

class ClientDio {
  static final ClientDio _instance = ClientDio._internal();
  factory ClientDio() => _instance;
  ClientDio._internal();

  static ClientDio get instance => _instance;
  static Dio get dioInstance => _instance.dio;
  
  late final Dio _dio;
  bool _initialise = false;

  late final CookieJar _cookieJar;

  String? _accessToken;
  bool _refreshEnCours = false;
  final List<Future<void> Function(String)> _attenteRefresh = [];

  void _logRequest(RequestOptions options) {
    if (!kDebugMode) return;
    final uri = options.uri;
    debugPrint('DIO REQUEST ${options.method} $uri');
    debugPrint('Headers: ${_safeHeaders(options.headers)}');
    debugPrint('Body: ${_stringifyData(options.data)}');
  }

  void _logError(DioException error) {
    if (!kDebugMode) return;
    final response = error.response;
    final uri = error.requestOptions.uri;
    debugPrint(
      'DIO ERROR ${error.requestOptions.method} $uri → status ${response?.statusCode}',
    );
    debugPrint('Response body: ${_stringifyData(response?.data)}');
    final fieldErrors = _extractFieldErrors(response?.data);
    if (fieldErrors.isNotEmpty) {
      for (final field in fieldErrors) {
        final fieldName = field['field'] ?? field['name'] ?? 'field';
        final message = field['defaultMessage'] ?? field['message'] ?? 'Validation failed';
        debugPrint('Validation: $fieldName → $message');
      }
    }
  }

  Map<String, dynamic> _safeHeaders(Map<String, dynamic> headers) {
    final safe = <String, dynamic>{};
    headers.forEach((key, value) {
      final lower = key.toLowerCase();
      if (lower == 'authorization' && value is String) {
        safe[key] = _redactToken(value);
      } else if (lower == 'cookie') {
        safe[key] = '<redacted>';
      } else {
        safe[key] = value;
      }
    });
    return safe;
  }

  String _redactToken(String value) {
    final trimmed = value.trim();
    if (trimmed.length <= 24) return '<redacted>';
    return '${trimmed.substring(0, 16)}…${trimmed.substring(trimmed.length - 8)}';
  }

  List<Map<String, dynamic>> _extractFieldErrors(dynamic data) {
    if (data is Map && data['fieldErrors'] is List) {
      return (data['fieldErrors'] as List)
          .whereType<Map>()
          .map((item) => item.cast<String, dynamic>())
          .toList();
    }
    if (data is Map && data['errors'] is List) {
      final errors = data['errors'] as List;
      final mapped =
          errors
              .whereType<Map>()
              .map((item) => item.cast<String, dynamic>())
              .toList();
      if (mapped.isNotEmpty) return mapped;
    }
    return [];
  }

  String _stringifyData(dynamic data) {
    if (data == null) return 'null';
    if (data is String) return data;
    if (data is FormData) {
      final parts = data.fields.map((f) => '${f.key}=${f.value}').join(', ');
      return 'FormData(fields: [$parts], files: ${data.files.length})';
    }
    try {
      return const JsonEncoder.withIndent('  ').convert(data);
    } catch (_) {
      return data.toString();
    }
  }

  Dio get dio {
    if (!_initialise) {
      throw Exception(
        "ClientDio non initialisé. Appelle ClientDio().initialiser()",
      );
    }
    return _dio;
  }

  void initialiser() {
    if (_initialise) return;

    _dio = Dio(
      BaseOptions(
        baseUrl: ConfigurationApp.apiBaseUrl,
        connectTimeout: ConfigurationApp.connexionTimeout,
        receiveTimeout: ConfigurationApp.receptionTimeout,
        headers: ConfigurationApp.entetesParDefaut,
      ),
    );

    _cookieJar = CookieJar();
    _dio.interceptors.add(CookieManager(_cookieJar));

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          final path = options.path;

          final isAuthEndpoint =
              path.contains('/auth/connecter') ||
              path.contains('/auth/rafraichir-jeton') ||
              path.contains('/auth/deconnexion');

            if (!isAuthEndpoint) {
              final token = _accessToken;
              if (token != null && token.isNotEmpty) {
                options.headers['Authorization'] = 'Bearer $token';
              }
            }

            _logRequest(options);
            handler.next(options);
          },

          onError: (e, handler) async {
            final path = e.requestOptions.path;

          final isAuthEndpoint =
              path.contains('/auth/connecter') ||
              path.contains('/auth/rafraichir-jeton') ||
              path.contains('/auth/deconnexion');
            _logError(e);

          if (isAuthEndpoint) {
            return handler.next(e); // pas de refresh ici
          }

          if (e.response?.statusCode != 401) {
            return handler.next(e);
          }

          // refresh déjà en cours => on attend
          if (_refreshEnCours) {
            final completer = Completer<Response>();
            _attenteRefresh.add((newToken) async {
              try {
                e.requestOptions.headers['Authorization'] = 'Bearer $newToken';
                final r = await _dio.fetch(e.requestOptions);
                completer.complete(r);
              } catch (err) {
                completer.completeError(err);
              }
            });
            return handler.resolve(await completer.future);
          }

          _refreshEnCours = true;

          try {
            final authApi =
                ServiceAuthentificationApi(); // utilise ClientDio().dio
            final newToken =
                await authApi
                    .rafraichirJeton(); // POST /auth/rafraichir-jeton (cookie auto)
            setAccessToken(newToken);

            // libère les requêtes en attente
            for (final cb in _attenteRefresh) {
              await cb(newToken);
            }
            _attenteRefresh.clear();

            // rejoue requête initiale
            e.requestOptions.headers['Authorization'] = 'Bearer $newToken';
            final response = await _dio.fetch(e.requestOptions);
            return handler.resolve(response);
          } catch (_) {
            // refresh échoue => session invalide
            viderSessionLocal();
            return handler.next(e);
          } finally {
            _refreshEnCours = false;
          }
        },
      ),
    );

    _initialise = true;
  }

  void setAccessToken(String token) {
    final t = token.trim();
    if (t.startsWith('{') || t.contains('access_token')) {
      throw Exception(
        "Token invalide: tu as passé une Map au lieu d'une String",
      );
    }
    _accessToken = t;
  }

  void viderSessionLocal() {
    _accessToken = null;
    _attenteRefresh.clear();
    _refreshEnCours = false;
    _cookieJar.deleteAll(); // important : supprime refresh cookie côté client
  }
}
