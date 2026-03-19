import 'dart:convert';

import 'package:dio/dio.dart';

class AnnonceProfesseurApi {
  final Dio _dio;
  AnnonceProfesseurApi(this._dio);

  Future<List<dynamic>> recupererAnnonces() async {
    final resp = await _dio.get('/api/professeur/annonces/mes-prof-annonces');
    final data = _decodeData(resp.data);
    return data is List ? data : const [];
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
}
