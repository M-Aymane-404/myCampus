import 'dart:typed_data';

import 'package:dio/dio.dart';

import '../api/etudiant_api.dart';
import '../dto/cours_etudiant_dto.dart';
import '../dto/etudiant_moi_dto.dart';
import '../dto/tableau_bord_etudiant_dto.dart';

class ResultatTelechargement {
  final String nomFichier;
  final Uint8List octets;

  ResultatTelechargement({required this.nomFichier, required this.octets});
}

class EtudiantRepository {
  final EtudiantApi api;
  final Dio dio; // ✅ on injecte Dio ici

  EtudiantRepository(this.api, this.dio);

  Future<TableauBordEtudiantDTO> chargerDashboard() async {
    final results = await Future.wait([
      api.getDashboard(),
      api.getProchaineSeance(),
      api.getAnnonces(),
    ]);

    final dashboardJson = results[0] as Map<String, dynamic>;
    final prochaineSeanceResp = results[1] as Map<String, dynamic>;
    final annoncesJson = results[2] as List<dynamic>;

    final prochaineSeanceJson =
        (prochaineSeanceResp.containsKey('dayOfWeek') ||
                prochaineSeanceResp.containsKey('start'))
            ? prochaineSeanceResp
            : (prochaineSeanceResp['prochaineSeance'] as Map?)
                ?.cast<String, dynamic>();

    dashboardJson['prochaineSeance'] = prochaineSeanceJson;
    dashboardJson['annonces'] = annoncesJson;

    return TableauBordEtudiantDTO.fromJson(dashboardJson);
  }

  Future<EtudiantMoiDTO> recupererEtudiantConnecte() async {
    final json = await api.recupererMoi();
    return EtudiantMoiDTO.fromJson(json);
  }

  Future<List<ModuleSortieEtudiantDTO>> trouverModules() async {
    return await api.trouverModules();
  }

  Future<List<MatiereSortieEtudiantDTO>> trouverMatieres(
    String codeModule,
  ) async {
    return await api.trouverMatieres(codeModule);
  }

  Future<List<SeanceSortieEtudiantDTO>> trouverSeances(
    String codeMatiere, {
    String? typeSeance,
  }) async {
    return await api.trouverSeances(codeMatiere, typeSeance: typeSeance);
  }

  Future<List<FichierSeanceSortieEtudiantDTO>> trouverRessources(
    int seanceId,
  ) async {
    return await api.trouverRessources(seanceId);
  }

  /// ✅ Télécharge en bytes (obligatoire sinon tu récupères "string" dans swagger)
  /// Endpoint: GET /api/etudiant/fichiers-seance/{fichierId}/telecharger
  Future<ResultatTelechargement?> telechargerFichier(int fichierId) async {
    final response = await dio.get<List<int>>(
      "/api/etudiant/fichiers-seance/$fichierId/telecharger",
      options: Options(
        responseType: ResponseType.bytes,
        followRedirects: false,
        // accepte 200..399 (utile si redirection)
        validateStatus: (s) => s != null && s >= 200 && s < 400,
      ),
    );

    final data = response.data;
    if (data == null) return null;

    final cd = response.headers.value('content-disposition') ?? '';
    final nom = _nomFichierDepuisContentDisposition(cd) ?? "fichier_$fichierId";

    return ResultatTelechargement(
      nomFichier: nom,
      octets: Uint8List.fromList(data),
    );
  }

  /// ✅ Version safe sans regex compliquée (évite tes erreurs "Invalid regular expression")
  String? _nomFichierDepuisContentDisposition(String cd) {
    // cas 1: filename="abc.pdf"
    final quoted = RegExp(r'filename="([^"]+)"');
    final m1 = quoted.firstMatch(cd);
    if (m1 != null && (m1.group(1) ?? '').trim().isNotEmpty) {
      return m1.group(1)!.trim();
    }

    // cas 2: filename=abc.pdf
    final plain = RegExp(r'filename=([^;]+)');
    final m2 = plain.firstMatch(cd);
    if (m2 != null && (m2.group(1) ?? '').trim().isNotEmpty) {
      return m2.group(1)!.trim();
    }

    // cas 3: filename*=UTF-8''abc%20def.pdf
    final utf8 = RegExp(r"filename\*\=UTF-8''(.+)");
    final m3 = utf8.firstMatch(cd);
    if (m3 != null && (m3.group(1) ?? '').trim().isNotEmpty) {
      return Uri.decodeFull(m3.group(1)!.trim());
    }

    return null;
  }
}
