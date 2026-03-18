import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_accueil_etudiant.dart';
import '../data/controller/controleur_annonce_etudiant.dart';
import '../data/controller/controleur_notification_etudiant.dart';
import '../data/controller/controleur_profil_etudiant.dart';
import '../data/dto/tableau_bord_etudiant_dto.dart';
import 'page_detail_annonce_etudiant.dart';
import 'page_liste_annonces_etudiant.dart';
import 'page_notifications_etudiant.dart';

class PageAccueilEtudiant extends StatefulWidget {
  const PageAccueilEtudiant({super.key});

  @override
  State<PageAccueilEtudiant> createState() => _PageAccueilEtudiantState();
}

class _PageAccueilEtudiantState extends State<PageAccueilEtudiant> {
  int _ongletFlux = 0; // 0 annonces, 1 notes
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurAccueilEtudiant>().chargerDashboard();
      context.read<ControleurProfilEtudiant>().chargerMoi();
      context.read<ControleurAnnonceEtudiant>().chargerAnnonces();
      context
          .read<ControleurNotificationEtudiant>()
          .rafraichirCompteurNonLues();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurAccueilEtudiant>();

    if (ctrl.chargement) return const _EtatChargement();

    if (ctrl.erreur != null) {
      return _EtatErreur(
        message: ctrl.erreur!,
        onRetry:
            () => context.read<ControleurAccueilEtudiant>().chargerDashboard(),
      );
    }

    final data = ctrl.dashboard;
    if (data == null) {
      return _EtatErreur(
        message: "Aucune donnÃ©e dashboard.",
        onRetry:
            () => context.read<ControleurAccueilEtudiant>().chargerDashboard(),
      );
    }

    final taux = data.assiduite.tauxPresence.round().clamp(0, 100);

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            await context.read<ControleurAccueilEtudiant>().chargerDashboard();
            await context.read<ControleurProfilEtudiant>().chargerMoi();
          },
          child: CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: _AppBarCompacte(
                  onTapNotifications: () async {
                    final ctrlNotif =
                        context.read<ControleurNotificationEtudiant>();
                    if (ctrlNotif.notifications.isEmpty) {
                      await ctrlNotif.chargerNotifications();
                    }

                    if (!context.mounted) return;
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const PageNotificationsEtudiant(),
                      ),
                    );
                  },
                ),
              ),
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(18, 14, 18, 18),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    _TitrePage(
                      titre: "Espace Etudiant",
                      sousTitre:
                          "${data.entete.niveau} an ${data.entete.session}",
                      actif: true,
                    ),
                    const SizedBox(height: 14),

                    _ResumeRow(
                      prochaineSeance: data.prochaineSeance,
                      assiduiteTaux: taux,
                      assiduiteLabel: data.assiduite.labelQualite,
                      onTapProchaineSeance:
                          () => Navigator.pushNamed(
                            context,
                            '/etudiant/calendrier',
                          ),
                      onTapAssiduite:
                          () => Navigator.pushNamed(
                            context,
                            '/etudiant/presence',
                          ),
                    ),
                    const SizedBox(height: 14),

                    _CarteFlux(
                      onglet: _ongletFlux,
                      onChange: (v) => setState(() => _ongletFlux = v),
                      annonces: data.annonces,
                      notes: data.dernieresNotes,
                      onVoirTout: () async {
                        if (_ongletFlux == 0) {
                          final ctrlAnnonce =
                              context.read<ControleurAnnonceEtudiant>();
                          if (ctrlAnnonce.annonces.isEmpty) {
                            await ctrlAnnonce.chargerAnnonces();
                          }
                          if (!context.mounted) return;
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => const PageListeAnnoncesEtudiant(),
                            ),
                          );
                          return;
                        }
                        if (!context.mounted) return;
                        Navigator.pushNamed(context, '/etudiant/notes');
                      },
                      onTapAnnonce: (a) async {
                        await context
                            .read<ControleurAnnonceEtudiant>()
                            .chargerAnnonceParId(a.id);
                        if (!context.mounted) return;
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder:
                                (_) =>
                                    PageDetailAnnonceEtudiant(annonceId: a.id),
                          ),
                        );
                      },
                    ),
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

/* =========================
   AppBar compacte + Dropdown Profil
   ========================= */

