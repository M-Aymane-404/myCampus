import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../../authentification/presentation/controleur_authentification.dart';
import '../../etudiant/presentation/AnimatedNotchBottomBar.dart';
import '../data/controller/controleur_espace_professeur.dart';
import '../data/dto/professeur_mobile_dto.dart';
import 'accueil_professeur.dart';
import 'page_cours_professeur.dart';
import 'page_presence_professeur.dart';

class ProfesseurShell extends StatefulWidget {
  const ProfesseurShell({super.key});

  @override
  State<ProfesseurShell> createState() => _ProfesseurShellState();
}

class _ProfesseurShellState extends State<ProfesseurShell> {
  static const Color _accent = Color(0xFF5B5BEA);
  int _index = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurEspaceProfesseur>().chargerEspace();
    });
  }

  @override
  Widget build(BuildContext context) {
    final profil = context.watch<ControleurEspaceProfesseur>().profil;
    final avatar = _AvatarMini(photoUrl: profil?.photoUrl, accent: _accent);
    final items = <NotchBarItem>[
      const NotchBarItem(icon: Icons.grid_view_rounded, label: 'Dashboard'),
      const NotchBarItem(icon: Icons.menu_book_rounded, label: 'Cours'),
      const NotchBarItem(
        icon: Icons.calendar_month_rounded,
        label: 'Calendrier',
      ),
      const NotchBarItem(icon: Icons.how_to_reg_rounded, label: 'Presences'),
      NotchBarItem(customIcon: avatar, label: 'Profil'),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: IndexedStack(
        index: _index,
        children: [
        const AccueilProfesseur(),
        const PageCoursProfesseur(),
          const _PageCalendrierProfesseur(),
          const PagePresenceProfesseur(),
          _PageProfilProfesseur(
            onLogout: () async {
              await context.read<ControleurAuthentification>().seDeconnecter();
            },
          ),
        ],
      ),
      bottomNavigationBar: AnimatedNotchBottomBar(
        items: items,
        currentIndex: _index,
        onTap: (i) => setState(() => _index = i),
        activeColor: _accent,
        inactiveColor: const Color(0xFF9AA3B2),
        backgroundColor: Colors.white,
        height: 82,
        notchSize: 58,
        notchRadius: 32,
      ),
    );
  }
}

class _PageCalendrierProfesseur extends StatefulWidget {
  const _PageCalendrierProfesseur();

  @override
  State<_PageCalendrierProfesseur> createState() =>
      _PageCalendrierProfesseurState();
}

class _PageCalendrierProfesseurState extends State<_PageCalendrierProfesseur> {
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

class _PageNotesProfesseur extends StatelessWidget {
  const _PageNotesProfesseur();

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    final classes = ctrl.classesExamens;
    final classeSelectionnee = _trouverClasse(
      classes,
      ctrl.codeClasseSelectionnee,
    );
    final matieres = classeSelectionnee?.matieres ?? const [];

