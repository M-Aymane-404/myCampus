import 'package:flutter/foundation.dart';
import 'package:file_picker/file_picker.dart';
import 'package:dio/dio.dart';

import '../dto/annonce_professeur_dto.dart';
import '../dto/ressource_annonce_professeur_dto.dart';
import '../repository/annonce_professeur_repository.dart';

class ControleurAnnonceProfesseur extends ChangeNotifier {
  ControleurAnnonceProfesseur(this._repo);

  final AnnonceProfesseurRepository _repo;
  final Set<int> _idsLus = <int>{};

  bool _chargement = false;
  String? _erreur;
  List<AnnonceProfesseurDTO> _annonces = <AnnonceProfesseurDTO>[];
  List<AnnonceProfesseurDTO> _annoncesPubliques = <AnnonceProfesseurDTO>[];
  AnnonceProfesseurDTO? _annonceSelectionnee;
  List<RessourceAnnonceProfesseurDTO> _ressources =
      <RessourceAnnonceProfesseurDTO>[];
  bool _ressourcesChargees = false;
  int? _ressourceAnnonceId;
  String? _erreurRessources;
  bool _uploadRessourcesEnCours = false;

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  List<AnnonceProfesseurDTO> get annonces => _annonces;
  List<AnnonceProfesseurDTO> get annoncesPubliques => _annoncesPubliques;
  AnnonceProfesseurDTO? get annonceSelectionnee => _annonceSelectionnee;
  List<RessourceAnnonceProfesseurDTO> get ressourcesAnnonceSelectionnee =>
      _ressources;
  bool get ressourcesChargees => _ressourcesChargees;
  String? get erreurRessources => _erreurRessources;
  bool get uploadRessourcesEnCours => _uploadRessourcesEnCours;

  Future<void> chargerAnnonces() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _annonces = await _repo.recupererAnnonces();
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> chargerAnnonceParId(int id) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _idsLus.add(id);
      _ressources = [];
      _ressourcesChargees = false;
      _ressourceAnnonceId = null;
      _erreurRessources = null;

      AnnonceProfesseurDTO? selectionnee;
      final all = [..._annonces, ..._annoncesPubliques];
      for (final annonce in all) {
        if (annonce.id == id) {
          selectionnee = annonce;
          break;
        }
      }
      if (selectionnee == null) {
        final annonce = await _repo.recupererAnnonceParId(id);
        selectionnee = annonce;
        if (!_annoncesPubliques.any((a) => a.id == annonce.id)) {
          _annoncesPubliques = [..._annoncesPubliques, annonce];
        }
      }

      if (selectionnee == null) {
        _erreur = 'Annonce introuvable.';
      } else {
        _annonceSelectionnee = selectionnee;
        _annonces = _annonces.map((a) {
          if (a.id == id) return a.copyWith(estLue: true);
          return a;
        }).toList();
      }
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> chargerRessources(int annonceId) async {
    if (_ressourcesChargees && _ressourceAnnonceId == annonceId) {
      return;
    }

    _ressourcesChargees = false;
    _erreurRessources = null;
    notifyListeners();

    try {
      _ressources = await _repo.recupererRessources(annonceId);
    } catch (e) {
      _ressources = [];
      _erreurRessources = e.toString();
    } finally {
      _ressourcesChargees = true;
      _ressourceAnnonceId = annonceId;
      notifyListeners();
    }
  }

  Future<AnnonceProfesseurDTO> creerAnnonce({
    required String titre,
    required String contenu,
    required String typeAnnonce,
    required String codeClasse,
  }) async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      final annonce = await _repo.creerAnnonce({
        'titre': titre,
        'contenu': contenu,
        'typeAnnonce': typeAnnonce,
        'cibleType': 'ETUDIANTS_CLASSE',
        'codeClasse': codeClasse,
      });
      _annonces = [annonce, ..._annonces];
      return annonce;
    } catch (e) {
      _erreur = e.toString();
      rethrow;
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> ajouterRessourcesAnnonce({
    required int annonceId,
    required List<PlatformFile> fichiers,
  }) async {
    if (fichiers.isEmpty) return;

    _uploadRessourcesEnCours = true;
    notifyListeners();

    try {
      final multiparts = <MultipartFile>[];
      for (final file in fichiers) {
        final path = file.path;
        if (path == null || path.trim().isEmpty) {
          throw Exception('Impossible d’identifier le chemin du fichier "${file.name}"');
        }
        multiparts.add(await MultipartFile.fromFile(path, filename: file.name));
      }

      await _repo.ajouterRessourcesAnnonce(annonceId, multiparts);
    } catch (e) {
      rethrow;
    } finally {
      _uploadRessourcesEnCours = false;
      notifyListeners();
    }
  }

  Future<void> chargerAnnoncesPubliques() async {
    _chargement = true;
    _erreur = null;
    notifyListeners();

    try {
      _annoncesPubliques = await _repo.recupererAnnoncesPubliques();
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }
}