class _AppBarCompacte extends StatelessWidget {
  const _AppBarCompacte({required this.onTapNotifications});

  final VoidCallback onTapNotifications;

  @override
  Widget build(BuildContext context) {
    final ctrlNotif = context.watch<ControleurNotificationEtudiant>();
    final hasUnread = ctrlNotif.aDesNotificationsNonLues;

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
              border: Border.all(
                color: const Color(0xFF5B5BEA).withOpacity(0.18),
              ),
            ),
            child: const Icon(Icons.school_rounded, color: Color(0xFF5B5BEA)),
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Text(
              "MyCampus",
              overflow: TextOverflow.ellipsis,
              style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
            ),
          ),
          _BoutonAnnonce(hasUnread: hasUnread, onTap: onTapNotifications),
          const SizedBox(width: 10),
        ],
      ),
    );
  }
}

class _BoutonAnnonce extends StatelessWidget {
  const _BoutonAnnonce({required this.hasUnread, required this.onTap});

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
              Icons.favorite_border_rounded,
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

/* =========================
   Titre page
   ========================= */

class _TitrePage extends StatelessWidget {
  const _TitrePage({
    required this.titre,
    required this.sousTitre,
    required this.actif,
  });

  final String titre;
  final String sousTitre;
  final bool actif;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                titre,
                style: const TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.4,
                  color: Color(0xFF0B1220),
                ),
              ),
              const SizedBox(height: 6),
              Text(
                sousTitre,
                style: const TextStyle(
                  fontSize: 13,
                  color: Color(0xFF61708A),
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(999),
            border: Border.all(color: Colors.black.withOpacity(0.06)),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 9,
                height: 9,
                decoration: BoxDecoration(
                  color:
                      actif ? const Color(0xFF22C55E) : const Color(0xFFCBD5E1),
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Text(
                actif ? "Active" : "Inactive",
                style: const TextStyle(
                  fontWeight: FontWeight.w800,
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

/* =========================
   RÃ©sumÃ©: 2 cartes
   ========================= */

class _ResumeRow extends StatelessWidget {
  const _ResumeRow({
    required this.prochaineSeance,
    required this.assiduiteTaux,
    required this.assiduiteLabel,
    required this.onTapProchaineSeance,
    required this.onTapAssiduite,
  });

  final ProchaineSeanceDTO? prochaineSeance;
  final int assiduiteTaux;
  final String assiduiteLabel;
  final VoidCallback onTapProchaineSeance;
  final VoidCallback onTapAssiduite;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (_, c) {
        final stack = c.maxWidth < 520;
        final seance = prochaineSeance;

        final seanceTitre = seance?.matiereNom ?? "Aucune seance";
        final seanceHoraire = (seance == null) ? "-" : seance.horaire;
        final seanceSalle = seance?.salle ?? "-";
        final seanceJour = seance?.dayOfWeek ?? "-";

        if (stack) {
          return Column(
            children: [
              InkWell(
                borderRadius: BorderRadius.circular(18),
                onTap: onTapProchaineSeance,
                child: _CarteResumeProchaineSeance(
                  titre: seanceTitre,
                  sousTitre: "$seanceJour du $seanceHoraire au $seanceSalle",
                ),
              ),
              const SizedBox(height: 12),
              _CarteResumeAssiduite(
                taux: assiduiteTaux,
                label: assiduiteLabel,
                onTap: onTapAssiduite,
              ),
            ],
          );
        }

        return Row(
          children: [
            Expanded(
              child: InkWell(
                borderRadius: BorderRadius.circular(18),
                onTap: onTapProchaineSeance,
                child: _CarteResumeProchaineSeance(
                  titre: seanceTitre,
                  sousTitre: "$seanceJour â€¢ $seanceHoraire â€¢ $seanceSalle",
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _CarteResumeAssiduite(
                taux: assiduiteTaux,
                label: assiduiteLabel,
                onTap: onTapAssiduite,
              ),
            ),
          ],
        );
      },
    );
  }
}

class _CarteBase extends StatelessWidget {
  const _CarteBase({
    required this.child,
    this.padding = const EdgeInsets.all(14),
  });
  final Widget child;
  final EdgeInsets padding;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
        boxShadow: [
          BoxShadow(
            blurRadius: 18,
            offset: const Offset(0, 10),
            color: Colors.black.withOpacity(0.06),
          ),
        ],
      ),
      child: child,
    );
  }
}

class _CarteResumeProchaineSeance extends StatelessWidget {
  const _CarteResumeProchaineSeance({
    required this.titre,
    required this.sousTitre,
  });
  final String titre;
  final String sousTitre;

  @override
  Widget build(BuildContext context) {
    return _CarteBase(
      child: Row(
        children: [
          Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(
              color: const Color(0xFFEEF2FF),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.schedule_rounded, color: Color(0xFF5B5BEA)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Prochaine seance",
                  style: TextStyle(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 4),
                Text(
                  titre,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    color: Color(0xFF0B1220),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  sousTitre,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 12.5,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFF61708A),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 6),
          Icon(
            Icons.chevron_right_rounded,
            color: Colors.black.withOpacity(0.35),
          ),
        ],
      ),
    );
  }
}

class _CarteResumeAssiduite extends StatelessWidget {
  const _CarteResumeAssiduite({
    required this.taux,
    required this.label,
    this.onTap,
  });
  final int taux;
  final String label;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final safe = taux.clamp(0, 100);
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: _CarteBase(
        child: Row(
          children: [
            SizedBox(
              width: 54,
              height: 54,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  CircularProgressIndicator(
                    value: safe / 100.0,
                    strokeWidth: 6,
                    color: const Color(0xFF5B5BEA),
                    backgroundColor: const Color(0xFFE5E7EB),
                  ),
                  Text(
                    "$safe%",
                    style: const TextStyle(fontWeight: FontWeight.w900),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    "Taux de presence",
                    style: TextStyle(fontWeight: FontWeight.w900),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    label.toUpperCase(),
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.w800,
                      color: Color(0xFF61708A),
                    ),
                  ),
                ],
              ),
            ),
            Icon(
              Icons.chevron_right_rounded,
              color: Colors.black.withOpacity(0.35),
            ),
          ],
        ),
      ),
    );
  }
}

