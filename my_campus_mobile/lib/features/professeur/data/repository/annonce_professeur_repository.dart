import '../api/annonce_professeur_api.dart';
import '../dto/annonce_professeur_dto.dart';
import '../dto/ressource_annonce_professeur_dto.dart';

class AnnonceProfesseurRepository {
  AnnonceProfesseurRepository(this._api);

  final AnnonceProfesseurApi _api;
  final Set<int> _idsLus = <int>{};

  Future<List<AnnonceProfesseurDTO>> recupererAnnonces() async {
    final data = await _api.recupererAnnonces();

    return data
        .map(
          (e) => AnnonceProfesseurDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
            estLue: _idsLus.contains(((e)['id'] as num?)?.toInt() ?? 0),
          ),
        )
        .toList();
  }

  Future<void> marquerCommeLue(int id) async {
    _idsLus.add(id);
  }

  Future<List<RessourceAnnonceProfesseurDTO>> recupererRessources(
    int annonceId,
  ) async {
    final data = await _api.recupererRessources(annonceId);
    return data
        .map(
          (entry) => RessourceAnnonceProfesseurDTO.fromJson(
            (entry as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }

  Future<AnnonceProfesseurDTO> creerAnnonce(Map<String, dynamic> body) async {
    final data = await _api.creerAnnonce(body);
    return AnnonceProfesseurDTO.fromJson(
      data,
      estLue: _idsLus.contains((data['id'] as num?)?.toInt() ?? 0),
    );
  }

  Future<List<AnnonceProfesseurDTO>> recupererAnnoncesPubliques() async {
    final data = await _api.recupererAnnoncesPubliques();

    return data
        .map(
          (e) => AnnonceProfesseurDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }
}
