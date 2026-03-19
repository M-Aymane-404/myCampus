import 'package:flutter/foundation.dart';

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

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  List<AnnonceProfesseurDTO> get annonces => _annonces;
  List<AnnonceProfesseurDTO> get annoncesPubliques => _annoncesPubliques;
  AnnonceProfesseurDTO? get annonceSelectionnee => _annonceSelectionnee;
  List<RessourceAnnonceProfesseurDTO> get ressourcesAnnonceSelectionnee =>
      _ressources;
  bool get ressourcesChargees => _ressourcesChargees;

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

      final source = [..._annonces, ..._annoncesPubliques];
      final annoncesTrouvees = source.where((annonce) => annonce.id == id);
      if (annoncesTrouvees.isEmpty) {
        _erreur = 'Annonce introuvable.';
      } else {
        _annonceSelectionnee = annoncesTrouvees.first;
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

    try {
      _ressources = await _repo.recupererRessources(annonceId);
    } catch (e) {
      _ressources = [];
    } finally {
      _ressourcesChargees = true;
      _ressourceAnnonceId = annonceId;
      notifyListeners();
    }
  }

  Future<void> creerAnnonce({
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
    } catch (e) {
      _erreur = e.toString();
      rethrow;
    } finally {
      _chargement = false;
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
