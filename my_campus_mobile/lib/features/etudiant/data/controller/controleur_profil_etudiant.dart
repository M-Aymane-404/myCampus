import 'package:flutter/material.dart';

import '../dto/etudiant_moi_dto.dart';
import '../repository/etudiant_repository.dart';

class ControleurProfilEtudiant extends ChangeNotifier {
  final EtudiantRepository _repo;
  ControleurProfilEtudiant(this._repo);

  bool chargement = false;
  String? erreur;
  EtudiantMoiDTO? moi;

  Future<void> chargerMoi() async {
    chargement = true;
    erreur = null;
    notifyListeners();

    try {
      moi = await _repo.recupererEtudiantConnecte();
    } catch (e) {
      erreur = e.toString();
    } finally {
      chargement = false;
      notifyListeners();
    }
  }
}
