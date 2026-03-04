import 'package:dwm_bot/features/etudiant/presentation/page_cours_etudiant.dart';
import 'package:dwm_bot/features/etudiant/presentation/page_emploi_etudiant.dart';
import 'package:dwm_bot/features/etudiant/presentation/page_notes_etudiant.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../../authentification/presentation/controleur_authentification.dart';
import '../data/controller/controleur_profil_etudiant.dart';
import 'AnimatedNotchBottomBar.dart';
import 'accueil_etudiant.dart';

class EtudiantShell extends StatefulWidget {
  const EtudiantShell({super.key});

  @override
  State<EtudiantShell> createState() => _EtudiantShellState();
}

class _EtudiantShellState extends State<EtudiantShell> {
  static const Color _accent = Color(0xFF5B5BEA);
  int _index = 0;
  static const List<String> _titres = <String>[
    "Dashboard",
    "Cours",
    "Calendrier",
    "Notes",
    "Plus",
  ];

  late final List<NotchBarItem> _items = const [
    NotchBarItem(icon: Icons.grid_view_rounded, label: "Dashboard"),
    NotchBarItem(icon: Icons.menu_book_rounded, label: "Cours"),
    NotchBarItem(icon: Icons.calendar_month_rounded, label: "Calendrier"),
    NotchBarItem(icon: Icons.workspace_premium_rounded, label: "Notes"),
    NotchBarItem(icon: Icons.more_horiz_rounded, label: "Plus"),
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurProfilEtudiant>().chargerMoi();
    });
  }

  @override
  Widget build(BuildContext context) {
    final profilCtrl = context.watch<ControleurProfilEtudiant>();

    final prenom = profilCtrl.moi?.prenom ?? '';
    final nom = profilCtrl.moi?.nom ?? '';
    final nomAffiche =
        (prenom + nom).trim().isEmpty ? "Étudiant" : "$prenom $nom".trim();

    final initiales = _initialesPrenomNom(prenom, nom);
    final photoUrl = profilCtrl.moi?.photoUrl;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),

      // ✅ AppBar seulement si index != 0
      appBar:
          _index == 0
              ? null
              : PreferredSize(
                preferredSize: const Size.fromHeight(72),
                child: SafeArea(
                  bottom: false,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 8,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      border: Border(
                        bottom: BorderSide(
                          color: Colors.black.withOpacity(0.06),
                        ),
                      ),
                    ),
                    child: Row(
                      children: [
                        Text(
                          _titres[_index],
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w900,
                            color: Color(0xFF0B1220),
                          ),
                        ),
                        const Spacer(),
                        _ProfilButton(
                          nomAffiche: nomAffiche,
                          initiales: initiales,
                          photoUrl: photoUrl,
                          accent: _accent,
                        ),
                      ],
                    ),
                  ),
                ),
              ),

      body: IndexedStack(
        index: _index,
        children: [
          PageAccueilEtudiant(),
          PageCoursEtudiant(),
          PageEmploiTimelineEtudiant(),
          PageNotesEtudiant(),
          const _PlaceholderPage(titre: "Plus"),
        ],
      ),

      bottomNavigationBar: AnimatedNotchBottomBar(
        items: _items,
        currentIndex: _index,
        onTap: (i) => setState(() => _index = i),
        activeColor: _accent,
        inactiveColor: const Color(0xFF9AA3B2),
        backgroundColor: Colors.white,
        height: 82,
        notchSize: 58,
        notchRadius: 32,
      ),
    );
  }
}

class _ProfilButton extends StatelessWidget {
  const _ProfilButton({
    required this.nomAffiche,
    required this.initiales,
    required this.photoUrl,
    required this.accent,
  });

  final String nomAffiche;
  final String initiales;
  final String? photoUrl;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    final url = _construireUrlPhoto(photoUrl);

    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: () => _ouvrirMenu(context),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 150),
            child: Text(
              nomAffiche,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800),
            ),
          ),
          const SizedBox(width: 10),
          CircleAvatar(
            radius: 16,
            backgroundColor: accent.withOpacity(0.12),
            backgroundImage: url != null ? NetworkImage(url) : null,
            child:
                url == null
                    ? Text(
                      initiales,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w900,
                        color: accent,
                      ),
                    )
                    : null,
          ),
        ],
      ),
    );
  }
}

void _ouvrirMenu(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    builder: (_) {
      return SafeArea(
        child: Container(
          margin: const EdgeInsets.all(12),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(18),
            boxShadow: [
              BoxShadow(
                blurRadius: 24,
                offset: const Offset(0, 14),
                color: Colors.black.withOpacity(0.14),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.person_outline),
                title: const Text("Profil"),
                onTap: () {
                  Navigator.pop(context);
                  Navigator.pushNamed(context, "/etudiant/profil");
                },
              ),
              ListTile(
                leading: const Icon(Icons.logout),
                title: const Text("Déconnexion"),
                onTap: () async {
                  Navigator.pop(context);
                  await context
                      .read<ControleurAuthentification>()
                      .seDeconnecter();
                },
              ),
            ],
          ),
        ),
      );
    },
  );
}

class _PlaceholderPage extends StatelessWidget {
  const _PlaceholderPage({required this.titre});
  final String titre;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        "Page $titre",
        style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
      ),
    );
  }
}

/* Helpers */

String _initialesPrenomNom(String prenom, String nom) {
  String first(String s) =>
      s.trim().isEmpty ? "" : s.trim().substring(0, 1).toUpperCase();
  final res = (first(prenom) + first(nom)).trim();
  return res.isEmpty ? "U" : res;
}

String? _construireUrlPhoto(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return "${ConfigurationApp.apiBaseUrl}$photoUrl";
}
