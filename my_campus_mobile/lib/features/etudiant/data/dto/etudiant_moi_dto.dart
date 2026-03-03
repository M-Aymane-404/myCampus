class EtudiantMoiDTO {
  final int id;
  final String nom;
  final String prenom;
  final String email;
  final String? photoUrl;
  final String? codeClasse;
  final String? niveau;
  final String? semestreActuel;

  const EtudiantMoiDTO({
    required this.id,
    required this.nom,
    required this.prenom,
    required this.email,
    required this.photoUrl,
    required this.codeClasse,
    required this.niveau,
    required this.semestreActuel,
  });

  String get nomComplet => "${prenom.trim()} ${nom.trim()}".trim();

  factory EtudiantMoiDTO.fromJson(Map<String, dynamic> json) {
    return EtudiantMoiDTO(
      id: (json['id'] is int) ? json['id'] : int.tryParse("${json['id']}") ?? 0,
      nom: (json['nom'] ?? '').toString(),
      prenom: (json['prenom'] ?? '').toString(),
      email: (json['email'] ?? '').toString(),
      photoUrl: json['photoUrl']?.toString(),
      codeClasse: json['codeClasse']?.toString(),
      niveau: json['niveau']?.toString(),
      semestreActuel: json['semestreActuel']?.toString(),
    );
  }
}
