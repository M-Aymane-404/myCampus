import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/ControleurFichier.dart';
import '../data/controller/controleur_releve_notes_etudiant.dart';
import '../data/dto/releve_notes_dto.dart';

class PageNotesEtudiant extends StatefulWidget {
  const PageNotesEtudiant({super.key, this.semestre = 'S1'});
  final String semestre;

  @override
  State<PageNotesEtudiant> createState() => _PageNotesEtudiantState();
}

class _PageNotesEtudiantState extends State<PageNotesEtudiant> {
  static const Color _bg = Color(0xFFF5F7FB);
  static const Color _primaire = Color(0xFF5B5BEA);
  static const List<String> _semestres = <String>[
    'S1',
    'S2',
    'S3',
    'S4',
    'S5',
    'S6',
  ];

  late String _semestreSelectionne;

  @override
  void initState() {
    super.initState();
    _semestreSelectionne = widget.semestre;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurReleveNotesEtudiant>().charger(
        _semestreSelectionne,
      );
    });
  }

  Future<void> _changerSemestre(String semestre) async {
    if (semestre == _semestreSelectionne) return;
    setState(() => _semestreSelectionne = semestre);
    await context.read<ControleurReleveNotesEtudiant>().charger(semestre);
  }

  Future<void> _telechargerEtOuvrirPdf({
    required BuildContext context,
    required String pathRelatif, // ex: "/copies/xxx.pdf"
    required String nomFichier,
  }) async {
    try {
      await context.read<ControleurFichier>().telechargerEtOuvrir(
        urlDownload: pathRelatif,
        nomFichier: nomFichier,
      );
      if (!context.mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Téléchargé : $nomFichier")));
    } catch (e) {
      if (!context.mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Erreur téléchargement: $e")));
    }
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurReleveNotesEtudiant>();
    final fichierCtrl = context.watch<ControleurFichier>();

    if (ctrl.chargement) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (ctrl.erreur != null) {
      return Scaffold(body: Center(child: Text('Erreur: ${ctrl.erreur}')));
    }
    final releve = ctrl.releve;
    if (releve == null) {
      return const Scaffold(body: Center(child: Text('Aucun relevé')));
    }
    final modulesPublies =
        releve.modules.where((m) {
          return m.matieres.any((mat) {
            return mat.examens.any((ex) {
              final hasNote = ex.note != null;
              final hasCorrige = (ex.corrigePath ?? '').trim().isNotEmpty;
              final hasCopie = (ex.copieEtudiantPath ?? '').trim().isNotEmpty;
              return hasNote || hasCorrige || hasCopie;
            });
          });
        }).toList();

    return Scaffold(
      backgroundColor: _bg,
      body: SafeArea(
        child: Column(
          children: [
            // âœ… barre tÃ©lÃ©chargement (comme Cours)
            if (fichierCtrl.telechargementEnCours)
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
                child: _CardBase(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Row(
                      children: [
                        SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.3,
                            value:
                                fichierCtrl.progression > 0
                                    ? fichierCtrl.progression
                                    : null,
                            color: _primaire,
                            backgroundColor: Colors.black.withOpacity(0.08),
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            "Téléchargement ¦ ${(fichierCtrl.progression * 100).toStringAsFixed(0)}%",
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              color: Color(0xFF0B1220),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),

            _HeaderNotes(
              semestre: releve.semestre,
              moyenneSession: releve.moyenneSession,
              nbValides: releve.nbModulesValider,
              nbModules: releve.nbModules,
              semestreSelectionne: _semestreSelectionne,
              semestresDisponibles: _semestres,
              onChangerSemestre: _changerSemestre,
            ),

            Expanded(
              child:
                  modulesPublies.isEmpty
                      ? Center(
                        child: Text(
                          'Aucune note publiee',
                          style: TextStyle(
                            fontWeight: FontWeight.w800,
                            color: Colors.black.withOpacity(0.55),
                          ),
                        ),
                      )
                      : ListView.separated(
                        padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                        itemCount: modulesPublies.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 10),
                        itemBuilder: (_, i) {
                          final module = modulesPublies[i];

                          // âœ… si module 1 matiÃ¨re => afficher juste le nom de la matiÃ¨re
                          if (module.matieres.length == 1) {
                            final mat = module.matieres.first;

                            return _LigneMatiere(
                              titre: mat.nomDuMatiere,
                              moyenne: mat.moyenneDuMatiere,
                              validee: mat.validee,
                              examens: mat.examens,
                              primaire: _primaire,
                              isChild: false,
                              onOuvrirCopie:
                                  (ex) => _telechargerEtOuvrirPdf(
                                    context: context,
                                    pathRelatif: ex.copieEtudiantPath!,
                                    nomFichier:
                                        "copie_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                                  ),
                              onOuvrirCorrige:
                                  (ex) => _telechargerEtOuvrirPdf(
                                    context: context,
                                    pathRelatif: ex.corrigePath!,
                                    nomFichier:
                                        "corrige_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                                  ),
                            );
                          }

                          // âœ… sinon expandable par module
                          return _CarteModuleExpandable(
                            module: module,
                            primaire: _primaire,
                            onOuvrirPdf:
                                ({
                                  required String pathRelatif,
                                  required String nomFichier,
                                }) => _telechargerEtOuvrirPdf(
                                  context: context,
                                  pathRelatif: pathRelatif,
                                  nomFichier: nomFichier,
                                ),
                          );
                        },
                      ),
            ),
          ],
        ),
      ),
    );
  }
}

class _HeaderNotes extends StatelessWidget {
  const _HeaderNotes({
    required this.semestre,
    required this.moyenneSession,
    required this.nbValides,
    required this.nbModules,
    required this.semestreSelectionne,
    required this.semestresDisponibles,
    required this.onChangerSemestre,
  });

  final String semestre;
  final double moyenneSession;
  final int nbValides;
  final int nbModules;
  final String semestreSelectionne;
  final List<String> semestresDisponibles;
  final ValueChanged<String> onChangerSemestre;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
      child: Row(
        children: [
          const Expanded(
            child: Text(
              'Notes',
              style: TextStyle(fontWeight: FontWeight.w900, fontSize: 26),
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: Colors.black.withOpacity(0.06)),
            ),
            child: PopupMenuButton<String>(
              onSelected: onChangerSemestre,
              itemBuilder:
                  (_) =>
                      semestresDisponibles
                          .map(
                            (s) =>
                                PopupMenuItem<String>(value: s, child: Text(s)),
                          )
                          .toList(),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    semestreSelectionne,
                    style: const TextStyle(fontWeight: FontWeight.w900),
                  ),
                  const SizedBox(width: 4),
                  const Icon(Icons.keyboard_arrow_down_rounded, size: 18),
                ],
              ),
            ),
          ),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                moyenneSession.toStringAsFixed(2),
                style: const TextStyle(fontWeight: FontWeight.w900),
              ),
              Text(
                '$nbValides/$nbModules',
                style: TextStyle(
                  fontWeight: FontWeight.w900,
                  color: Colors.black.withOpacity(0.6),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _CarteModuleExpandable extends StatefulWidget {
  const _CarteModuleExpandable({
    required this.module,
    required this.primaire,
    required this.onOuvrirPdf,
  });

  final ModuleNotesDTO module;
  final Color primaire;

  final Future<void> Function({
    required String pathRelatif,
    required String nomFichier,
  })
  onOuvrirPdf;

  @override
  State<_CarteModuleExpandable> createState() => _CarteModuleExpandableState();
}

class _CarteModuleExpandableState extends State<_CarteModuleExpandable> {
  bool ouvert = false;

  @override
  Widget build(BuildContext context) {
    final m = widget.module;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
        boxShadow: [
          BoxShadow(
            blurRadius: 16,
            offset: const Offset(0, 10),
            color: Colors.black.withOpacity(0.05),
          ),
        ],
      ),
      child: Column(
        children: [
          InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: () => setState(() => ouvert = !ouvert),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
              child: Row(
                children: [
                  _Badge(
                    letter: m.nomDuModule.isNotEmpty ? m.nomDuModule[0] : 'M',
                    primaire: widget.primaire,
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      m.nomDuModule,
                      style: const TextStyle(fontWeight: FontWeight.w900),
                    ),
                  ),
                  _PillMoyenne(
                    value: m.moyenneDuModule,
                    primaire: widget.primaire,
                  ),
                  const SizedBox(width: 6),
                  Icon(
                    ouvert
                        ? Icons.keyboard_arrow_up_rounded
                        : Icons.keyboard_arrow_down_rounded,
                    color: Colors.black.withOpacity(0.6),
                  ),
                ],
              ),
            ),
          ),
          if (ouvert) ...[
            Divider(height: 1, color: Colors.black.withOpacity(0.06)),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 10, 12, 12),
              child: Column(
                children: [
                  for (final mat in m.matieres) ...[
                    Builder(
                      builder: (context) {
                        return _LigneMatiere(
                          titre: mat.nomDuMatiere,
                          moyenne: mat.moyenneDuMatiere,
                          validee: mat.validee,
                          examens: mat.examens,
                          primaire: widget.primaire,
                          isChild: true,
                          onOuvrirCopie:
                              (ex) => widget.onOuvrirPdf(
                                pathRelatif: ex.copieEtudiantPath!,
                                nomFichier:
                                    "copie_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                              ),
                          onOuvrirCorrige:
                              (ex) => widget.onOuvrirPdf(
                                pathRelatif: ex.corrigePath!,
                                nomFichier:
                                    "corrige_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                              ),
                        );
                      },
                    ),
                    const SizedBox(height: 8),
                  ],
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _LigneMatiere extends StatefulWidget {
  const _LigneMatiere({
    required this.titre,
    required this.moyenne,
    required this.validee,
    required this.examens,
    required this.primaire,
    required this.isChild,
    required this.onOuvrirCopie,
    required this.onOuvrirCorrige,
  });

  final String titre;
  final double moyenne;
  final bool validee;
  final List<ExamenNotesDTO> examens;
  final Color primaire;
  final bool isChild;

  final Future<void> Function(ExamenNotesDTO ex) onOuvrirCopie;
  final Future<void> Function(ExamenNotesDTO ex) onOuvrirCorrige;

  @override
  State<_LigneMatiere> createState() => _LigneMatiereState();
}

class _LigneMatiereState extends State<_LigneMatiere> {
  bool _ouvert = false;

  @override
  Widget build(BuildContext context) {
    final examensList = [...widget.examens]
      ..sort((a, b) {
        final t = a.typeExamen.compareTo(b.typeExamen);
        if (t != 0) return t;
        return a.numeroExamen.compareTo(b.numeroExamen);
      });

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(14),
        onTap: () => setState(() => _ouvert = !_ouvert),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (widget.isChild) ...[
                Icon(
                  Icons.subdirectory_arrow_right_rounded,
                  size: 18,
                  color: widget.primaire,
                ),
                const SizedBox(width: 6),
              ],
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            widget.titre,
                            style: const TextStyle(fontWeight: FontWeight.w900),
                          ),
                        ),
                        Icon(
                          _ouvert
                              ? Icons.keyboard_arrow_up_rounded
                              : Icons.keyboard_arrow_down_rounded,
                          size: 20,
                          color: Colors.black.withOpacity(0.6),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        _EtatChip(validee: widget.validee),
                      ],
                    ),
                    if (_ouvert && examensList.isEmpty) ...[
                      const SizedBox(height: 8),
                      Text(
                        'Aucun examen',
                        style: TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Colors.black.withOpacity(0.55),
                        ),
                      ),
                    ],
                    if (_ouvert && examensList.isNotEmpty) ...[
                      const SizedBox(height: 8),
                      for (final ex in examensList) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 8,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.black.withOpacity(0.03),
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(
                              color: Colors.black.withOpacity(0.06),
                            ),
                          ),
                          child: Row(
                            children: [
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      '${ex.typeExamen} ${ex.numeroExamen}',
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w900,
                                        fontSize: 12,
                                      ),
                                    ),
                                    const SizedBox(height: 2),
                                    Text(
                                      ex.note == null
                                          ? 'Note non publiee'
                                          : 'Note: ${ex.note!.toStringAsFixed(2)}',
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: TextStyle(
                                        fontWeight: FontWeight.w800,
                                        fontSize: 11.5,
                                        color: Colors.black.withOpacity(0.60),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const SizedBox(width: 8),
                              _ActionFichier(
                                icon: Icons.picture_as_pdf_rounded,
                                enabled:
                                    (ex.copieEtudiantPath ?? '').trim().isNotEmpty,
                                onTap:
                                    (ex.copieEtudiantPath ?? '').trim().isNotEmpty
                                        ? () => widget.onOuvrirCopie(ex)
                                        : null,
                              ),
                              const SizedBox(width: 8),
                              _ActionFichier(
                                icon: Icons.task_alt_rounded,
                                enabled: (ex.corrigePath ?? '').trim().isNotEmpty,
                                onTap:
                                    (ex.corrigePath ?? '').trim().isNotEmpty
                                        ? () => widget.onOuvrirCorrige(ex)
                                        : null,
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 6),
                      ],
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 8),
              _PillMoyenne(value: widget.moyenne, primaire: widget.primaire),
            ],
          ),
        ),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  const _Badge({required this.letter, required this.primaire});
  final String letter;
  final Color primaire;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 34,
      height: 34,
      decoration: BoxDecoration(
        color: primaire.withOpacity(0.10),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Center(
        child: Text(
          letter.toUpperCase(),
          style: TextStyle(fontWeight: FontWeight.w900, color: primaire),
        ),
      ),
    );
  }
}

class _PillMoyenne extends StatelessWidget {
  const _PillMoyenne({required this.value, required this.primaire});
  final double value;
  final Color primaire;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      decoration: BoxDecoration(
        color: primaire.withOpacity(0.10),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        value.toStringAsFixed(2),
        style: TextStyle(
          fontWeight: FontWeight.w900,
          color: primaire,
          fontSize: 12,
        ),
      ),
    );
  }
}

