import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../etudiant/data/controller/ControleurFichier.dart';
import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/professeur_presence_dto.dart';

class PagePresenceProfesseur extends StatefulWidget {
  const PagePresenceProfesseur({super.key});

  @override
  State<PagePresenceProfesseur> createState() => _PagePresenceProfesseurState();
}

class _PagePresenceProfesseurState extends State<PagePresenceProfesseur> {
  Timer? _refreshTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurEspaceProfesseur>().initialiserPresence();
    });
    _refreshTimer = Timer.periodic(const Duration(seconds: 10), (_) {
      if (!mounted) return;
      final ctrl = context.read<ControleurEspaceProfesseur>();
      if (ctrl.seancePresenceSelectionnee == null) return;
      final lignes =
          ctrl.feuillePresence?.lignes ?? const <LignePresenceProfesseurDTO>[];
      final estOccupe = lignes.any(
        (ligne) =>
            ctrl.sauvegardePresenceEnCours(ligne.apogee) ||
            ctrl.traitementJustificatifEnCours(ligne.idPresence),
      );
      if (estOccupe) return;
      ctrl.chargerFeuillePresence(silencieux: true);
    });
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    super.dispose();
  }

  Future<void> _ouvrirJustificatif(LignePresenceProfesseurDTO ligne) async {
    final url = ligne.motifUrl;
    if (url == null || url.trim().isEmpty) {
      _toast('Aucun justificatif disponible pour cette absence.');
      return;
    }

    final fileName =
        (ligne.motifNom != null && ligne.motifNom!.trim().isNotEmpty)
            ? ligne.motifNom!
            : _extractFileName(url);

    try {
      await context.read<ControleurFichier>().telechargerEtOuvrir(
        urlDownload: url,
        nomFichier: fileName,
      );
    } catch (e) {
      _toast('Impossible d ouvrir le justificatif: $e');
    }
  }

  void _toast(String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    final fichierCtrl = context.watch<ControleurFichier>();
    final enseignements = ctrl.enseignements;
    final seances = ctrl.seancesPresence;
    final feuille = ctrl.feuillePresence;
    final selectedSeance = _findSelectedSeance(
      seances,
      ctrl.seancePresenceSelectionnee,
    );
    final isInitialLoading = ctrl.chargement && enseignements.isEmpty;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child:
            isInitialLoading
                ? const Center(child: CircularProgressIndicator())
                : RefreshIndicator(
                  onRefresh: ctrl.rafraichirPresence,
                  child: ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(18, 18, 18, 24),
                    children: [
                      _PresenceHeroCard(
                        seance: selectedSeance,
                        feuille: feuille,
                      ),
                      const SizedBox(height: 16),
                      _PresenceSelectorsBar(
                        enseignements: enseignements,
                        enseignementSelectionne:
                            ctrl.enseignementPresenceSelectionne,
                        seances: seances,
                        seanceSelectionnee: ctrl.seancePresenceSelectionnee,
                        chargementEnseignements:
                            ctrl.chargement && enseignements.isEmpty,
                        chargementSeances: ctrl.chargementSeancesPresence,
                        onSelectEnseignement: (value) {
                          if (value == null) return;
                          ctrl.selectionnerEnseignementPresence(value);
                        },
                        onSelectSeance: (value) {
                          if (value == null) return;
                          ctrl.selectionnerSeancePresence(value);
                        },
                      ),
                      const SizedBox(height: 14),
                      if (ctrl.erreur != null) ...[
                        _PresenceAlert(
                          message: ctrl.erreur!,
                          color: const Color(0xFFBE123C),
                          background: const Color(0xFFFFF1F2),
                          border: const Color(0xFFFECDD3),
                          icon: Icons.error_outline_rounded,
                        ),
                        const SizedBox(height: 12),
                      ],
                      if (fichierCtrl.telechargementEnCours) ...[
                        const _PresenceAlert(
                          message: 'Telechargement du justificatif en cours...',
                          color: Color(0xFF1D4ED8),
                          background: Color(0xFFEFF6FF),
                          border: Color(0xFFBFDBFE),
                          icon: Icons.download_rounded,
                        ),
                        const SizedBox(height: 12),
                      ],
                      if (enseignements.isEmpty && !ctrl.chargement) ...[
                        const _PresenceEmptyState(
                          icon: Icons.menu_book_rounded,
                          title: 'Aucun cours disponible',
                          subtitle:
                              'Aucun enseignement n est rattache a votre compte professeur.',
                        ),
                      ] else if (ctrl.chargementSeancesPresence ||
                          (ctrl.chargementFeuillePresence &&
                              feuille == null)) ...[
                        const _PresenceLoadingBox(
                          message: 'Chargement de la feuille d appel...',
                        ),
                      ] else if (selectedSeance == null) ...[
                        const _PresenceEmptyState(
                          icon: Icons.event_busy_rounded,
                          title: 'Aucune seance disponible',
                          subtitle:
                              'Selectionnez un cours avec des seances planifiees pour saisir les presences.',
                        ),
                      ] else if (feuille == null) ...[
                        const _PresenceEmptyState(
                          icon: Icons.fact_check_rounded,
                          title: 'Feuille introuvable',
                          subtitle:
                              'Impossible de charger la feuille de presence pour cette seance.',
                        ),
                      ] else if (feuille.lignes.isEmpty) ...[
                        const _PresenceEmptyState(
                          icon: Icons.groups_rounded,
                          title: 'Aucun etudiant inscrit',
                          subtitle:
                              'Cette seance ne contient actuellement aucun etudiant a pointer.',
                        ),
                      ] else ...[
                        _PresenceLinesCard(
                          lignes: feuille.lignes,
                          onPresent:
                              (ligne) => ctrl.saisirPresence(
                                apogee: ligne.apogee,
                                status: 'PRESENT',
                              ),
                          onAbsent:
                              (ligne) => ctrl.saisirPresence(
                                apogee: ligne.apogee,
                                status: 'ABSENT',
                              ),
                          onViewJustification: _ouvrirJustificatif,
                          onApproveJustification: (ligne) {
                            final id = ligne.idPresence;
                            if (id == null) return;
                            ctrl.validerJustificatif(id);
                          },
                          onRejectJustification: (ligne) {
                            final id = ligne.idPresence;
                            if (id == null) return;
                            ctrl.refuserJustificatif(id);
                          },
                          isSaving: ctrl.sauvegardePresenceEnCours,
                          isProcessing: ctrl.traitementJustificatifEnCours,
                        ),
                      ],
                    ],
                  ),
                ),
      ),
    );
  }
}

