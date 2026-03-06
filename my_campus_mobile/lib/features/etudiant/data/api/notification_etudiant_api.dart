import 'package:dio/dio.dart';

class NotificationEtudiantApi {
  final Dio _dio;
  NotificationEtudiantApi(this._dio);

  Future<Map<String, dynamic>> recupererNotifications({
    int page = 0,
    int size = 20,
  }) async {
    final resp = await _dio.get(
      '/api/notifications',
      queryParameters: {'page': page, 'size': size},
    );
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<int> recupererCompteurNonLues() async {
    final resp = await _dio.get('/api/notifications/non-lues/compteur');
    final data = (resp.data as Map).cast<String, dynamic>();
    return (data['compteur'] as num?)?.toInt() ?? 0;
  }

  Future<void> marquerCommeLue(int notificationId) async {
    await _dio.patch('/api/notifications/$notificationId/lire');
  }

  Future<void> marquerToutesCommeLues() async {
    await _dio.patch('/api/notifications/lire-tout');
  }
}