class _EtatChip extends StatelessWidget {
  const _EtatChip({required this.validee});
  final bool validee;

  @override
  Widget build(BuildContext context) {
    final bg = validee ? const Color(0xFFE8FFF1) : const Color(0xFFFFE8E8);
    final fg = validee ? const Color(0xFF10B981) : const Color(0xFFEF4444);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        validee ? 'VALIDÉ' : 'NON VALIDÉ',
        style: TextStyle(fontWeight: FontWeight.w900, color: fg, fontSize: 11),
      ),
    );
  }
}

class _ActionFichier extends StatelessWidget {
  const _ActionFichier({
    required this.icon,
    required this.enabled,
    required this.onTap,
  });

  final IconData icon;
  final bool enabled;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      onTap: enabled ? onTap : null,
      child: Container(
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          color:
              enabled
                  ? Colors.black.withOpacity(0.04)
                  : Colors.black.withOpacity(0.02),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.black.withOpacity(0.06)),
        ),
        child: Icon(
          icon,
          size: 18,
          color:
              enabled
                  ? Colors.black.withOpacity(0.65)
                  : Colors.black.withOpacity(0.25),
        ),
      ),
    );
  }
}

class _CardBase extends StatelessWidget {
  const _CardBase({required this.child, this.padding = EdgeInsets.zero});
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
