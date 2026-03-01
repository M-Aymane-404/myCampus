import 'dart:async';

import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';

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

          handler.next(options);
          print("REQ ${options.method} ${options.uri}");
          print("Authorization = ${options.headers['Authorization']}");
        },

        onError: (e, handler) async {
          final path = e.requestOptions.path;

          final isAuthEndpoint =
              path.contains('/auth/connecter') ||
              path.contains('/auth/rafraichir-jeton') ||
              path.contains('/auth/deconnexion');

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
