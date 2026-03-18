import 'dart:convert';

import 'package:dio/dio.dart';

class AnnonceProfesseurApi {
  final Dio _dio;
  AnnonceProfesseurApi(this._dio);

  Future<List<dynamic>> recupererAnnonces() async {
    final resp = await _dio.get('/api/professeur/annonces');
    final data = _decodeData(resp.data);
    return data is List ? data : const [];
  }

  Future<Map<String, dynamic>> recupererAnnonceParId(int id) async {
    final resp = await _dio.get('/api/professeur/annonces/$id');
    return _decodeMap(resp.data);
  }

  Future<List<dynamic>> recupererRessources(int annonceId) async {
    final resp = await _dio.get(
      '/api/professeur/annonces/$annonceId/ressources',
    );
    final data = _decodeData(resp.data);
    return data is List ? data : const [];
  }

  dynamic _decodeData(Object? data) {
    if (data is String) {
      return jsonDecode(data);
    }
    return data;
  }

  Map<String, dynamic> _decodeMap(Object? data) {
    final decoded = _decodeData(data);
    if (decoded is Map) {
      return decoded.cast<String, dynamic>();
    }
    return <String, dynamic>{};
  }
}
