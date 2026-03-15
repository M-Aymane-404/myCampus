import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../dto/professeur_mobile_dto.dart';
import '../repository/professeur_repository.dart';

class ControleurEspaceProfesseur extends ChangeNotifier {
  ControleurEspaceProfesseur(this._repository);

  final ProfesseurRepository _repository;

  bool _chargement = false;
  String? _erreur;
  ProfesseurProfilMobileDTO? _profil;
  List<EnseignementProfesseurMobileDTO> _enseignements = const [];
  List<SeanceProfesseurMobileDTO> _emploi = const [];
  SeanceProfesseurMobileDTO? _prochaineSeance;
  List<ClasseFiltreProfesseurMobileDTO> _classesExamens = const [];
  List<ExamenProfesseurMobileDTO> _examens = const [];
  String? _codeClasseSelectionnee;
  String? _codeMatiereSelectionnee;

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  ProfesseurProfilMobileDTO? get profil => _profil;
  List<EnseignementProfesseurMobileDTO> get enseignements => _enseignements;
  List<SeanceProfesseurMobileDTO> get emploi => _emploi;
  SeanceProfesseurMobileDTO? get prochaineSeance => _prochaineSeance;
  List<ClasseFiltreProfesseurMobileDTO> get classesExamens => _classesExamens;
  List<ExamenProfesseurMobileDTO> get examens => _examens;
  String? get codeClasseSelectionnee => _codeClasseSelectionnee;
  String? get codeMatiereSelectionnee => _codeMatiereSelectionnee;

  Future<void> chargerEspace() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      final results = await Future.wait<dynamic>([
        _repository.chargerProfil(),
        _repository.chargerEnseignements(),
        _repository.chargerEmploi(),
        _repository.chargerProchaineSeance(),
        _repository.chargerFiltresExamens(),
      ]);

      _profil = results[0] as ProfesseurProfilMobileDTO;
      _enseignements = results[1] as List<EnseignementProfesseurMobileDTO>;
      _emploi = results[2] as List<SeanceProfesseurMobileDTO>;
      _prochaineSeance = results[3] as SeanceProfesseurMobileDTO?;
      _classesExamens = results[4] as List<ClasseFiltreProfesseurMobileDTO>;

      if (_classesExamens.isNotEmpty) {
        _codeClasseSelectionnee = _classesExamens.first.codeClasse;
        final matieres = _classesExamens.first.matieres;
        if (matieres.isNotEmpty) {
          _codeMatiereSelectionnee = matieres.first.codeMatiere;
          _examens = await _repository.chargerExamens(
            _codeClasseSelectionnee!,
            _codeMatiereSelectionnee!,
          );
        } else {
          _codeMatiereSelectionnee = null;
          _examens = const [];
        }
      } else {
        _codeClasseSelectionnee = null;
        _codeMatiereSelectionnee = null;
        _examens = const [];
      }
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> selectionnerClasse(String codeClasse) async {
    _codeClasseSelectionnee = codeClasse;
    final classe = _classesExamens.where((e) => e.codeClasse == codeClasse).first;
    _codeMatiereSelectionnee =
        classe.matieres.isNotEmpty ? classe.matieres.first.codeMatiere : null;
    notifyListeners();
    await _rechargerExamensSelection();
  }

  Future<void> selectionnerMatiere(String codeMatiere) async {
    _codeMatiereSelectionnee = codeMatiere;
    notifyListeners();
    await _rechargerExamensSelection();
  }

  Future<void> _rechargerExamensSelection() async {
    final codeClasse = _codeClasseSelectionnee;
    final codeMatiere = _codeMatiereSelectionnee;
    if (codeClasse == null || codeMatiere == null) {
      _examens = const [];
      notifyListeners();
      return;
    }

    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _examens = await _repository.chargerExamens(codeClasse, codeMatiere);
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> modifierProfil(ModifierProfilProfesseurDTO dto) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _profil = await _repository.modifierProfil(dto);
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> modifierPhotoProfil(File file) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _profil = await _repository.changerPhoto(file);
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
        final message =
            data['message'] ??
            data['error'] ??
            data['detail'] ??
            data['title'] ??
            data['description'];
        if (message is String && message.trim().isNotEmpty) {
          return message.trim();
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
