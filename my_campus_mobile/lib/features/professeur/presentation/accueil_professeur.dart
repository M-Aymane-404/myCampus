import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_espace_professeur.dart';
import '../data/controller/controleur_notification_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'page_notifications_professeur.dart';

class AccueilProfesseur extends StatefulWidget {
  const AccueilProfesseur({super.key});

  static const Color accent = Color(0xFF5B5BEA);
  static const Color bg = Color(0xFFF5F7FB);

  @override
  State<AccueilProfesseur> createState() => _AccueilProfesseurState();
}

class _AccueilProfesseurState extends State<AccueilProfesseur> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context
          .read<ControleurNotificationProfesseur>()
          .rafraichirCompteurNonLues();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    final profil = ctrl.profil;
    final distinctMatieres =
        ctrl.enseignements.map((item) => item.codeMatiere).toSet().length;
    final distinctClasses =
        ctrl.enseignements.map((item) => item.codeClasse).toSet().length;

    if (ctrl.chargement &&
        profil == null &&
        ctrl.enseignements.isEmpty &&
        ctrl.emploi.isEmpty) {
      return const Scaffold(
        backgroundColor: AccueilProfesseur.bg,
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (ctrl.erreur != null &&
        profil == null &&
        ctrl.enseignements.isEmpty &&
        ctrl.emploi.isEmpty) {
      return Scaffold(
        backgroundColor: AccueilProfesseur.bg,
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

    return Scaffold(
      backgroundColor: AccueilProfesseur.bg,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh:
              () => context.read<ControleurEspaceProfesseur>().chargerEspace(),
          child: CustomScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              SliverToBoxAdapter(
                child: _TopBarProfesseur(
                  onTapNotifications: () async {
                    final ctrlNotif =
                        context.read<ControleurNotificationProfesseur>();
                    if (ctrlNotif.notifications.isEmpty) {
                      await ctrlNotif.chargerNotifications();
                    }

                    if (!context.mounted) return;
                    await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const PageNotificationsProfesseur(),
                      ),
                    );

                    if (!context.mounted) return;
                    await context
                        .read<ControleurNotificationProfesseur>()
                        .rafraichirCompteurNonLues();
                  },
                ),
              ),
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(18, 14, 18, 18),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    _TitreSection(
                      titre:
                          profil == null
                              ? 'Espace Professeur'
                              : 'Bonjour ${profil.prenom.isEmpty ? 'Professeur' : profil.prenom}',
                      sousTitre:
                          profil == null
                              ? 'Vue generale de vos modules et activites'
                              : _buildSousTitreProfil(profil),
                    ),
                    const SizedBox(height: 14),
                    if (ctrl.erreur != null) ...[
                      _InfoBanner(message: ctrl.erreur!),
                      const SizedBox(height: 14),
                    ],
                    _ResumeProfesseur(
                      modulesCount: distinctMatieres,
                      classesCount: distinctClasses,
                      seancesCount: ctrl.emploi.length,
                    ),
                    const SizedBox(height: 14),
                    _BlocActionsProfesseur(
                      enseignements: ctrl.enseignements,
                      examensCount: ctrl.examens.length,
                    ),
                    const SizedBox(height: 14),
                    _BlocActiviteProfesseur(
                      prochaineSeance: ctrl.prochaineSeance,
                      emploi: ctrl.emploi,
                    ),
                    const SizedBox(height: 14),
                    _BlocPlanningProfesseur(emploi: ctrl.emploi),
                  ]),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TopBarProfesseur extends StatelessWidget {
  const _TopBarProfesseur({required this.onTapNotifications});

  final VoidCallback onTapNotifications;

  @override
  Widget build(BuildContext context) {
    final profil = context.watch<ControleurEspaceProfesseur>().profil;
    final ctrlNotif = context.watch<ControleurNotificationProfesseur>();

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
              color: AccueilProfesseur.accent.withOpacity(0.12),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: AccueilProfesseur.accent.withOpacity(0.18),
              ),
            ),
            child: const Icon(
              Icons.auto_stories_rounded,
              color: AccueilProfesseur.accent,
            ),
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Text(
              'MyCampus',
              overflow: TextOverflow.ellipsis,
              style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
            ),
          ),
          _BoutonNotificationsProfesseur(
            hasUnread: ctrlNotif.aDesNotificationsNonLues,
            onTap: onTapNotifications,
          ),
          const SizedBox(width: 10),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
            decoration: BoxDecoration(
              color: const Color(0xFFF3F5FA),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Text(
              profil?.grade.isNotEmpty == true ? profil!.grade : 'Professeur',
              style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w800,
                color: Color(0xFF667085),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _BoutonNotificationsProfesseur extends StatelessWidget {
  const _BoutonNotificationsProfesseur({
    required this.hasUnread,
    required this.onTap,
  });

  final bool hasUnread;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: onTap,
      child: Container(
        width: 42,
        height: 42,
        decoration: BoxDecoration(
          color: const Color(0xFFF7F9FF),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.black.withOpacity(0.06)),
        ),
        child: Stack(
          clipBehavior: Clip.none,
          alignment: Alignment.center,
          children: [
            const Icon(
              Icons.notifications_none_rounded,
              color: Color(0xFF0B1220),
              size: 22,
            ),
            if (hasUnread)
              Positioned(
                top: 9,
                right: 9,
                child: Container(
                  width: 10,
                  height: 10,
                  decoration: BoxDecoration(
                    color: const Color(0xFFEF4444),
                    shape: BoxShape.circle,
                    border: Border.all(color: Colors.white, width: 1.8),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _TitreSection extends StatelessWidget {
  const _TitreSection({required this.titre, required this.sousTitre});

  final String titre;
  final String sousTitre;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          titre,
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w900,
            color: Color(0xFF101828),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          sousTitre,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: Color(0xFF667085),
          ),
        ),
      ],
    );
  }
}

class _ResumeProfesseur extends StatelessWidget {
  const _ResumeProfesseur({
    required this.modulesCount,
    required this.classesCount,
    required this.seancesCount,
  });

  final int modulesCount;
  final int classesCount;
  final int seancesCount;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            Expanded(
              child: _MiniStatCard(
                icon: Icons.menu_book_rounded,
                label: 'Matieres',
                value: modulesCount.toString().padLeft(2, '0'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _MiniStatCard(
                icon: Icons.groups_rounded,
                label: 'Classes',
                value: classesCount.toString().padLeft(2, '0'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        _WideStatCard(
          icon: Icons.event_available_rounded,
          label: 'Seances dans le planning',
          value: seancesCount.toString().padLeft(2, '0'),
        ),
      ],
    );
  }
}

class _MiniStatCard extends StatelessWidget {
  const _MiniStatCard({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: AccueilProfesseur.accent.withOpacity(0.12),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: AccueilProfesseur.accent),
          ),
          const SizedBox(height: 18),
          Text(
            value,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Color(0xFF667085),
            ),
          ),
        ],
      ),
    );
  }
}

