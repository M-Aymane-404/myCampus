import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/controller/controleur_espace_professeur.dart';
import '../data/controller/controleur_notification_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'page_gestion_annonces_professeur.dart';
import 'page_mes_annonces_professeur.dart';
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
    final emploi = ctrl.emploi;
    final enseignements = ctrl.enseignements;
    final maintenant = _DashboardSchedule.findCurrent(emploi);
    final prochaine =
        ctrl.prochaineSeance ?? _DashboardSchedule.findNext(emploi);
    final seancesDuJour = _DashboardSchedule.todaySessions(emploi);
    final classesDistinctes =
        enseignements.map((item) => item.codeClasse).toSet().length;
    final matieresDistinctes =
        enseignements.map((item) => item.codeMatiere).toSet().length;

    if (ctrl.chargement &&
        profil == null &&
        enseignements.isEmpty &&
        emploi.isEmpty) {
      return const Scaffold(
        backgroundColor: AccueilProfesseur.bg,
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (ctrl.erreur != null &&
        profil == null &&
        enseignements.isEmpty &&
        emploi.isEmpty) {
      return Scaffold(
        backgroundColor: AccueilProfesseur.bg,
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: _EmptyState(
                message: ctrl.erreur!,
                actionLabel: 'Réessayer',
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
                              ? 'Dashboard Professeur'
                              : 'Bonjour ${profil.prenom.isEmpty ? 'Professeur' : profil.prenom}',
                      sousTitre:
                          profil == null
                              ? 'Vue de votre journée pédagogique'
                              : _buildSousTitreProfil(profil),
                    ),
                    const SizedBox(height: 14),
                    const _AnnonceActionCard(),
                    const SizedBox(height: 16),
                    if (ctrl.erreur != null) ...[
                      _InfoBanner(message: ctrl.erreur!),
                      const SizedBox(height: 16),
                    ],
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          flex: 3,
                          child: _SummaryCard(
                            titre: 'Prochaine séance',
                            accent: const Color(0xFFEEF2FF),
                            iconColor: AccueilProfesseur.accent,
                            icon: Icons.schedule_rounded,
                            primary:
                                prochaine?.matiereNom.isNotEmpty == true
                                    ? prochaine!.matiereNom
                                    : 'Aucune séance',
                            secondary:
                                prochaine == null
                                    ? 'Rien de planifié pour le moment'
                                    : _buildSeanceMeta(prochaine),
                            chip:
                                prochaine == null
                                    ? 'À venir'
                                    : _buildTimeRange(prochaine),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          flex: 2,
                          child: _TrackingCard(
                            classesCount: classesDistinctes,
                            matieresCount: matieresDistinctes,
                            seancesDuJour: seancesDuJour.length,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 25),
                    const _SectionHeader(
                      titre: 'Priorités du jour',
                      sousTitre:
                          'Les actions qui demandent votre attention maintenant',
                    ),
                    const SizedBox(height: 12),

                    const SizedBox(height: 14),
                    _PriorityMetricCard(
                      titre: 'Copies à corriger',
                      valeur:
                          ctrl.chargementIndicateursDashboard
                              ? '...'
                              : ctrl.copiesACorriger.toString().padLeft(2, '0'),
                      aide:
                          ctrl.copiesACorriger == 0
                              ? 'Aucune copie à corriger aujourd’hui.'
                              : '${ctrl.copiesACorriger} évaluation(s) en attente de correction.',
                      icon: Icons.fact_check_rounded,
                      iconBg: const Color(0xFFFDECEC),
                      iconColor: const Color(0xFFDC2626),
                    ),
                    const SizedBox(height: 12),
                    _PriorityMetricCard(
                      titre: 'Cours sans support',
                      valeur:
                          ctrl.chargementIndicateursDashboard
                              ? '...'
                              : ctrl.coursSansSupport.toString().padLeft(
                                2,
                                '0',
                              ),
                      aide:
                          ctrl.coursSansSupport == 0
                              ? 'Tous les cours du jour ont déjà un support.'
                              : '${ctrl.coursSansSupport} cours aujourd’hui sans support partagé.',
                      icon: Icons.menu_book_outlined,
                      iconBg: const Color(0xFFFFF5E8),
                      iconColor: const Color(0xFFEA580C),
                    ),
                    const SizedBox(height: 12),
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

class _AnnonceActionCard extends StatelessWidget {
  const _AnnonceActionCard({super.key});

  static const _description =
      'Prévenez vos étudiants d’un événement, examen ou absence importante.';

  @override
  Widget build(BuildContext context) {
    const borderRadius = 28.0;
    const buttonHeight = 46.0;
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AccueilProfesseur.accent.withOpacity(0.10), Colors.white],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        color: Colors.white,
        borderRadius: BorderRadius.circular(borderRadius),
        border: Border.all(color: AccueilProfesseur.accent.withOpacity(0.14)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 18,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF5B5BEA), Color(0xFF8A8BFF)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: [
                      BoxShadow(
                        color: AccueilProfesseur.accent.withOpacity(0.22),
                        blurRadius: 14,
                        offset: const Offset(0, 8),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.campaign_rounded,
                    color: Colors.white,
                    size: 22,
                  ),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Annonces',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w800,
                          color: Color(0xFF667085),
                        ),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Créer une annonce',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 18,
                          height: 1.2,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF101828),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            const Text(
              _description,
              style: TextStyle(
                fontSize: 13,
                height: 1.35,
                fontWeight: FontWeight.w600,
                color: Color(0xFF667085),
              ),
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: buttonHeight,
                    child: ElevatedButton(
                      onPressed: () => _ouvrirCreation(context),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AccueilProfesseur.accent,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        minimumSize: const Size.fromHeight(buttonHeight),
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        textStyle: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w800,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                      ),
                      child: const Center(
                        child: FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Text(
                            'Créer une annonce',
                            maxLines: 1,
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: SizedBox(
                    height: buttonHeight,
                    child: OutlinedButton(
                      onPressed: () => _ouvrirMesAnnonces(context),
                      style: OutlinedButton.styleFrom(
                        side: BorderSide(
                          color: AccueilProfesseur.accent.withOpacity(0.32),
                        ),
                        foregroundColor: AccueilProfesseur.accent,
                        backgroundColor: AccueilProfesseur.accent.withOpacity(
                          0.06,
                        ),
                        minimumSize: const Size.fromHeight(buttonHeight),
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        textStyle: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w800,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                      ),
                      child: const Center(
                        child: FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Text(
                            'Voir mes annonces',
                            maxLines: 1,
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _ouvrirCreation(BuildContext context) async {
    if (!context.mounted) return;
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PageGestionAnnoncesProfesseur()),
    );
  }

  Future<void> _ouvrirMesAnnonces(BuildContext context) async {
    final ctrl = context.read<ControleurAnnonceProfesseur>();
    if (!ctrl.chargement && ctrl.annoncesPubliques.isEmpty) {
      await ctrl.chargerAnnoncesPubliques();
    }
    if (!context.mounted) return;
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PageMesAnnoncesProfesseur()),
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
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w900,
            color: Color(0xFF101828),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          sousTitre,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
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

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({
    required this.titre,
    required this.icon,
    required this.primary,
    required this.secondary,
    required this.chip,
    required this.accent,
    required this.iconColor,
  });

  final String titre;
  final IconData icon;
  final String primary;
  final String secondary;
  final String chip;
  final Color accent;
  final Color iconColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: _panelDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: accent,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(icon, color: iconColor, size: 22),
              ),
              const Spacer(),
              Flexible(
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: iconColor.withOpacity(0.10),
                    border: Border.all(color: iconColor.withOpacity(0.18)),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    chip,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w800,
                      color: iconColor,
                    ),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            titre,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w800,
              color: Color(0xFF667085),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            primary,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 18,
              height: 1.2,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            secondary,
            maxLines: 3,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 12,
              height: 1.35,
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
        ],
      ),
    );
  }
}

class _TrackingCard extends StatelessWidget {
  const _TrackingCard({
    required this.classesCount,
    required this.matieresCount,
    required this.seancesDuJour,
  });

  final int classesCount;
  final int matieresCount;
  final int seancesDuJour;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: _panelDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: const Color(0xFFEEF8FF),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(
                  Icons.insights_rounded,
                  color: Color(0xFF0284C7),
                  size: 22,
                ),
              ),
              const Spacer(),
              const Flexible(child: _StatistiquesBadge()),
            ],
          ),
          const SizedBox(height: 14),
          _TrackingLine(
            label: 'Classes',
            value: classesCount.toString().padLeft(2, '0'),
          ),
          const SizedBox(height: 10),
          _TrackingLine(
            label: 'Matières',
            value: matieresCount.toString().padLeft(2, '0'),
          ),
          const SizedBox(height: 10),
          _TrackingLine(
            label: 'Séances du jour',
            value: seancesDuJour.toString().padLeft(2, '0'),
          ),
        ],
      ),
    );
  }
}

