import '../../../core/network/client_dio.dart';

class ServiceAuthentificationApi {
  final ClientDio _clientDio = ClientDio();

  Future<Map<String, dynamic>> seConnecter(
    String email,
    String motDePasse,
  ) async {
    try {
      final response = await _clientDio.dio.post(
        '/auth/login',
        data: {'email': email, 'password': motDePasse},
      );
      return response.data;
    } catch (e) {
      throw Exception('Erreur de connexion: $e');
    }
  }

  Future<Map<String, dynamic>> rafraichirToken(String token) async {
    try {
      final response = await _clientDio.dio.post(
        '/auth/refresh',
        data: {'token': token},
      );
      return response.data;
    } catch (e) {
      throw Exception('Erreur de rafraichissement: $e');
    }
  }

  Future<void> seDeconnecter(String token) async {
    try {
      await _clientDio.dio.post('/auth/logout', data: {'token': token});
    } catch (e) {
      throw Exception('Erreur de déconnexion: $e');
    }
  }
}
