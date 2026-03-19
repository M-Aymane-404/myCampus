class ProfesseurProfilMobileDTO {
  const ProfesseurProfilMobileDTO({
    required this.nom,
    required this.prenom,
    required this.email,
    required this.tel,
    required this.ppr,
    required this.specialite,
    required this.grade,
    required this.photoUrl,
    required this.codeDepartementOuTravail,
  });

  final String nom;
  final String prenom;
  final String email;
  final String tel;
  final String ppr;
  final String specialite;
  final String grade;
  final String? photoUrl;
  final String? codeDepartementOuTravail;

  factory ProfesseurProfilMobileDTO.fromJson(Map<String, dynamic> json) {
    return ProfesseurProfilMobileDTO(
      nom: (json['nom'] ?? '').toString(),
      prenom: (json['prenom'] ?? '').toString(),
      email: (json['email'] ?? '').toString(),
      tel: (json['tel'] ?? json['telephone'] ?? '').toString(),
      ppr: (json['ppr'] ?? '').toString(),
      specialite: (json['specialite'] ?? '').toString(),
      grade: (json['grade'] ?? '').toString(),
      photoUrl:
          (json['photoUrl'] ?? '').toString().trim().isEmpty
              ? null
              : (json['photoUrl']).toString(),
      codeDepartementOuTravail:
          (json['codeDepartementOuTravail'] ?? '').toString().trim().isEmpty
              ? null
              : (json['codeDepartementOuTravail']).toString(),
    );
  }

  String get nomComplet {
    final value = '$prenom $nom'.trim();
    return value.isEmpty ? 'Professeur' : value;
  }
}

class ModifierProfilProfesseurDTO {
  const ModifierProfilProfesseurDTO({
    this.tel,
    this.ancienPassword,
    this.nouveauPassword,
  });

  final String? tel;
  final String? ancienPassword;
  final String? nouveauPassword;

  Map<String, dynamic> toJson() => {
    if (tel != null) 'tel': tel,
    if (ancienPassword != null) 'ancienPassword': ancienPassword,
    if (nouveauPassword != null) 'nouveauPassword': nouveauPassword,
  };
}

class EnseignementProfesseurMobileDTO {
  const EnseignementProfesseurMobileDTO({
    required this.id,
    required this.codeClasse,
    required this.classeNom,
    required this.codeMatiere,
    required this.matiereNom,
    required this.anneeUniversitaire,
  });

  final int? id;
  final String codeClasse;
  final String classeNom;
  final String codeMatiere;
  final String matiereNom;
  final String anneeUniversitaire;

  factory EnseignementProfesseurMobileDTO.fromJson(Map<String, dynamic> json) {
    return EnseignementProfesseurMobileDTO(
      id: json['id'] is num ? (json['id'] as num).toInt() : null,
      codeClasse: (json['codeClasse'] ?? '').toString(),
      classeNom:
          (json['classeNom'] ?? json['nomClasse'] ?? json['codeClasse'] ?? '')
              .toString(),
      codeMatiere: (json['codeMatiere'] ?? '').toString(),
      matiereNom:
          (json['matiereNom'] ?? json['nomMatiere'] ?? json['codeMatiere'] ?? '')
              .toString(),
      anneeUniversitaire:
          (json['anneeUniversitaire'] ?? json['annee'] ?? '').toString(),
    );
  }
}

class SeanceProfesseurMobileDTO {
  const SeanceProfesseurMobileDTO({
    required this.id,
    required this.jour,
    required this.heureDebut,
    required this.heureFin,
    required this.matiereNom,
    required this.typeSeance,
    required this.salle,
    required this.codeClasse,
    required this.classeNom,
    required this.codeMatiere,
    required this.supportDisponible,
    required this.ressourcesCount,
  });

  final int? id;
  final String jour;
  final String heureDebut;
  final String heureFin;
  final String matiereNom;
  final String typeSeance;
  final String salle;
  final String codeClasse;
  final String classeNom;
  final String codeMatiere;
  final bool? supportDisponible;
  final int? ressourcesCount;

