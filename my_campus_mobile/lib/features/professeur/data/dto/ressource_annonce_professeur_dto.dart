class RessourceAnnonceProfesseurDTO {
  const RessourceAnnonceProfesseurDTO({
    required this.id,
    required this.annonceId,
    required this.nomOriginal,
    required this.typeMime,
    required this.taille,
    required this.dateUpload,
  });

  final int id;
  final int annonceId;
  final String nomOriginal;
  final String typeMime;
  final int? taille;
  final String? dateUpload;

  factory RessourceAnnonceProfesseurDTO.fromJson(
    Map<String, dynamic> json,
  ) {
    return RessourceAnnonceProfesseurDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      annonceId: (json['annonceId'] as num?)?.toInt() ?? 0,
      nomOriginal: (json['nomOriginal'] ?? '').toString(),
      typeMime: (json['typeMime'] ?? '').toString(),
      taille: (json['taille'] as num?)?.toInt(),
      dateUpload: json['dateUpload']?.toString(),
    );
  }
}
