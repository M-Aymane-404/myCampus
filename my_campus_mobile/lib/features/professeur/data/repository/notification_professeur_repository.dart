import '../api/notification_professeur_api.dart';
import '../dto/notification_professeur_dto.dart';

class NotificationProfesseurRepository {
  NotificationProfesseurRepository(this._api);

  final NotificationProfesseurApi _api;

  Future<List<NotificationProfesseurDTO>> recupererNotifications({
    int page = 0,
    int size = 20,
  }) async {
    final data = await _api.recupererNotifications(page: page, size: size);
    final content = (data['content'] as List?) ?? const [];
    return content
        .map(
          (e) => NotificationProfesseurDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<int> recupererCompteurNonLues() async {
    return _api.recupererCompteurNonLues();
  }

  Future<void> marquerCommeLue(int notificationId) async {
    await _api.marquerCommeLue(notificationId);
  }

  Future<void> marquerToutesCommeLues() async {
    await _api.marquerToutesCommeLues();
  }
}
