// etudiant_api.dart
import 'package:dio/dio.dart';
import '../dto/cours_etudiant_dto.dart';

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
        .map((e) => ModuleSortieEtudiantDTO.fromJson((e as Map).cast<String, dynamic>()))
        .toList();
  }

  Future<List<MatiereSortieEtudiantDTO>> trouverMatieres(String codeModule) async {
    final resp = await _dio.get('/api/etudiant/moi/modules/$codeModule/matieres');
    return (resp.data as List)
        .map((e) => MatiereSortieEtudiantDTO.fromJson((e as Map).cast<String, dynamic>()))
        .toList();
  }

  Future<List<SeanceSortieEtudiantDTO>> trouverSeances(String codeMatiere, {String? typeSeance}) async {
    String url = '/api/etudiant/moi/matieres/$codeMatiere/seances';
    if (typeSeance != null && typeSeance != 'TOUS') {
      url += '?typeSeance=$typeSeance';
    }
    final resp = await _dio.get(url);
    return (resp.data as List)
        .map((e) => SeanceSortieEtudiantDTO.fromJson((e as Map).cast<String, dynamic>()))
        .toList();
  }

  Future<List<FichierSeanceSortieEtudiantDTO>> trouverRessources(int seanceId) async {
    final resp = await _dio.get('/api/etudiant/seances/$seanceId/ressources');
    return (resp.data as List)
        .map((e) => FichierSeanceSortieEtudiantDTO.fromJson((e as Map).cast<String, dynamic>()))
        .toList();
  }

  Future<Response> telechargerFichier(int fichierId) async {
    return await _dio.get('/api/etudiant/fichiers-seance/$fichierId/telecharger');
  }
}
