// etudiant_api.dart
import 'package:dio/dio.dart';

class EtudiantApi {
  final Dio _dio;
  EtudiantApi(this._dio);

  Future<Map<String, dynamic>> getDashboard() async {
    final resp = await _dio.get('/api/etudiant/tableau-de-bord');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<Map<String, dynamic>> getProchaineSeance() async {
    final resp = await _dio.get('/api/etudiant/emploi/prochaine');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<List<dynamic>> getAnnonces() async {
    final resp = await _dio.get('/api/etudiant/annonces');
    return (resp.data as List);
  }
}
