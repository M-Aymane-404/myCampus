import 'package:file_picker/file_picker.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/annonce_professeur_dto.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'page_detail_annonce_professeur.dart';
import 'page_mes_annonces_professeur.dart';
import 'page_notifications_professeur.dart';
import 'widgets/annonce_list_tile.dart';

const Color _kAccent = Color(0xFF5B5BEA);
const Color _kBg = Color(0xFFF5F7FB);
const Color _kText = Color(0xFF0B1220);
const Color _kMuted = Color(0xFF667085);

class PageGestionAnnoncesProfesseur extends StatefulWidget {
  const PageGestionAnnoncesProfesseur({super.key});

  @override
  State<PageGestionAnnoncesProfesseur> createState() =>
      _PageGestionAnnoncesProfesseurState();
}

class _PageGestionAnnoncesProfesseurState
    extends State<PageGestionAnnoncesProfesseur> {
  static const _typeOptions = ['EXAMEN', 'EVENT', 'REUNION', 'COURS', 'AUTRES'];
  static const _allowedExtensions = ['pdf', 'png', 'jpg', 'jpeg'];

  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _titleCtrl = TextEditingController();
  final TextEditingController _contentCtrl = TextEditingController();
  bool _envoiEnCours = false;
  bool _chargementInitial = false;
  String _selectedType = _typeOptions.first;
  String? _selectedClasse;
  List<PlatformFile> _piecesJointes = const [];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_chargementInitial) return;
    _chargementInitial = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurAnnonceProfesseur>().chargerAnnonces();
    });
  }

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
    _selectedClasse ??= classes.isNotEmpty ? classes.first : null;

    return Scaffold(
      backgroundColor: _kBg,
      appBar: AppBar(
        title: const Text('Annonces'),
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: _kText),
        titleTextStyle: const TextStyle(
          color: _kText,
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
                child: _buildCreationArea(
                  context,
                  ctrl,
                  classes: classes,
                ),
              ),
            ),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(18, 10, 18, 10),
                child: _buildRecentHeader(ctrl.annonces.length),
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
                            padding: EdgeInsets.symmetric(vertical: 28),
                            child: Center(
                              child: Text(
                                'Aucune annonce disponible pour le moment.',
                                style: TextStyle(color: _kMuted),
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
                                  actionsEnCours: ctrl.actionEnCoursPour(
                                    annonce.id,
                                  ),
                                  onTap: () async {
                                    await context
                                        .read<ControleurAnnonceProfesseur>()
                                        .chargerAnnonceParId(annonce.id);

                                    if (!context.mounted) return;
                                    await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) => PageDetailAnnonceProfesseur(
                                          annonceId: annonce.id,
                                        ),
                                      ),
                                    );
                                  },
                                  onSupprimer: () => _supprimerAnnonce(annonce),
                                  onBasculerPublication: () =>
                                      _basculerPublication(annonce),
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

  Widget _buildCreationArea(
    BuildContext context,
    ControleurAnnonceProfesseur ctrl, {
    required List<String> classes,
  }) {
    final disabled = classes.isEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildHeaderBanner(
          disabled: disabled,
          selectedClasse: _selectedClasse,
          selectedType: _selectedType,
        ),
        const SizedBox(height: 14),
        Form(
          key: _formKey,
          child: Column(
            children: [
              _buildSectionCard(
                title: 'Annonce',
                subtitle:
                    'Rédigez le titre, le message et choisissez une catégorie.',
                icon: Icons.edit_note_rounded,
                child: Column(
                  children: [
                    TextFormField(
                      controller: _titleCtrl,
                      decoration: _fieldDecoration(
                        label: 'Titre',
                        hint: 'Ex: Contrôle continu, changement de salle…',
                        icon: Icons.title_rounded,
                      ),
                      maxLength: 200,
                      validator:
                          (value) =>
                              (value ?? '').trim().isEmpty
                                  ? 'Titre requis'
                                  : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _contentCtrl,
                      decoration: _fieldDecoration(
                        label: 'Contenu',
                        hint: 'Écrivez votre annonce…',
                        icon: Icons.notes_rounded,
                      ),
                      maxLength: 2000,
                      minLines: 4,
                      maxLines: 7,
                      validator:
                          (value) =>
                              (value ?? '').trim().isEmpty
                                  ? 'Contenu requis'
                                  : null,
                    ),
                    const SizedBox(height: 10),
                    _buildTypeChips(disabled: disabled || _envoiEnCours),
                  ],
                ),
              ),
              const SizedBox(height: 14),
              _buildSectionCard(
                title: 'Audience / Ciblage',
                subtitle: 'Sélectionnez la classe qui recevra cette annonce.',
                icon: Icons.groups_rounded,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    DropdownButtonFormField<String>(
                      value: _selectedClasse,
                      decoration: _fieldDecoration(
                        label: 'Classe ciblée',
                        hint: 'Choisir une classe',
                        icon: Icons.school_rounded,
                      ),
                      items:
                          classes
                              .map(
                                (code) => DropdownMenuItem(
                                  value: code,
                                  child: Text(code),
                                ),
                              )
                              .toList(),
                      onChanged:
                          disabled || _envoiEnCours
                              ? null
                              : (value) => setState(
                                () => _selectedClasse = value,
                              ),
                      validator:
                          (_) => disabled ? 'Pas de classe disponible' : null,
                    ),
                    const SizedBox(height: 12),
                    _buildAudienceSummary(
                      disabled: disabled,
                      selectedClasse: _selectedClasse,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 14),
              _buildSectionCard(
                title: 'Ressources jointes',
                subtitle: 'Ajoutez des documents (optionnel) comme sur le web.',
                icon: Icons.attach_file_rounded,
                child: _buildAttachmentsBlock(disabled: _envoiEnCours),
              ),
              const SizedBox(height: 14),
              _buildActionButtons(
                disabled: disabled || _envoiEnCours,
                isSending: _envoiEnCours,
                onSend: () => _soumettre(ctrl),
                onViewMine: () => _ouvrirMesAnnonces(context),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Future<void> _basculerPublication(AnnonceProfesseurDTO annonce) async {
    final ctrl = context.read<ControleurAnnonceProfesseur>();
    final wasPublished = annonce.publiee;

    try {
      await ctrl.basculerPublication(annonce.id);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            wasPublished ? 'Annonce depubliee.' : 'Annonce publiee.',
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_messageErreur(e))),
      );
    }
  }

  Future<void> _supprimerAnnonce(AnnonceProfesseurDTO annonce) async {
    final confirmation = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text("Supprimer l'annonce ?"),
        content: Text(
          'Voulez-vous vraiment supprimer "${annonce.titre}" ?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (!mounted) return;
    if (confirmation != true) return;

    try {
      await context.read<ControleurAnnonceProfesseur>().supprimerAnnonce(
            annonce.id,
          );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Annonce supprimee.')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_messageErreur(e))),
      );
    }
  }

  String _messageErreur(Object e) {
    if (e is DioException) {
      final code = e.response?.statusCode;
      final data = e.response?.data;
      if (data is Map) {
        final message = data['message'] ?? data['error'] ?? data['detail'];
        if (message != null && message.toString().trim().isNotEmpty) {
          return message.toString().trim();
        }
      }
      if (data is String && data.trim().isNotEmpty && data.length < 180) {
        return data.trim();
      }
      if (code == 404) return 'Endpoint introuvable (404).';
      if (code == 405) return 'Methode non supportee (405).';
      if (code == 401) return 'Non authentifie (401).';
      if (code == 403) return 'Acces refuse (403).';
      if (code == 409) return 'Conflit (409).';
      if (code != null) return 'Erreur serveur ($code).';

      if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.receiveTimeout ||
          e.type == DioExceptionType.sendTimeout) {
        return 'Delai depasse. Verifiez la connexion.';
      }
      if (e.type == DioExceptionType.connectionError) {
        return 'Erreur reseau. Verifiez la connexion.';
      }

      return 'Erreur reseau.';
    }

    final msg = e.toString().trim();
    if (msg.isNotEmpty) return msg;
    return 'Action impossible pour le moment.';
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

  Widget _buildHeaderBanner({
    required bool disabled,
    required String? selectedClasse,
    required String selectedType,
  }) {
    final typeLabel =
        selectedType == 'AUTRES'
            ? 'Autres'
            : selectedType[0] + selectedType.substring(1).toLowerCase();

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(26),
        gradient: LinearGradient(
          colors: [_kAccent.withOpacity(0.14), Colors.white],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        border: Border.all(color: _kAccent.withOpacity(0.16)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 18,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: _kAccent,
                  borderRadius: BorderRadius.circular(18),
                  boxShadow: [
                    BoxShadow(
                      color: _kAccent.withOpacity(0.28),
                      blurRadius: 14,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.campaign_rounded,
                  color: Colors.white,
                  size: 24,
                ),
              ),
              const SizedBox(width: 12),
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Créer et envoyer',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
                        color: _kMuted,
                      ),
                    ),
                    SizedBox(height: 2),
                    Text(
                      'Une annonce',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w900,
                        color: _kText,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            disabled
                ? 'Vous n’êtes affecté à aucune classe. L’envoi est désactivé.'
                : 'Créez une annonce structurée (titre, contenu, audience, ressources) et envoyez-la à votre classe.',
            style: const TextStyle(
              fontSize: 13,
              height: 1.35,
              fontWeight: FontWeight.w600,
              color: _kMuted,
            ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _MiniChip(icon: Icons.category_rounded, label: typeLabel),
              _MiniChip(
                icon: Icons.school_rounded,
                label: selectedClasse ?? 'Aucune classe',
                dimmed: disabled,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSectionCard({
    required String title,
    required String subtitle,
    required IconData icon,
    required Widget child,
  }) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: _kAccent.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: _kAccent.withOpacity(0.18)),
                ),
                child: Icon(icon, color: _kAccent),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w900,
                        color: _kText,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: const TextStyle(
                        fontSize: 12.5,
                        fontWeight: FontWeight.w600,
                        color: _kMuted,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          child,
        ],
      ),
    );
  }

  Widget _buildTypeChips({required bool disabled}) {
    String label(String type) {
      if (type == 'AUTRES') return 'Autres';
      return type[0] + type.substring(1).toLowerCase();
    }

    return Align(
      alignment: Alignment.centerLeft,
      child: Wrap(
        spacing: 8,
        runSpacing: 8,
        children: [
          for (final option in _typeOptions)
            ChoiceChip(
              label: Text(label(option)),
              selected: option == _selectedType,
              onSelected:
                  disabled ? null : (_) => setState(() => _selectedType = option),
              selectedColor: const Color(0xFFEEF2FF),
              backgroundColor: const Color(0xFFF7F9FF),
              side: BorderSide(
                color:
                    option == _selectedType
                        ? _kAccent.withOpacity(0.22)
                        : Colors.black.withOpacity(0.08),
              ),
              labelStyle: TextStyle(
                fontWeight: FontWeight.w800,
                color: option == _selectedType ? _kAccent : _kText,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildAudienceSummary({
    required bool disabled,
    required String? selectedClasse,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: disabled ? const Color(0xFFFFF7ED) : const Color(0xFFEEF2FF),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(
          color:
              disabled
                  ? const Color(0xFFF59E0B).withOpacity(0.18)
                  : _kAccent.withOpacity(0.18),
        ),
      ),
      child: Row(
        children: [
          Icon(
            disabled ? Icons.info_outline_rounded : Icons.check_circle_rounded,
            color: disabled ? const Color(0xFFF59E0B) : _kAccent,
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              disabled
                  ? 'Aucune audience disponible pour le moment.'
                  : 'Cette annonce sera envoyée à : ${selectedClasse ?? '-'}',
              style: const TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w800,
                color: _kText,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAttachmentsBlock({required bool disabled}) {
    final hasFiles = _piecesJointes.isNotEmpty;
    final formats = _allowedExtensions.map((e) => e.toUpperCase()).join(', ');

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: Text(
                hasFiles ? 'Fichiers sélectionnés' : 'Aucun fichier pour l’instant',
                style: const TextStyle(
                  fontWeight: FontWeight.w900,
                  color: _kText,
                ),
              ),
            ),
            OutlinedButton.icon(
              onPressed: disabled ? null : _choisirRessources,
              icon: const Icon(Icons.add_rounded),
              label: Text(hasFiles ? 'Ajouter' : 'Joindre'),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Text(
          'Formats autorisés : $formats',
          style: const TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.w600,
            color: _kMuted,
          ),
        ),
        const SizedBox(height: 12),
        if (!hasFiles)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xFFF7F9FF),
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: Colors.black.withOpacity(0.06)),
            ),
            child: const Text(
              'Déposez un fichier ou utilisez « Joindre ».',
              style: TextStyle(
                fontWeight: FontWeight.w700,
                color: _kMuted,
              ),
            ),
          )
        else
          Column(
            children: [
              for (final file in _piecesJointes) ...[
                _AttachmentTile(
                  name: file.name,
                  sizeLabel: _formatBytes(file.size),
                  onRemove: disabled ? null : () => _removeFile(file),
                ),
                const SizedBox(height: 8),
              ],
            ],
          ),
      ],
    );
  }

  Widget _buildActionButtons({
    required bool disabled,
    required bool isSending,
    required VoidCallback onSend,
    required VoidCallback onViewMine,
  }) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
      ),
      child: Column(
        children: [
          SizedBox(
            width: double.infinity,
            height: 48,
            child: ElevatedButton.icon(
              onPressed: disabled ? null : onSend,
              icon:
                  isSending
                      ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                      : const Icon(Icons.send_rounded),
              label: Text(isSending ? 'Envoi…' : 'Envoyer'),
              style: ElevatedButton.styleFrom(
                backgroundColor: _kAccent,
                foregroundColor: Colors.white,
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
                textStyle: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
          ),
          const SizedBox(height: 10),
          SizedBox(
            width: double.infinity,
            height: 46,
            child: OutlinedButton.icon(
              onPressed: onViewMine,
              icon: const Icon(Icons.view_list_rounded),
              label: const Text('Voir mes annonces'),
              style: OutlinedButton.styleFrom(
                foregroundColor: _kAccent,
                side: BorderSide(color: _kAccent.withOpacity(0.32)),
                backgroundColor: _kAccent.withOpacity(0.06),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
                textStyle: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRecentHeader(int count) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Annonces récentes',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w900,
                  color: _kText,
                ),
              ),
              SizedBox(height: 4),
              Text(
                'Historique des derniers envois.',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: _kMuted,
                ),
              ),
            ],
          ),
        ),
        if (count > 0) ...[
          const SizedBox(width: 12),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: _kAccent.withOpacity(0.10),
              borderRadius: BorderRadius.circular(999),
              border: Border.all(color: _kAccent.withOpacity(0.16)),
            ),
            child: Text(
              count.toString(),
              style: const TextStyle(
                fontWeight: FontWeight.w900,
                color: _kAccent,
              ),
            ),
          ),
        ],
      ],
    );
  }

  InputDecoration _fieldDecoration({
    required String label,
    String? hint,
    IconData? icon,
  }) {
    return InputDecoration(
      labelText: label,
      hintText: hint,
      prefixIcon: icon == null ? null : Icon(icon),
      filled: true,
      fillColor: const Color(0xFFF7F9FF),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: Colors.black.withOpacity(0.06)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: _kAccent.withOpacity(0.9), width: 1.2),
      ),
    );
  }

  Future<void> _ouvrirMesAnnonces(BuildContext context) async {
    final ctrlAnnonce = context.read<ControleurAnnonceProfesseur>();
    if (!ctrlAnnonce.chargement && ctrlAnnonce.annonces.isEmpty) {
      await ctrlAnnonce.chargerAnnonces();
    }
    if (!context.mounted) return;
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PageMesAnnoncesProfesseur()),
    );
  }

  Future<void> _choisirRessources() async {
    final result = await FilePicker.platform.pickFiles(
      allowMultiple: true,
      withData: false,
      type: FileType.custom,
      allowedExtensions: _allowedExtensions,
    );

    final picked = result?.files ?? const <PlatformFile>[];
    if (picked.isEmpty) return;

    final merged = <PlatformFile>[..._piecesJointes];
    for (final file in picked) {
      final path = (file.path ?? '').trim();
      final exists = merged.any((e) {
        final otherPath = (e.path ?? '').trim();
        if (path.isNotEmpty && otherPath.isNotEmpty) return otherPath == path;
        return e.name == file.name && e.size == file.size;
      });
      if (!exists) merged.add(file);
    }

    setState(() => _piecesJointes = merged);
  }

  void _removeFile(PlatformFile file) {
    setState(
      () => _piecesJointes = _piecesJointes.where((f) => f != file).toList(),
    );
  }

  String _formatBytes(int bytes) {
    if (bytes <= 0) return '—';
    const units = ['o', 'Ko', 'Mo', 'Go'];
    var value = bytes.toDouble();
    var idx = 0;
    while (value >= 1024 && idx < units.length - 1) {
      value /= 1024;
      idx++;
    }
    final precision = idx == 0 ? 0 : 1;
    return '${value.toStringAsFixed(precision)} ${units[idx]}';
  }

  Future<void> _soumettre(ControleurAnnonceProfesseur ctrl) async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedClasse == null) return;
    final titre = _titleCtrl.text.trim();
    final contenu = _contentCtrl.text.trim();
    final hadFiles = _piecesJointes.isNotEmpty;

    setState(() => _envoiEnCours = true);
    try {
      final annonce = await ctrl.creerAnnonce(
        titre: titre,
        contenu: contenu,
        typeAnnonce: _selectedType,
        codeClasse: _selectedClasse!,
      );

      if (_piecesJointes.isNotEmpty) {
        await ctrl.ajouterRessourcesAnnonce(
          annonceId: annonce.id,
          fichiers: _piecesJointes,
        );
      }

      if (!mounted) return;
      _titleCtrl.clear();
      _contentCtrl.clear();
      setState(() => _piecesJointes = const []);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            hadFiles ? 'Annonce envoyée avec ressources.' : 'Annonce envoyée.',
          ),
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

class _MiniChip extends StatelessWidget {
  const _MiniChip({
    required this.icon,
    required this.label,
    this.dimmed = false,
  });

  final IconData icon;
  final String label;
  final bool dimmed;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(dimmed ? 0.72 : 0.95),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: const Color(0xFF344054)),
          const SizedBox(width: 8),
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w800,
              color: _kText,
            ),
          ),
        ],
      ),
    );
  }
}

class _AttachmentTile extends StatelessWidget {
  const _AttachmentTile({
    required this.name,
    required this.sizeLabel,
    required this.onRemove,
  });

  final String name;
  final String sizeLabel;
  final VoidCallback? onRemove;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F9FF),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
      ),
      child: Row(
        children: [
          const Icon(Icons.insert_drive_file_rounded, color: _kAccent),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontWeight: FontWeight.w800,
                    color: _kText,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  sizeLabel,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                    color: _kMuted,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 10),
          IconButton(
            onPressed: onRemove,
            icon: const Icon(Icons.close_rounded),
            color: const Color(0xFF94A3B8),
            tooltip: 'Retirer',
          ),
        ],
      ),
    );
  }
}
