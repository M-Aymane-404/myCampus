import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';

class PageCalendrierProfesseur extends StatefulWidget {
  const PageCalendrierProfesseur({super.key});

  @override
  State<PageCalendrierProfesseur> createState() =>
      _PageCalendrierProfesseurState();
}

class _PageCalendrierProfesseurState extends State<PageCalendrierProfesseur> {
  static const _joursUi = <_JourUiProf>[
    _JourUiProf(labelTop: 'Mo', labelBottom: 'Lun', key: 'LUNDI', weekday: 1),
    _JourUiProf(labelTop: 'Tu', labelBottom: 'Mar', key: 'MARDI', weekday: 2),
    _JourUiProf(
      labelTop: 'We',
      labelBottom: 'Mer',
      key: 'MERCREDI',
      weekday: 3,
    ),
    _JourUiProf(labelTop: 'Th', labelBottom: 'Jeu', key: 'JEUDI', weekday: 4),
    _JourUiProf(
      labelTop: 'Fr',
      labelBottom: 'Ven',
      key: 'VENDREDI',
      weekday: 5,
    ),
    _JourUiProf(labelTop: 'Sa', labelBottom: 'Sam', key: 'SAMEDI', weekday: 6),
  ];

  late String _jourSelectionne;

  @override
  void initState() {
    super.initState();
    _jourSelectionne = _defaultJourKey();
  }

  String _defaultJourKey() {
    final now = DateTime.now().weekday;
    final match = _joursUi.firstWhere(
      (j) => j.weekday == now,
      orElse: () => _joursUi.first,
    );
    return match.key;
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    final emploi = ctrl.emploi;

    if (ctrl.chargement && emploi.isEmpty) {
      return const Scaffold(
        backgroundColor: Color(0xFFF5F7FB),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (ctrl.erreur != null && emploi.isEmpty) {
      return Scaffold(
        backgroundColor: const Color(0xFFF5F7FB),
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: _EmptyState(
                message: ctrl.erreur!,
                actionLabel: 'Reessayer',
                onTap:
                    () =>
                        context
                            .read<ControleurEspaceProfesseur>()
                            .chargerEspace(),
              ),
            ),
          ),
        ),
      );
    }

    if (emploi.isEmpty) {
      return const Scaffold(
        backgroundColor: Color(0xFFF5F7FB),
        body: SafeArea(child: Center(child: _EmptyJourProf())),
      );
    }

    final daysWithSlots =
        emploi
            .map((s) => _simpleDayKeyProf(s.jour))
            .where((d) => d.isNotEmpty)
            .toSet();
    final selectedKey = _simpleDayKeyProf(_jourSelectionne);

    if (daysWithSlots.isNotEmpty && !daysWithSlots.contains(selectedKey)) {
      final firstDayWithSlots = daysWithSlots.first;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        setState(() => _jourSelectionne = firstDayWithSlots);
      });
    }

    final slotsJour =
        emploi
            .where(
              (s) =>
                  _simpleDayKeyProf(s.jour) ==
                  _simpleDayKeyProf(_jourSelectionne),
            )
            .toList()
          ..sort((a, b) => a.heureDebut.compareTo(b.heureDebut));

    final dateLabel = _formatDateFrProf(DateTime.now());

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child: Column(
          children: [
            const _SectionTopBar(
              icon: Icons.calendar_month_rounded,
              titre: 'Calendrier',
            ),
            _HeaderTodayProf(
              dateLabel: dateLabel,
              titre: _labelJourProf(_jourSelectionne),
            ),
            const SizedBox(height: 10),
            _DayPickerProf(
              jours: _joursUi,
              selectedKey: _jourSelectionne,
              onSelect: (k) => setState(() => _jourSelectionne = k),
              couleurSelection: const Color(0xFF5B5BEA),
            ),
            const SizedBox(height: 14),
            if (ctrl.erreur != null)
              Padding(
                padding: const EdgeInsets.fromLTRB(18, 0, 18, 12),
                child: _ErrorBanner(message: ctrl.erreur!),
              ),
            Expanded(
              child: RefreshIndicator(
                onRefresh:
                    () =>
                        context
                            .read<ControleurEspaceProfesseur>()
                            .chargerEspace(),
                child:
                    slotsJour.isEmpty
                        ? ListView(
                          physics: const AlwaysScrollableScrollPhysics(),
                          children: const [
                            SizedBox(height: 120),
                            _EmptyJourProf(),
                          ],
                        )
                        : _TimelineListProf(
                          slots: slotsJour,
                          couleur: const Color(0xFF5B5BEA),
                        ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionTopBar extends StatelessWidget {
  const _SectionTopBar({required this.icon, required this.titre});

  final IconData icon;
  final String titre;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(18, 12, 18, 12),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border(
          bottom: BorderSide(color: Colors.black.withOpacity(0.06)),
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: const Color(0xFF5B5BEA).withOpacity(0.12),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: const Color(0xFF5B5BEA)),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              titre,
              style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
            ),
          ),
        ],
      ),
    );
  }
}

