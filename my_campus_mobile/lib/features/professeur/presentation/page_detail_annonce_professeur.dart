import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../etudiant/data/controller/ControleurFichier.dart';
import '../data/controller/controleur_annonce_professeur.dart';
import '../data/dto/ressource_annonce_professeur_dto.dart';

class PageDetailAnnonceProfesseur extends StatefulWidget {
  const PageDetailAnnonceProfesseur({
    super.key,
    required this.annonceId,
  });

  final int annonceId;

  @override
  State<PageDetailAnnonceProfesseur> createState() =>
      _PageDetailAnnonceProfesseurState();
}

class _PageDetailAnnonceProfesseurState
    extends State<PageDetailAnnonceProfesseur> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final ctrl = context.read<ControleurAnnonceProfesseur>();
      ctrl.chargerAnnonceParId(widget.annonceId).then((_) {
        if (!mounted) return;
        ctrl.chargerRessources(widget.annonceId);
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceProfesseur>();
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
                    if ((annonce.codeClasse ?? '').trim().isNotEmpty) ...[
                      const SizedBox(height: 10),
                      Text(
                        'Classe : ${annonce.codeClasse}',
                        style: const TextStyle(
                          color: Color(0xFF61708A),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
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
              const SizedBox(height: 20),
              _buildRessourcesSection(context, ctrl, widget.annonceId),
            ],
          );
        },
      ),
    );
  }

  Widget _buildRessourcesSection(
    BuildContext context,
    ControleurAnnonceProfesseur ctrl,
    int annonceId,
  ) {
    final ressources = ctrl.ressourcesAnnonceSelectionnee;

    if (!ctrl.ressourcesChargees) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 12),
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (ctrl.erreurRessources != null) {
      final raw = ctrl.erreurRessources ?? '';
      final message =
          raw.contains('Vous ne pouvez')
              ? 'Les ressources ne sont visibles que pour les annonces que vous avez créées.'
              : 'Impossible de charger les ressources pour le moment.';

      return Container(
        margin: const EdgeInsets.only(top: 8),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: Colors.black.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            const Icon(Icons.info_outline_rounded, color: Color(0xFF5B5BEA)),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                message,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 14.5,
                  color: Color(0xFF61708A),
                ),
              ),
            ),
            const SizedBox(width: 10),
            OutlinedButton(
              onPressed: () => ctrl.chargerRessources(annonceId),
              child: const Text('Réessayer'),
            ),
          ],
        ),
      );
    }

    if (ressources.isEmpty) {
      return Container(
        margin: const EdgeInsets.only(top: 8),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: Colors.black.withOpacity(0.05)),
        ),
        child: Row(
          children: const [
            Icon(Icons.folder_open_rounded, color: Color(0xFF5B5BEA)),
            SizedBox(width: 12),
            Expanded(
              child: Text(
                'Aucune ressource attachée',
                style: TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 15,
                  color: Color(0xFF61708A),
                ),
              ),
            ),
          ],
        ),
      );
    }

    return Container(
      margin: const EdgeInsets.only(top: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.only(top: 16, left: 16, right: 16),
            child: Text(
              'Ressources jointes',
              style: TextStyle(
                fontWeight: FontWeight.w900,
                fontSize: 16,
                color: Color(0xFF0B1220),
              ),
            ),
          ),
          const SizedBox(height: 8),
          for (var i = 0; i < ressources.length; i++) ...[
            _buildRessourceRow(context, ressources[i]),
            if (i < ressources.length - 1) const Divider(height: 1),
          ],
        ],
      ),
    );
  }

  Widget _buildRessourceRow(
    BuildContext context,
    RessourceAnnonceProfesseurDTO ressource,
  ) {
    final fichierCtrl = context.watch<ControleurFichier>();
    final tailleFormatee = _formatTaille(ressource.taille);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        leading: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: const Color(0xFFEEF2FF),
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Icon(
            Icons.picture_as_pdf_rounded,
            color: Color(0xFF5B5BEA),
          ),
        ),
        title: Text(
          ressource.nomOriginal,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
            fontWeight: FontWeight.w900,
            color: Color(0xFF0B1220),
          ),
        ),
        subtitle: Text(
          tailleFormatee,
          style: const TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.w700,
            color: Color(0xFF61708A),
          ),
        ),
        trailing:
            fichierCtrl.telechargementEnCours
                ? SizedBox(
                  width: 34,
                  height: 34,
                  child: CircularProgressIndicator(
                    strokeWidth: 2.4,
                    value:
                        fichierCtrl.progression > 0
                            ? fichierCtrl.progression
                            : null,
                    color: const Color(0xFF5B5BEA),
                    backgroundColor: Colors.black.withOpacity(0.08),
                  ),
                )
                : Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(
                        Icons.open_in_new_rounded,
                        color: Color(0xFF5B5BEA),
                      ),
                      onPressed: () => _telechargerEtOuvrir(context, ressource),
                    ),
                    IconButton(
                      icon: const Icon(
                        Icons.download_rounded,
                        color: Color(0xFF5B5BEA),
                      ),
                      onPressed: () => _telechargerEtOuvrir(context, ressource),
                    ),
                  ],
                ),
      ),
    );
  }

  Future<void> _telechargerEtOuvrir(
    BuildContext context,
    RessourceAnnonceProfesseurDTO ressource,
  ) async {
    try {
      await context.read<ControleurFichier>().telechargerEtOuvrir(
        urlDownload: '/api/professeur/annonces/ressources/${ressource.id}/telecharger',
        nomFichier: ressource.nomOriginal,
      );
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Impossible d'ouvrir ${ressource.nomOriginal}")),
      );
    }
  }

  String _formatTaille(int? bytes) {
    if (bytes == null || bytes <= 0) return 'Taille inconnue';
    const units = ['o', 'Ko', 'Mo', 'Go'];
    var valeur = bytes.toDouble();
    var index = 0;
    while (valeur >= 1024 && index < units.length - 1) {
      valeur /= 1024;
      index++;
    }
    final precision = index == 0 ? 0 : 1;
    return '${valeur.toStringAsFixed(precision)} ${units[index]}';
  }
}
