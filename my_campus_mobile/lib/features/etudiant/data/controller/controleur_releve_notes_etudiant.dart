import 'package:flutter/foundation.dart';

import '../dto/releve_notes_dto.dart';
import '../repository/releve_notes_repo.dart';

class ControleurReleveNotesEtudiant extends ChangeNotifier {
  final ReleveNotesRepo repo;

  ControleurReleveNotesEtudiant({required this.repo});

  bool chargement = false;
  String? erreur;
  ReleveNotesDTO? releve;

  Future<void> charger(String semestre) async {
    chargement = true;
    erreur = null;
    notifyListeners();

    try {
      releve = await repo.chargerReleveNotes(semestre);
    } catch (e) {
      erreur = e.toString();
    } finally {
      chargement = false;
      notifyListeners();
    }
  }
}