class _PresenceHeroCard extends StatelessWidget {
  const _PresenceHeroCard({required this.seance, required this.feuille});

  final SeanceRechercheProfesseurDTO? seance;
  final FeuillePresenceProfesseurDTO? feuille;

  @override
  Widget build(BuildContext context) {
    final type = (seance?.type ?? '').trim().toUpperCase();
    final color = _typeColor(type);
    final date = seance?.heureDebut ?? seance?.date;
    final periode = _formatPeriod(seance?.heureDebut, seance?.heureFin);
    final room =
        seance?.salle.trim().isNotEmpty == true
            ? 'Salle ${seance!.salle}'
            : null;

    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        gradient: const LinearGradient(
          colors: [Color(0xFFF8FBFF), Color(0xFFEEF4FF)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 24,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Appel des etudiants',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: Color(0xFF0F172A),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            feuille?.nomMatiere.trim().isNotEmpty == true
                ? feuille!.nomMatiere
                : 'Selectionnez un cours et une seance pour gerer les presences.',
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Color(0xFF64748B),
            ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              if (type.isNotEmpty)
                _HeroTag(
                  label: type,
                  foreground: color,
                  background: color.withOpacity(0.10),
                ),
              if (date != null)
                _HeroTag(
                  label: _formatDateLong(date),
                  foreground: const Color(0xFF334155),
                  background: const Color(0xFFFFFFFF),
                ),
              if (periode != null)
                _HeroTag(
                  label: periode,
                  foreground: const Color(0xFF334155),
                  background: const Color(0xFFFFFFFF),
                ),
              if (room != null)
                _HeroTag(
                  label: room,
                  foreground: const Color(0xFF334155),
                  background: const Color(0xFFFFFFFF),
                ),
            ],
          ),
          const SizedBox(height: 18),
          Row(
            children: [
              Expanded(
                child: _HeroStat(
                  label: 'PRESENTS',
                  value: '${feuille?.nbPresents ?? 0}',
                  color: const Color(0xFF059669),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _HeroStat(
                  label: 'ABSENTS',
                  value: '${feuille?.nbAbsents ?? 0}',
                  color: const Color(0xFFDC2626),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _HeroStat(
                  label: 'TOTAL',
                  value: '${feuille?.totalEtudiants ?? 0}',
                  color: const Color(0xFF1E293B),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _PresenceSelectorsBar extends StatelessWidget {
  const _PresenceSelectorsBar({
    required this.enseignements,
    required this.enseignementSelectionne,
    required this.seances,
    required this.seanceSelectionnee,
    required this.chargementEnseignements,
    required this.chargementSeances,
    required this.onSelectEnseignement,
    required this.onSelectSeance,
  });

  final List<dynamic> enseignements;
  final String? enseignementSelectionne;
  final List<SeanceRechercheProfesseurDTO> seances;
  final int? seanceSelectionnee;
  final bool chargementEnseignements;
  final bool chargementSeances;
  final ValueChanged<String?> onSelectEnseignement;
  final ValueChanged<int?> onSelectSeance;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.03),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        children: [
          _SelectorBlock<String>(
            icon: Icons.school_rounded,
            label: 'Cours',
            value: enseignementSelectionne,
            loading: chargementEnseignements,
            items:
                enseignements
                    .map<DropdownMenuItem<String>>(
                      (item) => DropdownMenuItem<String>(
                        value: '${item.codeClasse}|${item.codeMatiere}',
                        child: Text('${item.codeClasse} - ${item.matiereNom}'),
                      ),
                    )
                    .toList(),
            onChanged: onSelectEnseignement,
            emptyLabel: 'Aucun cours disponible',
          ),
          const SizedBox(height: 12),
          _SelectorBlock<int>(
            icon: Icons.calendar_month_rounded,
            label: 'Seance',
            value: seanceSelectionnee,
            loading: chargementSeances,
            items:
                seances
                    .where((item) => item.id != null)
                    .map<DropdownMenuItem<int>>(
                      (item) => DropdownMenuItem<int>(
                        value: item.id!,
                        child: Text(_sessionLabel(item)),
                      ),
                    )
                    .toList(),
            onChanged: onSelectSeance,
            emptyLabel: 'Aucune seance disponible',
          ),
        ],
      ),
    );
  }
}

class _SelectorBlock<T> extends StatelessWidget {
  const _SelectorBlock({
    required this.icon,
    required this.label,
    required this.value,
    required this.loading,
    required this.items,
    required this.onChanged,
    required this.emptyLabel,
  });

  final IconData icon;
  final String label;
  final T? value;
  final bool loading;
  final List<DropdownMenuItem<T>> items;
  final ValueChanged<T?> onChanged;
  final String emptyLabel;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(icon, size: 16, color: const Color(0xFF475569)),
            const SizedBox(width: 6),
            Text(
              label.toUpperCase(),
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
                color: Color(0xFF475569),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (loading)
          const _SelectorLoading()
        else if (items.isEmpty)
          _SelectorEmpty(label: emptyLabel)
        else
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
            decoration: BoxDecoration(
              color: const Color(0xFFF8FAFC),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFFCBD5E1)),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<T>(
                value: value,
                isExpanded: true,
                items: items,
                onChanged: onChanged,
              ),
            ),
          ),
      ],
    );
  }
}

