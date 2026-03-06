import 'dart:io';

import 'package:dio/dio.dart';

class ProfilEtudiantApi {
  final Dio _dio;
  ProfilEtudiantApi(this._dio);

  Future<Map<String, dynamic>> getMoi() async {
    final resp = await _dio.get('/api/etudiant/moi');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<Map<String, dynamic>> modifierMoi(Map<String, dynamic> body) async {
    final resp = await _dio.put('/api/etudiant/moi/modifier', data: body);
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<void> uploadPhoto(File file) async {
    final formData = FormData.fromMap({
      'photo': await MultipartFile.fromFile(file.path),
    });
    await _dio.put('/api/etudiant/photo', data: formData);
  }
}
