import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'page_detail_annonce_professeur.dart';
import 'page_mes_annonces_professeur.dart';
import 'page_notifications_professeur.dart';
import 'widgets/annonce_list_tile.dart';

class PageGestionAnnoncesProfesseur extends StatefulWidget {
  const PageGestionAnnoncesProfesseur({super.key});

  @override
  State<PageGestionAnnoncesProfesseur> createState() =>
      _PageGestionAnnoncesProfesseurState();
}

class _PageGestionAnnoncesProfesseurState
    extends State<PageGestionAnnoncesProfesseur> {
  static const _typeOptions = ['EXAMEN', 'EVENT', 'REUNION', 'COURS', 'AUTRES'];

  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _titleCtrl = TextEditingController();
  final TextEditingController _contentCtrl = TextEditingController();
  bool _envoiEnCours = false;
  String _selectedType = _typeOptions.first;
  String? _selectedClasse;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _contentCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAnnonceProfesseur>();
    final ctrlEspace = context.watch<ControleurEspaceProfesseur>();
    final classes = _buildClassOptions(ctrlEspace.enseignements);
    if (_selectedClasse == null && classes.isNotEmpty) {
      _selectedClasse = classes.first;
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        title: const Text('Annonces'),
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
        titleTextStyle: const TextStyle(
          color: Color(0xFF0B1220),
          fontWeight: FontWeight.w900,
          fontSize: 18,
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_active_outlined),
            onPressed: () async {
              await Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const PageNotificationsProfesseur(),
                ),
              );
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ctrl.chargerAnnonces(),
        child: CustomScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(18, 18, 18, 12),
                child: _buildCreationForm(
                  context,
                  ctrl,
                  classes,
                ),
              ),
            ),
            if (classes.isEmpty)
              const SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 18, vertical: 12),
                  child: Text(
                    'Vous n’êtes affecté à aucune classe. Veuillez contacter l’administration pour créer des annonces.',
                    style: TextStyle(color: Color(0xFF667085)),
                  ),
                ),
              ),
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(18, 6, 18, 24),
              sliver: ctrl.chargement && ctrl.annonces.isEmpty
                  ? const SliverToBoxAdapter(
                      child: Center(child: CircularProgressIndicator()),
                    )
                  : ctrl.annonces.isEmpty
                      ? const SliverToBoxAdapter(
                          child: Padding(
                            padding: EdgeInsets.symmetric(vertical: 32),
                            child: Center(
                              child: Text(
                                'Aucune annonce disponible pour le moment.',
                                style: TextStyle(color: Color(0xFF667085)),
                              ),
                            ),
                          ),
                        )
                      : SliverList(
                          delegate: SliverChildBuilderDelegate(
                            (context, index) {
                              final annonce = ctrl.annonces[index];
                              return Padding(
                                padding: const EdgeInsets.only(bottom: 12),
                                child: AnnonceListTileProfesseur(
                                  annonce: annonce,
                                  onTap: () async {
                                    await context
                                        .read<ControleurAnnonceProfesseur>()
                                        .chargerAnnonceParId(annonce.id);

                                    if (!context.mounted) return;
                                    await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) =>
                                            PageDetailAnnonceProfesseur(
                                          annonceId: annonce.id,
                                        ),
                                      ),
                                    );
                                  },
                                ),
                              );
                            },
                            childCount: ctrl.annonces.length,
                          ),
                        ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCreationForm(
    BuildContext context,
    ControleurAnnonceProfesseur ctrl,
    List<String> classes,
  ) {
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Créer une annonce',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 12),
            Form(
              key: _formKey,
              child: Column(
                children: [
                  TextFormField(
                    controller: _titleCtrl,
                    decoration: const InputDecoration(
                      labelText: 'Titre',
                      border: OutlineInputBorder(),
                    ),
                    maxLength: 200,
                    validator: (value) => (value ?? '').trim().isEmpty
                        ? 'Titre requis'
                        : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _contentCtrl,
                    decoration: const InputDecoration(
                      labelText: 'Contenu',
                      border: OutlineInputBorder(),
                    ),
                    maxLength: 2000,
                    minLines: 3,
                    maxLines: 5,
                    validator: (value) => (value ?? '').trim().isEmpty
                        ? 'Contenu requis'
                        : null,
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: _selectedType,
                          decoration: const InputDecoration(
                            labelText: 'Type',
                            border: OutlineInputBorder(),
                          ),
                          items: _typeOptions
                              .map(
                                (type) => DropdownMenuItem(
                                  value: type,
                                  child: Text(
                                    type == 'AUTRES'
                                        ? 'Autres'
                                        : type[0] + type.substring(1).toLowerCase(),
                                  ),
                                ),
                              )
                              .toList(),
                          onChanged: (value) {
                            if (value != null) {
                              setState(() => _selectedType = value);
                            }
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: _selectedClasse,
                          decoration: const InputDecoration(
                            labelText: 'Classe',
                            border: OutlineInputBorder(),
                          ),
                          items: classes
                              .map(
                                (code) => DropdownMenuItem(
                                  value: code,
                                  child: Text(code),
                                ),
                              )
                              .toList(),
                          onChanged: classes.isEmpty
                              ? null
                              : (value) {
                                  if (value != null) {
                                    setState(() => _selectedClasse = value);
                                  }
                                },
                          validator: (_) => classes.isEmpty
                              ? 'Pas de classe disponible'
                              : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton(
                          onPressed: classes.isEmpty || _envoiEnCours
                              ? null
                              : () => _soumettre(ctrl),
                          child: _envoiEnCours
                              ? const SizedBox(
                                  height: 16,
                                  width: 16,
                                  child: CircularProgressIndicator(strokeWidth: 2),
                                )
                              : const Text('Envoyer'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      TextButton(
                        onPressed: () async {
                          final ctrlAnnonce =
                              context.read<ControleurAnnonceProfesseur>();
                          if (!ctrlAnnonce.chargement &&
                              ctrlAnnonce.annoncesPubliques.isEmpty) {
                            await ctrlAnnonce.chargerAnnoncesPubliques();
                          }
                          if (!context.mounted) return;
                          await Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => const PageMesAnnoncesProfesseur(),
                            ),
                          );
                        },
                        child: const Text('Voir mes annonces'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<String> _buildClassOptions(List<EnseignementProfesseurMobileDTO> enseignements) {
    final classes = enseignements
        .map((item) => item.codeClasse.trim())
        .where((code) => code.isNotEmpty)
        .toSet()
        .toList();
    classes.sort();
    return classes;
  }

  Future<void> _soumettre(ControleurAnnonceProfesseur ctrl) async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedClasse == null) return;
    final titre = _titleCtrl.text.trim();
    final contenu = _contentCtrl.text.trim();

    setState(() => _envoiEnCours = true);
    try {
      await ctrl.creerAnnonce(
        titre: titre,
        contenu: contenu,
        typeAnnonce: _selectedType,
        codeClasse: _selectedClasse!,
      );
      if (!mounted) return;
      _titleCtrl.clear();
      _contentCtrl.clear();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Annonce envoyée.'),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Erreur: ${e.toString()}'),
          backgroundColor: const Color(0xFFE11D48),
        ),
      );
    } finally {
      if (mounted) setState(() => _envoiEnCours = false);
    }
  }
}
