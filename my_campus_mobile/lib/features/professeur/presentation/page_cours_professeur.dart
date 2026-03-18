import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../etudiant/data/controller/ControleurFichier.dart';
import '../data/controller/controleur_cours_professeur.dart';
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

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final ctrl = context.read<ControleurEspaceProfesseur>();
    _scheduleSelectionIfNeeded(ctrl);
  }

  void _scheduleSelectionIfNeeded(ControleurEspaceProfesseur ctrl) {
    final enseignements = ctrl.enseignements;
    if (enseignements.isEmpty) return;
    final hasSelection = _selectedEnseignementKey != null &&
        enseignements.any((e) => _enseignementKey(e) == _selectedEnseignementKey);
    if (hasSelection) return;
    if (_selectionScheduled) return;
    _selectionScheduled = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final first = ctrl.enseignements.first;
      _updateSelection(first);
    });
  }

  Future<void> _updateSelection(EnseignementProfesseurMobileDTO enseignement) async {
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
    final ctrlCours = context.watch<ControleurCoursProfesseur>();
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
                const Text(
                  'Cours simplifiés',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w900,
                    color: Color(0xFF0B1220),
                  ),
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
                _buildSummaryChips(seances.length, examens.length, ctrlCours.ressources.length),
                const SizedBox(height: 16),
                const Divider(),
                TabBar(
                  padding: EdgeInsets.zero,
                  labelColor: const Color(0xFF5B5BEA),
                  unselectedLabelColor: const Color(0xFF8B95AE),
                  indicatorColor: const Color(0xFF5B5BEA),
                  tabs: const [
                    Tab(text: 'Séances'),
                    Tab(text: 'Examens'),
                  ],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: TabBarView(
                    children: [
                      _buildSeancesTab(
                        seances,
                        teaching: enseignement,
                        ctrlCours: ctrlCours,
                        isLoading: seancesLoading,
                      ),
                      _buildExamensTab(examens, teaching: enseignement, ctrlCours: ctrlCours),
                    ],
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
        children: enseignements.map((enseignement) {
          final key = _enseignementKey(enseignement);
          final selected = key == _selectedEnseignementKey;
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: ChoiceChip(
              label: Text('${enseignement.matiereNom} • ${enseignement.codeClasse}'),
              selected: selected,
              onSelected: (_) => _updateSelection(enseignement),
              selectedColor: const Color(0xFFEEF2FF),
              backgroundColor: Colors.white,
              labelStyle: TextStyle(
                fontWeight: FontWeight.w600,
                color: selected ? const Color(0xFF5B5BEA) : const Color(0xFF0B1220),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildSummaryChips(int seances, int examens, int ressources) {
    return Wrap(
      spacing: 10,
      runSpacing: 8,
      children: [
        SummaryChip(label: 'Séances', value: seances),
        SummaryChip(label: 'Examens', value: examens),
        SummaryChip(label: 'Ressources', value: ressources),
      ],
    );
  }

  Widget _buildSeancesTab(
    List<SeanceProfesseurMobileDTO> seances, {
    required EnseignementProfesseurMobileDTO? teaching,
    required ControleurCoursProfesseur ctrlCours,
    required bool isLoading,
  }) {
    final hasData = seances.isNotEmpty;
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      children: [
        ActionRow(
          title: 'Planifier rapidement une séance',
          buttonLabel: 'Créer une séance',
          onTap: teaching == null ? null : () => _openCreateSeanceModal(teaching),
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
          for (final seance in seances) ...[
            SessionCard(
              seance: seance,
              onVoirRessources: seance.id == null
                  ? null
                  : () => _showRessourcesForSeance(seance),
              onUploaderSupport: seance.id == null
                  ? null
                  : () => _uploadRessourceForSeance(seance),
              isUploading:
                  ctrlCours.uploadEnCours && ctrlCours.uploadSeanceId == seance.id,
            ),
          ],
        ],
      ],
    );
  }

  Widget _buildExamensTab(
    List<ExamenProfesseurMobileDTO> examens, {
    required EnseignementProfesseurMobileDTO? teaching,
    required ControleurCoursProfesseur ctrlCours,
  }) {
    final hasData = examens.isNotEmpty;
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      children: [
        ActionRow(
          title: 'Suivez vos examens principaux',
          buttonLabel: 'Créer un examen',
          onTap: teaching == null ? null : () => _openCreateExamenModal(teaching),
        ),
        const SizedBox(height: 12),
        if (!hasData)
          const Center(
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 32),
              child: Text('Aucun examen lié à cet enseignement pour le moment.'),
            ),
          )
        else ...[
          for (final examen in examens)
            ExamCard(
              examen: examen,
            ),
        ],
      ],
    );
  }

  Future<void> _showRessourcesForSeance(SeanceProfesseurMobileDTO seance) async {
    if (seance.id == null) return;
    final ctrlCours = context.read<ControleurCoursProfesseur>();
    try {
      await ctrlCours.chargerRessources(seance.id!);
      if (!mounted) return;
      final fichierCtrl = context.read<ControleurFichier>();
      await showModalBottomSheet(
        context: context,
        isScrollControlled: true,
        builder: (_) => ChangeNotifierProvider.value(
          value: ctrlCours,
          child: RessourceSheet(
            seanceId: seance.id!,
            fichierCtrl: fichierCtrl,
          ),
        ),
      );
    } catch (e) {
      _showSnack(e.toString());
    }
  }

  Future<void> _uploadRessourceForSeance(SeanceProfesseurMobileDTO seance) async {
    if (seance.id == null) return;
    final ctrlCours = context.read<ControleurCoursProfesseur>();
    try {
      await ctrlCours.televerserRessource(seance.id!);
      _showSnack('Ressource envoyée avec succès.');
    } catch (e) {
      _showSnack(e.toString(), isError: true);
    }
  }

  Future<void> _openCreateSeanceModal(EnseignementProfesseurMobileDTO enseignement) async {
    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => CreateSeanceModal(enseignement: enseignement),
    );
  }

  Future<void> _openCreateExamenModal(EnseignementProfesseurMobileDTO enseignement) async {
    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => CreateExamenModal(enseignement: enseignement),
    );
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