/* =========================
   Flux: annonces / notes
   ========================= */

class _CarteFlux extends StatelessWidget {
  const _CarteFlux({
    required this.onglet,
    required this.onChange,
    required this.annonces,
    required this.notes,
    required this.onVoirTout,
    required this.onTapAnnonce,
  });

  final int onglet;
  final ValueChanged<int> onChange;
  final List<AnnonceDTO> annonces;
  final List<NoteEtudiantDTO> notes;
  final VoidCallback onVoirTout;
  final ValueChanged<AnnonceDTO> onTapAnnonce;

  @override
  Widget build(BuildContext context) {
    final itemsCount = onglet == 0 ? annonces.length : notes.length;
    final titre = onglet == 0 ? " Annonces" : " Notes";

    return _CarteBase(
      padding: const EdgeInsets.fromLTRB(14, 14, 14, 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  titre,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    fontSize: 14.5,
                    color: Color(0xFF0B1220),
                  ),
                ),
              ),
              TextButton(
                onPressed: onVoirTout,
                style: TextButton.styleFrom(
                  foregroundColor: const Color(0xFF5B5BEA),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 6,
                  ),
                ),
                child: const Text(
                  "Voir tout",
                  style: TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          _Segmente(
            valeur: onglet,
            onChange: onChange,
            gauche: "Annonces",
            droite: "Notes",
          ),
          const SizedBox(height: 12),
          if (itemsCount == 0)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 18),
              child: Text(
                "Aucune donnee pour le moment.",
                style: TextStyle(
                  color: Colors.black.withOpacity(0.55),
                  fontWeight: FontWeight.w700,
                ),
              ),
            )
          else
            Column(
              children: [
                if (onglet == 0)
                  ...annonces
                      .take(3)
                      .map(
                        (a) => _FluxAnnonceTile(
                          annonce: a,
                          onTap: () => onTapAnnonce(a),
                        ),
                      )
                else
                  ...notes.take(3).map((n) => _FluxNoteTile(note: n)),
              ],
            ),
        ],
      ),
    );
  }
}

class _Segmente extends StatelessWidget {
  const _Segmente({
    required this.valeur,
    required this.onChange,
    required this.gauche,
    required this.droite,
  });