class _StatistiquesBadge extends StatelessWidget {
  const _StatistiquesBadge();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: const Color(0xFFF3F5FA),
        borderRadius: BorderRadius.circular(999),
      ),
      child: const Text(
        'Statistiques',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w800,
          color: Color(0xFF344054),
        ),
      ),
    );
  }
}

class _TrackingLine extends StatelessWidget {
  const _TrackingLine({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.baseline,
      textBaseline: TextBaseline.alphabetic,
      children: [
        Expanded(
          child: Text(
            label,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Color(0xFF667085),
            ),
          ),
        ),
        Text(
          value,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w900,
            color: Color(0xFF101828),
          ),
        ),
      ],
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.titre, required this.sousTitre});

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
            fontSize: 20,
            fontWeight: FontWeight.w900,
            color: Color(0xFF101828),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          sousTitre,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w600,
            color: Color(0xFF667085),
          ),
        ),
      ],
    );
  }
}

class _PriorityPill extends StatelessWidget {
  const _PriorityPill({
    required this.label,
    required this.subtitle,
    required this.value,
    required this.icon,
    required this.tooltip,
    required this.color,
    required this.background,
  });

  final String label;
  final String subtitle;
  final String value;
  final IconData icon;
  final String tooltip;
  final Color color;
  final Color background;

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: tooltip,
      waitDuration: const Duration(milliseconds: 450),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: background,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: color.withOpacity(0.16)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, color: color, size: 18),
                const Spacer(),
                Text(
                  value,
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w900,
                    color: color,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              label,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 12,
                height: 1.2,
                fontWeight: FontWeight.w800,
                color: Color(0xFF101828),
              ),
            ),
            const SizedBox(height: 2),
            Text(
              subtitle,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 10.5,
                height: 1.2,
                fontWeight: FontWeight.w700,
                color: Color(0xFF667085),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PriorityMetricCard extends StatelessWidget {
  const _PriorityMetricCard({
    required this.titre,
    required this.valeur,
    required this.aide,
    required this.icon,
    required this.iconBg,
    required this.iconColor,
  });

  final String titre;
  final String valeur;
  final String aide;
  final IconData icon;
  final Color iconBg;
  final Color iconColor;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: _panelDecoration(),
      child: Row(
        children: [
          Container(
            width: 54,
            height: 54,
            decoration: BoxDecoration(
              color: iconBg,
              borderRadius: BorderRadius.circular(18),
            ),
            child: Icon(icon, color: iconColor),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  titre,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w800,
                    color: Color(0xFF101828),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  aide,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF667085),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Text(
            valeur,
            style: const TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
        ],
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

class _DashboardSchedule {
  static SeanceProfesseurMobileDTO? findCurrent(
    List<SeanceProfesseurMobileDTO> seances,
  ) {
    final now = DateTime.now();
    final currentMinutes = now.hour * 60 + now.minute;

    for (final seance in seances) {
      if (!matchesToday(now, seance.jour)) continue;
      final start = startMinutes(seance);
      final end = endMinutes(seance);
      if (start == null || end == null) continue;
      if (currentMinutes >= start && currentMinutes < end) {
        return seance;
      }
    }
    return null;
  }

  static SeanceProfesseurMobileDTO? findNext(
    List<SeanceProfesseurMobileDTO> seances,
  ) {
    final now = DateTime.now();
    final currentMinutes = now.hour * 60 + now.minute;
    SeanceProfesseurMobileDTO? candidate;
    var candidateWeight = 1 << 30;

    for (final seance in seances) {
      final dayDistance = _dayDistance(now, seance.jour);
      if (dayDistance == null) continue;
      final start = startMinutes(seance);
      if (start == null) continue;

      final adjustedDayDistance =
          dayDistance == 0 && start <= currentMinutes ? 7 : dayDistance;
      final weight = adjustedDayDistance * 1440 + start;
      if (weight < candidateWeight) {
        candidateWeight = weight;
        candidate = seance;
      }
    }

    return candidate;
  }

  static List<SeanceProfesseurMobileDTO> todaySessions(
    List<SeanceProfesseurMobileDTO> seances,
  ) {
    final now = DateTime.now();
    return seances.where((item) => matchesToday(now, item.jour)).toList();
  }

  static bool matchesToday(DateTime now, String jour) {
    final normalized = _normalizeDay(jour);
    if (normalized.isEmpty) return false;
    return normalized == _weekdayKey(now.weekday);
  }

  static int? startMinutes(SeanceProfesseurMobileDTO seance) =>
      _parseMinutes(seance.heureDebut);

  static int? endMinutes(SeanceProfesseurMobileDTO seance) =>
      _parseMinutes(seance.heureFin);

  static int? _dayDistance(DateTime now, String jour) {
    final target = _dayNumber(_normalizeDay(jour));
    if (target == null) return null;
    return (target - now.weekday + 7) % 7;
  }

  static int? _parseMinutes(String raw) {
    final value = raw.trim();
    if (value.isEmpty) return null;
    final parts = value.split(':');
    if (parts.length < 2) return null;
    final hours = int.tryParse(parts[0]);
    final minutes = int.tryParse(parts[1]);
    if (hours == null || minutes == null) return null;
    return (hours * 60) + minutes;
  }

  static String _normalizeDay(String value) {
    switch (value.trim().toUpperCase()) {
      case 'MONDAY':
      case 'MON':
      case 'LUN':
      case 'LUNDI':
        return 'LUNDI';
      case 'TUESDAY':
      case 'TUE':
      case 'MAR':
      case 'MARDI':
        return 'MARDI';
      case 'WEDNESDAY':
      case 'WED':
      case 'MER':
      case 'MERCREDI':
        return 'MERCREDI';
      case 'THURSDAY':
      case 'THU':
      case 'JEU':
      case 'JEUDI':
        return 'JEUDI';
      case 'FRIDAY':
      case 'FRI':
      case 'VEN':
        return 'VENDREDI';
      case 'VENDREDI':
        return 'VENDREDI';
      case 'SATURDAY':
      case 'SAT':
      case 'SAM':
      case 'SAMEDI':
        return 'SAMEDI';
      case 'SUNDAY':
      case 'SUN':
      case 'DIM':
      case 'DIMANCHE':
        return 'DIMANCHE';
      default:
        return value.trim().toUpperCase();
    }
  }

  static String _weekdayKey(int weekday) {
    return switch (weekday) {
      DateTime.monday => 'LUNDI',
      DateTime.tuesday => 'MARDI',
      DateTime.wednesday => 'MERCREDI',
      DateTime.thursday => 'JEUDI',
      DateTime.friday => 'VENDREDI',
      DateTime.saturday => 'SAMEDI',
      DateTime.sunday => 'DIMANCHE',
      _ => '',
    };
  }

  static int? _dayNumber(String normalized) {
    return switch (normalized) {
      'LUNDI' => DateTime.monday,
      'MARDI' => DateTime.tuesday,
      'MERCREDI' => DateTime.wednesday,
      'JEUDI' => DateTime.thursday,
      'VENDREDI' => DateTime.friday,
      'SAMEDI' => DateTime.saturday,
      'DIMANCHE' => DateTime.sunday,
      _ => null,
    };
  }
}

BoxDecoration _panelDecoration() {
  return BoxDecoration(
    color: Colors.white,
    borderRadius: BorderRadius.circular(28),
    border: Border.all(color: Colors.black.withOpacity(0.05)),
    boxShadow: [
      BoxShadow(
        color: Colors.black.withOpacity(0.04),
        blurRadius: 16,
        offset: const Offset(0, 8),
      ),
    ],
  );
}

String _buildSousTitreProfil(ProfesseurProfilMobileDTO profil) {
  final values = <String>[
    if (profil.grade.isNotEmpty) profil.grade,
    if (profil.specialite.isNotEmpty) profil.specialite,
  ];
  if (values.isEmpty) {
    return 'Vue de votre journée pédagogique';
  }
  return values.join(' - ');
}

String _buildSeanceMeta(SeanceProfesseurMobileDTO seance) {
  final values = <String>[
    if (seance.classeNom.isNotEmpty) seance.classeNom else seance.codeClasse,
    if (seance.salle.isNotEmpty) 'Salle ${seance.salle}',
    _buildTimeRange(seance),
  ];
  return values.where((item) => item.trim().isNotEmpty).join(' - ');
}

String _buildTimeRange(SeanceProfesseurMobileDTO seance) {
  final debut = seance.heureDebut.trim();
  final fin = seance.heureFin.trim();
  if (debut.isEmpty && fin.isEmpty) return seance.jour;
  if (fin.isEmpty) return debut;
  if (debut.isEmpty) return fin;
  return '$debut - $fin';
}
