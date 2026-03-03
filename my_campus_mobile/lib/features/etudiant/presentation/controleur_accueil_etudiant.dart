// controleur_accueil_etudiant.dart
import 'package:flutter/foundation.dart';

import '../data/dto/tableau_bord_etudiant_dto.dart';
import '../data/repository/etudiant_repository.dart';

class ControleurAccueilEtudiant extends ChangeNotifier {
  final EtudiantRepository _repo;

  ControleurAccueilEtudiant(this._repo);

  TableauBordEtudiantDTO? _dashboard;
  bool _chargement = false;
  String? _erreur;

  TableauBordEtudiantDTO? get dashboard => _dashboard;
  bool get chargement => _chargement;
  String? get erreur => _erreur;

  Future<void> chargerDashboard() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _dashboard = await _repo.chargerDashboard();
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
    debugPrint("PROCHAINE = ${_dashboard?.prochaineSeance?.matiereNom}");
    debugPrint("PROCHAINE JSON OK");
  }
}