class _PresenceLinesCard extends StatelessWidget {
  const _PresenceLinesCard({
    required this.lignes,
    required this.onPresent,
    required this.onAbsent,
    required this.onViewJustification,
    required this.onApproveJustification,
    required this.onRejectJustification,
    required this.isSaving,
    required this.isProcessing,
  });

  final List<LignePresenceProfesseurDTO> lignes;
  final ValueChanged<LignePresenceProfesseurDTO> onPresent;
  final ValueChanged<LignePresenceProfesseurDTO> onAbsent;
  final ValueChanged<LignePresenceProfesseurDTO> onViewJustification;
  final ValueChanged<LignePresenceProfesseurDTO> onApproveJustification;
  final ValueChanged<LignePresenceProfesseurDTO> onRejectJustification;
  final bool Function(String apogee) isSaving;
  final bool Function(int? presenceId) isProcessing;

  @override
  Widget build(BuildContext context) {
    return Column(
      children:
          lignes
              .asMap()
              .entries
              .map(
                (entry) => Padding(
                  padding: EdgeInsets.only(
                    bottom: entry.key == lignes.length - 1 ? 0 : 12,
                  ),
                  child: _PresenceLineCard(
                    ligne: entry.value,
                    avatarColor: _avatarColor(entry.key),
                    onPresent: () => onPresent(entry.value),
                    onAbsent: () => onAbsent(entry.value),
                    onViewJustification: () => onViewJustification(entry.value),
                    onApproveJustification:
                        () => onApproveJustification(entry.value),
                    onRejectJustification:
                        () => onRejectJustification(entry.value),
                    saving: isSaving(entry.value.apogee),
                    processing: isProcessing(entry.value.idPresence),
                  ),
                ),
              )
              .toList(),
    );
  }
}