    return _SectionScaffold(
      icon: Icons.workspace_premium_rounded,
      titreTopBar: 'Notes',
      titre: 'Examens et notes',
      sousTitre: 'Selectionnez une classe et une matiere pour voir les examens',
      child: _EtatContenuProfesseur(
        chargement: ctrl.chargement && classes.isEmpty,
        erreur: ctrl.erreur,
        estVide: classes.isEmpty,
        messageVide: 'Aucun filtre examen disponible.',
        onRetry:
            () => context.read<ControleurEspaceProfesseur>().chargerEspace(),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _SelectCard(
              label: 'Classe',
              value: ctrl.codeClasseSelectionnee,
              items:
                  classes
                      .map(
                        (c) => DropdownMenuItem<String>(
                          value: c.codeClasse,
                          child: Text(c.nomClasse),
                        ),
                      )
                      .toList(),
              onChanged: (value) {
                if (value == null) return;
                context.read<ControleurEspaceProfesseur>().selectionnerClasse(
                  value,
                );
              },
            ),
            const SizedBox(height: 10),
            _SelectCard(
              label: 'Matiere',
              value: ctrl.codeMatiereSelectionnee,
              items:
                  matieres
                      .map(
                        (m) => DropdownMenuItem<String>(
                          value: m.codeMatiere,
                          child: Text(m.nomMatiere),
                        ),
                      )
                      .toList(),
              onChanged: (value) {
                if (value == null) return;
                context.read<ControleurEspaceProfesseur>().selectionnerMatiere(
                  value,
                );
              },
            ),
            const SizedBox(height: 14),
            if (ctrl.chargement && ctrl.examens.isEmpty)
              const Center(child: CircularProgressIndicator())
            else if (ctrl.examens.isEmpty)
              const _EmptyInline(
                message: 'Aucun examen trouve pour cette matiere.',
              )
            else
              ...ctrl.examens.map(
                (examen) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: _InfoCard(
                    icon: Icons.fact_check_rounded,
                    titre: examen.label,
                    sousTitre:
                        '${examen.semestre} - ${examen.dateExamen.isEmpty ? 'Date non renseignee' : examen.dateExamen}',
                    badge: examen.etat,
                    trailing: Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          examen.nomMatiere,
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                            color: Color(0xFF101828),
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          examen.corrigeDisponible
                              ? 'Corrige disponible'
                              : 'Sans corrige',
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                            color: Color(0xFF667085),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _PageProfilProfesseur extends StatefulWidget {
  const _PageProfilProfesseur({required this.onLogout});

  final Future<void> Function() onLogout;

  @override
  State<_PageProfilProfesseur> createState() => _PageProfilProfesseurState();
}

class _PageProfilProfesseurState extends State<_PageProfilProfesseur> {
  final _formKey = GlobalKey<FormState>();
  final _telephone = TextEditingController();
  final _ancienMdp = TextEditingController();
  final _nouveauMdp = TextEditingController();

  @override
  void dispose() {
    _telephone.dispose();
    _ancienMdp.dispose();
    _nouveauMdp.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurEspaceProfesseur>();
    final profil = ctrl.profil;

    if (ctrl.chargement && profil == null) {
      return const Scaffold(
        backgroundColor: Color(0xFFF5F7FB),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (ctrl.erreur != null && profil == null) {
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

    if (profil == null) {
      return const Scaffold(
        backgroundColor: Color(0xFFF5F7FB),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_telephone.text.isEmpty && profil.tel.isNotEmpty) {
      _telephone.text = profil.tel;
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh:
              () => context.read<ControleurEspaceProfesseur>().chargerEspace(),
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 24),
            children: [
              const _SectionTitle(
                titre: 'Profil',
                sousTitre: 'Compte professeur et informations principales',
              ),
              const SizedBox(height: 18),
              if (ctrl.erreur != null) ...[
                _ErrorBanner(message: ctrl.erreur!),
                const SizedBox(height: 14),
              ],
              _ProfileHeroCard(
                profil: profil,
                chargement: ctrl.chargement,
                onChangePhoto:
                    ctrl.chargement ? null : () => _changerPhoto(context),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: Column(
                  children: [
                    _ProfileAction(
                      icon: Icons.badge_rounded,
                      titre:
                          profil.grade.isEmpty
                              ? 'Grade non renseigne'
                              : profil.grade,
                      sousTitre:
                          profil.ppr.isEmpty
                              ? 'PPR non renseigne'
                              : 'PPR ${profil.ppr}',
                    ),
                    const SizedBox(height: 10),
                    _ProfileAction(
                      icon: Icons.phone_rounded,
                      titre:
                          profil.tel.trim().isEmpty
                              ? 'Telephone non renseigne'
                              : profil.tel,
                      sousTitre: 'Contact principal',
                    ),
                    const SizedBox(height: 10),
                    _ProfileAction(
                      icon: Icons.school_rounded,
                      titre:
                          profil.specialite.isEmpty
                              ? 'Specialite non renseignee'
                              : profil.specialite,
                      sousTitre:
                          profil.codeDepartementOuTravail == null
                              ? 'Departement non renseigne'
                              : 'Departement ${profil.codeDepartementOuTravail}',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Profil et securite',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF101828),
                        ),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Modifier votre telephone et votre mot de passe',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF667085),
                        ),
                      ),
                      const SizedBox(height: 14),
                      _Field(
                        label: 'Telephone',
                        controller: _telephone,
                        keyboardType: TextInputType.phone,
                        validator: (v) {
                          final value = (v ?? '').trim();
                          if (value.isEmpty) return null;
                          if (value.length < 8) {
                            return 'Numero de telephone invalide';
                          }
                          return null;
                        },
                      ),
                      _Field(
                        label: 'Ancien mot de passe',
                        controller: _ancienMdp,
                        obscure: true,
                        validator: (v) {
                          final value = (v ?? '').trim();
                          final hasNew = _nouveauMdp.text.trim().isNotEmpty;
                          if (value.isEmpty) {
                            return hasNew ? 'Ancien mot de passe requis' : null;
                          }
                          if (value.length < 8) return 'Minimum 8 caracteres';
                          return null;
                        },
                      ),
                      _Field(
                        label: 'Nouveau mot de passe',
                        controller: _nouveauMdp,
                        obscure: true,
                        validator: (v) {
                          final value = (v ?? '').trim();
                          final hasOld = _ancienMdp.text.trim().isNotEmpty;
                          if (value.isEmpty) {
                            return hasOld
                                ? 'Nouveau mot de passe requis'
                                : null;
                          }
                          if (value.length < 8) return 'Minimum 8 caracteres';
                          return null;
                        },
                      ),
                      const SizedBox(height: 14),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed:
                              ctrl.chargement
                                  ? null
                                  : () => _enregistrer(context),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF5B5BEA),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(18),
                            ),
                          ),
                          child:
                              ctrl.chargement
                                  ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  )
                                  : const Text(
                                    'Enregistrer',
                                    style: TextStyle(
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed:
                        ctrl.chargement
                            ? null
                            : () async {
                              await widget.onLogout();
                            },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF5B5BEA),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(18),
                      ),
                    ),
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text(
                      'Se deconnecter',
                      style: TextStyle(fontWeight: FontWeight.w800),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _enregistrer(BuildContext context) async {
    if (!_formKey.currentState!.validate()) return;

    final dto = ModifierProfilProfesseurDTO(
      tel: _telephone.text.trim().isEmpty ? null : _telephone.text.trim(),
      ancienPassword:
          _ancienMdp.text.trim().isEmpty ? null : _ancienMdp.text.trim(),
      nouveauPassword:
          _nouveauMdp.text.trim().isEmpty ? null : _nouveauMdp.text.trim(),
    );

    await context.read<ControleurEspaceProfesseur>().modifierProfil(dto);

    if (!mounted) return;
    final err = context.read<ControleurEspaceProfesseur>().erreur;
    if (err == null) {
      _ancienMdp.clear();
      _nouveauMdp.clear();
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Profil mis a jour')));
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(err)));
    }
  }

  Future<void> _changerPhoto(BuildContext context) async {
    try {
      final picker = ImagePicker();
      final picked = await picker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 85,
      );
      if (picked == null || !mounted) return;

      await context.read<ControleurEspaceProfesseur>().modifierPhotoProfil(
        File(picked.path),
      );

      if (!mounted) return;
      final err = context.read<ControleurEspaceProfesseur>().erreur;
      if (err == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Photo de profil mise a jour')),
        );
      }
    } on PlatformException {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Service photo indisponible. Redemarrez completement l app puis reessayez.',
          ),
        ),
      );
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Erreur lors de la selection de la photo'),
        ),
      );
    }
  }
}

