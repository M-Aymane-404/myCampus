import 'dart:io';

import '../api/professeur_api.dart';
import '../dto/creation_examen_professeur_dto.dart';
import '../dto/creation_seance_professeur_dto.dart';
import '../dto/professeur_mobile_dto.dart';
import '../dto/professeur_presence_dto.dart';

class ProfesseurRepository {
  ProfesseurRepository(this._api);

  final ProfesseurApi _api;

  Future<ProfesseurProfilMobileDTO> chargerProfil() async {
    final json = await _api.recupererMoi();
    return ProfesseurProfilMobileDTO.fromJson(json);
  }

  Future<ProfesseurProfilMobileDTO> modifierProfil(
    ModifierProfilProfesseurDTO dto,
  ) async {
    await _api.modifierMoi(dto.toJson());
    return chargerProfil();
  }

  Future<ProfesseurProfilMobileDTO> changerPhoto(File file) async {
    await _api.uploadPhoto(file);
    return chargerProfil();
  }

  Future<List<EnseignementProfesseurMobileDTO>> chargerEnseignements() async {
    final liste = await _api.listerEnseignements();
    return liste
        .map(
          (e) => EnseignementProfesseurMobileDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<SeanceProfesseurMobileDTO>> chargerEmploi() async {
    final liste = await _api.obtenirEmploi();
    return liste
        .map(
          (e) => SeanceProfesseurMobileDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<SeanceProfesseurMobileDTO>> rechercherSeances({
    required String codeClasse,
    required String codeMatiere,
  }) async {
    final liste = await _api.rechercherSeances({
      'codeClasse': codeClasse,
      'codeMatiere': codeMatiere,
    });
    return liste
        .map(
          (e) => SeanceProfesseurMobileDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<SeanceProfesseurMobileDTO?> chargerProchaineSeance() async {
    final json = await _api.obtenirProchaineSeance();
    final prochaine = json['prochaineSeance'];
    if (prochaine is! Map) return null;
    return SeanceProfesseurMobileDTO.fromJson(
      prochaine.cast<String, dynamic>(),
    );
  }

  Future<List<ClasseFiltreProfesseurMobileDTO>> chargerFiltresExamens() async {
    final json = await _api.chargerFiltresExamens();
    final classes = (json['classes'] as List?) ?? const [];
    return classes
        .map(
          (e) => ClasseFiltreProfesseurMobileDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<ExamenProfesseurMobileDTO>> chargerExamens(
    String codeClasse,
    String codeMatiere,
  ) async {
    final liste = await _api.listerExamens(codeClasse, codeMatiere);
    return liste
        .map(
          (e) => ExamenProfesseurMobileDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<SeanceRechercheProfesseurDTO>> rechercherSeancesPresence({
    required String codeClasse,
    required String codeMatiere,
  }) async {
    final liste = await _api.rechercherSeances({
      'codeClasse': codeClasse,
      'codeMatiere': codeMatiere,
    });
    return liste
        .map(
          (e) => SeanceRechercheProfesseurDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<FeuillePresenceProfesseurDTO> chargerFeuillePresence(
    int seanceId,
  ) async {
    final json = await _api.chargerFeuillePresence(seanceId);
    return FeuillePresenceProfesseurDTO.fromJson(json);
  }

  Future<LignePresenceProfesseurDTO> saisirPresence({
    required int seanceId,
    required String apogee,
    required String status,
  }) async {
    final json = await _api.saisirPresence(seanceId, {
      'apogee': apogee,
      'status': status,
    });
    return LignePresenceProfesseurDTO.fromJson(json);
  }

  Future<LignePresenceProfesseurDTO> validerJustificatif({
    required int seanceId,
    required int presenceId,
  }) async {
    final json = await _api.validerJustificatif(seanceId, presenceId);
    return LignePresenceProfesseurDTO.fromJson(json);
  }

  Future<LignePresenceProfesseurDTO> refuserJustificatif({
    required int seanceId,
    required int presenceId,
  }) async {
    final json = await _api.refuserJustificatif(seanceId, presenceId);
    return LignePresenceProfesseurDTO.fromJson(json);
  }

  Future<void> creerSeance(CreationSeanceProfesseurDTO dto) async {
    await _api.creerSeance(dto.toJson());
  }

  Future<void> creerExamen(
    CreationExamenProfesseurDTO dto, {
    required String codeClasse,
    required String codeMatiere,
  }) async {
    await _api.creerExamen(
      codeClasse: codeClasse,
      codeMatiere: codeMatiere,
      body: dto.toJson(),
    );
  }
}