class _HeaderTodayProf extends StatelessWidget {
  const _HeaderTodayProf({required this.dateLabel, required this.titre});

  final String dateLabel;
  final String titre;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(18, 14, 18, 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  dateLabel,
                  style: TextStyle(
                    fontWeight: FontWeight.w700,
                    color: Colors.black.withOpacity(0.55),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  titre,
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    fontSize: 32,
                    height: 1.0,
                    color: Colors.black,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _DayPickerProf extends StatelessWidget {
  const _DayPickerProf({
    required this.jours,
    required this.selectedKey,
    required this.onSelect,
    required this.couleurSelection,
  });

  final List<_JourUiProf> jours;
  final String selectedKey;
  final ValueChanged<String> onSelect;
  final Color couleurSelection;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 64,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        scrollDirection: Axis.horizontal,
        itemBuilder: (_, i) {
          final j = jours[i];
          final isSelected = j.key == selectedKey;

          return InkWell(
            borderRadius: BorderRadius.circular(18),
            onTap: () => onSelect(j.key),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 180),
              width: 56,
              decoration: BoxDecoration(
                color: isSelected ? couleurSelection : Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: Colors.black.withOpacity(0.06)),
                boxShadow:
                    isSelected
                        ? [
                          BoxShadow(
                            blurRadius: 18,
                            offset: const Offset(0, 10),
                            color: Colors.black.withOpacity(0.10),
                          ),
                        ]
                        : null,
              ),
              padding: const EdgeInsets.symmetric(vertical: 10),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    j.labelTop,
                    style: TextStyle(
                      fontWeight: FontWeight.w800,
                      color:
                          isSelected
                              ? Colors.white
                              : Colors.black.withOpacity(0.55),
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    j.labelBottom,
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                      color: isSelected ? Colors.white : Colors.black,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
            ),
          );
        },
        separatorBuilder: (_, __) => const SizedBox(width: 10),
        itemCount: jours.length,
      ),
    );
  }
}

class _TimelineListProf extends StatelessWidget {
  const _TimelineListProf({required this.slots, required this.couleur});

  final List<SeanceProfesseurMobileDTO> slots;
  final Color couleur;

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(18, 6, 18, 18),
      itemBuilder: (_, i) {
        final s = slots[i];
        final isHero = i == 0;

        return _TimelineRowProf(
          heure: s.heureDebut,
          couleur: couleur,
          card:
              isHero
                  ? _CarteSeanceHeroProf(slot: s, couleur: couleur)
                  : _CarteSeanceSimpleProf(slot: s),
          isLast: i == slots.length - 1,
        );
      },
      separatorBuilder: (_, __) => const SizedBox(height: 14),
      itemCount: slots.length,
    );
  }
}

class _TimelineRowProf extends StatelessWidget {
  const _TimelineRowProf({
    required this.heure,
    required this.card,
    required this.isLast,
    required this.couleur,
  });

  final String heure;
  final Widget card;
  final bool isLast;
  final Color couleur;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 56,
          child: Column(
            children: [
              const SizedBox(height: 6),
              _TimelineDotProf(couleur: couleur),
              if (!isLast)
                Container(
                  width: 2,
                  height: 84,
                  margin: const EdgeInsets.only(top: 6),
                  color: Colors.black.withOpacity(0.10),
                ),
            ],
          ),
        ),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                heure,
                style: TextStyle(
                  fontWeight: FontWeight.w800,
                  color: Colors.black.withOpacity(0.55),
                ),
              ),
              const SizedBox(height: 8),
              card,
            ],
          ),
        ),
      ],
    );
  }
}

class _TimelineDotProf extends StatelessWidget {
  const _TimelineDotProf({required this.couleur});

  final Color couleur;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 12,
      height: 12,
      decoration: BoxDecoration(
        color: couleur,
        borderRadius: BorderRadius.circular(99),
        boxShadow: [
          BoxShadow(
            blurRadius: 10,
            offset: const Offset(0, 6),
            color: Colors.black.withOpacity(0.12),
          ),
        ],
      ),
    );
  }
}

class _CarteSeanceHeroProf extends StatelessWidget {
  const _CarteSeanceHeroProf({required this.slot, required this.couleur});

