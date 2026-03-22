class PresenceEtudiantDTO {
  final int id;
  final String status;
  final DateTime? dateDeclaration;
  final String? typeMotif;
  final String? descriptionDeJustification;
  final String statutJustificatif;
  final bool justificationSoumise;
  final bool justificationValidee;
  final String? motifUrl;
  final String? motifNom;
  final MiniSeancePresenceDTO? seance;

  const PresenceEtudiantDTO({
    required this.id,
    required this.status,
    required this.dateDeclaration,
    required this.typeMotif,
    required this.descriptionDeJustification,
    required this.statutJustificatif,
    required this.justificationSoumise,
    required this.justificationValidee,
    required this.motifUrl,
    required this.motifNom,
    required this.seance,
  });

  factory PresenceEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return PresenceEtudiantDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      status: (json['status'] ?? '').toString(),
      dateDeclaration: _parseDateTime(json['dateDeclaration']),
      typeMotif: json['typeMotif']?.toString(),
      descriptionDeJustification: json['descriptionDeJustification']?.toString(),
      statutJustificatif: (json['statutJustificatif'] ?? '').toString(),
      justificationSoumise: json['justificationSoumise'] == true,
      justificationValidee: json['justificationValidee'] == true,
      motifUrl: json['motifUrl']?.toString(),
      motifNom: json['motifNom']?.toString(),
      seance:
          json['seance'] is Map
              ? MiniSeancePresenceDTO.fromJson(
                (json['seance'] as Map).cast<String, dynamic>(),
              )
              : null,
    );
  }

  bool get estAbsent => status.toUpperCase() == 'ABSENT';

  bool get aJustification {
    if (justificationSoumise) return true;
    final hasDesc =
        descriptionDeJustification != null &&
        descriptionDeJustification!.trim().isNotEmpty;
    final hasFileName = motifNom != null && motifNom!.trim().isNotEmpty;
    final hasFileUrl = motifUrl != null && motifUrl!.trim().isNotEmpty;
    return hasDesc || hasFileName || hasFileUrl;
  }

  String get justificationState {
    final value = statutJustificatif.trim().toUpperCase();
    if (value == 'VALIDE') return 'validated';
    if (value == 'REFUSE') return 'rejected';
    if (value == 'EN_ATTENTE') return 'pending';
    if (value == 'AUCUN') return 'none';

    if (justificationValidee) return 'validated';
    if (aJustification) return 'pending';
    return 'none';
  }

  bool get peutAjouterJustification {
    if (!estAbsent) return false;
    if (justificationState == 'validated') return false;
    if (dateDeclaration == null) return true;
    final diff = DateTime.now().difference(dateDeclaration!);
    return diff.inHours < 48;
  }

  static DateTime? _parseDateTime(dynamic raw) {
    if (raw == null) return null;

    if (raw is String && raw.trim().isNotEmpty) {
      return DateTime.tryParse(raw)?.toLocal();
    }

    if (raw is List && raw.length >= 3) {
      final y = (raw[0] as num?)?.toInt() ?? 0;
      final m = (raw[1] as num?)?.toInt() ?? 1;
      final d = (raw[2] as num?)?.toInt() ?? 1;
      final h = raw.length > 3 ? (raw[3] as num?)?.toInt() ?? 0 : 0;
      final min = raw.length > 4 ? (raw[4] as num?)?.toInt() ?? 0 : 0;
      final s = raw.length > 5 ? (raw[5] as num?)?.toInt() ?? 0 : 0;
      return DateTime(y, m, d, h, min, s);
    }
    return null;
  }
}

class MiniSeancePresenceDTO {
  final DateTime? date;
  final String? salle;
  final String? type;
  final String? prof;

  const MiniSeancePresenceDTO({
    required this.date,
    required this.salle,
    required this.type,
    required this.prof,
  });

  factory MiniSeancePresenceDTO.fromJson(Map<String, dynamic> json) {
    return MiniSeancePresenceDTO(
      date: PresenceEtudiantDTO._parseDateTime(json['date']),
      salle: json['salle']?.toString(),
      type: json['type']?.toString(),
      prof: json['prof']?.toString(),
    );
  }
}
