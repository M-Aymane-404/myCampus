import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/dto/annonce_professeur_dto.dart';
import 'page_detail_annonce_professeur.dart';
import 'widgets/annonce_list_tile.dart';

class PageListeAnnoncesProfesseur extends StatefulWidget {
  const PageListeAnnoncesProfesseur({super.key});

  @override
  State<PageListeAnnoncesProfesseur> createState() =>
      _PageListeAnnoncesProfesseurState();
}

class _PageListeAnnoncesProfesseurState
    extends State<PageListeAnnoncesProfesseur> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurAnnonceProfesseur>().chargerAnnonces();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceProfesseur>();

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
                return AnnonceListTileProfesseur(
                  annonce: annonce,
                  onTap: () async {
                    await context
                        .read<ControleurAnnonceProfesseur>()
                        .chargerAnnonceParId(annonce.id);

                    if (!context.mounted) return;

                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => PageDetailAnnonceProfesseur(
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
