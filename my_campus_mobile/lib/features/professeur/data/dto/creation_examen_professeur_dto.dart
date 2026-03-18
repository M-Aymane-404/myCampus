class CreationExamenProfesseurDTO {
  const CreationExamenProfesseurDTO({
    required this.typeExamen,
    required this.semestre,
    required this.dateExamen,
    this.duree,
    this.numeroExamen,
  });

  final String typeExamen;
  final String semestre;
  final String dateExamen;
  final int? duree;
  final int? numeroExamen;

  Map<String, dynamic> toJson() => {
        'typeExamen': typeExamen,
        'semestre': semestre,
        'dateExamen': dateExamen,
        if (duree != null) 'duree': duree,
        if (numeroExamen != null) 'numeroExamen': numeroExamen,
      };
}
