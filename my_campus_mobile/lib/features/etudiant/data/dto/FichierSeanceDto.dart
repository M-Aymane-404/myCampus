class FichierSeanceDto {
  final int id;
  final String nomOriginal;
  final String mimeType;

  /// ex: "/api/seances/fichiers/12/telecharger"
  final String urlDownload;

  const FichierSeanceDto({
    required this.id,
    required this.nomOriginal,
    required this.mimeType,
    required this.urlDownload,
  });
}