import 'dart:io';

import 'package:path_provider/path_provider.dart';

class AppFileStorage {
  /// Dossier privé et persistant de l’app (Option A)
  Future<Directory> _appDir() async {
    return getApplicationDocumentsDirectory();
  }

  /// Sous-dossier (ex: "mycampus/downloads")
  Future<Directory> ensureSubDir(String relativePath) async {
    final base = await _appDir();
    final dir = Directory('${base.path}/$relativePath');
    if (!await dir.exists()) {
      await dir.create(recursive: true);
    }
    return dir;
  }

  /// Nettoie un nom de fichier pour éviter caractères problématiques
  String sanitizeFileName(String name) {
    final replaced = name.replaceAll(RegExp(r'[\\/:*?"<>|]'), '_');
    return replaced.trim().isEmpty ? 'fichier' : replaced;
  }

  /// Retourne un chemin unique si le fichier existe déjà
  Future<File> uniqueFile(Directory dir, String fileName) async {
    final sanitized = sanitizeFileName(fileName);

    final hasDot = sanitized.contains('.');
    final base =
        hasDot ? sanitized.substring(0, sanitized.lastIndexOf('.')) : sanitized;
    final ext = hasDot ? sanitized.substring(sanitized.lastIndexOf('.')) : '';

    File f = File('${dir.path}/$sanitized');
    if (!await f.exists()) return f;

    int i = 1;
    while (true) {
      final candidate = File('${dir.path}/$base($i)$ext');
      if (!await candidate.exists()) return candidate;
      i++;
    }
  }

  Future<File> saveBytes({
    required List<int> bytes,
    required String fileName,
    String subDir = 'mycampus/downloads',
  }) async {
    final dir = await ensureSubDir(subDir);
    final file = await uniqueFile(dir, fileName);
    await file.writeAsBytes(bytes, flush: true);
    return file;
  }
}
