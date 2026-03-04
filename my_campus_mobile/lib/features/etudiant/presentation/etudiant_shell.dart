import 'package:dwm_bot/features/etudiant/presentation/page_cours_etudiant.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../../authentification/presentation/controleur_authentification.dart';
import 'accueil_etudiant.dart';
import 'controleur_profil_etudiant.dart';

class EtudiantShell extends StatefulWidget {
  const EtudiantShell({super.key});

  @override
  State<EtudiantShell> createState() => _EtudiantShellState();
}

class _EtudiantShellState extends State<EtudiantShell> {
  static const Color _accent = Color(0xFF5B5BEA);

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
        (prenom + "" + nom).trim().isEmpty
            ? "Étudiant"
            : (prenom + " " + nom).trim();

    final initiales = _initialesPrenomNom(prenom, nom);
    final photoUrl = profilCtrl.moi?.photoUrl;

    return CupertinoTabScaffold(
      tabBar: CupertinoTabBar(
        activeColor: _accent,
        inactiveColor: CupertinoColors.systemGrey,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(CupertinoIcons.rectangle_grid_2x2),
            label: "Dashboard",
          ),
          BottomNavigationBarItem(
            icon: Icon(CupertinoIcons.book),
            label: "Cours",
          ),
          BottomNavigationBarItem(
            icon: Icon(CupertinoIcons.rosette),
            label: "Notes",
          ),
          BottomNavigationBarItem(
            icon: Icon(CupertinoIcons.checkmark_seal),
            label: "Présence",
          ),
          BottomNavigationBarItem(
            icon: Icon(CupertinoIcons.ellipsis_circle),
            label: "Plus",
          ),
        ],
      ),

      tabBuilder: (context, index) {
        return CupertinoTabView(
          builder: (_) {
            if (index == 0) {
              return const PageAccueilEtudiant();
            }
            return CupertinoPageScaffold(
              backgroundColor: CupertinoColors.white,
              navigationBar: CupertinoNavigationBar(
                leading: Text(_titre(index)),
                trailing: _ProfilButtonIOS(
                  nomAffiche: nomAffiche,
                  initiales: initiales,
                  photoUrl: photoUrl,
                  accent: _accent,
                ),
              ),
              child: SafeArea(bottom: false, child: _page(index)),
            );
          },
        );
      },
    );
  }

  String _titre(int index) {
    switch (index) {
      case 0:
        return "Dashboard";
      case 1:
        return "Cours";
      case 2:
        return "Notes";
      case 3:
        return "Présence";
      case 4:
        return "Plus";
      default:
        return "MyCampus";
    }
  }

  Widget _page(int index) {
    switch (index) {
      case 0:
        return const PageAccueilEtudiant();
      case 1:
        return const PageCoursEtudiant();
      case 2:
        return const _PlaceholderPage(titre: "Notes");
      case 3:
        return const _PlaceholderPage(titre: "Présence");
      case 4:
        return const _PlaceholderPage(titre: "Plus");
      default:
        return const _PlaceholderPage(titre: "MyCampus");
    }
  }
}

class _ProfilButtonIOS extends StatelessWidget {
  const _ProfilButtonIOS({
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

    return CupertinoButton(
      padding: EdgeInsets.zero,
      // ✅ minSize deprecated -> on ne met rien (ou utilise minimumSize si besoin)
      onPressed: () => _ouvrirMenu(context),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 140),
            child: Text(
              nomAffiche,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w600,
                color: CupertinoColors.label,
              ),
            ),
          ),
          const SizedBox(width: 20),
          CircleAvatar(
            radius: 16,
            backgroundColor: accent.withValues(alpha: 0.12),
            backgroundImage: url != null ? NetworkImage(url) : null,
            child:
                url == null
                    ? Text(
                      initiales,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
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
  showCupertinoModalPopup<void>(
    context: context,
    builder: (ctx) {
      return CupertinoActionSheet(
        title: const Text("Compte"),
        actions: [
          CupertinoActionSheetAction(
            onPressed: () {
              Navigator.pop(ctx);
              Navigator.pushNamed(context, "/etudiant/profil");
            },
            child: const Text("Profil"),
          ),
        ],
        cancelButton: CupertinoActionSheetAction(
          isDestructiveAction: true,
          onPressed: () async {
            Navigator.pop(ctx);
            await context.read<ControleurAuthentification>().seDeconnecter();
          },
          child: const Text("Déconnexion"),
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
        style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 18),
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
