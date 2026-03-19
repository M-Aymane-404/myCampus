import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../dto/professeur_mobile_dto.dart';
import '../dto/professeur_presence_dto.dart';
import '../repository/professeur_repository.dart';
import '../repository/ressource_seance_professeur_repository.dart';

class ControleurEspaceProfesseur extends ChangeNotifier {
  ControleurEspaceProfesseur(this._repository, this._ressourceRepository);

  final ProfesseurRepository _repository;
  final RessourceSeanceProfesseurRepository _ressourceRepository;

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
  List<SeanceProfesseurMobileDTO> _seancesSelectionnees = const [];
  bool _chargementSeancesSelection = false;
  int _copiesACorriger = 0;
  int _coursSansSupport = 0;
  bool _chargementIndicateursDashboard = false;

  String? _enseignementPresenceSelectionne;
  List<SeanceRechercheProfesseurDTO> _seancesPresence = const [];
  int? _seancePresenceSelectionnee;
  FeuillePresenceProfesseurDTO? _feuillePresence;
  bool _chargementSeancesPresence = false;
  bool _chargementFeuillePresence = false;
  final Map<String, bool> _sauvegardePresenceParApogee = {};
  final Map<int, bool> _traitementJustificatifParPresence = {};

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
  List<SeanceProfesseurMobileDTO> get seancesSelectionnees =>
      _seancesSelectionnees.isEmpty ? _emploi : _seancesSelectionnees;
  bool get chargementSeancesSelection => _chargementSeancesSelection;
  int get copiesACorriger => _copiesACorriger;
  int get coursSansSupport => _coursSansSupport;
  bool get chargementIndicateursDashboard => _chargementIndicateursDashboard;

  String? get enseignementPresenceSelectionne =>
      _enseignementPresenceSelectionne;
  List<SeanceRechercheProfesseurDTO> get seancesPresence => _seancesPresence;
  int? get seancePresenceSelectionnee => _seancePresenceSelectionnee;
  FeuillePresenceProfesseurDTO? get feuillePresence => _feuillePresence;
  bool get chargementSeancesPresence => _chargementSeancesPresence;
  bool get chargementFeuillePresence => _chargementFeuillePresence;

  EnseignementProfesseurMobileDTO? get enseignementPresenceSelectionneItem {
    final key = _enseignementPresenceSelectionne;
    if (key == null) return null;
    for (final enseignement in _enseignements) {
      if (_enseignementKey(enseignement) == key) return enseignement;
    }
    return null;
  }

  bool sauvegardePresenceEnCours(String apogee) =>
      _sauvegardePresenceParApogee[apogee] == true;