class _WideStatCard extends StatelessWidget {
  const _WideStatCard({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(
              color: const Color(0xFFEEF2FF),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Icon(icon, color: AccueilProfesseur.accent),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFF667085),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                    color: Color(0xFF101828),
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

class _BlocActionsProfesseur extends StatelessWidget {
  const _BlocActionsProfesseur({
    required this.enseignements,
    required this.examensCount,
  });

  final List<EnseignementProfesseurMobileDTO> enseignements;
  final int examensCount;

  @override
  Widget build(BuildContext context) {
    final apercu = enseignements.take(3).toList();

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Affectations',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            examensCount == 0
                ? 'Aucun examen charge pour la selection actuelle'
                : '$examensCount examens disponibles pour votre selection',
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
          const SizedBox(height: 12),
          if (apercu.isEmpty)
            const _InlineEmpty(
              message: 'Aucun enseignement assigne pour le moment.',
            )
          else
            ...apercu.map(
              (item) => Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: _ActionTile(
                  icon: Icons.library_books_rounded,
                  titre: item.matiereNom,
                  sousTitre: '${item.classeNom} - ${item.codeClasse}',
                  trailing: item.codeMatiere,
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  const _ActionTile({
    required this.icon,
    required this.titre,
    required this.sousTitre,
    required this.trailing,
  });

  final IconData icon;
  final String titre;
  final String sousTitre;
  final String trailing;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F8FC),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: AccueilProfesseur.accent),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  titre,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                    color: Color(0xFF101828),
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  sousTitre,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF667085),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 10),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(999),
            ),
            child: Text(
              trailing,
              style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w800,
                color: Color(0xFF5B5BEA),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _BlocActiviteProfesseur extends StatelessWidget {
  const _BlocActiviteProfesseur({
    required this.prochaineSeance,
    required this.emploi,
  });

  final SeanceProfesseurMobileDTO? prochaineSeance;
  final List<SeanceProfesseurMobileDTO> emploi;

  @override
  Widget build(BuildContext context) {
    final seance = prochaineSeance;
    final titre =
        seance == null
            ? 'Aucune prochaine seance'
            : (seance.matiereNom.isEmpty
                ? 'Seance a venir'
                : seance.matiereNom);
    final details =
        seance == null
            ? 'Votre planning sera affiche ici des qu une seance est planifiee.'
            : '${seance.jour} - ${seance.heureDebut} - ${seance.salle}';
    final resume =
        emploi.isEmpty
            ? 'Aucun element dans le planning pour le moment.'
            : '${emploi.length} seances chargees dans votre calendrier mobile.';

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF5B5BEA), Color(0xFF7474F3)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(28),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Prochaine activite',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Colors.white70,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            titre,
            style: const TextStyle(
              fontSize: 22,
              height: 1.25,
              fontWeight: FontWeight.w900,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            details,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            resume,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Colors.white70,
            ),
          ),
        ],
      ),
    );
  }
}

