/*import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../../authentification/presentation/controleur_authentification.dart';

class BarreHautMyCampus extends StatelessWidget {
  const BarreHautMyCampus({
    super.key,
    required this.titre,
    required this.nomAffiche,
    required this.role,
    required this.initiales,
    required this.photoUrl,
  });

  final String titre; // ex: "Dashboard" / "Cours"
  final String nomAffiche; // ex: data.entete.nom
  final String role; // ex: "ETUDIANT"
  final String initiales;
  final String? photoUrl;

  static const Color _accent = Color(0xFF5B5BEA);

  @override
  Widget build(BuildContext context) {
    final url = construireUrlPhoto(photoUrl);

    return Container(
      padding: const EdgeInsets.fromLTRB(18, 12, 18, 12),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border(
          bottom: BorderSide(color: Colors.black.withValues(alpha: 0.06)),
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: _accent.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: _accent.withValues(alpha: 0.18)),
            ),
            child: const Icon(Icons.school_rounded, color: _accent),
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Text(
              "MyCampus",
              overflow: TextOverflow.ellipsis,
              style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
            ),
          ),
          const SizedBox(width: 8),
          Text(
            titre,
            style: const TextStyle(
              fontWeight: FontWeight.w900,
              fontSize: 14,
              color: Color(0xFF61708A),
            ),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 160),
                child: Text(
                  nomAffiche,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w800),
                ),
              ),
              const SizedBox(height: 4),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 5,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFFEEF2FF),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  role.toUpperCase(),
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w900,
                    color: _accent,
                    letterSpacing: 0.6,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(width: 12),
          BoutonProfilActionSheetIOS(initiales: initiales, imageUrl: url),
        ],
      ),
    );
  }
}

class BoutonProfilActionSheetIOS extends StatelessWidget {
  const BoutonProfilActionSheetIOS({
    super.key,
    required this.initiales,
    required this.imageUrl,
  });

  final String initiales;
  final String? imageUrl;

  static const Color _accent = Color(0xFF5B5BEA);

  @override
  Widget build(BuildContext context) {
    return CupertinoButton(
      padding: EdgeInsets.zero,
      minSize: 36,
      onPressed: () => _ouvrirMenu(context),
      child: CircleAvatar(
        radius: 20,
        backgroundColor: _accent,
        backgroundImage: imageUrl != null ? NetworkImage(imageUrl!) : null,
        child:
            imageUrl == null
                ? Text(
                  initiales,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w900,
                  ),
                )
                : null,
      ),
    );
  }

  void _ouvrirMenu(BuildContext context) {
    showCupertinoModalPopup<void>(
      context: context,
      builder:
          (ctx) => CupertinoActionSheet(
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
                // ✅ No BuildContext across gap issue: capture controller before await
                final auth = context.read<ControleurAuthentification>();
                await auth.seDeconnecter();
              },
              child: const Text("Déconnexion"),
            ),
          ),
    );
  }
}

/* ===================== Helpers communs ===================== */

String initialesPrenomNom(String prenom, String nom) {
  final p = prenom.trim();
  final n = nom.trim();

  String firstLetter(String s) =>
      s.isEmpty ? '' : s.substring(0, 1).toUpperCase();
  final res = (firstLetter(p) + firstLetter(n)).trim();
  return res.isEmpty ? "U" : res;
}

String? construireUrlPhoto(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return "${ConfigurationApp.apiBaseUrl}$photoUrl";
}
*/