  factory SeanceProfesseurMobileDTO.fromJson(Map<String, dynamic> json) {
    return SeanceProfesseurMobileDTO(
      id: json['id'] is num ? (json['id'] as num).toInt() : null,
      jour:
          (json['jour'] ??
                  json['dayOfWeek'] ??
                  json['date'] ??
                  '')
              .toString(),
      heureDebut:
          (json['heureDebut'] ?? json['start'] ?? '').toString(),
      heureFin:
          (json['heureFin'] ??
                  json['end'] ??
                  json['endTime'] ??
                  json['fin'] ??
                  '')
              .toString(),
      matiereNom:
          (json['matiereNom'] ??
                  json['nomMatiere'] ??
                  json['matiere'] ??
                  json['nom'] ??
                  '')
              .toString(),
      typeSeance: (json['typeSeance'] ?? json['type'] ?? 'COURS').toString(),
      salle: (json['salle'] ?? '').toString(),
      codeClasse: (json['codeClasse'] ?? '').toString(),
      codeMatiere: (json['codeMatiere'] ?? '').toString(),
      classeNom:
          (json['classeNom'] ?? json['nomClasse'] ?? json['codeClasse'] ?? '')
              .toString(),
      supportDisponible:
          json['supportDisponible'] is bool
              ? json['supportDisponible'] as bool
              : json['hasSupport'] is bool
              ? json['hasSupport'] as bool
              : json['supportExists'] is bool
              ? json['supportExists'] as bool
              : null,
      ressourcesCount:
          json['ressourcesCount'] is num
              ? (json['ressourcesCount'] as num).toInt()
              : json['resourcesCount'] is num
              ? (json['resourcesCount'] as num).toInt()
              : json['nombreRessources'] is num
              ? (json['nombreRessources'] as num).toInt()
              : null,
    );
  }
}

class MatiereFiltreProfesseurMobileDTO {
  const MatiereFiltreProfesseurMobileDTO({
    required this.codeMatiere,
    required this.nomMatiere,
  });

  final String codeMatiere;
  final String nomMatiere;

  factory MatiereFiltreProfesseurMobileDTO.fromJson(
    Map<String, dynamic> json,
  ) {
    return MatiereFiltreProfesseurMobileDTO(
      codeMatiere: (json['codeMatiere'] ?? '').toString(),
      nomMatiere:
          (json['nomMatiere'] ?? json['codeMatiere'] ?? '').toString(),
    );
  }
}

class ClasseFiltreProfesseurMobileDTO {
  const ClasseFiltreProfesseurMobileDTO({
    required this.codeClasse,
    required this.nomClasse,
    required this.matieres,
  });

  final String codeClasse;
  final String nomClasse;
  final List<MatiereFiltreProfesseurMobileDTO> matieres;

  factory ClasseFiltreProfesseurMobileDTO.fromJson(Map<String, dynamic> json) {
    final rawMatieres = (json['matieres'] as List?) ?? const [];
    return ClasseFiltreProfesseurMobileDTO(
      codeClasse: (json['codeClasse'] ?? '').toString(),
      nomClasse: (json['nomClasse'] ?? json['codeClasse'] ?? '').toString(),
      matieres:
          rawMatieres
              .map(
                (e) => MatiereFiltreProfesseurMobileDTO.fromJson(
                  (e as Map).cast<String, dynamic>(),
                ),
              )
              .toList(),
    );
  }
}

class ExamenProfesseurMobileDTO {
  const ExamenProfesseurMobileDTO({
    required this.idExamen,
    required this.nomMatiere,
    required this.semestre,
    required this.numeroExamen,
    required this.typeExamen,
    required this.dateExamen,
    required this.duree,
    required this.etat,
    required this.corrigeDisponible,
  });

  final int? idExamen;
  final String nomMatiere;
  final String semestre;
  final int? numeroExamen;
  final String typeExamen;
  final String dateExamen;
  final int? duree;
  final String etat;
  final bool corrigeDisponible;

  factory ExamenProfesseurMobileDTO.fromJson(Map<String, dynamic> json) {
    return ExamenProfesseurMobileDTO(
      idExamen:
          json['idExamen'] is num ? (json['idExamen'] as num).toInt() : null,
      nomMatiere:
          (json['nomMatiere'] ?? json['codeMatiere'] ?? '').toString(),
      semestre: (json['semestre'] ?? '').toString(),
      numeroExamen:
          json['numeroExamen'] is num
              ? (json['numeroExamen'] as num).toInt()
              : null,
      typeExamen: (json['typeExamen'] ?? '').toString(),
      dateExamen: (json['dateExamen'] ?? '').toString(),
      duree: json['duree'] is num ? (json['duree'] as num).toInt() : null,
      etat: (json['etat'] ?? '').toString(),
      corrigeDisponible: json['corrigeDisponible'] == true,
    );
  }

  String get label {
    final numero = numeroExamen == null ? '' : ' #$numeroExamen';
    return '$typeExamen$numero';
  }
}
