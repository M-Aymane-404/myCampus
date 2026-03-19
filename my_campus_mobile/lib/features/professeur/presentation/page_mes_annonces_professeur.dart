import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/dto/annonce_professeur_dto.dart';
import '../presentation/widgets/annonce_preview_card.dart';
import 'page_detail_annonce_professeur.dart';

class PageMesAnnoncesProfesseur extends StatefulWidget {
  const PageMesAnnoncesProfesseur({super.key});

  @override
  State<PageMesAnnoncesProfesseur> createState() =>
      _PageMesAnnoncesProfesseurState();
}

class _PageMesAnnoncesProfesseurState
    extends State<PageMesAnnoncesProfesseur> {
  bool _chargementInitial = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_chargementInitial) return;
    _chargementInitial = true;

    final ctrl = context.read<ControleurAnnonceProfesseur>();
    if (!ctrl.chargement) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      ctrl.chargerAnnoncesPubliques();
    });
  }
  }

  Future<void> _refreshListe() async {
    await context.read<ControleurAnnonceProfesseur>().chargerAnnoncesPubliques();
  }

  Future<void> _ouvrirDetail(AnnonceProfesseurDTO annonce) async {
    final ctrl = context.read<ControleurAnnonceProfesseur>();
    await ctrl.chargerAnnonceParId(annonce.id);
    if (!mounted) return;
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => PageDetailAnnonceProfesseur(annonceId: annonce.id),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceProfesseur>();
    final annonces = ctrl.annoncesPubliques;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Mes annonces',
          style: TextStyle(
            color: Color(0xFF0B1220),
            fontWeight: FontWeight.w900,
          ),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: RefreshIndicator(
      onRefresh: _refreshListe,
        child: ctrl.chargement && annonces.isEmpty
            ? const Center(child: CircularProgressIndicator())
            : annonces.isEmpty
                ? const _EmptyStateAnnonces()
                : ListView.separated(
                    padding: const EdgeInsets.fromLTRB(18, 18, 18, 20),
                    itemCount: annonces.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (_, index) {
                      final annonce = annonces[index];
                      return AnnoncePreviewCard(
                        annonce: annonce,
                        onTap: () => _ouvrirDetail(annonce),
                      );
                    },
                  ),
      ),
    );
  }
}

class _EmptyStateAnnonces extends StatelessWidget {
  const _EmptyStateAnnonces();

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(18, 32, 18, 20),
      children: const [
        Icon(Icons.campaign_outlined, size: 60, color: Color(0xFF5B5BEA)),
        SizedBox(height: 16),
        Text(
          'Vous n’avez pas encore publié d’annonce.',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: Color(0xFF475569),
          ),
        ),
        SizedBox(height: 6),
        Text(
          'Créez une annonce depuis votre tableau de bord pour la voir ici.',
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 14, color: Color(0xFF8A92AB)),
        ),
      ],
    );
  }
}
