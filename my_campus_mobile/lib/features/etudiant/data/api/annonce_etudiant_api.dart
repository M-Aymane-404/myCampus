import 'package:dio/dio.dart';

class AnnonceEtudiantApi {
  final Dio _dio;
  AnnonceEtudiantApi(this._dio);

  Future<List<dynamic>> recupererAnnonces() async {
    final resp = await _dio.get('/api/etudiant/annonces');
    return resp.data as List<dynamic>;
  }

  Future<Map<String, dynamic>> recupererAnnonceParId(int id) async {
    final resp = await _dio.get('/api/etudiant/annonces/$id');
    return (resp.data as Map).cast<String, dynamic>();
  }
}
