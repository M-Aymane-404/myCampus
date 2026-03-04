import 'package:dio/dio.dart';

import '../../../../core/storage/app_file_storage.dart';

class FichierRepository {
  final Dio dio;
  final AppFileStorage storage;

  FichierRepository(this.dio, this.storage);

  /// Télécharge le fichier en bytes et l’enregistre dans le dossier privé app.
  /// Retourne le chemin local.
  Future<String> telechargerEtSauvegarder({
    required String urlDownload, // ex: "/api/seances/1/fichiers/9/telecharger"
    required String nomFichier,
    void Function(int received, int total)? onProgress,
  }) async {
    final resp = await dio.get<List<int>>(
      urlDownload,
      options: Options(
        responseType: ResponseType.bytes,
        followRedirects: true,
        receiveDataWhenStatusError: true,
      ),
      onReceiveProgress: onProgress,
    );

    final code = resp.statusCode ?? 0;
    if (code < 200 || code >= 300) {
      throw Exception('Échec téléchargement (HTTP $code)');
    }

    final data = resp.data;
    if (data == null || data.isEmpty) {
      throw Exception('Fichier vide ou non reçu.');
    }

    final file = await storage.saveBytes(
      bytes: data,
      fileName: nomFichier,
      subDir: 'mycampus/downloads',
    );

    return file.path;
  }
}
