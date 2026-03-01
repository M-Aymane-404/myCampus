// tableau_bord_etudiant_dto.dart

class TableauBordEtudiantDTO {
  final EnteteTableauBordDTO entete;
  final List<NoteEtudiantDTO> dernieresNotes;
  final AssiduiteDTO assiduite;
  final ProchaineSeanceDTO? prochaineSeance; // nullable si pas de séance
  final List<AnnonceDTO> annonces;

  const TableauBordEtudiantDTO({
    required this.entete,
    required this.dernieresNotes,
    required this.assiduite,
    required this.prochaineSeance,
    required this.annonces,
  });

  factory TableauBordEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return TableauBordEtudiantDTO(
      entete: EnteteTableauBordDTO.fromJson(
        (json['entete'] as Map?)?.cast<String, dynamic>() ??
            const <String, dynamic>{},
      ),
      dernieresNotes:
          ((json['dernieresNotes'] as List?) ?? const [])
              .map(
                (e) => NoteEtudiantDTO.fromJson(
                  (e as Map).cast<String, dynamic>(),
                ),
              )
              .toList(),
      assiduite: AssiduiteDTO.fromJson(
        (json['assiduite'] as Map?)?.cast<String, dynamic>() ??
            const <String, dynamic>{},
      ),
      prochaineSeance:
          (json['prochaineSeance'] == null)
              ? null
              : ProchaineSeanceDTO.fromJson(
                (json['prochaineSeance'] as Map).cast<String, dynamic>(),
              ),
      annonces:
          ((json['annonces'] as List?) ?? const [])
              .map(
                (e) => AnnonceDTO.fromJson((e as Map).cast<String, dynamic>()),
              )
              .toList(),
    );
  }

  Map<String, dynamic> toJson() => {
    'entete': entete.toJson(),
    'dernieresNotes': dernieresNotes.map((e) => e.toJson()).toList(),
    'assiduite': assiduite.toJson(),
    'prochaineSeance': prochaineSeance?.toJson(),
    'annonces': annonces.map((e) => e.toJson()).toList(),
  };
}

class EnteteTableauBordDTO {
  final String nom;
  final String niveau;
  final String filiere;
  final String classe;
  final String session;
  final String statut;

  const EnteteTableauBordDTO({
    required this.nom,
    required this.niveau,
    required this.filiere,
    required this.classe,
    required this.session,
    required this.statut,
  });

  factory EnteteTableauBordDTO.fromJson(Map<String, dynamic> json) {
    return EnteteTableauBordDTO(
      nom: (json['nom'] ?? '').toString(),
      niveau: (json['niveau'] ?? '').toString(),
      filiere: (json['filiere'] ?? '').toString(),
      classe: (json['classe'] ?? '').toString(),
      session: (json['session'] ?? '').toString(),
      statut: (json['statut'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'nom': nom,
    'niveau': niveau,
    'filiere': filiere,
    'classe': classe,
    'session': session,
    'statut': statut,
  };
}

class NoteEtudiantDTO {
  final String matiere;
  final double note;
  final String datePublication;

  const NoteEtudiantDTO({
    required this.matiere,
    required this.note,
    required this.datePublication,
  });

  factory NoteEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return NoteEtudiantDTO(
      matiere: (json['matiere'] ?? '').toString(),
      note: _asDouble(json['note']),
      datePublication: (json['datePublication'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'matiere': matiere,
    'note': note,
    'datePublication': datePublication,
  };
}

class AssiduiteDTO {
  final double tauxPresence;
  final String labelQualite;

  const AssiduiteDTO({required this.tauxPresence, required this.labelQualite});

  int get tauxPresenceArrondi => tauxPresence.round();

  factory AssiduiteDTO.fromJson(Map<String, dynamic> json) {
    return AssiduiteDTO(
      tauxPresence: _asDouble(json['tauxPresence']),
      labelQualite: (json['labelQualite'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'tauxPresence': tauxPresence,
    'labelQualite': labelQualite,
  };
}

class AnnonceDTO {
  final int id;
  final String titre;
  final String contenu;
  final String datePublication;
  final bool urgent;
  final bool publie;
  final String? codeClasse;

  const AnnonceDTO({
    required this.id,
    required this.titre,
    required this.contenu,
    required this.datePublication,
    required this.urgent,
    required this.publie,
    required this.codeClasse,
  });

  factory AnnonceDTO.fromJson(Map<String, dynamic> json) {
    return AnnonceDTO(
      id:
          (json['id'] ?? 0) is int
              ? (json['id'] ?? 0)
              : int.tryParse("${json['id']}") ?? 0,
      titre: (json['titre'] ?? '').toString(),
      contenu: (json['contenu'] ?? '').toString(),
      datePublication: (json['datePublication'] ?? '').toString(),
      urgent: (json['urgent'] ?? false) == true,
      publie: (json['publie'] ?? false) == true,
      codeClasse: json['codeClasse']?.toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'titre': titre,
    'contenu': contenu,
    'datePublication': datePublication,
    'urgent': urgent,
    'publie': publie,
    'codeClasse': codeClasse,
  };
}

/// Helpers sûrs

class ProchaineSeanceDTO {
  final String dayOfWeek;
  final String start;
  final String end;
  final String matiereCode;
  final String matiereNom;
  final String professeurNom;
  final String salle;
  final String typeSeance;

  const ProchaineSeanceDTO({
    required this.dayOfWeek,
    required this.start,
    required this.end,
    required this.matiereCode,
    required this.matiereNom,
    required this.professeurNom,
    required this.salle,
    required this.typeSeance,
  });

  factory ProchaineSeanceDTO.fromJson(Map<String, dynamic> json) {
    return ProchaineSeanceDTO(
      dayOfWeek: (json['dayOfWeek'] ?? '').toString(),
      start: (json['start'] ?? '').toString(),
      end: (json['end'] ?? '').toString(),
      matiereCode: (json['matiereCode'] ?? '').toString(),
      matiereNom: (json['matiereNom'] ?? '').toString(),
      professeurNom: (json['professeurNom'] ?? '').toString(),
      salle: (json['salle'] ?? '').toString(),
      typeSeance: (json['typeSeance'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() => {
    'dayOfWeek': dayOfWeek,
    'start': start,
    'end': end,
    'matiereCode': matiereCode,
    'matiereNom': matiereNom,
    'professeurNom': professeurNom,
    'salle': salle,
    'typeSeance': typeSeance,
  };

  String get horaire => "$start - $end";
}

double _asDouble(dynamic v) {
  if (v == null) return 0.0;
  if (v is num) return v.toDouble();
  return double.tryParse(v.toString()) ?? 0.0;
}
