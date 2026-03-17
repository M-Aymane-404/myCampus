class NotificationProfesseurDTO {
  const NotificationProfesseurDTO({
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

  final int id;
  final String titre;
  final String message;
  final String dateEnvoi;
  final bool lu;
  final String? dateLecture;
  final int? referenceId;
  final String? referenceType;
  final String? type;

  factory NotificationProfesseurDTO.fromJson(Map<String, dynamic> json) {
    return NotificationProfesseurDTO(
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

  NotificationProfesseurDTO copyWith({bool? lu}) {
    return NotificationProfesseurDTO(
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
