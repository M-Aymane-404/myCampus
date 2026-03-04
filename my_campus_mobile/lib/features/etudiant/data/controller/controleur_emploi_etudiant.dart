import 'package:flutter/foundation.dart';

import '../api/etudiant_api.dart';
import '../dto/emploi_du_temps_etudiant_dto.dart';

class ControleurEmploiEtudiant extends ChangeNotifier {
  final EtudiantApi api;

  ControleurEmploiEtudiant({required this.api});

  bool chargement = false;
  String? erreur;
  EmploiDuTempsEtudiantDTO? emploi;

  Future<void> charger(String semestre) async {
    chargement = true;
    erreur = null;
    notifyListeners();

    try {
      emploi = await api.getEmploi(semestre);
    } catch (e) {
      erreur = e.toString();
    } finally {
      chargement = false;
      notifyListeners();
    }
  }
}
