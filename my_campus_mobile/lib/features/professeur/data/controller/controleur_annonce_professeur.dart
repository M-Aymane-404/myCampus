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
  AnnonceProfesseurDTO? _annonceSelectionnee;
  List<RessourceAnnonceProfesseurDTO> _ressources =
      <RessourceAnnonceProfesseurDTO>[];
  bool _ressourcesChargees = false;
  int? _ressourceAnnonceId;

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  List<AnnonceProfesseurDTO> get annonces => _annonces;
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

      final annoncesTrouvees =
          _annonces.where((annonce) => annonce.id == id);
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
}
