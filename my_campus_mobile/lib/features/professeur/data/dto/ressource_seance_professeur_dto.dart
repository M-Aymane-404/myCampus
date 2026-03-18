class RessourceSeanceProfesseurDTO {
  const RessourceSeanceProfesseurDTO({
    required this.id,
    required this.seanceId,
    required this.nomOriginal,
    required this.typeMime,
    required this.taille,
    required this.dateUpload,
  });

  final int id;
  final int seanceId;
  final String nomOriginal;
  final String typeMime;
  final int? taille;
  final String? dateUpload;

  factory RessourceSeanceProfesseurDTO.fromJson(Map<String, dynamic> json) {
    return RessourceSeanceProfesseurDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      seanceId: (json['seanceId'] as num?)?.toInt() ?? 0,
      nomOriginal: (json['nomOriginal'] ?? '').toString(),
      typeMime: (json['typeMime'] ?? '').toString(),
      taille: (json['taille'] as num?)?.toInt(),
      dateUpload: json['dateUpload']?.toString(),
    );
  }
}
