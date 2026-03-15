import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../dto/profil_etudiant_dto.dart';
import '../repository/profil_etudiant_repository.dart';

class ControleurProfilEtudiant extends ChangeNotifier {
  final ProfilEtudiantRepository _repo;
  ControleurProfilEtudiant(this._repo);

  bool _chargement = false;
  String? _erreur;
  ProfilEtudiantDTO? _moi;

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  ProfilEtudiantDTO? get moi => _moi;

  Future<void> chargerMoi() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();
    try {
      _moi = await _repo.chargerMoi();
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> modifierProfil(ModifierProfilEtudiantDTO dto) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();
    try {
      _moi = await _repo.modifierMoi(dto);
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> changerPhoto(File file) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();
    try {
      await _repo.uploadPhoto(file);
      _moi = await _repo.chargerMoi();
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  String _extraireMessageErreur(Object e) {
    if (e is DioException) {
      final status = e.response?.statusCode;
      final data = e.response?.data;

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
            final msg = first['message'] ?? first['defaultMessage'] ?? first['error'];
            if (msg is String && msg.trim().isNotEmpty) {
              return msg.trim();
            }
          }
        }
      }

      if (status == 400 || status == 401) {
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
