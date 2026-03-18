import 'package:dio/dio.dart';

import '../api/ressource_seance_professeur_api.dart';
import '../dto/ressource_seance_professeur_dto.dart';

class RessourceSeanceProfesseurRepository {
  RessourceSeanceProfesseurRepository(this._api);

  final RessourceSeanceProfesseurApi _api;

  Future<List<RessourceSeanceProfesseurDTO>> recupererRessources(
    int seanceId,
  ) async {
    final data = await _api.recupererRessources(seanceId);
    return data
        .map(
          (e) => RessourceSeanceProfesseurDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<void> televerserRessource(
    int seanceId,
    MultipartFile fichier,
  ) async {
    await _api.televerserRessource(seanceId, fichier);
  }
}
