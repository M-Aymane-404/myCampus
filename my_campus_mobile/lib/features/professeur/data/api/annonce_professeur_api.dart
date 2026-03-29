import 'dart:convert';

import 'package:dio/dio.dart';

class AnnonceProfesseurApi {
  final Dio _dio;
  AnnonceProfesseurApi(this._dio);

  Future<List<dynamic>> recupererAnnonces() async {
    try {
      final resp = await _dio.get('/api/professeur/annonces/mes-annonces');
      final data = _decodeData(resp.data);
      return data is List ? data : const [];
    } on DioException catch (e) {
      final code = e.response?.statusCode;
      if (code == 404) return const [];
      rethrow;
    }
  }

  Future<List<dynamic>> recupererAnnoncesPubliques() async {
    final resp = await _dio.get('/api/professeur/annonces/mes-dep-et-global');
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

  Future<List<dynamic>> ajouterRessourcesAnnonce(
    int annonceId,
    List<MultipartFile> fichiers,
  ) async {
    final data = FormData();
    for (final fichier in fichiers) {
      data.files.add(MapEntry('fichiers', fichier));
    }

    final resp = await _dio.post(
      '/api/professeur/annonces/$annonceId/ressources',
      data: data,
      options: Options(
        headers: {
          Headers.contentTypeHeader: 'multipart/form-data',
        },
      ),
    );

    final decoded = _decodeData(resp.data);
    return decoded is List ? decoded : const [];
  }

  Future<Map<String, dynamic>> recupererAnnonceParId(int annonceId) async {
    final resp = await _dio.get('/api/professeur/annonces/$annonceId');
    return _decodeMap(resp.data);
  }

  Future<Map<String, dynamic>> creerAnnonce(Map<String, dynamic> body) async {
    final resp = await _dio.post(
      '/api/professeur/annonces',
      data: body,
    );
    return _decodeMap(resp.data);
  }

  Future<void> supprimerAnnonce(int annonceId) async {
    await _dio.delete('/api/professeur/annonces/$annonceId');
  }

  Future<Map<String, dynamic>> publierAnnonce(int annonceId) async {
    final resp = await _putOuPost(
      '/api/professeur/annonces/$annonceId/publier',
    );
    return _decodeMap(resp.data);
  }

  Future<Map<String, dynamic>> depublierAnnonce(int annonceId) async {
    final resp = await _putOuPost(
      '/api/professeur/annonces/$annonceId/depublier',
    );
    return _decodeMap(resp.data);
  }

  Future<Response<dynamic>> _putOuPost(String path) async {
    try {
      return await _dio.put(path);
    } on DioException catch (e) {
      final code = e.response?.statusCode;
      if (code == 404 || code == 405) {
        return await _dio.post(path);
      }
      rethrow;
    }
  }

  dynamic _decodeData(Object? data) {
    if (data is String) {
      final trimmed = data.trim();
      if (trimmed.isEmpty) return null;
      try {
        return jsonDecode(trimmed);
      } catch (_) {
        return data;
      }
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