  final SeanceProfesseurMobileDTO slot;
  final Color couleur;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: couleur,
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            blurRadius: 22,
            offset: const Offset(0, 14),
            color: Colors.black.withOpacity(0.14),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: _SeanceTextsProf(
              slot: slot,
              titleColor: Colors.white,
              subColor: Colors.white.withOpacity(0.85),
              minorColor: Colors.white.withOpacity(0.75),
            ),
          ),
          const SizedBox(width: 10),
          Container(
            width: 34,
            height: 34,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.15),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(Icons.school_rounded, color: Colors.white),
          ),
        ],
      ),
    );
  }
}

class _CarteSeanceSimpleProf extends StatelessWidget {
  const _CarteSeanceSimpleProf({required this.slot});

  final SeanceProfesseurMobileDTO slot;

  Color _leftColor() {
    switch (slot.typeSeance.toUpperCase()) {
      case 'TP':
        return const Color(0xFF10B981);
      case 'TD':
        return const Color(0xFF6366F1);
      default:
        return const Color(0xFFEF4444);
    }
  }

  @override
  Widget build(BuildContext context) {
    final c = _leftColor();
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
        boxShadow: [
          BoxShadow(
            blurRadius: 18,
            offset: const Offset(0, 10),
            color: Colors.black.withOpacity(0.06),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 4,
            height: 54,
            decoration: BoxDecoration(
              color: c,
              borderRadius: BorderRadius.circular(999),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _SeanceTextsProf(
              slot: slot,
              titleColor: const Color(0xFF111827),
              subColor: Colors.black.withOpacity(0.60),
              minorColor: Colors.black.withOpacity(0.55),
            ),
          ),
        ],
      ),
    );
  }
}

class _SeanceTextsProf extends StatelessWidget {
  const _SeanceTextsProf({
    required this.slot,
    required this.titleColor,
    required this.subColor,
    required this.minorColor,
  });

  final SeanceProfesseurMobileDTO slot;
  final Color titleColor;
  final Color subColor;
  final Color minorColor;

  @override
  Widget build(BuildContext context) {
    final titre = slot.matiereNom.isEmpty ? 'Seance' : slot.matiereNom;
    final salle = slot.salle.isEmpty ? 'Salle ?' : slot.salle;
    final heureFin = slot.heureFin.isEmpty ? '--:--' : slot.heureFin;
    final classe =
        slot.classeNom.isNotEmpty
            ? slot.classeNom
            : (slot.codeClasse.isNotEmpty
                ? slot.codeClasse
                : 'Classe non renseignee');

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          titre,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(fontWeight: FontWeight.w900, color: titleColor),
        ),
        const SizedBox(height: 6),
        Text(
          '${slot.typeSeance} - $salle - $heureFin',
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(
            fontWeight: FontWeight.w800,
            fontSize: 12.5,
            color: subColor,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          classe,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(
            fontWeight: FontWeight.w700,
            fontSize: 12.5,
            color: minorColor,
          ),
        ),
      ],
    );
  }
}

class _EmptyJourProf extends StatelessWidget {
  const _EmptyJourProf();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        'Aucune seance pour ce jour',
        style: TextStyle(
          fontWeight: FontWeight.w800,
          color: Colors.black.withOpacity(0.55),
        ),
      ),
    );
  }
}

class _ErrorBanner extends StatelessWidget {
  const _ErrorBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF1F3),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFFDA4AF)),
      ),
      child: Row(
        children: [
          const Icon(Icons.error_outline_rounded, color: Color(0xFFE11D48)),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w700,
                color: Color(0xFF9F1239),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.message, this.actionLabel, this.onTap});

  final String message;
  final String? actionLabel;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        children: [
          const Icon(Icons.inbox_rounded, size: 34, color: Color(0xFF98A2B3)),
          const SizedBox(height: 12),
          Text(
            message,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Color(0xFF667085),
            ),
          ),
          if (actionLabel != null && onTap != null) ...[
            const SizedBox(height: 12),
            OutlinedButton(onPressed: onTap, child: Text(actionLabel!)),
          ],
        ],
      ),
    );
  }
}

class _JourUiProf {
  const _JourUiProf({
    required this.labelTop,
    required this.labelBottom,
    required this.key,
    required this.weekday,
  });

  final String labelTop;
  final String labelBottom;
  final String key;
  final int weekday;
}

String _simpleDayKeyProf(String raw) => raw.trim().toUpperCase();

String _formatDateFrProf(DateTime d) {
  const mois = [
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
  return '${d.day} ${mois[d.month - 1]} ${d.year}';
}

String _labelJourProf(String key) {
  switch (key) {
    case 'LUNDI':
      return 'Lundi';
    case 'MARDI':
      return 'Mardi';
    case 'MERCREDI':
      return 'Mercredi';
    case 'JEUDI':
      return 'Jeudi';
    case 'VENDREDI':
      return 'Vendredi';
    case 'SAMEDI':
      return 'Samedi';
    case 'DIMANCHE':
      return 'Dimanche';
    default:
      return key;
  }
}
