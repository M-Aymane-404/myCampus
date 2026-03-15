import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../data/depot_authentification.dart';

class ControleurAuthentification extends ChangeNotifier {
  final DepotAuthentification _depot = DepotAuthentification();

  bool _estInitialise = false;
  bool _estAuthentifie = false;
  String _roleUtilisateur = '';
  bool _chargement = false;
  String? _erreur;

  bool get estInitialise => _estInitialise;
  bool get estAuthentifie => _estAuthentifie;
  String get roleUtilisateur => _roleUtilisateur;
  bool get chargement => _chargement;
  String? get erreur => _erreur;

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
    _erreur = null;
    notifyListeners();

    try {
      await _depot.seConnecter(email, motDePasse);

      _estAuthentifie = true;
      _roleUtilisateur = (await _depot.recupererRoleUtilisateur()) ?? '';
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
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
      _estInitialise = true;
    } catch (e) {
      debugPrint('Erreur de deconnexion: $e');
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  String _extraireMessageErreur(Object e) {
    if (e is DioException) {
      final status = e.response?.statusCode;
      final data = e.response?.data;
      final path = e.requestOptions.path;
      final isLoginRequest =
          path.contains('/auth/connecter') ||
          path.contains('/auth/connecter-mobile');

      if (isLoginRequest && status == 401) {
        return 'Email ou mot de passe incorrect';
      }

      if (data is Map) {
        final direct =
            data['message'] ??
            data['error'] ??
            data['detail'] ??
            data['title'] ??
            data['description'];
        if (direct is String && direct.trim().isNotEmpty) {
          return direct.trim();
        }

        final erreurs = data['errors'];
        if (erreurs is List && erreurs.isNotEmpty) {
          final first = erreurs.first;
          if (first is String && first.trim().isNotEmpty) {
            return first.trim();
          }
          if (first is Map) {
            final msg =
                first['message'] ?? first['defaultMessage'] ?? first['error'];
            if (msg is String && msg.trim().isNotEmpty) {
              return msg.trim();
            }
          }
        }
      }

      if (status == 400) {
        return 'Requete invalide. Verifiez les informations envoyees';
      }
      if (status == 401) {
        return 'Email ou mot de passe incorrect';
      }
      if (status == 403) {
        return 'Action non autorisee';
      }
      if (status == 404) {
        return 'Ressource introuvable';
      }
      if (status == 500) {
        return 'Erreur serveur. Reessayez plus tard';
      }
      return 'Erreur reseau. Verifiez votre connexion';
    }
    return e.toString();
  }
}
