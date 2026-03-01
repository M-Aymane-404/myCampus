// etudiant_repository.dart
import '../api/etudiant_api.dart';
import '../dto/tableau_bord_etudiant_dto.dart';

class EtudiantRepository {
  final EtudiantApi api;
  EtudiantRepository(this.api);

  Future<TableauBordEtudiantDTO> chargerDashboard() async {
    final results = await Future.wait([
      api.getDashboard(),
      api.getProchaineSeance(),
      api.getAnnonces(),
    ]);

    final dashboardJson = results[0] as Map<String, dynamic>;
    final prochaineSeanceResp = results[1] as Map<String, dynamic>;
    final annoncesJson = results[2] as List<dynamic>;

    // ✅ Si le backend renvoie directement les champs
    // ou si le backend renvoie un wrapper { "prochaineSeance": { ... } }
    final prochaineSeanceJson =
        (prochaineSeanceResp.containsKey('dayOfWeek') ||
                prochaineSeanceResp.containsKey('start'))
            ? prochaineSeanceResp
            : (prochaineSeanceResp['prochaineSeance'] as Map?)
                ?.cast<String, dynamic>();

    dashboardJson['prochaineSeance'] =
        prochaineSeanceJson; // peut être null si aucune séance
    dashboardJson['annonces'] = annoncesJson;

    return TableauBordEtudiantDTO.fromJson(dashboardJson);
  }
}
