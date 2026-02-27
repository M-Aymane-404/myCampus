import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class StockageSecurise {
  static const FlutterSecureStorage _storage = FlutterSecureStorage();

  static Future<void> sauvegarderToken(String token) async {
    await _storage.write(key: 'auth_token', value: token);
  }

  static Future<String?> recupererToken() async {
    return await _storage.read(key: 'auth_token');
  }

  static Future<void> supprimerToken() async {
    await _storage.delete(key: 'auth_token');
  }

  static Future<void> sauvegarderUtilisateur(String utilisateur) async {
    await _storage.write(key: 'user_data', value: utilisateur);
  }

  static Future<String?> recupererUtilisateur() async {
    return await _storage.read(key: 'user_data');
  }

  static Future<void> supprimerUtilisateur() async {
    await _storage.delete(key: 'user_data');
  }
}
