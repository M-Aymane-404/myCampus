import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';

class ProfesseurApi {
  ProfesseurApi(this._dio);

  final Dio _dio;

  Future<Map<String, dynamic>> recupererMoi() async {
    final response = await _dio.get('/api/professeur/moi');
    return _decodeMap(response.data);
  }

  Future<void> modifierMoi(Map<String, dynamic> body) async {
    await _dio.put('/api/professeur/moi/modifier', data: body);
  }

  Future<void> uploadPhoto(File file) async {
    final formData = FormData.fromMap({
      'photo': await MultipartFile.fromFile(file.path),
    });
    await _dio.put('/api/professeur/photo', data: formData);
  }

  Future<List<dynamic>> listerEnseignements() async {
    final response = await _dio.get('/api/professeur/enseignements');
    final data = _decodeData(response.data);
    return data is List ? data : const [];
  }

  Future<List<dynamic>> obtenirEmploi() async {
    final response = await _dio.get('/api/professeur/emploi');
    final data = _decodeData(response.data);

    if (data is List) return data;
    if (data is Map && data['slots'] is List) return data['slots'] as List;
    if (data is Map && data['seances'] is List) return data['seances'] as List;
    return const [];
  }

  Future<Map<String, dynamic>> obtenirProchaineSeance() async {
    final response = await _dio.get('/api/professeur/prochaine');
    return _decodeMap(response.data);
  }

  Future<Map<String, dynamic>> chargerFiltresExamens() async {
    final response = await _dio.get('/api/professeur/examens/filtres');
    return _decodeMap(response.data);
  }

  Future<List<dynamic>> listerExamens(
    String codeClasse,
    String codeMatiere,
  ) async {
    final response = await _dio.get(
      '/api/professeur/examens/classes/$codeClasse/matieres/$codeMatiere',
    );
    final data = _decodeData(response.data);
    return data is List ? data : const [];
  }

  Future<List<dynamic>> rechercherSeances(Map<String, dynamic> body) async {
    final response = await _dio.post(
      '/api/professeur/seances/recherche',
      data: body,
    );
    final data = _decodeData(response.data);
    return data is List ? data : const [];
  }

  Future<Map<String, dynamic>> chargerFeuillePresence(int seanceId) async {
    final response = await _dio.get(
      '/api/professeur/seances/$seanceId/presences',
    );
    return _decodeMap(response.data);
  }

  Future<Map<String, dynamic>> saisirPresence(
    int seanceId,
    Map<String, dynamic> body,
  ) async {
    final response = await _dio.put(
      '/api/professeur/seances/$seanceId/presences',
      data: body,
    );
    return _decodeMap(response.data);
  }

  Future<Map<String, dynamic>> validerJustificatif(
    int seanceId,
    int presenceId,
  ) async {
    final response = await _dio.patch(
      '/api/professeur/seances/$seanceId/presences/$presenceId/valider-justificatif',
    );
    return _decodeMap(response.data);
  }

  Future<Map<String, dynamic>> refuserJustificatif(
    int seanceId,
    int presenceId,
  ) async {
    final response = await _dio.patch(
      '/api/professeur/seances/$seanceId/presences/$presenceId/refuser-justificatif',
    );
    return _decodeMap(response.data);
  }

  Future<void> creerSeance(Map<String, dynamic> body) async {
    await _dio.post('/api/professeur/seances', data: body);
  }

  Future<void> creerExamen({
    required String codeClasse,
    required String codeMatiere,
    required Map<String, dynamic> body,
  }) async {
    final encodedClasse = Uri.encodeComponent(codeClasse);
    final encodedMatiere = Uri.encodeComponent(codeMatiere);
    await _dio.post(
      '/api/professeur/examens/classes/$encodedClasse/matieres/$encodedMatiere',
      data: body,
    );
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
