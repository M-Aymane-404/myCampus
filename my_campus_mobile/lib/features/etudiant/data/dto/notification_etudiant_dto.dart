class NotificationEtudiantDTO {
  final int id;
  final String titre;
  final String message;
  final String dateEnvoi;
  final bool lu;
  final String? dateLecture;
  final int? referenceId;
  final String? referenceType;
  final String? type;

  const NotificationEtudiantDTO({
    required this.id,
    required this.titre,
    required this.message,
    required this.dateEnvoi,
    required this.lu,
    required this.dateLecture,
    required this.referenceId,
    required this.referenceType,
    required this.type,
  });

  factory NotificationEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return NotificationEtudiantDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      titre: (json['titre'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      dateEnvoi: (json['dateEnvoi'] ?? '').toString(),
      lu: json['lu'] == true,
      dateLecture: json['dateLecture']?.toString(),
      referenceId: (json['referenceId'] as num?)?.toInt(),
      referenceType: json['referenceType']?.toString(),
      type: json['type']?.toString(),
    );
  }

  bool get estAnnonce => referenceType == 'ANNONCE' && referenceId != null;

  NotificationEtudiantDTO copyWith({bool? lu}) {
    return NotificationEtudiantDTO(
      id: id,
      titre: titre,
      message: message,
      dateEnvoi: dateEnvoi,
      lu: lu ?? this.lu,
      dateLecture: dateLecture,
      referenceId: referenceId,
      referenceType: referenceType,
      type: type,
    );
  }
}
