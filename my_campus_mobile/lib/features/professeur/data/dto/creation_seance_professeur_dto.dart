class CreationSeanceProfesseurDTO {
  const CreationSeanceProfesseurDTO({
    required this.codeClasse,
    required this.codeMatiere,
    required this.anneeUniversitaire,
    required this.date,
    required this.heureDebut,
    required this.heureFin,
    required this.type,
    required this.etat,
    required this.salle,
    this.contenu,
  });

  final String codeClasse;
  final String codeMatiere;
  final String anneeUniversitaire;
  final String date;
  final String heureDebut;
  final String heureFin;
  final String type;
  final String etat;
  final String salle;
  final String? contenu;

  Map<String, dynamic> toJson() => {
        'codeClasse': codeClasse,
        'codeMatiere': codeMatiere,
        'anneeUniversitaire': anneeUniversitaire,
        'date': date,
        'heureDebut': heureDebut,
        'heureFin': heureFin,
        'type': type,
        'etat': etat,
        'salle': salle,
        if (contenu != null) 'contenu': contenu,
      };
}
