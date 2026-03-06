import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_emploi_etudiant.dart';
import '../data/dto/emploi_du_temps_etudiant_dto.dart';

class PageEmploiTimelineEtudiant extends StatefulWidget {
  const PageEmploiTimelineEtudiant({super.key, this.semestre = 'S1'});
  final String semestre;

  @override
  State<PageEmploiTimelineEtudiant> createState() =>
      _PageEmploiTimelineEtudiantState();
}

class _PageEmploiTimelineEtudiantState
    extends State<PageEmploiTimelineEtudiant> {
  // ✅ Change ici la couleur principale (remplace le noir)
  static const Color _primaire = Color(0xFF5B5BEA);

  static const Color _bg = Color(0xFFF5F7FB);

  // UI: 7 jours
  static const _joursUi = <_JourUi>[
    _JourUi(labelTop: 'Mo', labelBottom: 'Lun', key: 'LUNDI', weekday: 1),
    _JourUi(labelTop: 'Tu', labelBottom: 'Mar', key: 'MARDI', weekday: 2),
    _JourUi(labelTop: 'We', labelBottom: 'Mer', key: 'MERCREDI', weekday: 3),
    _JourUi(labelTop: 'Th', labelBottom: 'Jeu', key: 'JEUDI', weekday: 4),
    _JourUi(labelTop: 'Fr', labelBottom: 'Ven', key: 'VENDREDI', weekday: 5),
    _JourUi(labelTop: 'Sa', labelBottom: 'Sam', key: 'SAMEDI', weekday: 6),
  ];

  late String _jourSelectionne;

  @override
  void initState() {
    super.initState();
    _jourSelectionne = _defaultJourKey();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurEmploiEtudiant>().charger(widget.semestre);
    });
  }

  String _defaultJourKey() {
    final now = DateTime.now().weekday; // 1..7
    final match = _joursUi.firstWhere(
      (j) => j.weekday == now,
      orElse: () => _joursUi.first,
    );
    return match.key;
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEmploiEtudiant>();

    if (ctrl.chargement) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (ctrl.erreur != null) {
      return Scaffold(body: Center(child: Text('Erreur: ${ctrl.erreur}')));
    }

    final emploi = ctrl.emploi;
    if (emploi == null) {
      return const Scaffold(body: Center(child: Text('Aucun emploi du temps')));
    }

    // ✅ fallback : si le jour sélectionné n'a pas de slots (ex: dimanche), basculer sur un jour qui en a
    final daysWithSlots =
        emploi.slots
            .map((s) => _simpleDayKey(s.dayOfWeek))
            .where((d) => d.isNotEmpty)
            .toSet();
    final selectedKey = _simpleDayKey(_jourSelectionne);

    if (daysWithSlots.isNotEmpty && !daysWithSlots.contains(selectedKey)) {
      final firstDayWithSlots = daysWithSlots.first;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        setState(() => _jourSelectionne = firstDayWithSlots);
      });
    }

    final slotsJour =
        emploi.slots
            .where(
              (s) =>
                  _simpleDayKey(s.dayOfWeek) == _simpleDayKey(_jourSelectionne),
            )
            .toList()
          ..sort((a, b) => _compareHeure(a.start, b.start));

    final dateLabel = _formatDateFr(DateTime.now());

    return Scaffold(
      backgroundColor: _bg,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          "Emploi",
          style: TextStyle(
            fontWeight: FontWeight.w900,
            color: Color(0xFF0B1220),
          ),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: SafeArea(
        child: Column(
          children: [
            _HeaderToday(
              dateLabel: dateLabel,
              titre: _labelJour(_jourSelectionne),
            ),
            const SizedBox(height: 10),
            _DayPicker(
              jours: _joursUi,
              selectedKey: _jourSelectionne,
              onSelect: (k) => setState(() => _jourSelectionne = k),
              couleurSelection: _primaire,
            ),
            const SizedBox(height: 14),
            Expanded(
              child:
                  slotsJour.isEmpty
                      ? _EmptyJour()
                      : _TimelineList(slots: slotsJour, couleur: _primaire),
            ),
          ],
        ),
      ),
    );
  }

  static int _compareHeure(String a, String b) => a.compareTo(b);
  static String _simpleDayKey(String raw) => raw.trim().toUpperCase();

  static String _formatDateFr(DateTime d) {
    const mois = [
      'janvier',
      'février',
      'mars',
      'avril',
      'mai',
      'juin',
      'juillet',
      'août',
      'septembre',
      'octobre',
      'novembre',
      'décembre',
    ];
    return '${d.day} ${mois[d.month - 1]} ${d.year}';
  }
}

class _HeaderToday extends StatelessWidget {
  const _HeaderToday({required this.dateLabel, required this.titre});
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

class _DayPicker extends StatelessWidget {
  const _DayPicker({
    required this.jours,
    required this.selectedKey,
    required this.onSelect,
    required this.couleurSelection,
  });

  final List<_JourUi> jours;
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

class _TimelineList extends StatelessWidget {
  const _TimelineList({required this.slots, required this.couleur});
  final List<SlotEmploiDTO> slots;
  final Color couleur;

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(18, 6, 18, 18),
      itemBuilder: (_, i) {
        final s = slots[i];
        final bool isHero = i == 0;

        return _TimelineRow(
          heure: s.start,
          couleur: couleur,
          card:
              isHero
                  ? _CarteSeanceHero(slot: s, couleur: couleur)
                  : _CarteSeanceSimple(slot: s),
          isLast: i == slots.length - 1,
        );
      },
      separatorBuilder: (_, __) => const SizedBox(height: 14),
      itemCount: slots.length,
    );
  }
}

class _TimelineRow extends StatelessWidget {
  const _TimelineRow({
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
              _Dot(couleur: couleur),
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

class _Dot extends StatelessWidget {
  const _Dot({required this.couleur});
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

class _CarteSeanceHero extends StatelessWidget {
  const _CarteSeanceHero({required this.slot, required this.couleur});
  final SlotEmploiDTO slot;
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
            child: _SeanceTexts(
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
            child: const Icon(Icons.check_rounded, color: Colors.white),
          ),
        ],
      ),
    );
  }
}

class _CarteSeanceSimple extends StatelessWidget {
  const _CarteSeanceSimple({required this.slot});
  final SlotEmploiDTO slot;

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
            child: _SeanceTexts(
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

class _SeanceTexts extends StatelessWidget {
  const _SeanceTexts({
    required this.slot,
    required this.titleColor,
    required this.subColor,
    required this.minorColor,
  });

  final SlotEmploiDTO slot;
  final Color titleColor;
  final Color subColor;
  final Color minorColor;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          slot.matiereNom,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(fontWeight: FontWeight.w900, color: titleColor),
        ),
        const SizedBox(height: 6),
        Text(
          '${slot.typeSeance} • ${slot.salle} • ${slot.end}',
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
          slot.professeurNom,
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

class _EmptyJour extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        'Aucune séance pour ce jour',
        style: TextStyle(
          fontWeight: FontWeight.w800,
          color: Colors.black.withOpacity(0.55),
        ),
      ),
    );
  }
}

class _JourUi {
  final String labelTop;
  final String labelBottom;
  final String key;
  final int weekday;

  const _JourUi({
    required this.labelTop,
    required this.labelBottom,
    required this.key,
    required this.weekday,
  });
}

String _labelJour(String key) {
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
