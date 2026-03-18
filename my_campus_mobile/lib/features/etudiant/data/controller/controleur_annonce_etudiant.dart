import 'package:flutter/foundation.dart';

import '../dto/annonce_etudiant_dto.dart';
import '../dto/ressource_annonce_dto.dart';
import '../repository/annonce_etudiant_repository.dart';

class ControleurAnnonceEtudiant extends ChangeNotifier {
  final AnnonceEtudiantRepository _repo;
  ControleurAnnonceEtudiant(this._repo);

  bool _chargement = false;
  String? _erreur;
  List<AnnonceEtudiantDTO> _annonces = <AnnonceEtudiantDTO>[];
  AnnonceEtudiantDTO? _annonceSelectionnee;
  List<RessourceAnnonceDTO> _ressources = <RessourceAnnonceDTO>[];
  bool _ressourcesChargees = false;
  int? _ressourceAnnonceId;

  bool get chargement => _chargement;
  String? get erreur => _erreur;
  List<AnnonceEtudiantDTO> get annonces => _annonces;
  AnnonceEtudiantDTO? get annonceSelectionnee => _annonceSelectionnee;
  List<RessourceAnnonceDTO> get ressourcesAnnonceSelectionnee => _ressources;
  bool get ressourcesChargees => _ressourcesChargees;

  bool get aDesAnnoncesNonLues => _annonces.any((a) => !a.estLue);
  int get nombreNonLues => _annonces.where((a) => !a.estLue).length;

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
      await _repo.marquerCommeLue(id);
      _annonceSelectionnee = await _repo.recupererAnnonceParId(id);
      _ressources = [];
      _ressourcesChargees = false;
      _ressourceAnnonceId = null;

      _annonces = _annonces.map((a) {
        if (a.id == id) return a.copyWith(estLue: true);
        return a;
      }).toList();
    } catch (e) {
      _erreur = e.toString();
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> marquerCommeLue(int id) async {
    await _repo.marquerCommeLue(id);
    _annonces = _annonces.map((a) {
      if (a.id == id) return a.copyWith(estLue: true);
      return a;
    }).toList();
    notifyListeners();
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
