import '../api/etudiant_api.dart';
import '../dto/releve_notes_dto.dart';

class ReleveNotesRepo {
  final EtudiantApi api;
  ReleveNotesRepo({required this.api});

  Future<ReleveNotesDTO> chargerReleveNotes(String semestre) async {
    return api.getReleveNotes(semestre);
  }
}
