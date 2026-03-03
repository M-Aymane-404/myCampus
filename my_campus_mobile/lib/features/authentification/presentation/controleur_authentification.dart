import 'package:flutter/foundation.dart';

import '../data/depot_authentification.dart';

class ControleurAuthentification extends ChangeNotifier {
  final DepotAuthentification _depot = DepotAuthentification();

  bool _estInitialise = false;
  bool _estAuthentifie = false;
  String _roleUtilisateur = '';
  bool _chargement = false;

  bool get estInitialise => _estInitialise;
  bool get estAuthentifie => _estAuthentifie;
  String get roleUtilisateur => _roleUtilisateur;
  bool get chargement => _chargement;

  Future<void> initialiser() async {
    _chargement = true;
    notifyListeners();

    try {
      _estAuthentifie = await _depot.estAuthentifie();
      if (_estAuthentifie) {
        _roleUtilisateur = (await _depot.recupererRoleUtilisateur()) ?? '';
      }
      _estInitialise = true;
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> seConnecter(String email, String motDePasse) async {
    _chargement = true;
    notifyListeners();

    try {
      // Le dépôt :
      // - appelle /auth/connecter
      // - extrait access_token
      // - injecte le token dans ClientDio
      // - sauvegarde token + rôle
      await _depot.seConnecter(email, motDePasse);

      _estAuthentifie = true;
      _roleUtilisateur = (await _depot.recupererRoleUtilisateur()) ?? '';
    } catch (e) {
      // Permet d’afficher une erreur propre côté UI
      rethrow;
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> seDeconnecter() async {
    _chargement = true;
    notifyListeners();

    try {
      await _depot.seDeconnecter();
      _estAuthentifie = false;
      _roleUtilisateur = '';
      _estInitialise = true; // reste initialisé
    } catch (e) {
      debugPrint('Erreur de déconnexion: $e');
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }
}
