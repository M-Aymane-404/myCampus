enum TypeSeance { COURS, TD, TP }

TypeSeance parseTypeSeance(dynamic value) {
  final str = (value ?? '').toString().toUpperCase();
  switch (str) {
    case 'COURS':
      return TypeSeance.COURS;
    case 'TD':
      return TypeSeance.TD;
    case 'TP':
      return TypeSeance.TP;
    default:
      return TypeSeance.COURS;
  }
}

class ModuleSortieEtudiantDTO {
  final String codeModule;
  final String nom;
  final String semestre;

  const ModuleSortieEtudiantDTO({
    required this.codeModule,
    required this.nom,
    required this.semestre,
  });

  factory ModuleSortieEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return ModuleSortieEtudiantDTO(
      codeModule: (json['codeModule'] ?? '').toString(),
      nom: (json['nom'] ?? '').toString(),
      semestre: (json['semestre'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'codeModule': codeModule,
    'nom': nom,
    'semestre': semestre,
  };

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ModuleSortieEtudiantDTO &&
          runtimeType == other.runtimeType &&
          codeModule == other.codeModule;

  @override
  int get hashCode => codeModule.hashCode;

  @override
  String toString() => 'Module($codeModule)';
}

class MatiereSortieEtudiantDTO {
  final String codeMatiere;
  final String nom;

  const MatiereSortieEtudiantDTO({
    required this.codeMatiere,
    required this.nom,
  });

  factory MatiereSortieEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return MatiereSortieEtudiantDTO(
      codeMatiere: (json['codeMatiere'] ?? '').toString(),
      nom: (json['nom'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {'codeMatiere': codeMatiere, 'nom': nom};

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is MatiereSortieEtudiantDTO &&
          runtimeType == other.runtimeType &&
          codeMatiere == other.codeMatiere;

  @override
  int get hashCode => codeMatiere.hashCode;

  @override
  String toString() => 'Matiere($codeMatiere)';
}

class SeanceSortieEtudiantDTO {
  final int id;
  final TypeSeance type;
  final String? date;

  final String salle;
  final String etat;
  final String contenu;

  const SeanceSortieEtudiantDTO({
    required this.id,
    required this.type,
    required this.date,

    required this.salle,
    required this.etat,
    required this.contenu,
  });

  factory SeanceSortieEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return SeanceSortieEtudiantDTO(
      id: (json['id'] is int) ? json['id'] : int.tryParse("${json['id']}") ?? 0,
      type: parseTypeSeance(json['type']),
      date: json['date']?.toString(),

      salle: (json['salle'] ?? '').toString(),
      etat: (json['etat'] ?? '').toString(),
      contenu: (json['contenu'] ?? '').toString(),
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SeanceSortieEtudiantDTO &&
          runtimeType == other.runtimeType &&
          id == other.id;

  @override
  int get hashCode => id.hashCode;

  String get libelleTemps {
    final d =
        (date == null || date!.trim().isEmpty) ? "Date non définie" : date!;

    return "$d";
  }

  String get libelleType => type.name;
}

class FichierSeanceSortieEtudiantDTO {
  final int id;
  final String nomOriginal;
  final String chemin;
  final String typeMime;
  final int taille;
  final String dateUpload;

  const FichierSeanceSortieEtudiantDTO({
    required this.id,
    required this.nomOriginal,
    required this.chemin,
    required this.typeMime,
    required this.taille,
    required this.dateUpload,
  });

  factory FichierSeanceSortieEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return FichierSeanceSortieEtudiantDTO(
      id: (json['id'] is int) ? json['id'] : int.tryParse("${json['id']}") ?? 0,
      nomOriginal: (json['nomOriginal'] ?? '').toString(),
      chemin: (json['chemin'] ?? '').toString(),
      typeMime: (json['typeMime'] ?? '').toString(),
      taille:
          (json['taille'] is int)
              ? json['taille']
              : int.tryParse("${json['taille']}") ?? 0,
      dateUpload: (json['dateUpload'] ?? '').toString(),
    );
  }

  String get tailleFormatee {
    if (taille < 1024) return '${taille}B';
    if (taille < 1024 * 1024) return '${(taille / 1024).toStringAsFixed(1)}KB';
    return '${(taille / (1024 * 1024)).toStringAsFixed(1)}MB';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FichierSeanceSortieEtudiantDTO &&
          runtimeType == other.runtimeType &&
          id == other.id;

  @override
  int get hashCode => id.hashCode;
}