class _PresenceLineCard extends StatelessWidget {
  const _PresenceLineCard({
    required this.ligne,
    required this.avatarColor,
    required this.onPresent,
    required this.onAbsent,
    required this.onViewJustification,
    required this.onApproveJustification,
    required this.onRejectJustification,
    required this.saving,
    required this.processing,
  });

  final LignePresenceProfesseurDTO ligne;
  final Color avatarColor;
  final VoidCallback onPresent;
  final VoidCallback onAbsent;
  final VoidCallback onViewJustification;
  final VoidCallback onApproveJustification;
  final VoidCallback onRejectJustification;
  final bool saving;
  final bool processing;

  @override
  Widget build(BuildContext context) {
    final status = ligne.normalizedStatus;
    final statusColor = _statusColor(status);
    final statusBg = _statusBackground(status);
    final isAbsent = status == 'ABSENT';
    final canReview =
        ligne.hasJustification && ligne.justificationState == 'pending';

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 20,
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
                width: 50,
                height: 50,
                decoration: BoxDecoration(
                  color: avatarColor.withOpacity(0.14),
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: Text(
                  _initiales(ligne.prenomEtudiant, ligne.nomEtudiant),
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                    color: avatarColor,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      ligne.nomComplet,
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w900,
                        color: Color(0xFF0F172A),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Apogee: ${ligne.apogee}',
                      style: const TextStyle(
                        fontSize: 12.5,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF64748B),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: statusBg,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  _statusLabel(status),
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w900,
                    color: statusColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _ActionButton(
                label: saving && status == 'PRESENT' ? '...' : 'Present',
                onTap: saving ? null : onPresent,
                active: status == 'PRESENT',
                activeColor: const Color(0xFF059669),
              ),
              _ActionButton(
                label: saving && status == 'ABSENT' ? '...' : 'Absent',
                onTap: saving ? null : onAbsent,
                active: status == 'ABSENT',
                activeColor: const Color(0xFFEF4444),
              ),
            ],
          ),
          const SizedBox(height: 14),
          if (isAbsent)
            _JustificationPanel(
              ligne: ligne,
              processing: processing,
              canReview: canReview,
              onView: ligne.hasJustification ? onViewJustification : null,
              onApprove: canReview ? onApproveJustification : null,
              onReject: canReview ? onRejectJustification : null,
            )
          else
            const _JustificationNotApplicable(),
        ],
      ),
    );
  }
}

class _JustificationPanel extends StatelessWidget {
  const _JustificationPanel({
    required this.ligne,
    required this.processing,
    required this.canReview,
    required this.onView,
    required this.onApprove,
    required this.onReject,
  });

  final LignePresenceProfesseurDTO ligne;
  final bool processing;
  final bool canReview;
  final VoidCallback? onView;
  final VoidCallback? onApprove;
  final VoidCallback? onReject;

  @override
  Widget build(BuildContext context) {
    final state = ligne.justificationState;
    final colors = _justificationPalette(state);
    final detail =
        ligne.typeMotif ??
        ligne.motif ??
        ligne.motifNom ??
        'Aucun document soumis';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: colors.background,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: colors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            ligne.justificationLabel,
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w900,
              color: colors.foreground,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            detail,
            style: const TextStyle(
              fontSize: 12.5,
              fontWeight: FontWeight.w600,
              color: Color(0xFF475569),
            ),
          ),
          if ((ligne.motif ?? '').trim().isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(
              ligne.motif!,
              style: const TextStyle(fontSize: 12.5, color: Color(0xFF64748B)),
            ),
          ],
          if (ligne.hasJustification) ...[
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                _MiniActionButton(
                  label: 'Consulter',
                  onTap: processing ? null : onView,
                  foreground: const Color(0xFF334155),
                  background: Colors.white,
                  border: const Color(0xFFE2E8F0),
                ),
                if (canReview)
                  _MiniActionButton(
                    label: processing ? '...' : 'Valider',
                    onTap: processing ? null : onApprove,
                    foreground: const Color(0xFF166534),
                    background: const Color(0xFFDCFCE7),
                    border: const Color(0xFF86EFAC),
                  ),
                if (canReview)
                  _MiniActionButton(
                    label: processing ? '...' : 'Refuser',
                    onTap: processing ? null : onReject,
                    foreground: const Color(0xFFB91C1C),
                    background: const Color(0xFFFEE2E2),
                    border: const Color(0xFFFECACA),
                  ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class _JustificationNotApplicable extends StatelessWidget {
  const _JustificationNotApplicable();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Non applicable',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w900,
              color: Color(0xFF0F172A),
            ),
          ),
          SizedBox(height: 6),
          Text(
            'Le justificatif apparait uniquement pour les absences.',
            style: TextStyle(
              fontSize: 12.5,
              fontWeight: FontWeight.w600,
              color: Color(0xFF64748B),
            ),
          ),
        ],
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({
    required this.label,
    required this.onTap,
    required this.active,
    required this.activeColor,
  });

