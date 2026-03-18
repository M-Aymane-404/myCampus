class SeanceRechercheProfesseurDTO {
  const SeanceRechercheProfesseurDTO({
    required this.id,
    required this.date,
    required this.heureDebut,
    required this.heureFin,
    required this.type,
    required this.salle,
    required this.etat,
  });

  final int? id;
  final DateTime? date;
  final DateTime? heureDebut;
  final DateTime? heureFin;
  final String type;
  final String salle;
  final String etat;

  factory SeanceRechercheProfesseurDTO.fromJson(Map<String, dynamic> json) {
    return SeanceRechercheProfesseurDTO(
      id: json['id'] is num ? (json['id'] as num).toInt() : null,
      date: _parseDateTime(json['date']),
      heureDebut: _parseDateTime(json['heureDebut']),
      heureFin: _parseDateTime(json['heureFin']),
      type: (json['type'] ?? '').toString(),
      salle: (json['salle'] ?? '').toString(),
      etat: (json['etat'] ?? '').toString(),
    );
  }
}

class FeuillePresenceProfesseurDTO {
  const FeuillePresenceProfesseurDTO({
    required this.seanceId,
    required this.date,
    required this.heureDebut,
    required this.heureFin,
    required this.salle,
    required this.codeClasse,
    required this.nomMatiere,
    required this.lignes,
  });

  final int? seanceId;
  final DateTime? date;
  final DateTime? heureDebut;
  final DateTime? heureFin;
  final String salle;
  final String codeClasse;
  final String nomMatiere;
  final List<LignePresenceProfesseurDTO> lignes;

  int get nbPresents =>
      lignes.where((ligne) => ligne.normalizedStatus == 'PRESENT').length;

  int get nbAbsents =>
      lignes.where((ligne) => ligne.normalizedStatus == 'ABSENT').length;

  int get totalEtudiants => lignes.length;

  FeuillePresenceProfesseurDTO copyWith({
    int? seanceId,
    DateTime? date,
    DateTime? heureDebut,
    DateTime? heureFin,
    String? salle,
    String? codeClasse,
    String? nomMatiere,
    List<LignePresenceProfesseurDTO>? lignes,
  }) {
    return FeuillePresenceProfesseurDTO(
      seanceId: seanceId ?? this.seanceId,
      date: date ?? this.date,
      heureDebut: heureDebut ?? this.heureDebut,
      heureFin: heureFin ?? this.heureFin,
      salle: salle ?? this.salle,
      codeClasse: codeClasse ?? this.codeClasse,
      nomMatiere: nomMatiere ?? this.nomMatiere,
      lignes: lignes ?? this.lignes,
    );
  }

  factory FeuillePresenceProfesseurDTO.fromJson(Map<String, dynamic> json) {
    final rawLignes = (json['lignes'] as List?) ?? const [];
    return FeuillePresenceProfesseurDTO(
      seanceId:
          json['seanceId'] is num ? (json['seanceId'] as num).toInt() : null,
      date: _parseDateTime(json['date']),
      heureDebut: _parseDateTime(json['heureDebut']),
      heureFin: _parseDateTime(json['heureFin']),
      salle: (json['salle'] ?? '').toString(),
      codeClasse: (json['codeClasse'] ?? '').toString(),
      nomMatiere: (json['nomMatiere'] ?? '').toString(),
      lignes:
          rawLignes
              .map(
                (e) => LignePresenceProfesseurDTO.fromJson(
                  (e as Map).cast<String, dynamic>(),
                ),
              )
              .toList(),
    );
  }
}

class LignePresenceProfesseurDTO {
  const LignePresenceProfesseurDTO({
    required this.idPresence,
    required this.idEtudiant,
    required this.apogee,
    required this.nomEtudiant,
    required this.prenomEtudiant,
    required this.status,
    required this.statutJustificatif,
    required this.justifiee,
    required this.justificationSoumise,
    required this.motif,
    required this.typeMotif,
    required this.motifUrl,
    required this.motifNom,
    required this.dateDeclaration,
  });

  final int? idPresence;
  final int? idEtudiant;
  final String apogee;
  final String nomEtudiant;
  final String prenomEtudiant;
  final String status;
  final String statutJustificatif;
  final bool justifiee;
  final bool justificationSoumise;
  final String? motif;
  final String? typeMotif;
  final String? motifUrl;
  final String? motifNom;
  final DateTime? dateDeclaration;

