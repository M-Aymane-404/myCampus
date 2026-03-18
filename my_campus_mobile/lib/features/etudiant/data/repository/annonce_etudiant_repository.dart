import '../api/annonce_etudiant_api.dart';
import '../dto/annonce_etudiant_dto.dart';
import '../dto/ressource_annonce_dto.dart';

class AnnonceEtudiantRepository {
  final AnnonceEtudiantApi _api;
  AnnonceEtudiantRepository(this._api);

  final Set<int> _idsLus = <int>{};

  Future<List<AnnonceEtudiantDTO>> recupererAnnonces() async {
    final data = await _api.recupererAnnonces();

    return data
        .map(
          (e) => AnnonceEtudiantDTO.fromJson(
            (e as Map).cast<String, dynamic>(),
            estLue: _idsLus.contains(((e)['id'] as num?)?.toInt() ?? 0),
          ),
        )
        .toList();
  }

  Future<AnnonceEtudiantDTO> recupererAnnonceParId(int id) async {
    final data = await _api.recupererAnnonceParId(id);
    return AnnonceEtudiantDTO.fromJson(
      data,
      estLue: _idsLus.contains(id),
    );
  }

  Future<void> marquerCommeLue(int id) async {
    _idsLus.add(id);
  }

  bool estLue(int id) => _idsLus.contains(id);

  Future<List<RessourceAnnonceDTO>> recupererRessources(int annonceId) async {
    final data = await _api.recupererRessources(annonceId);
    return data
        .map(
          (entry) => RessourceAnnonceDTO.fromJson(
            (entry as Map).cast<String, dynamic>(),
          ),
        )
        .toList();
  }
}
