import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_etudiant.dart';
import '../data/dto/annonce_etudiant_dto.dart';
import 'page_detail_annonce_etudiant.dart';

class PageListeAnnoncesEtudiant extends StatefulWidget {
  const PageListeAnnoncesEtudiant({super.key});

  @override
  State<PageListeAnnoncesEtudiant> createState() =>
      _PageListeAnnoncesEtudiantState();
}

class _PageListeAnnoncesEtudiantState extends State<PageListeAnnoncesEtudiant> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurAnnonceEtudiant>().chargerAnnonces();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceEtudiant>();

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Annonces',
          style: TextStyle(
            color: Color(0xFF0B1220),
            fontWeight: FontWeight.w900,
          ),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: Builder(
        builder: (_) {
          if (ctrl.chargement && ctrl.annonces.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (ctrl.erreur != null && ctrl.annonces.isEmpty) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Text(
                  ctrl.erreur!,
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }

          if (ctrl.annonces.isEmpty) {
            return const Center(
              child: Text('Aucune annonce disponible.'),
            );
          }

          return RefreshIndicator(
            onRefresh: () => ctrl.chargerAnnonces(),
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: ctrl.annonces.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (_, i) {
                final annonce = ctrl.annonces[i];
                return _AnnonceListTile(
                  annonce: annonce,
                  onTap: () async {
                    await context
                        .read<ControleurAnnonceEtudiant>()
                        .chargerAnnonceParId(annonce.id);

                    if (!context.mounted) return;

                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (_) => PageDetailAnnonceEtudiant(
                              annonceId: annonce.id,
                            ),
                      ),
                    );
                  },
                );
              },
            ),
          );
        },
      ),
    );
  }
}

class _AnnonceListTile extends StatelessWidget {
  const _AnnonceListTile({
    required this.annonce,
    required this.onTap,
  });

  final AnnonceEtudiantDTO annonce;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final bg = annonce.estLue ? Colors.white : const Color(0xFFEEF2FF);

    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: annonce.estLue
                ? Colors.black.withOpacity(0.05)
                : const Color(0xFF5B5BEA).withOpacity(0.18),
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 11,
              height: 11,
              decoration: BoxDecoration(
                color: annonce.estLue
                    ? const Color(0xFFCBD5E1)
                    : const Color(0xFFEF4444),
                shape: BoxShape.circle,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    annonce.titre,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF0B1220),
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    annonce.datePublication,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: Color(0xFF61708A),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            Icon(
              Icons.chevron_right_rounded,
              color: Colors.black.withOpacity(0.35),
            ),
          ],
        ),
      ),
    );
  }
}
