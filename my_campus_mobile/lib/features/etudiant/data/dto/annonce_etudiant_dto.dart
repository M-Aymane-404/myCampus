class AuteurAnnonceDTO {
  final int id;
  final String nom;
  final String prenom;

  const AuteurAnnonceDTO({
    required this.id,
    required this.nom,
    required this.prenom,
  });

  factory AuteurAnnonceDTO.fromJson(Map<String, dynamic> json) {
    return AuteurAnnonceDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      nom: (json['nom'] ?? '').toString(),
      prenom: (json['prenom'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'nom': nom,
    'prenom': prenom,
  };

  String get nomComplet => '$prenom $nom'.trim();
}

class AnnonceEtudiantDTO {
  final int id;
  final String titre;
  final String contenu;
  final String datePublication;
  final bool urgent;
  final String? typeAnnonce;
  final bool publiee;
  final String cibleType;
  final String? codeClasse;
  final AuteurAnnonceDTO? auteur;

  // local uniquement cote Flutter
  final bool estLue;

  const AnnonceEtudiantDTO({
    required this.id,
    required this.titre,
    required this.contenu,
    required this.datePublication,
    required this.urgent,
    required this.typeAnnonce,
    required this.publiee,
    required this.cibleType,
    required this.codeClasse,
    required this.auteur,
    this.estLue = false,
  });

  factory AnnonceEtudiantDTO.fromJson(
    Map<String, dynamic> json, {
    bool estLue = false,
  }) {
    return AnnonceEtudiantDTO(
      id: (json['id'] as num?)?.toInt() ?? 0,
      titre: (json['titre'] ?? '').toString(),
      contenu: (json['contenu'] ?? '').toString(),
      datePublication: (json['datePublication'] ?? '').toString(),
      urgent: json['urgent'] == true,
      typeAnnonce: json['typeAnnonce']?.toString(),
      publiee: json['publiee'] == true,
      cibleType: (json['cibleType'] ?? '').toString(),
      codeClasse: json['codeClasse']?.toString(),
      auteur: json['auteur'] is Map
          ? AuteurAnnonceDTO.fromJson(
              (json['auteur'] as Map).cast<String, dynamic>(),
            )
          : null,
      estLue: estLue,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'titre': titre,
    'contenu': contenu,
    'datePublication': datePublication,
    'urgent': urgent,
    'typeAnnonce': typeAnnonce,
    'publiee': publiee,
    'cibleType': cibleType,
    'codeClasse': codeClasse,
    'auteur': auteur?.toJson(),
    'estLue': estLue,
  };

  AnnonceEtudiantDTO copyWith({bool? estLue}) {
    return AnnonceEtudiantDTO(
      id: id,
      titre: titre,
      contenu: contenu,
      datePublication: datePublication,
      urgent: urgent,
      typeAnnonce: typeAnnonce,
      publiee: publiee,
      cibleType: cibleType,
      codeClasse: codeClasse,
      auteur: auteur,
      estLue: estLue ?? this.estLue,
    );
  }
}