  final String label;
  final VoidCallback? onTap;
  final bool active;
  final Color activeColor;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Ink(
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
        decoration: BoxDecoration(
          color: active ? activeColor : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: active ? activeColor : const Color(0xFFE2E8F0),
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.w800,
            color: active ? Colors.white : const Color(0xFF64748B),
          ),
        ),
      ),
    );
  }
}

class _MiniActionButton extends StatelessWidget {
  const _MiniActionButton({
    required this.label,
    required this.onTap,
    required this.foreground,
    required this.background,
    required this.border,
  });

  final String label;
  final VoidCallback? onTap;
  final Color foreground;
  final Color background;
  final Color border;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Ink(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: background,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: border),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: foreground,
          ),
        ),
      ),
    );
  }
}

class _PresenceAlert extends StatelessWidget {
  const _PresenceAlert({
    required this.message,
    required this.color,
    required this.background,
    required this.border,
    required this.icon,
  });

  final String message;
  final Color color;
  final Color background;
  final Color border;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: border),
      ),
      child: Row(
        children: [
          Icon(icon, color: color),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w700,
                color: color,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _PresenceLoadingBox extends StatelessWidget {
  const _PresenceLoadingBox({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        children: [
          const SizedBox(
            width: 22,
            height: 22,
            child: CircularProgressIndicator(strokeWidth: 2.2),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: Color(0xFF64748B),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _PresenceEmptyState extends StatelessWidget {
  const _PresenceEmptyState({
    required this.icon,
    required this.title,
    required this.subtitle,
  });

  final IconData icon;
  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(28),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Column(
        children: [
          Icon(icon, size: 44, color: const Color(0xFF94A3B8)),
          const SizedBox(height: 12),
          Text(
            title,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: Color(0xFF0F172A),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            subtitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF64748B),
            ),
          ),
        ],
      ),
    );
  }
}

class _HeroTag extends StatelessWidget {
  const _HeroTag({
    required this.label,
    required this.foreground,
    required this.background,
  });

  final String label;
  final Color foreground;
  final Color background;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w800,
          color: foreground,
        ),
      ),
    );
  }
}

class _HeroStat extends StatelessWidget {
  const _HeroStat({
    required this.label,
    required this.value,
    required this.color,
  });

  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.92),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFDCE3EE)),
      ),
      child: Column(
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w800,
              color: Color(0xFF64748B),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}

class _SelectorLoading extends StatelessWidget {
  const _SelectorLoading();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.symmetric(vertical: 14),
      child: Row(
        children: [
          SizedBox(
            width: 16,
            height: 16,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
          SizedBox(width: 10),
          Text(
            'Chargement...',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF94A3B8),
            ),
          ),
        ],
      ),
    );
  }
}

class _SelectorEmpty extends StatelessWidget {
  const _SelectorEmpty({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 14),
      child: Text(
        label,
        style: const TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w600,
          color: Color(0xFF94A3B8),
        ),
      ),
    );
  }
}

SeanceRechercheProfesseurDTO? _findSelectedSeance(
  List<SeanceRechercheProfesseurDTO> seances,
  int? id,
) {
  if (id == null) return null;
  for (final seance in seances) {
    if (seance.id == id) return seance;
  }
  return null;
}

String _sessionLabel(SeanceRechercheProfesseurDTO seance) {
  final pieces = <String>[];
  if (seance.type.trim().isNotEmpty) pieces.add(seance.type);
  final date = seance.heureDebut ?? seance.date;
  if (date != null) pieces.add(_formatDateShort(date));
  final period = _formatPeriod(seance.heureDebut, seance.heureFin);
  if (period != null) pieces.add(period);
  if (seance.salle.trim().isNotEmpty) pieces.add('Salle ${seance.salle}');
  return pieces.join(' | ');
}

