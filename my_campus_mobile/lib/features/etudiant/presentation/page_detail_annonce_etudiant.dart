import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_etudiant.dart';

class PageDetailAnnonceEtudiant extends StatefulWidget {
  const PageDetailAnnonceEtudiant({
    super.key,
    required this.annonceId,
  });

  final int annonceId;

  @override
  State<PageDetailAnnonceEtudiant> createState() =>
      _PageDetailAnnonceEtudiantState();
}

class _PageDetailAnnonceEtudiantState extends State<PageDetailAnnonceEtudiant> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context
          .read<ControleurAnnonceEtudiant>()
          .chargerAnnonceParId(widget.annonceId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceEtudiant>();
    final annonce = ctrl.annonceSelectionnee;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Detail annonce',
          style: TextStyle(
            color: Color(0xFF0B1220),
            fontWeight: FontWeight.w900,
          ),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: Builder(
        builder: (_) {
          if (ctrl.chargement && annonce == null) {
            return const Center(child: CircularProgressIndicator());
          }

          if (ctrl.erreur != null && annonce == null) {
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

          if (annonce == null) {
            return const Center(
              child: Text('Annonce introuvable.'),
            );
          }

          return ListView(
            padding: const EdgeInsets.all(18),
            children: [
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: Colors.black.withOpacity(0.05)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (annonce.urgent)
                      Container(
                        margin: const EdgeInsets.only(bottom: 12),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 10,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFFFFF1F2),
                          borderRadius: BorderRadius.circular(999),
                        ),
                        child: const Text(
                          'URGENT',
                          style: TextStyle(
                            color: Color(0xFFEF4444),
                            fontWeight: FontWeight.w900,
                            fontSize: 11,
                            letterSpacing: 0.6,
                          ),
                        ),
                      ),
                    Text(
                      annonce.titre,
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w900,
                        color: Color(0xFF0B1220),
                      ),
                    ),
                    const SizedBox(height: 10),
                    Text(
                      annonce.datePublication,
                      style: const TextStyle(
                        color: Color(0xFF61708A),
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 10),
                    if (annonce.auteur != null)
                      Text(
                        'Auteur : ${annonce.auteur!.nomComplet}',
                        style: const TextStyle(
                          color: Color(0xFF61708A),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    const SizedBox(height: 18),
                    Text(
                      annonce.contenu,
                      style: const TextStyle(
                        fontSize: 15.5,
                        height: 1.6,
                        color: Color(0xFF0B1220),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
