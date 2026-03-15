import 'dart:io';
import 'dart:convert';

import 'package:dio/dio.dart';

class ProfesseurApi {
  ProfesseurApi(this._dio);

  final Dio _dio;

  Future<Map<String, dynamic>> recupererMoi() async {
    final response = await _dio.get('/api/professeur/moi');
    return (response.data as Map).cast<String, dynamic>();
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
    return response.data as List<dynamic>;
  }

  Future<List<dynamic>> obtenirEmploi() async {
    final response = await _dio.get('/api/professeur/emploi');
    final data = response.data;

    if (data is String) {
      final parsed = jsonDecode(data);
      if (parsed is List) return parsed;
      if (parsed is Map && parsed['slots'] is List) return parsed['slots'] as List;
      if (parsed is Map && parsed['seances'] is List) return parsed['seances'] as List;
      return const [];
    }

    if (data is List) return data;
    if (data is Map && data['slots'] is List) return data['slots'] as List;
    if (data is Map && data['seances'] is List) return data['seances'] as List;
    return const [];
  }

  Future<Map<String, dynamic>> obtenirProchaineSeance() async {
    final response = await _dio.get('/api/professeur/prochaine');
    return (response.data as Map).cast<String, dynamic>();
  }

  Future<Map<String, dynamic>> chargerFiltresExamens() async {
    final response = await _dio.get('/api/professeur/examens/filtres');
    return (response.data as Map).cast<String, dynamic>();
  }

  Future<List<dynamic>> listerExamens(
    String codeClasse,
    String codeMatiere,
  ) async {
    final response = await _dio.get(
      '/api/professeur/examens/classes/$codeClasse/matieres/$codeMatiere',
    );
    return response.data as List<dynamic>;
  }
}
