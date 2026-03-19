import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../authentification/presentation/controleur_authentification.dart';
import '../../etudiant/presentation/AnimatedNotchBottomBar.dart';
import '../data/controller/controleur_espace_professeur.dart';
import 'accueil_professeur.dart';
import 'page_calendrier_professeur.dart';
import 'page_cours_professeur.dart';
import 'page_presence_professeur.dart';
import 'page_profil_professeur.dart';
import 'widgets/avatar_mini_professeur.dart';

class ProfesseurShell extends StatefulWidget {
  const ProfesseurShell({super.key});

  @override
  State<ProfesseurShell> createState() => _ProfesseurShellState();
}

class _ProfesseurShellState extends State<ProfesseurShell> {
  static const Color _accent = Color(0xFF5B5BEA);
  int _index = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurEspaceProfesseur>().chargerEspace();
    });
  }

  @override
  Widget build(BuildContext context) {
    final profil = context.watch<ControleurEspaceProfesseur>().profil;
    final avatar = AvatarMiniProfesseur(
      photoUrl: profil?.photoUrl,
      accent: _accent,
    );

    final items = <NotchBarItem>[
      const NotchBarItem(icon: Icons.grid_view_rounded, label: 'Dashboard'),
      const NotchBarItem(icon: Icons.menu_book_rounded, label: 'Cours'),
      const NotchBarItem(
        icon: Icons.calendar_month_rounded,
        label: 'Calendrier',
      ),
      const NotchBarItem(icon: Icons.how_to_reg_rounded, label: 'Presences'),
      NotchBarItem(customIcon: avatar, label: 'Profil'),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: IndexedStack(
        index: _index,
        children: [
          const AccueilProfesseur(),
          const PageCoursProfesseur(),
          const PageCalendrierProfesseur(),
          const PagePresenceProfesseur(),
          PageProfilProfesseur(
            onLogout: () async {
              await context.read<ControleurAuthentification>().seDeconnecter();
            },
          ),
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
