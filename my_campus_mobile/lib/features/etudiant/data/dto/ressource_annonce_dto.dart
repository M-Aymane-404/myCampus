class RessourceAnnonceDTO {
  final int id;
  final int annonceId;
  final String nomOriginal;
  final String typeMime;
  final int? taille;
  final String? dateUpload;

  const RessourceAnnonceDTO({
    required this.id,
    required this.annonceId,
    required this.nomOriginal,
    required this.typeMime,
    required this.taille,
    required this.dateUpload,
  });

  factory RessourceAnnonceDTO.fromJson(Map<String, dynamic> json) {
    return RessourceAnnonceDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      annonceId: (json['annonceId'] as num?)?.toInt() ?? 0,
      nomOriginal: (json['nomOriginal'] ?? '').toString(),
      typeMime: (json['typeMime'] ?? '').toString(),
      taille: (json['taille'] as num?)?.toInt(),
      dateUpload: json['dateUpload']?.toString(),
    );
  }
}
