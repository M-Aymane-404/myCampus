import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'cours/widgets.dart';

class PageCoursProfesseur extends StatefulWidget {
  const PageCoursProfesseur({super.key});

  @override
  State<PageCoursProfesseur> createState() => _PageCoursProfesseurState();
}

class _PageCoursProfesseurState extends State<PageCoursProfesseur> {
  String? _selectedEnseignementKey;
  bool _selectionScheduled = false;

  Future<void> _refreshCours(EnseignementProfesseurMobileDTO? enseignement) async {
    final ctrl = context.read<ControleurEspaceProfesseur>();
    try {
      if (enseignement == null) {
        await ctrl.chargerEspace();
        return;
      }

      await Future.wait([
        ctrl.rechercherSeancesPourEnseignement(
          codeClasse: enseignement.codeClasse,
          codeMatiere: enseignement.codeMatiere,
        ),
        ctrl.rechargerExamensSelection(),
      ]);
    } catch (e) {
      if (!mounted) return;
      _showSnack(e.toString(), isError: true);
    }
  }

  List<SeanceProfesseurMobileDTO> _sortedSeances(
    List<SeanceProfesseurMobileDTO> seances,
  ) {
    if (seances.length < 2) return seances;
    final sorted = List<SeanceProfesseurMobileDTO>.from(seances);
    sorted.sort(_compareSeancesNewestFirst);
    return sorted;
  }

  int _compareSeancesNewestFirst(
    SeanceProfesseurMobileDTO a,
    SeanceProfesseurMobileDTO b,
  ) {
    final aKey = _seanceDateTimeKey(a);
    final bKey = _seanceDateTimeKey(b);
    if (aKey != null && bKey != null) {
      final byDate = bKey.compareTo(aKey);
      if (byDate != 0) return byDate;
      final aId = a.id;
      final bId = b.id;
      if (aId != null && bId != null) return bId.compareTo(aId);
      return 0;
    }
    if (aKey != null) return -1;
    if (bKey != null) return 1;
    final aId = a.id;
    final bId = b.id;
    if (aId != null && bId != null) return bId.compareTo(aId);
    return 0;
  }

  DateTime? _seanceDateTimeKey(SeanceProfesseurMobileDTO seance) {
    final date = _parseDateOnly(seance.jour);
    if (date == null) return null;
    final start = _parseTime(seance.heureDebut) ?? Duration.zero;
    return date.add(start);
  }

  DateTime? _parseDateOnly(String raw) {
    final value = raw.trim();
    if (value.isEmpty) return null;

    final embeddedIso = RegExp(r'(\d{4}-\d{2}-\d{2})').firstMatch(value);
    if (embeddedIso != null) {
      final parsed = DateTime.tryParse(embeddedIso.group(1)!);
      if (parsed != null) return DateTime(parsed.year, parsed.month, parsed.day);
    }

    final embeddedSlash = RegExp(r'(\d{2})/(\d{2})/(\d{4})').firstMatch(value);
    if (embeddedSlash != null) {
      final day = int.parse(embeddedSlash.group(1)!);
      final month = int.parse(embeddedSlash.group(2)!);
      final year = int.parse(embeddedSlash.group(3)!);
      return DateTime(year, month, day);
    }

    final direct = DateTime.tryParse(value);
    if (direct != null) return DateTime(direct.year, direct.month, direct.day);

    final dash = RegExp(r'^(\d{2})-(\d{2})-(\d{4})$').firstMatch(value);
    if (dash != null) {
      final day = int.parse(dash.group(1)!);
      final month = int.parse(dash.group(2)!);
      final year = int.parse(dash.group(3)!);
      return DateTime(year, month, day);
    }

    final ymdSlash = RegExp(r'^(\d{4})/(\d{2})/(\d{2})$').firstMatch(value);
    if (ymdSlash != null) {
      final year = int.parse(ymdSlash.group(1)!);
      final month = int.parse(ymdSlash.group(2)!);
      final day = int.parse(ymdSlash.group(3)!);
      return DateTime(year, month, day);
    }

    return null;
  }

