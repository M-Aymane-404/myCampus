import 'package:flutter/foundation.dart';

import '../dto/notification_professeur_dto.dart';
import '../repository/notification_professeur_repository.dart';

class ControleurNotificationProfesseur extends ChangeNotifier {
  ControleurNotificationProfesseur(this._repo);

  final NotificationProfesseurRepository _repo;

  bool _chargement = false;
  String? _erreur;
  int _compteurNonLues = 0;
  List<NotificationProfesseurDTO> _notifications = <NotificationProfesseurDTO>[];

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  int get compteurNonLues => _compteurNonLues;
  List<NotificationProfesseurDTO> get notifications => _notifications;
  bool get aDesNotificationsNonLues => _compteurNonLues > 0;

  Future<void> chargerNotifications() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _notifications = await _repo.recupererNotifications(page: 0, size: 40);
      _compteurNonLues = _notifications.where((n) => !n.lu).length;
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> rafraichirCompteurNonLues() async {
    try {
      _compteurNonLues = await _repo.recupererCompteurNonLues();
      notifyListeners();
    } catch (_) {}
  }

  Future<void> marquerCommeLue(int notificationId) async {
    final index = _notifications.indexWhere((n) => n.id == notificationId);
    if (index == -1) return;
    if (_notifications[index].lu) return;

    _notifications = _notifications
        .map((n) => n.id == notificationId ? n.copyWith(lu: true) : n)
        .toList();
    _compteurNonLues = (_compteurNonLues - 1).clamp(0, 1 << 30);
    notifyListeners();

    try {
      await _repo.marquerCommeLue(notificationId);
    } catch (_) {}
  }

  Future<void> marquerToutesCommeLues() async {
    _notifications = _notifications.map((n) => n.copyWith(lu: true)).toList();
    _compteurNonLues = 0;
    notifyListeners();

    try {
      await _repo.marquerToutesCommeLues();
    } catch (_) {}
  }

  void ajouterNotificationRealtime(NotificationProfesseurDTO notification) {
    final index = _notifications.indexWhere((n) => n.id == notification.id);
    if (index >= 0) {
      _notifications = _notifications
          .map((n) => n.id == notification.id ? notification : n)
          .toList();
    } else {
      _notifications = [notification, ..._notifications];
    }

    _compteurNonLues = _notifications.where((n) => !n.lu).length;
    notifyListeners();
  }
}