  String get normalizedStatus {
    final value = status.trim().toUpperCase();
    if (['P', 'PR', 'PRESENTE'].contains(value)) return 'PRESENT';
    if (['A', 'ABS', 'ABSENTE'].contains(value)) return 'ABSENT';
    if (['JUSTIFIE', 'JUSTIFIEE', 'JUSTIFIED'].contains(value)) {
      return 'JUSTIFIE';
    }
    if (['RETARD', 'LATE'].contains(value)) return 'RETARD';
    return value;
  }

  bool get hasJustification {
    return justificationSoumise ||
        (motif?.trim().isNotEmpty ?? false) ||
        (motifUrl?.trim().isNotEmpty ?? false) ||
        (motifNom?.trim().isNotEmpty ?? false) ||
        (typeMotif?.trim().isNotEmpty ?? false);
  }

  String get justificationState {
    final value = statutJustificatif.trim().toUpperCase();
    if (value == 'VALIDE') return 'validated';
    if (value == 'REFUSE') return 'rejected';
    if (value == 'EN_ATTENTE') return 'pending';
    if (justifiee) return 'validated';
    if (hasJustification) return 'pending';
    return 'empty';
  }

  String get justificationLabel {
    switch (justificationState) {
      case 'validated':
        return 'Justifie';
      case 'rejected':
        return 'Refuse';
      case 'pending':
        return 'En attente';
      default:
        return 'Aucun justificatif';
    }
  }

  String get nomComplet {
    final value = '$prenomEtudiant $nomEtudiant'.trim();
    return value.isEmpty ? apogee : value;
  }

  LignePresenceProfesseurDTO copyWith({
    int? idPresence,
    int? idEtudiant,
    String? apogee,
    String? nomEtudiant,
    String? prenomEtudiant,
    String? status,
    String? statutJustificatif,
    bool? justifiee,
    bool? justificationSoumise,
    String? motif,
    String? typeMotif,
    String? motifUrl,
    String? motifNom,
    DateTime? dateDeclaration,
  }) {
    return LignePresenceProfesseurDTO(
      idPresence: idPresence ?? this.idPresence,
      idEtudiant: idEtudiant ?? this.idEtudiant,
      apogee: apogee ?? this.apogee,
      nomEtudiant: nomEtudiant ?? this.nomEtudiant,
      prenomEtudiant: prenomEtudiant ?? this.prenomEtudiant,
      status: status ?? this.status,
      statutJustificatif: statutJustificatif ?? this.statutJustificatif,
      justifiee: justifiee ?? this.justifiee,
      justificationSoumise: justificationSoumise ?? this.justificationSoumise,
      motif: motif ?? this.motif,
      typeMotif: typeMotif ?? this.typeMotif,
      motifUrl: motifUrl ?? this.motifUrl,
      motifNom: motifNom ?? this.motifNom,
      dateDeclaration: dateDeclaration ?? this.dateDeclaration,
    );
  }

  factory LignePresenceProfesseurDTO.fromJson(Map<String, dynamic> json) {
    return LignePresenceProfesseurDTO(
      idPresence:
          json['idPresence'] is num
              ? (json['idPresence'] as num).toInt()
              : null,
      idEtudiant:
          json['idEtudiant'] is num
              ? (json['idEtudiant'] as num).toInt()
              : null,
      apogee: (json['apogee'] ?? '').toString(),
      nomEtudiant: (json['nomEtudiant'] ?? '').toString(),
      prenomEtudiant: (json['prenomEtudiant'] ?? '').toString(),
      status: (json['status'] ?? '').toString(),
      statutJustificatif: (json['statutJustificatif'] ?? '').toString(),
      justifiee: json['justifiee'] == true,
      justificationSoumise: json['justificationSoumise'] == true,
      motif:
          (json['motif'] ?? '').toString().trim().isEmpty
              ? null
              : (json['motif'] ?? '').toString(),
      typeMotif:
          (json['typeMotif'] ?? '').toString().trim().isEmpty
              ? null
              : (json['typeMotif'] ?? '').toString(),
      motifUrl:
          (json['motifUrl'] ?? '').toString().trim().isEmpty
              ? null
              : (json['motifUrl'] ?? '').toString(),
      motifNom:
          (json['motifNom'] ?? '').toString().trim().isEmpty
              ? null
              : (json['motifNom'] ?? '').toString(),
      dateDeclaration: _parseDateTime(json['dateDeclaration']),
    );
  }
}

DateTime? _parseDateTime(Object? raw) {
  if (raw == null) return null;
  final value = raw.toString().trim();
  if (value.isEmpty) return null;
  return DateTime.tryParse(value);
}
