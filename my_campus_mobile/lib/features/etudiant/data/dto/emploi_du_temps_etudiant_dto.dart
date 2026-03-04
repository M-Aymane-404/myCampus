class EmploiDuTempsEtudiantDTO {
  final String semestre;
  final List<SlotEmploiDTO> slots;

  EmploiDuTempsEtudiantDTO({required this.semestre, required this.slots});

  factory EmploiDuTempsEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return EmploiDuTempsEtudiantDTO(
      semestre: (json['semestre'] ?? '').toString(),
      slots:
          ((json['slots'] ?? []) as List)
              .map((e) => SlotEmploiDTO.fromJson(e as Map<String, dynamic>))
              .toList(),
    );
  }
}

class SlotEmploiDTO {
  final String dayOfWeek; // LUNDI...
  final String start; // "08:00"
  final String end; // "09:00"
  final String matiereCode;
  final String matiereNom;
  final String professeurPpr;
  final String professeurNom;
  final String salle;
  final String typeSeance; // COURS/TD/TP

  SlotEmploiDTO({
    required this.dayOfWeek,
    required this.start,
    required this.end,
    required this.matiereCode,
    required this.matiereNom,
    required this.professeurPpr,
    required this.professeurNom,
    required this.salle,
    required this.typeSeance,
  });

  factory SlotEmploiDTO.fromJson(Map<String, dynamic> json) {
    return SlotEmploiDTO(
      dayOfWeek:
          (json['dayOfWeek'] ??
                  json['day'] ??
                  json['jour'] ??
                  json['weekday'] ??
                  json['date'] ??
                  '')
              .toString(),
      start:
          (json['start'] ??
                  json['startTime'] ??
                  json['heureDebut'] ??
                  json['debut'] ??
                  '')
              .toString(),
      end:
          (json['end'] ??
                  json['endTime'] ??
                  json['heureFin'] ??
                  json['fin'] ??
                  '')
              .toString(),
      matiereCode: (json['matiereCode'] ?? '').toString(),
      matiereNom: (json['matiereNom'] ?? '').toString(),
      professeurPpr: (json['professeurPpr'] ?? '').toString(),
      professeurNom: (json['professeurNom'] ?? '').toString(),
      salle: (json['salle'] ?? '').toString(),
      typeSeance: (json['typeSeance'] ?? '').toString(),
    );
  }
}