  final int valeur;
  final ValueChanged<int> onChange;
  final String gauche;
  final String droite;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 42,
      decoration: BoxDecoration(
        color: const Color(0xFFF4F6FB),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
      ),
      padding: const EdgeInsets.all(4),
      child: Row(
        children: [
          Expanded(
            child: _SegmenteBtn(
              actif: valeur == 0,
              label: gauche,
              onTap: () => onChange(0),
            ),
          ),
          const SizedBox(width: 6),
          Expanded(
            child: _SegmenteBtn(
              actif: valeur == 1,
              label: droite,
              onTap: () => onChange(1),
            ),
          ),
        ],
      ),
    );
  }
}

class _SegmenteBtn extends StatelessWidget {
  const _SegmenteBtn({
    required this.actif,
    required this.label,
    required this.onTap,
  });

  final bool actif;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        curve: Curves.easeOut,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: actif ? Colors.white : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          boxShadow:
              actif
                  ? [
                    BoxShadow(
                      blurRadius: 12,
                      offset: const Offset(0, 6),
                      color: Colors.black.withOpacity(0.08),
                    ),
                  ]
                  : null,
        ),
        child: Text(
          label,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(
            fontWeight: FontWeight.w900,
            color:
                actif
                    ? const Color(0xFF0B1220)
                    : Colors.black.withOpacity(0.55),
          ),
        ),
      ),
    );
  }
}

class _FluxAnnonceTile extends StatelessWidget {
  const _FluxAnnonceTile({required this.annonce, required this.onTap});
  final AnnonceDTO annonce;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final estNouveau = annonce.urgent;
    return _FluxTileBase(
      onTap: onTap,
      leading: Container(
        width: 10,
        height: 10,
        decoration: BoxDecoration(
          color: estNouveau ? const Color(0xFFEF4444) : const Color(0xFFCBD5E1),
          shape: BoxShape.circle,
        ),
      ),
      title: annonce.titre,
      subtitle: annonce.datePublication,
      trailing: Icon(
        Icons.chevron_right_rounded,
        color: Colors.black.withOpacity(0.35),
      ),
    );
  }
}

class _FluxNoteTile extends StatelessWidget {
  const _FluxNoteTile({required this.note});
  final NoteEtudiantDTO note;

  @override
  Widget build(BuildContext context) {
    return _FluxTileBase(
      onTap: () {},
      leading: Container(
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          color: const Color(0xFFEEF2FF),
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Icon(
          Icons.menu_book_outlined,
          size: 18,
          color: Color(0xFF5B5BEA),
        ),
      ),
      title: note.matiere,
      subtitle: note.datePublication,
      trailing: Text(
        note.note.toStringAsFixed(2),
        style: const TextStyle(fontWeight: FontWeight.w900),
      ),
    );
  }
}

class _FluxTileBase extends StatelessWidget {
  const _FluxTileBase({
    required this.onTap,
    required this.leading,
    required this.title,
    required this.subtitle,
    required this.trailing,
  });

  final VoidCallback onTap;
  final Widget leading;
  final String title;
  final String subtitle;
  final Widget trailing;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(14),
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        decoration: BoxDecoration(
          color: const Color(0xFFF7F9FF),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.black.withOpacity(0.04)),
        ),
        child: Row(
          children: [
            leading,
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.w900),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    subtitle,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF61708A),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            trailing,
          ],
        ),
      ),
    );
  }
}

/* =========================
   Ã‰tats
   ========================= */

class _EtatChargement extends StatelessWidget {
  const _EtatChargement();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFFF5F7FB),
      body: Center(
        child: SizedBox(
          width: 26,
          height: 26,
          child: CircularProgressIndicator(strokeWidth: 2.4),
        ),
      ),
    );
  }
}

class _EtatErreur extends StatelessWidget {
  const _EtatErreur({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline_rounded, size: 34),
              const SizedBox(height: 10),
              Text(message, textAlign: TextAlign.center),
              const SizedBox(height: 12),
              SizedBox(
                height: 46,
                child: ElevatedButton(
                  onPressed: onRetry,
                  child: const Text("RÃ©essayer"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/* =========================
   Helpers
   ========================= */