String _formatDateLong(DateTime date) {
  const weekdays = [
    'lundi',
    'mardi',
    'mercredi',
    'jeudi',
    'vendredi',
    'samedi',
    'dimanche',
  ];
  const months = [
    'janvier',
    'fevrier',
    'mars',
    'avril',
    'mai',
    'juin',
    'juillet',
    'aout',
    'septembre',
    'octobre',
    'novembre',
    'decembre',
  ];
  return '${weekdays[date.weekday - 1]} ${_two(date.day)} ${months[date.month - 1]}';
}

String _formatDateShort(DateTime date) {
  return '${_two(date.day)}/${_two(date.month)}/${date.year}';
}

String? _formatPeriod(DateTime? start, DateTime? end) {
  if (start == null && end == null) return null;
  if (start != null && end != null) {
    return '${_formatHour(start)} -> ${_formatHour(end)}';
  }
  return start != null ? _formatHour(start) : _formatHour(end!);
}

String _formatHour(DateTime date) {
  return '${_two(date.hour)}:${_two(date.minute)}';
}

String _two(int value) => value.toString().padLeft(2, '0');

String _initiales(String prenom, String nom) {
  final p = prenom.isNotEmpty ? prenom[0] : '';
  final n = nom.isNotEmpty ? nom[0] : '';
  final value = '$p$n'.trim();
  return value.isEmpty ? '?' : value.toUpperCase();
}

String _statusLabel(String status) {
  switch (status) {
    case 'PRESENT':
      return 'Present';
    case 'ABSENT':
      return 'Absent';
    case 'JUSTIFIE':
      return 'Justifie';
    case 'RETARD':
      return 'Retard';
    default:
      return 'Non defini';
  }
}

Color _typeColor(String type) {
  switch (type) {
    case 'COURS':
      return const Color(0xFF3B82F6);
    case 'TD':
      return const Color(0xFF16A34A);
    case 'TP':
      return const Color(0xFF9333EA);
    default:
      return const Color(0xFF64748B);
  }
}

Color _statusColor(String status) {
  switch (status) {
    case 'PRESENT':
      return const Color(0xFF166534);
    case 'ABSENT':
      return const Color(0xFFB91C1C);
    case 'JUSTIFIE':
      return const Color(0xFF92400E);
    case 'RETARD':
      return const Color(0xFF6D28D9);
    default:
      return const Color(0xFF475569);
  }
}

Color _statusBackground(String status) {
  switch (status) {
    case 'PRESENT':
      return const Color(0xFFDCFCE7);
    case 'ABSENT':
      return const Color(0xFFFEE2E2);
    case 'JUSTIFIE':
      return const Color(0xFFFEF3C7);
    case 'RETARD':
      return const Color(0xFFEDE9FE);
    default:
      return const Color(0xFFF1F5F9);
  }
}

_JustificationPalette _justificationPalette(String state) {
  switch (state) {
    case 'validated':
      return const _JustificationPalette(
        foreground: Color(0xFF166534),
        background: Color(0xFFECFDF5),
        border: Color(0xFF86EFAC),
      );
    case 'rejected':
      return const _JustificationPalette(
        foreground: Color(0xFFB91C1C),
        background: Color(0xFFFEF2F2),
        border: Color(0xFFFCA5A5),
      );
    case 'pending':
      return const _JustificationPalette(
        foreground: Color(0xFF9A3412),
        background: Color(0xFFFFF7ED),
        border: Color(0xFFFDBA74),
      );
    default:
      return const _JustificationPalette(
        foreground: Color(0xFF334155),
        background: Color(0xFFF8FAFC),
        border: Color(0xFFE2E8F0),
      );
  }
}

Color _avatarColor(int index) {
  const colors = [
    Color(0xFF0369A1),
    Color(0xFFBE185D),
    Color(0xFF7E22CE),
    Color(0xFF166534),
    Color(0xFF6B21A8),
    Color(0xFF0F766E),
    Color(0xFF1D4ED8),
    Color(0xFF9A3412),
  ];
  return colors[index % colors.length];
}

String _extractFileName(String raw) {
  final trimmed = raw.trim();
  if (!trimmed.contains('/')) return trimmed;
  return trimmed.split('/').last;
}

class _JustificationPalette {
  const _JustificationPalette({
    required this.foreground,
    required this.background,
    required this.border,
  });

  final Color foreground;
  final Color background;
  final Color border;
}
