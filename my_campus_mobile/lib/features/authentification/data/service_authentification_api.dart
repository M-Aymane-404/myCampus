import 'package:dio/dio.dart';

import '../../../core/network/client_dio.dart';

class ServiceAuthentificationApi {
  final Dio _dio = ClientDio().dio;

  Future<Map<String, dynamic>> seConnecter(
    String email,
    String motDePasse,
  ) async {
    try {
      final response = await _dio.post(
        '/auth/connecter-mobile',
        data: {'email': email, 'motDePasse': motDePasse},
        options: Options(
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        ),
      );
      print('POST URL = ${_dio.options.baseUrl}/auth/connecter-mobile');
      print('BODY = ${{'email': email, 'motDePasse': motDePasse}}');
      return Map<String, dynamic>.from(response.data);
    } on DioException catch (e) {
      // Diagnostic complet
      print('DioException.type = ${e.type}');
      print('DioException.message = ${e.message}');
      print('DioException.error = ${e.error}');
      print('DioException.response = ${e.response?.statusCode}');
      print('DioException.data = ${e.response?.data}');
      rethrow;
    }
  }

  Future<String> rafraichirJeton() async {
    final response = await _dio.post('/auth/rafraichir-jeton');

    final accessToken = response.data['access_token']?.toString();
    if (accessToken == null || accessToken.isEmpty) {
      throw Exception("Réponse refresh invalide: access_token manquant");
    }
    return accessToken;
  }

  Future<Map<String, dynamic>> recupererProfil(String token) async {
    final response = await _dio.get(
      '/auth/profile',
      options: Options(headers: {'Authorization': 'Bearer $token'}),
    );
    return Map<String, dynamic>.from(response.data);
  }

  Future<void> seDeconnecter() async {
    await _dio.post(
      '/auth/deconnexion',
      options: Options(extra: {'skipAuth': true}),
    );
  }
}
