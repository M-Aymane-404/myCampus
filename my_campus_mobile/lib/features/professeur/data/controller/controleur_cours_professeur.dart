import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';

import '../dto/creation_examen_professeur_dto.dart';
import '../dto/creation_seance_professeur_dto.dart';
import '../dto/ressource_seance_professeur_dto.dart';
import '../repository/professeur_repository.dart';
import '../repository/ressource_seance_professeur_repository.dart';

class ControleurCoursProfesseur extends ChangeNotifier {
  ControleurCoursProfesseur(
    this._professeurRepository,
    this._ressourceRepository,
  );

  final ProfesseurRepository _professeurRepository;
  final RessourceSeanceProfesseurRepository _ressourceRepository;

  bool chargementRessources = false;
  bool uploadEnCours = false;
  int? uploadSeanceId;
  int? seanceRessourceSelectionnee;
  List<RessourceSeanceProfesseurDTO> ressources = const [];
  String? erreur;

  Future<void> chargerRessources(int seanceId) async {
    chargementRessources = true;
    erreur = null;
    notifyListeners();

    try {
      final data = await _ressourceRepository.recupererRessources(seanceId);
      ressources = data;
      seanceRessourceSelectionnee = seanceId;
    } catch (e) {
      erreur = e.toString();
      rethrow;
    } finally {
      chargementRessources = false;
      notifyListeners();
    }
  }

  Future<void> televerserRessource(int seanceId) async {
    final result = await FilePicker.platform.pickFiles(
      allowMultiple: false,
      withData: false,
    );
    final file = result?.files.first;
    if (file == null) return;

    final path = file.path;
    if (path == null) {
      throw Exception('Impossible d\'identifier le chemin du fichier');
    }

    final multipart = await MultipartFile.fromFile(
      path,
      filename: file.name,
    );

    uploadEnCours = true;
    uploadSeanceId = seanceId;
    erreur = null;
    notifyListeners();

    try {
      await _ressourceRepository.televerserRessource(seanceId, multipart);
      await chargerRessources(seanceId);
    } catch (e) {
      erreur = e.toString();
      rethrow;
    } finally {
      uploadEnCours = false;
      uploadSeanceId = null;
      notifyListeners();
    }
  }

  Future<void> creerSeance(CreationSeanceProfesseurDTO dto) async {
    erreur = null;
    notifyListeners();

    try {
      await _professeurRepository.creerSeance(dto);
    } catch (e) {
      erreur = e.toString();
      rethrow;
    }
  }

  Future<void> creerExamen(
    CreationExamenProfesseurDTO dto, {
    required String codeClasse,
    required String codeMatiere,
  }) async {
    erreur = null;
    notifyListeners();

    try {
      await _professeurRepository.creerExamen(
        dto,
        codeClasse: codeClasse,
        codeMatiere: codeMatiere,
      );
    } catch (e) {
      erreur = e.toString();
      rethrow;
    }
  }
}
