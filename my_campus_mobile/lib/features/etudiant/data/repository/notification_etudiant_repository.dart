import '../api/notification_etudiant_api.dart';
import '../dto/notification_etudiant_dto.dart';

class NotificationEtudiantRepository {
  final NotificationEtudiantApi _api;
  NotificationEtudiantRepository(this._api);

  Future<List<NotificationEtudiantDTO>> recupererNotifications({
    int page = 0,
    int size = 20,
  }) async {
    final data = await _api.recupererNotifications(page: page, size: size);
    final content = (data['content'] as List?) ?? const [];
    return content
        .map(
          (e) => NotificationEtudiantDTO.fromJson(
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
