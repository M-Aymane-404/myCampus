import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../data/controller/controleur_profil_etudiant.dart';
import 'AnimatedNotchBottomBar.dart';
import 'accueil_etudiant.dart';
import 'page_cours_etudiant.dart';
import 'page_emploi_etudiant.dart';
import 'page_notes_etudiant.dart';
import 'page_profil_etudiant.dart';

class EtudiantShell extends StatefulWidget {
  const EtudiantShell({super.key});

  @override
  State<EtudiantShell> createState() => _EtudiantShellState();
}

class _EtudiantShellState extends State<EtudiantShell> {
  static const Color _accent = Color(0xFF5B5BEA);
  int _index = 0;

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
    final photoUrl = profilCtrl.moi?.photoUrl;
    final avatar = _AvatarMini(photoUrl: photoUrl, accent: _accent);

    final items = <NotchBarItem>[
      const NotchBarItem(icon: Icons.grid_view_rounded, label: "Dashboard"),
      const NotchBarItem(icon: Icons.menu_book_rounded, label: "Cours"),
      const NotchBarItem(
        icon: Icons.calendar_month_rounded,
        label: "Calendrier",
      ),
      const NotchBarItem(icon: Icons.workspace_premium_rounded, label: "Notes"),
      NotchBarItem(customIcon: avatar, label: "Profil"),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),

      body: IndexedStack(
        index: _index,
        children: const [
          PageAccueilEtudiant(),
          PageCoursEtudiant(),
          PageEmploiTimelineEtudiant(),
          PageNotesEtudiant(),
          PageProfilEtudiant(),
        ],
      ),
      bottomNavigationBar: AnimatedNotchBottomBar(
        items: items,
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

class _AvatarMini extends StatelessWidget {
  const _AvatarMini({required this.photoUrl, required this.accent});
  final String? photoUrl;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    final url = _construireUrlPhoto(photoUrl);
    if (url != null) {
      return ClipOval(
        child: SizedBox.expand(
          child: Image.network(
            url,
            fit: BoxFit.cover,
            errorBuilder:
                (_, __, ___) => Container(
                  color: accent.withOpacity(0.12),
                  alignment: Alignment.center,
                  child: Icon(Icons.person, size: 14, color: accent),
                ),
          ),
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        color: accent.withOpacity(0.12),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Icon(Icons.person, size: 14, color: accent),
    );
  }
}

String? _construireUrlPhoto(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return "${ConfigurationApp.apiBaseUrl}$photoUrl";
}