  bool traitementJustificatifEnCours(int? presenceId) {
    if (presenceId == null) return false;
    return _traitementJustificatifParPresence[presenceId] == true;
  }

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
        _codeMatiereSelectionnee =
            matieres.isNotEmpty ? matieres.first.codeMatiere : null;
        if (_codeMatiereSelectionnee != null) {
          _examens = await _repository.chargerExamens(
            _codeClasseSelectionnee!,
            _codeMatiereSelectionnee!,
          );
        } else {
          _examens = const [];
        }
      } else {
        _codeClasseSelectionnee = null;
        _codeMatiereSelectionnee = null;
        _examens = const [];
      }

      if (_enseignementPresenceSelectionne == null &&
          _enseignements.isNotEmpty) {
        _enseignementPresenceSelectionne = _enseignementKey(
          _enseignements.first,
        );
      }

      _copiesACorriger =
          _examens.where((item) => !item.corrigeDisponible).length;
      _coursSansSupport = 0;
      _chargerIndicateursDashboard();
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> selectionnerClasse(String codeClasse) async {
    _codeClasseSelectionnee = codeClasse;
    final classe =
        _classesExamens.where((e) => e.codeClasse == codeClasse).first;
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

  Future<void> rechercherSeancesPourEnseignement({
    required String codeClasse,
    required String codeMatiere,
  }) async {
    if (codeClasse.isEmpty || codeMatiere.isEmpty) {
      _seancesSelectionnees = const [];
      notifyListeners();
      return;
    }

    _chargementSeancesSelection = true;
    _erreur = null;
    notifyListeners();

    try {
      _seancesSelectionnees = await _repository.rechercherSeances(
        codeClasse: codeClasse,
        codeMatiere: codeMatiere,
      );
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
      _seancesSelectionnees = const [];
    } finally {
      _chargementSeancesSelection = false;
      notifyListeners();
    }
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
      _copiesACorriger =
          _examens.where((item) => !item.corrigeDisponible).length;
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargement = false;
      notifyListeners();
    }
  }

  Future<void> initialiserPresence() async {
    if (_enseignements.isEmpty && !_chargement) {
      await chargerEspace();
    }

    if (_enseignements.isEmpty) return;

    if (_enseignementPresenceSelectionne == null) {
      _enseignementPresenceSelectionne = _enseignementKey(_enseignements.first);
    }

    if (_seancesPresence.isNotEmpty || _chargementSeancesPresence) return;
    await _chargerSeancesPresence(resetSelection: true);
  }

  Future<void> selectionnerEnseignementPresence(String value) async {
    if (_enseignementPresenceSelectionne == value &&
        _seancesPresence.isNotEmpty) {
      return;
    }
    _enseignementPresenceSelectionne = value;
    _seancePresenceSelectionnee = null;
    _feuillePresence = null;
    notifyListeners();
    await _chargerSeancesPresence(resetSelection: true);
  }

  Future<void> selectionnerSeancePresence(int seanceId) async {
    if (_seancePresenceSelectionnee == seanceId && _feuillePresence != null) {
      return;
    }
    _seancePresenceSelectionnee = seanceId;
    notifyListeners();
    await chargerFeuillePresence();
  }

  Future<void> rafraichirPresence() async {
    if (_enseignementPresenceSelectionne == null) {
      await initialiserPresence();
      return;
    }
    await _chargerSeancesPresence(resetSelection: false);
  }

  Future<void> chargerFeuillePresence({bool silencieux = false}) async {
    final seanceId = _seancePresenceSelectionnee;
    if (seanceId == null) return;

    _chargementFeuillePresence = !silencieux;
    _erreur = null;
    if (!silencieux) {
      _feuillePresence = null;
    }
    notifyListeners();

    try {
      _feuillePresence = await _repository.chargerFeuillePresence(seanceId);
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargementFeuillePresence = false;
      notifyListeners();
    }
  }

  Future<void> saisirPresence({
    required String apogee,
    required String status,
  }) async {
    final seanceId = _seancePresenceSelectionnee;
    final feuille = _feuillePresence;
    if (seanceId == null || feuille == null) return;

    final index = feuille.lignes.indexWhere((ligne) => ligne.apogee == apogee);
    if (index < 0) return;

    final precedente = feuille.lignes[index];
    final nouvellesLignes = [...feuille.lignes];
    nouvellesLignes[index] = precedente.copyWith(status: status);
    _feuillePresence = feuille.copyWith(lignes: nouvellesLignes);
    _sauvegardePresenceParApogee[apogee] = true;
    _erreur = null;
    notifyListeners();

    try {
      final updated = await _repository.saisirPresence(
        seanceId: seanceId,
        apogee: apogee,
        status: status,
      );
      _remplacerLignePresence((ligne) => ligne.apogee == apogee, updated);
    } catch (e) {
      _remplacerLignePresence((ligne) => ligne.apogee == apogee, precedente);
      _erreur = _extraireMessageErreur(e);
    } finally {
      _sauvegardePresenceParApogee[apogee] = false;
      notifyListeners();
    }
  }

  Future<void> validerJustificatif(int presenceId) async {
    await _traiterJustificatif(presenceId, approuver: true);
  }

  Future<void> refuserJustificatif(int presenceId) async {
    await _traiterJustificatif(presenceId, approuver: false);
  }

  Future<void> _chargerSeancesPresence({required bool resetSelection}) async {
    final enseignement = enseignementPresenceSelectionneItem;
    if (enseignement == null) return;

    _chargementSeancesPresence = true;
    _chargementFeuillePresence = false;
    _erreur = null;
    _seancesPresence = const [];
    _feuillePresence = null;
    _sauvegardePresenceParApogee.clear();
    _traitementJustificatifParPresence.clear();
    if (resetSelection) {
      _seancePresenceSelectionnee = null;
    }
    notifyListeners();

    try {
      final seances = await _repository.rechercherSeancesPresence(
        codeClasse: enseignement.codeClasse,
        codeMatiere: enseignement.codeMatiere,
      );
      seances.sort((a, b) {
        final aDate = a.heureDebut ?? a.date ?? DateTime(1900);
        final bDate = b.heureDebut ?? b.date ?? DateTime(1900);
        return bDate.compareTo(aDate);
      });
      _seancesPresence = seances;

      final preserveSelection =
          !resetSelection &&
          _seancePresenceSelectionnee != null &&
          seances.any((item) => item.id == _seancePresenceSelectionnee);

      if (!preserveSelection) {
        _seancePresenceSelectionnee =
            seances.isNotEmpty ? seances.first.id : null;
      }
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _chargementSeancesPresence = false;
      notifyListeners();
    }

    if (_seancePresenceSelectionnee != null) {
      await chargerFeuillePresence();
    }
  }

  Future<void> _traiterJustificatif(
    int presenceId, {
    required bool approuver,
  }) async {
    final seanceId = _seancePresenceSelectionnee;
    if (seanceId == null) return;

    _traitementJustificatifParPresence[presenceId] = true;
    _erreur = null;
    notifyListeners();

    try {
      final updated =
          approuver
              ? await _repository.validerJustificatif(
                seanceId: seanceId,
                presenceId: presenceId,
              )
              : await _repository.refuserJustificatif(
                seanceId: seanceId,
                presenceId: presenceId,
              );
      _remplacerLignePresence(
        (ligne) => ligne.idPresence == presenceId,
        updated,
      );
    } catch (e) {
      _erreur = _extraireMessageErreur(e);
    } finally {
      _traitementJustificatifParPresence[presenceId] = false;
      notifyListeners();
    }
  }

  void _remplacerLignePresence(
    bool Function(LignePresenceProfesseurDTO ligne) predicate,
    LignePresenceProfesseurDTO updated,
  ) {
    final feuille = _feuillePresence;
    if (feuille == null) return;
    final lignes = [...feuille.lignes];
    final index = lignes.indexWhere(predicate);
    if (index < 0) return;
    lignes[index] = updated;
    _feuillePresence = feuille.copyWith(lignes: lignes);
  }

  String _enseignementKey(EnseignementProfesseurMobileDTO enseignement) {
    return '${enseignement.codeClasse}|${enseignement.codeMatiere}';
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

  Future<void> _chargerIndicateursDashboard() async {
    _chargementIndicateursDashboard = true;
    notifyListeners();

    try {
      _copiesACorriger = await _compterCopiesACorriger();
      _coursSansSupport = await _compterCoursSansSupportAujourdHui();
    } catch (_) {
      // Conserver les valeurs de secours deja disponibles si une requete echoue.
    } finally {
      _chargementIndicateursDashboard = false;
      notifyListeners();
    }
  }

  Future<int> _compterCopiesACorriger() async {
    final requetes = <Future<List<ExamenProfesseurMobileDTO>>>[];
    for (final classe in _classesExamens) {
      for (final matiere in classe.matieres) {
        requetes.add(
          _repository.chargerExamens(classe.codeClasse, matiere.codeMatiere),
        );
      }
    }

    if (requetes.isEmpty) {
      return _examens.where((item) => !item.corrigeDisponible).length;
    }

    final lots = await Future.wait(requetes);
    return lots
        .expand((items) => items)
        .where((item) => !item.corrigeDisponible)
        .length;
  }

  Future<int> _compterCoursSansSupportAujourdHui() async {
    final seancesDuJour =
        _emploi.where((item) => _jourCorrespond(DateTime.now(), item.jour)).where(
          _estUnCours,
        );

    if (seancesDuJour.isEmpty) return 0;

    var total = 0;
    for (final seance in seancesDuJour) {
      if (seance.supportDisponible == true) {
        continue;
      }
      if ((seance.ressourcesCount ?? 0) > 0) {
        continue;
      }
      if (seance.supportDisponible == false || seance.id == null) {
        total += 1;
        continue;
      }

      final ressources = await _ressourceRepository.recupererRessources(
        seance.id!,
      );
      if (ressources.isEmpty) {
        total += 1;
      }
    }

    return total;
  }

  bool _estUnCours(SeanceProfesseurMobileDTO seance) =>
      seance.typeSeance.trim().toUpperCase().contains('COURS');

  bool _jourCorrespond(DateTime now, String jour) {
    final normalise = _normaliserJour(jour);
    if (normalise.isEmpty) return false;
    return switch (now.weekday) {
      DateTime.monday => normalise == 'LUNDI',
      DateTime.tuesday => normalise == 'MARDI',
      DateTime.wednesday => normalise == 'MERCREDI',
      DateTime.thursday => normalise == 'JEUDI',
      DateTime.friday => normalise == 'VENDREDI',
      DateTime.saturday => normalise == 'SAMEDI',
      DateTime.sunday => normalise == 'DIMANCHE',
      _ => false,
    };
  }

  String _normaliserJour(String value) {
    final upper = value.trim().toUpperCase();
    switch (upper) {
      case 'MONDAY':
      case 'MON':
      case 'LUN':
      case 'LUNDI':
        return 'LUNDI';
      case 'TUESDAY':
      case 'TUE':
      case 'MAR':
      case 'MARDI':
        return 'MARDI';
      case 'WEDNESDAY':
      case 'WED':
      case 'MER':
      case 'MERCREDI':
        return 'MERCREDI';
      case 'THURSDAY':
      case 'THU':
      case 'JEU':
      case 'JEUDI':
        return 'JEUDI';
      case 'FRIDAY':
      case 'FRI':
      case 'VEN':
      case 'VENDREDI':
        return 'VENDREDI';
      case 'SATURDAY':
      case 'SAT':
      case 'SAM':
      case 'SAMEDI':
        return 'SAMEDI';
      case 'SUNDAY':
      case 'SUN':
      case 'DIM':
      case 'DIMANCHE':
        return 'DIMANCHE';
      default:
        return upper;
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
            final msg =
                first['message'] ?? first['defaultMessage'] ?? first['error'];
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