  Duration? _parseTime(String raw) {
    final value = raw.trim();
    if (value.isEmpty) return null;
    final match =
        RegExp(r'(\d{1,2}):(\d{2})(?::(\d{2}))?').firstMatch(value);
    if (match == null) return null;
    final hour = int.tryParse(match.group(1) ?? '');
    final minute = int.tryParse(match.group(2) ?? '');
    if (hour == null || minute == null) return null;
    final second = int.tryParse(match.group(3) ?? '') ?? 0;
    if (hour < 0 || hour > 23) return null;
    if (minute < 0 || minute > 59) return null;
    if (second < 0 || second > 59) return null;
    return Duration(hours: hour, minutes: minute, seconds: second);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final ctrl = context.read<ControleurEspaceProfesseur>();
    _scheduleSelectionIfNeeded(ctrl);
  }

  void _scheduleSelectionIfNeeded(ControleurEspaceProfesseur ctrl) {
    final enseignements = ctrl.enseignements;
    if (enseignements.isEmpty) return;
    final hasSelection =
        _selectedEnseignementKey != null &&
        enseignements.any(
          (e) => _enseignementKey(e) == _selectedEnseignementKey,
        );
    if (hasSelection) return;
    if (_selectionScheduled) return;
    _selectionScheduled = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final first = ctrl.enseignements.first;
      _updateSelection(first);
    });
  }

  Future<void> _updateSelection(
    EnseignementProfesseurMobileDTO enseignement,
  ) async {
    final ctrl = context.read<ControleurEspaceProfesseur>();
    final key = _enseignementKey(enseignement);
    setState(() => _selectedEnseignementKey = key);
    await ctrl.selectionnerClasse(enseignement.codeClasse);
    await ctrl.selectionnerMatiere(enseignement.codeMatiere);
    await ctrl.rechercherSeancesPourEnseignement(
      codeClasse: enseignement.codeClasse,
      codeMatiere: enseignement.codeMatiere,
    );
  }

  String _enseignementKey(EnseignementProfesseurMobileDTO enseignement) {
    return '${enseignement.codeClasse}|${enseignement.codeMatiere}|${enseignement.id ?? ''}';
  }

  EnseignementProfesseurMobileDTO? _selectedEnseignement(
    ControleurEspaceProfesseur ctrl,
  ) {
    if (_selectedEnseignementKey != null) {
      for (final enseignement in ctrl.enseignements) {
        if (_enseignementKey(enseignement) == _selectedEnseignementKey) {
          return enseignement;
        }
      }
    }
    if (ctrl.enseignements.isNotEmpty) {
      return ctrl.enseignements.first;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    _scheduleSelectionIfNeeded(ctrl);
    final enseignement = _selectedEnseignement(ctrl);
    final seances = ctrl.seancesSelectionnees;
    final examens = ctrl.examens;
    final seancesLoading = ctrl.chargementSeancesSelection;

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        backgroundColor: const Color(0xFFF5F7FB),
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 18),
                Row(
                  children: [
                    const Expanded(
                      child: Text(
                        'Cours simplifiés',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF0B1220),
                        ),
                      ),
                    ),
                    IconButton(
                      tooltip: 'Rafraîchir',
                      onPressed: () => _refreshCours(enseignement),
                      icon: const Icon(Icons.refresh_rounded),
                      color: const Color(0xFF5B5BEA),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  enseignement == null
                      ? 'Selectionnez un enseignement'
                      : '${enseignement.matiereNom} · ${enseignement.classeNom}',
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF667085),
                  ),
                ),
                const SizedBox(height: 12),
                _buildEnseignementChips(ctrl),
                const SizedBox(height: 16),
                _buildSummaryChips(
                  seances.length,
                  examens.length,
                ),
                const SizedBox(height: 16),
                const Divider(),
                TabBar(
                  padding: EdgeInsets.zero,
                  labelColor: const Color(0xFF5B5BEA),
                  unselectedLabelColor: const Color(0xFF8B95AE),
                  indicatorColor: const Color(0xFF5B5BEA),
                  tabs: const [Tab(text: 'Séances'), Tab(text: 'Examens')],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: RefreshIndicator(
                    onRefresh: () => _refreshCours(enseignement),
                    child: TabBarView(
                      children: [
                        _buildSeancesTab(
                          seances,
                          teaching: enseignement,
                          isLoading: seancesLoading,
                        ),
                        _buildExamensTab(
                          examens,
                          teaching: enseignement,
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildEnseignementChips(ControleurEspaceProfesseur ctrl) {
    final enseignements = ctrl.enseignements;
    if (enseignements.isEmpty) {
      return const SizedBox(
        height: 28,
        child: Center(
          child: Text('Aucun enseignement disponible pour le moment.'),
        ),
      );
    }

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children:
            enseignements.map((enseignement) {
              final key = _enseignementKey(enseignement);
              final selected = key == _selectedEnseignementKey;
              return Padding(
                padding: const EdgeInsets.only(right: 8),
                child: ChoiceChip(
                  label: Text(
                    '${enseignement.matiereNom} • ${enseignement.codeClasse}',
                  ),
                  selected: selected,
                  onSelected: (_) => _updateSelection(enseignement),
                  selectedColor: const Color(0xFFEEF2FF),
                  backgroundColor: Colors.white,
                  labelStyle: TextStyle(
                    fontWeight: FontWeight.w600,
                    color:
                        selected
                            ? const Color(0xFF5B5BEA)
                            : const Color(0xFF0B1220),
                  ),
                ),
              );
            }).toList(),
      ),
    );
  }

  Widget _buildSummaryChips(int seances, int examens) {
    return Wrap(
      spacing: 10,
      runSpacing: 8,
      children: [
        SummaryChip(label: 'Séances', value: seances),
        SummaryChip(label: 'Examens', value: examens),
      ],
    );
  }

  Widget _buildSeancesTab(
    List<SeanceProfesseurMobileDTO> seances, {
    required EnseignementProfesseurMobileDTO? teaching,
    required bool isLoading,
  }) {
    final sortedSeances = _sortedSeances(seances);
    final hasData = sortedSeances.isNotEmpty;
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        ActionRow(
          title: 'Planifier rapidement une séance',
          buttonLabel: 'Créer une séance',
          onTap:
              teaching == null ? null : () => _openCreateSeanceModal(teaching),
        ),
        const SizedBox(height: 12),
        if (isLoading)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 32),
            child: Center(child: CircularProgressIndicator()),
          )
        else if (!hasData)
          const Center(
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 32),
              child: Text('Aucune séance prévue pour cet enseignement.'),
            ),
          )
        else ...[
          for (final seance in sortedSeances) ...[
            SessionCard(
              seance: seance,
              showActions: false,
            ),
          ],
        ],
      ],
    );
  }

  Widget _buildExamensTab(
    List<ExamenProfesseurMobileDTO> examens, {
    required EnseignementProfesseurMobileDTO? teaching,
  }) {
    final hasData = examens.isNotEmpty;
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        ActionRow(
          title: 'Suivez vos examens principaux',
          buttonLabel: 'Créer un examen',
          onTap:
              teaching == null ? null : () => _openCreateExamenModal(teaching),
        ),
        const SizedBox(height: 12),
        if (!hasData)
          const Center(
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 32),
              child: Text(
                'Aucun examen lié à cet enseignement pour le moment.',
              ),
            ),
          )
        else ...[
          for (final examen in examens) ExamCard(examen: examen),
        ],
      ],
    );
  }

  Future<void> _openCreateSeanceModal(
    EnseignementProfesseurMobileDTO enseignement,
  ) async {
    final created = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => CreateSeanceModal(enseignement: enseignement),
    );

    if (created != true || !mounted) return;
    final ctrl = context.read<ControleurEspaceProfesseur>();
    try {
      await ctrl.rechercherSeancesPourEnseignement(
        codeClasse: enseignement.codeClasse,
        codeMatiere: enseignement.codeMatiere,
      );
      _showSnack('Séance créée.');
    } catch (e) {
      _showSnack(e.toString(), isError: true);
    }
  }

  Future<void> _openCreateExamenModal(
    EnseignementProfesseurMobileDTO enseignement,
  ) async {
    final created = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => CreateExamenModal(enseignement: enseignement),
    );

    if (created != true || !mounted) return;
    final ctrl = context.read<ControleurEspaceProfesseur>();
    try {
      await ctrl.rechargerExamensSelection();
      _showSnack('Examen créé.');
    } catch (e) {
      _showSnack(e.toString(), isError: true);
    }
  }

  void _showSnack(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor:
            isError ? const Color(0xFFE11D48) : const Color(0xFF0B1220),
      ),
    );
  }
}
