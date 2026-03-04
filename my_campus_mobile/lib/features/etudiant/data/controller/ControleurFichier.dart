import 'package:flutter/foundation.dart';
import 'package:open_filex/open_filex.dart';

import '../repository/fichier_repository.dart';

class ControleurFichier extends ChangeNotifier {
  final FichierRepository repo;

  ControleurFichier(this.repo);

  bool telechargementEnCours = false;
  double progression = 0.0; // 0..1
  String? dernierChemin;
  String? erreur;

  Future<void> telechargerEtOuvrir({
    required String urlDownload,
    required String nomFichier,
  }) async {
    telechargementEnCours = true;
    progression = 0.0;
    dernierChemin = null;
    erreur = null;
    notifyListeners();

    try {
      final path = await repo.telechargerEtSauvegarder(
        urlDownload: urlDownload,
        nomFichier: nomFichier,
        onProgress: (received, total) {
          if (total > 0) {
            progression = received / total;
            notifyListeners();
          }
        },
      );

      dernierChemin = path;
      notifyListeners();

      await OpenFilex.open(path);
    } catch (e) {
      erreur = e.toString();
      notifyListeners();
      rethrow;
    } finally {
      telechargementEnCours = false;
      notifyListeners();
    }
  }
}
