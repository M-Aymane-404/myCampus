// etudiant_api.dart
import 'package:dio/dio.dart';

import '../../../../core/config/configuration_app.dart';
import '../dto/cours_etudiant_dto.dart';
import '../dto/emploi_du_temps_etudiant_dto.dart';
import '../dto/presence_etudiant_dto.dart';
import '../dto/releve_notes_dto.dart';

class EtudiantApi {
  final Dio _dio;
  EtudiantApi(this._dio);

  Future<Map<String, dynamic>> getDashboard() async {
    final resp = await _dio.get('/api/etudiant/tableau-de-bord');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<Map<String, dynamic>> getProchaineSeance() async {
    final resp = await _dio.get('/api/etudiant/emploi/prochaine');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<List<dynamic>> getAnnonces() async {
    final resp = await _dio.get('/api/etudiant/annonces');
    return (resp.data as List);
  }

  Future<Map<String, dynamic>> recupererMoi() async {
    final resp = await _dio.get('/api/etudiant/moi');
    return (resp.data as Map).cast<String, dynamic>();
  }

  Future<List<ModuleSortieEtudiantDTO>> trouverModules() async {
    final resp = await _dio.get('/api/etudiant/moi/modules');
    return (resp.data as List)
        .map(
          (e) => ModuleSortieEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<MatiereSortieEtudiantDTO>> trouverMatieres(
    String codeModule,
  ) async {
    final resp = await _dio.get(
      '/api/etudiant/moi/modules/$codeModule/matieres',
    );
    return (resp.data as List)
        .map(
          (e) => MatiereSortieEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<SeanceSortieEtudiantDTO>> trouverSeances(
    String codeMatiere, {
    String? typeSeance,
  }) async {
    String url = '/api/etudiant/moi/matieres/$codeMatiere/seances';
    if (typeSeance != null && typeSeance != 'TOUS') {
      url += '?typeSeance=$typeSeance';
    }
    final resp = await _dio.get(url);
    return (resp.data as List)
        .map(
          (e) => SeanceSortieEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<List<FichierSeanceSortieEtudiantDTO>> trouverRessources(
    int seanceId,
  ) async {
    final resp = await _dio.get('/api/etudiant/seances/$seanceId/ressources');
    return (resp.data as List)
        .map(
          (e) => FichierSeanceSortieEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<Response> telechargerFichier(int fichierId) async {
    return await _dio.get(
      '/api/etudiant/fichiers-seance/$fichierId/telecharger',
    );
  }

  Future<EmploiDuTempsEtudiantDTO> getEmploi(String semestre) async {
    final res = await _dio.get(
      "${ConfigurationApp.apiBaseUrl}/api/etudiant/emploi/$semestre",
      options: Options(headers: {"Accept": "application/json"}),
    );
    return EmploiDuTempsEtudiantDTO.fromJson(res.data as Map<String, dynamic>);
  }

  Future<ReleveNotesDTO> getReleveNotes(String semestre) async {
    final res = await _dio.get(
      "${ConfigurationApp.apiBaseUrl}/api/etudiant/moi/releve-notes",
      queryParameters: {"semestre": semestre},
      options: Options(headers: {"Accept": "application/json"}),
    );

    return ReleveNotesDTO.fromJson(res.data as Map<String, dynamic>);
  }

  Future<List<PresenceEtudiantDTO>> recupererMesPresences({
    String? status,
    String? dateIso,
  }) async {
    final query = <String, dynamic>{};
    if (status != null && status.trim().isNotEmpty) query['status'] = status;
    if (dateIso != null && dateIso.trim().isNotEmpty) query['date'] = dateIso;

    final resp = await _dio.get(
      '/api/etudiant/moi/presences',
      queryParameters: query,
    );

    return (resp.data as List)
        .map(
          (e) => PresenceEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<void> ajouterJustificationPresence({
    required int presenceId,
    required String typeMotif,
    String? description,
    String? fichierPath,
  }) async {
    final query = <String, dynamic>{'typeMotif': typeMotif};
    if (description != null && description.trim().isNotEmpty) {
      query['descriptionDeJustification'] = description.trim();
    }

    final formData = FormData();
    if (fichierPath != null && fichierPath.trim().isNotEmpty) {
      final cleanPath = fichierPath.trim();
      final fileName = cleanPath.split(RegExp(r'[\\/]')).last;
      formData.files.add(
        MapEntry(
          'motif',
          await MultipartFile.fromFile(cleanPath, filename: fileName),
        ),
      );
    }

    await _dio.put(
      '/justification/$presenceId',
      queryParameters: query,
      data: formData,
      options: Options(contentType: 'multipart/form-data'),
    );
  }
}
