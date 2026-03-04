import 'package:flutter/foundation.dart';

import '../dto/cours_etudiant_dto.dart';
import '../repository/etudiant_repository.dart';

class ControleurCoursEtudiant extends ChangeNotifier {
  final EtudiantRepository _repo;
  ControleurCoursEtudiant(this._repo);

  bool chargement = false;
  String? erreur;

  List<ModuleSortieEtudiantDTO> modules = [];
  List<MatiereSortieEtudiantDTO> matieres = [];
  List<SeanceSortieEtudiantDTO> seances = [];
  List<FichierSeanceSortieEtudiantDTO> ressources = [];

  ModuleSortieEtudiantDTO? moduleSelectionne;
  MatiereSortieEtudiantDTO? matiereSelectionnee;
  SeanceSortieEtudiantDTO? seanceSelectionnee;

  String filtreTypeSeance = 'TOUS';
  String recherche = '';

  EtudiantRepository get repo => _repo;

  Future<void> chargerModules() async {
    chargement = true;
    erreur = null;
    notifyListeners();

    try {
      modules = await _repo.trouverModules();

      // Sélection stable (évite bug Dropdown)
      if (modules.isNotEmpty) {
        moduleSelectionne =
            _trouverModuleParCode(moduleSelectionne?.codeModule) ??
            modules.first;
        await chargerMatieres(moduleSelectionne!.codeModule);
      } else {
        _viderTout();
      }
    } catch (e) {
      erreur = e.toString();
    } finally {
      chargement = false;
      notifyListeners();
    }
  }

  Future<void> chargerMatieres(String codeModule) async {
    try {
      matieres = await _repo.trouverMatieres(codeModule);

      if (matieres.isNotEmpty) {
        matiereSelectionnee =
            _trouverMatiereParCode(matiereSelectionnee?.codeMatiere) ??
            matieres.first;
        await chargerSeances(matiereSelectionnee!.codeMatiere);
      } else {
        seances = [];
        ressources = [];
        seanceSelectionnee = null;
      }
      notifyListeners();
    } catch (e) {
      erreur = e.toString();
      notifyListeners();
    }
  }

  Future<void> chargerSeances(String codeMatiere) async {
    try {
      final typeSeance = (filtreTypeSeance == 'TOUS') ? null : filtreTypeSeance;
      seances = await _repo.trouverSeances(codeMatiere, typeSeance: typeSeance);

      if (seances.isNotEmpty) {
        seanceSelectionnee =
            _trouverSeanceParId(seanceSelectionnee?.id) ?? seances.first;
        await chargerRessources(seanceSelectionnee!.id);
      } else {
        ressources = [];
        seanceSelectionnee = null;
      }
      notifyListeners();
    } catch (e) {
      erreur = e.toString();
      notifyListeners();
    }
  }

  Future<void> chargerRessources(int seanceId) async {
    try {
      ressources = await _repo.trouverRessources(seanceId);
    } catch (e) {
      erreur = e.toString();
    } finally {
      notifyListeners();
    }
  }

  Future<void> selectionnerModule(ModuleSortieEtudiantDTO module) async {
    if (moduleSelectionne?.codeModule == module.codeModule) return;

    moduleSelectionne = module;
    matiereSelectionnee = null;
    seanceSelectionnee = null;
    matieres = [];
    seances = [];
    ressources = [];
    notifyListeners();

    await chargerMatieres(module.codeModule);
  }

  Future<void> selectionnerMatiere(MatiereSortieEtudiantDTO matiere) async {
    if (matiereSelectionnee?.codeMatiere == matiere.codeMatiere) return;

    matiereSelectionnee = matiere;
    seanceSelectionnee = null;
    seances = [];
    ressources = [];
    notifyListeners();

    await chargerSeances(matiere.codeMatiere);
  }

  Future<void> selectionnerSeance(SeanceSortieEtudiantDTO seance) async {
    if (seanceSelectionnee?.id == seance.id) return;
    seanceSelectionnee = seance;
    notifyListeners();
    await chargerRessources(seance.id);
  }

  Future<void> changerFiltreTypeSeance(String filtre) async {
    if (filtreTypeSeance == filtre) return;
    filtreTypeSeance = filtre;
    seanceSelectionnee = null;
    ressources = [];
    notifyListeners();

    if (matiereSelectionnee != null) {
      await chargerSeances(matiereSelectionnee!.codeMatiere);
    }
  }

  void setRecherche(String texte) {
    recherche = texte;
    notifyListeners();
  }

  List<FichierSeanceSortieEtudiantDTO> get ressourcesFiltrees {
    final q = recherche.trim().toLowerCase();
    if (q.isEmpty) return ressources;
    return ressources
        .where((f) => f.nomOriginal.toLowerCase().contains(q))
        .toList();
  }

  Future<void> reinitialiser() async {
    _viderTout();
    filtreTypeSeance = 'TOUS';
    recherche = '';
    erreur = null;
    notifyListeners();
    await chargerModules();
  }

  void _viderTout() {
    modules = [];
    matieres = [];
    seances = [];
    ressources = [];
    moduleSelectionne = null;
    matiereSelectionnee = null;
    seanceSelectionnee = null;
  }

  ModuleSortieEtudiantDTO? _trouverModuleParCode(String? code) {
    if (code == null) return null;
    for (final m in modules) {
      if (m.codeModule == code) return m;
    }
    return null;
  }

  MatiereSortieEtudiantDTO? _trouverMatiereParCode(String? code) {
    if (code == null) return null;
    for (final m in matieres) {
      if (m.codeMatiere == code) return m;
    }
    return null;
  }

  SeanceSortieEtudiantDTO? _trouverSeanceParId(int? id) {
    if (id == null) return null;
    for (final s in seances) {
      if (s.id == id) return s;
    }
    return null;
  }
}
