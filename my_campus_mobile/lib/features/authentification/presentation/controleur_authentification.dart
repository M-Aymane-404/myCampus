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
    } catch (e) {
      debugPrint('Erreur d\'initialisation: $e');
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> seConnecter(String email, String motDePasse) async {
    _chargement = true;
    notifyListeners();
    
    try {
      await _depot.seConnecter(email, motDePasse);
      _estAuthentifie = true;
      _roleUtilisateur = (await _depot.recupererRoleUtilisateur()) ?? '';
    } catch (e) {
      debugPrint('Erreur de connexion: $e');
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
    } catch (e) {
      debugPrint('Erreur de déconnexion: $e');
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }
}
