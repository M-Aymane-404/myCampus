import 'dart:convert';

import 'package:dio/dio.dart';

class RessourceSeanceProfesseurApi {
  RessourceSeanceProfesseurApi(this._dio);

  final Dio _dio;

  Future<List<dynamic>> recupererRessources(int seanceId) async {
    final resp =
        await _dio.get('/api/professeur/seances/$seanceId/ressources');
    return _decodeList(resp.data);
  }

  Future<void> televerserRessource(
    int seanceId,
    MultipartFile fichier,
  ) async {
    final data = FormData.fromMap({
      'fichier': fichier,
    });
    await _dio.post(
      '/api/professeur/seances/$seanceId/ressources',
      data: data,
      options: Options(
        headers: {
          Headers.contentTypeHeader: 'multipart/form-data',
        },
      ),
    );
  }

  List<dynamic> _decodeList(Object? data) {
    final decoded = _decodeData(data);
    return decoded is List ? decoded : const [];
  }

  dynamic _decodeData(Object? data) {
    if (data is String) {
      return jsonDecode(data);
    }
    return data;
  }
}