class _SectionScaffold extends StatelessWidget {
  const _SectionScaffold({
    required this.icon,
    required this.titreTopBar,
    required this.titre,
    required this.sousTitre,
    required this.child,
  });

  final IconData icon;
  final String titreTopBar;
  final String titre;
  final String sousTitre;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: _SectionTopBar(icon: icon, titre: titreTopBar),
            ),
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(18, 14, 18, 18),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  _SectionTitle(titre: titre, sousTitre: sousTitre),
                  const SizedBox(height: 14),
                  child,
                ]),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _EtatContenuProfesseur extends StatelessWidget {
  const _EtatContenuProfesseur({
    required this.chargement,
    required this.erreur,
    required this.estVide,
    required this.messageVide,
    required this.onRetry,
    required this.child,
  });

  final bool chargement;
  final String? erreur;
  final bool estVide;
  final String messageVide;
  final VoidCallback onRetry;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    if (chargement) {
      return const Center(child: CircularProgressIndicator());
    }

    if (erreur != null && estVide) {
      return _EmptyState(
        message: erreur!,
        actionLabel: 'Reessayer',
        onTap: onRetry,
      );
    }

    if (estVide) {
      return _EmptyState(message: messageVide);
    }

    return child;
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

class _EmptyInline extends StatelessWidget {
  const _EmptyInline({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
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

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.titre, required this.sousTitre});

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

class _InfoCard extends StatelessWidget {
  const _InfoCard({
    required this.icon,
    required this.titre,
    required this.sousTitre,
    required this.badge,
    this.trailing,
  });

  final IconData icon;
  final String titre;
  final String sousTitre;
  final String badge;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(22),
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
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: const Color(0xFFEEF2FF),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: const Color(0xFF5B5BEA)),
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
                const SizedBox(height: 4),
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
          trailing ??
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFFEEF2FF),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  badge,
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

class _SelectCard extends StatelessWidget {
  const _SelectCard({
    required this.label,
    required this.value,
    required this.items,
    required this.onChanged,
  });

  final String label;
  final String? value;
  final List<DropdownMenuItem<String>> items;
  final ValueChanged<String?> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
      ),
      child: DropdownButtonFormField<String>(
        value: value,
        items: items,
        onChanged: onChanged,
        decoration: InputDecoration(
          labelText: label,
          border: InputBorder.none,
          labelStyle: const TextStyle(fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

class _ProfileHeroCard extends StatelessWidget {
  const _ProfileHeroCard({
    required this.profil,
    required this.chargement,
    required this.onChangePhoto,
  });

  final ProfesseurProfilMobileDTO profil;
  final bool chargement;
  final VoidCallback? onChangePhoto;

  @override
  Widget build(BuildContext context) {
    final photoUrl = _buildPhotoUrl(profil.photoUrl);

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
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
        children: [
          CircleAvatar(
            radius: 34,
            backgroundColor: const Color(0xFFE9ECFF),
            backgroundImage: photoUrl == null ? null : NetworkImage(photoUrl),
            child:
                photoUrl == null
                    ? const Icon(
                      Icons.person_rounded,
                      size: 34,
                      color: Color(0xFF5B5BEA),
                    )
                    : null,
          ),
          const SizedBox(height: 14),
          Text(
            profil.nomComplet,
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            profil.email,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
          const SizedBox(height: 14),
          OutlinedButton.icon(
            onPressed: onChangePhoto,
            style: OutlinedButton.styleFrom(
              foregroundColor: const Color(0xFF5B5BEA),
              side: BorderSide(color: const Color(0xFF5B5BEA).withOpacity(0.2)),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
              ),
            ),
            icon:
                chargement
                    ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                    : const Icon(Icons.photo_camera_rounded),
            label: const Text(
              'Changer la photo',
              style: TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
        ],
      ),
    );
  }
}

class _SurfaceCard extends StatelessWidget {
  const _SurfaceCard({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
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
      child: child,
    );
  }
}

class _ProfileAction extends StatelessWidget {
  const _ProfileAction({
    required this.icon,
    required this.titre,
    required this.sousTitre,
  });

  final IconData icon;
  final String titre;
  final String sousTitre;

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
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: const Color(0xFF5B5BEA)),
          ),
          const SizedBox(width: 12),
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
        ],
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

class _Field extends StatelessWidget {
  const _Field({
    required this.label,
    required this.controller,
    required this.validator,
    this.obscure = false,
    this.keyboardType,
  });

  final String label;
  final TextEditingController controller;
  final String? Function(String?) validator;
  final bool obscure;
  final TextInputType? keyboardType;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: TextFormField(
        controller: controller,
        obscureText: obscure,
        keyboardType: keyboardType,
        validator: validator,
        decoration: InputDecoration(
          labelText: label,
          filled: true,
          fillColor: const Color(0xFFF7F8FC),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(18),
            borderSide: BorderSide.none,
          ),
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
      ),
    );
  }
}

String? _buildPhotoUrl(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return '${ConfigurationApp.apiBaseUrl}$photoUrl';
}

ClasseFiltreProfesseurMobileDTO? _trouverClasse(
  List<ClasseFiltreProfesseurMobileDTO> classes,
  String? codeClasse,
) {
  if (codeClasse == null) return null;
  for (final classe in classes) {
    if (classe.codeClasse == codeClasse) return classe;
  }
  return null;
}

class _AvatarMini extends StatelessWidget {
  const _AvatarMini({required this.photoUrl, required this.accent});

  final String? photoUrl;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    final url = _buildPhotoUrl(photoUrl);
    if (url != null) {
      return ClipOval(
        child: SizedBox.expand(
          child: Image.network(
            url,
            fit: BoxFit.cover,
            errorBuilder:
                (_, __, ___) => Container(
                  color: accent.withOpacity(0.12),
                  alignment: Alignment.center,
                  child: Icon(Icons.person, size: 14, color: accent),
                ),
          ),
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        color: accent.withOpacity(0.12),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Icon(Icons.person, size: 14, color: accent),
    );
  }
}
