import 'dart:io';

import '../api/profil_etudiant_api.dart';
import '../dto/profil_etudiant_dto.dart';

class ProfilEtudiantRepository {
  final ProfilEtudiantApi _api;
  ProfilEtudiantRepository(this._api);

  Future<ProfilEtudiantDTO> chargerMoi() async {
    final json = await _api.getMoi();
    return ProfilEtudiantDTO.fromJson(json);
  }

  Future<ProfilEtudiantDTO> modifierMoi(ModifierProfilEtudiantDTO dto) async {
    final json = await _api.modifierMoi(dto.toJson());
    return ProfilEtudiantDTO.fromJson(json);
  }

  Future<void> uploadPhoto(File file) async {
    await _api.uploadPhoto(file);
  }
}
