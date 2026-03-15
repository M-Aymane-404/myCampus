class ProfilEtudiantDTO {
  final String nom;
  final String prenom;
  final String email;
  final String tel;
  final String dateNaissance;
  final String? photoUrl;

  const ProfilEtudiantDTO({
    required this.nom,
    required this.prenom,
    required this.email,
    required this.tel,
    required this.dateNaissance,
    required this.photoUrl,
  });

  factory ProfilEtudiantDTO.fromJson(Map<String, dynamic> json) {
    return ProfilEtudiantDTO(
      nom: (json['nom'] ?? '').toString(),
      prenom: (json['prenom'] ?? '').toString(),
      email: (json['email'] ?? '').toString(),
      tel: (json['tel'] ?? '').toString(),
      dateNaissance: (json['dateNaissance'] ?? '').toString(),
      photoUrl:
          (json['photoUrl'] ?? '').toString().isEmpty
              ? null
              : (json['photoUrl']).toString(),
    );
  }
}

class ModifierProfilEtudiantDTO {
  final String? tel;
  final String? ancienPassword;
  final String? nouveauPassword;

  const ModifierProfilEtudiantDTO({
    this.tel,
    this.ancienPassword,
    this.nouveauPassword,
  });

  Map<String, dynamic> toJson() => {
    if (tel != null) 'tel': tel,
    if (ancienPassword != null) 'ancienPassword': ancienPassword,
    if (nouveauPassword != null) 'nouveauPassword': nouveauPassword,
  };
}
