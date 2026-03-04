// releve_notes_dto.dart

class ReleveNotesDTO {
  final String semestre;
  final double moyenneSession;
  final int nbModules;
  final int nbModulesValider;
  final List<ModuleNotesDTO> modules;

  ReleveNotesDTO({
    required this.semestre,
    required this.moyenneSession,
    required this.nbModules,
    required this.nbModulesValider,
    required this.modules,
  });

  factory ReleveNotesDTO.fromJson(Map<String, dynamic> json) {
    return ReleveNotesDTO(
      semestre: (json['semestre'] ?? '').toString(),
      moyenneSession: ((json['moyenneSession'] ?? 0) as num).toDouble(),
      nbModules: (json['nbModules'] ?? 0) as int,
      nbModulesValider: (json['nbModulesValider'] ?? 0) as int,
      modules:
          ((json['modules'] ?? []) as List)
              .map((e) => ModuleNotesDTO.fromJson(e as Map<String, dynamic>))
              .toList(),
    );
  }
}

class ModuleNotesDTO {
  final int id;
  final String nomDuModule;
  final String codeModule;
  final double moyenneDuModule;
  final List<MatiereNotesDTO> matieres;

  ModuleNotesDTO({
    required this.id,
    required this.nomDuModule,
    required this.codeModule,
    required this.moyenneDuModule,
    required this.matieres,
  });

  factory ModuleNotesDTO.fromJson(Map<String, dynamic> json) {
    return ModuleNotesDTO(
      id: (json['id'] ?? 0) as int,
      nomDuModule: (json['nomDuModule'] ?? '').toString(),
      codeModule: (json['codeModule'] ?? '').toString(),
      moyenneDuModule: ((json['moyenneDuModule'] ?? 0) as num).toDouble(),
      matieres:
          ((json['matieres'] ?? []) as List)
              .map((e) => MatiereNotesDTO.fromJson(e as Map<String, dynamic>))
              .toList(),
    );
  }
}

class MatiereNotesDTO {
  final int id;
  final String nomDuMatiere;
  final String codeMatiere;
  final List<ExamenNotesDTO> examens;
  final double moyenneDuMatiere;
  final bool validee;

  MatiereNotesDTO({
    required this.id,
    required this.nomDuMatiere,
    required this.codeMatiere,
    required this.examens,
    required this.moyenneDuMatiere,
    required this.validee,
  });

  factory MatiereNotesDTO.fromJson(Map<String, dynamic> json) {
    return MatiereNotesDTO(
      id: (json['id'] ?? 0) as int,
      nomDuMatiere: (json['nomDuMatiere'] ?? '').toString(),
      codeMatiere: (json['codeMatiere'] ?? '').toString(),
      examens:
          ((json['examens'] ?? []) as List)
              .map((e) => ExamenNotesDTO.fromJson(e as Map<String, dynamic>))
              .toList(),
      moyenneDuMatiere: ((json['moyenneDuMatiere'] ?? 0) as num).toDouble(),
      validee: (json['validee'] ?? false) as bool,
    );
  }

  /// ✅ Renvoie l'examen le plus pertinent à afficher dans l'UI.
  ///
  /// Priorité:
  /// 1) NORMAL + numero 1 si sa note est non-null
  /// 2) sinon le 1er examen qui a une note (peu importe le type/numéro)
  /// 3) sinon NORMAL + numero 1 même si note null
  /// 4) sinon le 1er examen de la liste
  ExamenNotesDTO? examenAffiche() {
    final normal1 = examens.cast<ExamenNotesDTO?>().firstWhere(
      (e) =>
          e != null &&
          e.typeExamen.toUpperCase() == 'NORMAL' &&
          e.numeroExamen == 1,
      orElse: () => null,
    );

    if (normal1 != null && normal1.note != null) return normal1;

    final avecNote = examens.cast<ExamenNotesDTO?>().firstWhere(
      (e) => e != null && e.note != null,
      orElse: () => null,
    );
    if (avecNote != null) return avecNote;

    if (normal1 != null) return normal1;

    return examens.isNotEmpty ? examens.first : null;
  }

  /// ✅ Ancienne logique (si tu veux la garder) :
  /// - retourne NORMAL #1 s'il existe, sinon le 1er examen.
  ///
  /// ⚠️ Limite : si NORMAL #1 existe mais note == null,
  /// tu vas afficher "Non noté" même si un autre examen a une note.
  ExamenNotesDTO? examenNormal1() {
    final normal1 = examens.cast<ExamenNotesDTO?>().firstWhere(
      (e) =>
          e != null &&
          e.typeExamen.toUpperCase() == 'NORMAL' &&
          e.numeroExamen == 1,
      orElse: () => null,
    );

    return normal1 ?? (examens.isNotEmpty ? examens.first : null);
  }
}

class ExamenNotesDTO {
  final int id;
  final int numeroExamen;
  final String typeExamen; // NORMAL, RATTRAPAGE...
  final double? note;
  final String? corrigePath; // /corriges/xxx.pdf
  final String? copieEtudiantPath; // /copies/xxx.pdf

  ExamenNotesDTO({
    required this.id,
    required this.numeroExamen,
    required this.typeExamen,
    required this.note,
    required this.corrigePath,
    required this.copieEtudiantPath,
  });

  factory ExamenNotesDTO.fromJson(Map<String, dynamic> json) {
    final rawCorrige = (json['corrigePath'] ?? '').toString().trim();
    final rawCopie = (json['copieEtudiantPath'] ?? '').toString().trim();

    return ExamenNotesDTO(
      id: (json['id'] ?? 0) as int,
      numeroExamen: (json['numeroExamen'] ?? 0) as int,
      typeExamen: (json['typeExamen'] ?? '').toString(),
      note: json['note'] == null ? null : (json['note'] as num).toDouble(),
      corrigePath: rawCorrige.isEmpty ? null : rawCorrige,
      copieEtudiantPath: rawCopie.isEmpty ? null : rawCopie,
    );
  }
}