class _BlocPlanningProfesseur extends StatelessWidget {
  const _BlocPlanningProfesseur({required this.emploi});

  final List<SeanceProfesseurMobileDTO> emploi;

  @override
  Widget build(BuildContext context) {
    final apercu = emploi.take(3).toList();

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Planning',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 4),
          const Text(
            'Apercu rapide de votre emploi du temps',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
          const SizedBox(height: 12),
          if (apercu.isEmpty)
            const _InlineEmpty(
              message: 'Aucune seance disponible dans le planning.',
            )
          else
            ...apercu.map(
              (item) => Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: _PlanningTile(item: item),
              ),
            ),
        ],
      ),
    );
  }
}

class _PlanningTile extends StatelessWidget {
  const _PlanningTile({required this.item});

  final SeanceProfesseurMobileDTO item;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F8FC),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(
              Icons.schedule_rounded,
              color: AccueilProfesseur.accent,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.matiereNom.isEmpty ? 'Seance' : item.matiereNom,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                    color: Color(0xFF101828),
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  '${item.jour} - ${item.heureDebut}',
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF667085),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 10),
          Text(
            item.salle.isEmpty ? item.typeSeance : item.salle,
            style: const TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w800,
              color: Color(0xFF5B5BEA),
            ),
          ),
        ],
      ),
    );
  }
}

class _InlineEmpty extends StatelessWidget {
  const _InlineEmpty({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F8FC),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        message,
        textAlign: TextAlign.center,
        style: const TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w700,
          color: Color(0xFF667085),
        ),
      ),
    );
  }
}

class _InfoBanner extends StatelessWidget {
  const _InfoBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF7ED),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFF59E0B).withOpacity(0.18)),
      ),
      child: Row(
        children: [
          const Icon(Icons.info_outline_rounded, color: Color(0xFFF59E0B)),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w700,
                color: Color(0xFF92400E),
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
        mainAxisSize: MainAxisSize.min,
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

String _buildSousTitreProfil(ProfesseurProfilMobileDTO profil) {
  final values = <String>[
    if (profil.grade.isNotEmpty) profil.grade,
    if (profil.specialite.isNotEmpty) profil.specialite,
  ];
  if (values.isEmpty) {
    return 'Vue generale de vos modules et activites';
  }
  return values.join(' - ');
}
