import 'dart:io';

import 'package:flutter/material.dart';
import 'package:open_filex/open_filex.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:provider/provider.dart';

import '../../../core/storage/app_file_storage.dart';
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
  static const List<String> _semestres = <String>['S1', 'S2', 'S3', 'S4'];

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

  Future<void> _rafraichirPage() async {
    await context.read<ControleurReleveNotesEtudiant>().charger(
      _semestreSelectionne,
    );
  }

  Future<void> _telechargerEtOuvrirPdf({
    required BuildContext context,
    required String pathRelatif,
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

  Future<void> _telechargerReleveNotes(ReleveNotesDTO releve) async {
    try {
      final storage = context.read<AppFileStorage>();
      final bytes = await _construirePdfReleve(releve);
      final File file = await storage.saveBytes(
        bytes: bytes,
        fileName:
            'releve_notes_${releve.semestre}_${DateTime.now().millisecondsSinceEpoch}.pdf',
        subDir: 'mycampus/downloads',
      );

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Releve telecharge : ${file.uri.pathSegments.last}'),
          action: SnackBarAction(
            label: 'Ouvrir',
            onPressed: () => OpenFilex.open(file.path),
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Erreur export releve: $e')));
    }
  }

  Future<List<int>> _construirePdfReleve(ReleveNotesDTO releve) async {
    final doc = pw.Document();

    doc.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(24),
        footer:
            (ctx) => pw.Align(
              alignment: pw.Alignment.centerRight,
              child: pw.Text(
                'Page ${ctx.pageNumber}/${ctx.pagesCount}',
                style: pw.TextStyle(fontSize: 9, color: PdfColors.grey600),
              ),
            ),
        build: (ctx) {
          final rows = <List<String>>[];

          for (final module in releve.modules) {
            rows.add([
              (module.codeModule.trim().isEmpty
                  ? 'M'
                  : _textePdf(module.codeModule)),
              _textePdf(module.nomDuModule),
              module.moyenneDuModule.toStringAsFixed(2),
              module.moyenneDuModule >= 10 ? 'VALIDE' : 'NON VALIDE',
            ]);

            for (final matiere in module.matieres) {
              rows.add([
                '',
                '  - ${_textePdf(matiere.nomDuMatiere)}',
                matiere.moyenneDuMatiere.toStringAsFixed(2),
                matiere.validee ? 'VALIDE' : 'NON VALIDE',
              ]);
            }
          }

          return [
            pw.Text(
              'Releve de notes',
              style: pw.TextStyle(fontSize: 20, fontWeight: pw.FontWeight.bold),
            ),
            pw.SizedBox(height: 4),
            pw.Text(
              'Semestre ${_textePdf(releve.semestre)}',
              style: pw.TextStyle(fontSize: 11, color: PdfColors.grey700),
            ),
            pw.SizedBox(height: 16),
            pw.Row(
              children: [
                _kpiPdf(
                  label: 'Moyenne session',
                  value: '${releve.moyenneSession.toStringAsFixed(2)}/20',
                ),
                pw.SizedBox(width: 10),
                _kpiPdf(
                  label: 'Modules valides',
                  value: '${releve.nbModulesValider}/${releve.nbModules}',
                ),
                pw.SizedBox(width: 10),
                _kpiPdf(
                  label: 'Resultat',
                  value: releve.moyenneSession >= 10 ? 'VALIDE' : 'AJOURNE',
                ),
              ],
            ),
            pw.SizedBox(height: 16),
            if (rows.isEmpty)
              pw.Padding(
                padding: const pw.EdgeInsets.only(top: 12),
                child: pw.Text(
                  'Aucun module disponible pour ce semestre.',
                  style: pw.TextStyle(color: PdfColors.grey700, fontSize: 11),
                ),
              )
            else
              pw.TableHelper.fromTextArray(
                context: ctx,
                headers: const ['Code', 'Intitule', 'Note /20', 'Resultat'],
                data: rows,
                headerDecoration: const pw.BoxDecoration(
                  color: PdfColors.grey200,
                ),
                headerStyle: pw.TextStyle(
                  fontWeight: pw.FontWeight.bold,
                  fontSize: 10,
                  color: PdfColors.grey800,
                ),
                cellStyle: const pw.TextStyle(fontSize: 10),
                cellAlignments: const {
                  0: pw.Alignment.centerLeft,
                  1: pw.Alignment.centerLeft,
                  2: pw.Alignment.centerRight,
                  3: pw.Alignment.centerRight,
                },
                columnWidths: const {
                  0: pw.FlexColumnWidth(1),
                  1: pw.FlexColumnWidth(3.1),
                  2: pw.FlexColumnWidth(1),
                  3: pw.FlexColumnWidth(1.3),
                },
              ),
          ];
        },
      ),
    );

    return doc.save();
  }

  String _textePdf(String value) {
    return value
        .replaceAll('\uFEFF', '')
        .replaceAll('\u00A0', ' ')
        .replaceAll('\u202F', ' ')
        .replaceAll('\u2019', "'")
        .replaceAll('\u2018', "'")
        .replaceAll('\u201C', '"')
        .replaceAll('\u201D', '"')
        .replaceAll('\u2013', '-')
        .replaceAll('\u2014', '-');
  }

  pw.Widget _kpiPdf({required String label, required String value}) {
    return pw.Expanded(
      child: pw.Container(
        padding: const pw.EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: pw.BoxDecoration(
          color: PdfColors.grey100,
          border: pw.Border.all(color: PdfColors.grey300),
          borderRadius: pw.BorderRadius.circular(10),
        ),
        child: pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.start,
          children: [
            pw.Text(
              label.toUpperCase(),
              style: pw.TextStyle(
                fontSize: 9,
                color: PdfColors.grey700,
                letterSpacing: 0.6,
              ),
            ),
            pw.SizedBox(height: 6),
            pw.Text(
              value,
              style: pw.TextStyle(
                fontSize: 14,
                fontWeight: pw.FontWeight.bold,
                color: PdfColors.indigo800,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Ancien export HTML (gardé au cas où).
  // ignore: unused_element
  String _construireHtmlReleve(ReleveNotesDTO releve) {
    final moduleRows =
        releve.modules.isEmpty
            ? '<tr><td colspan="4" style="padding:24px;text-align:center;color:#64748b;">Aucun module disponible pour ce semestre.</td></tr>'
            : releve.modules.map((module) {
              final subjectRows =
                  module.matieres
                      .map(
                        (matiere) => '''
<tr style="background:#f8fafc;">
  <td></td>
  <td style="padding-left:24px;">${_escapeHtml(matiere.nomDuMatiere)}</td>
  <td style="text-align:center;">${matiere.moyenneDuMatiere.toStringAsFixed(2)}</td>
  <td></td>
</tr>''',
                      )
                      .join();

              return '''
<tr>
  <td>${_escapeHtml(module.codeModule.isEmpty ? 'M' : module.codeModule)}</td>
  <td>${_escapeHtml(module.nomDuModule)}</td>
  <td style="text-align:center;">${module.moyenneDuModule.toStringAsFixed(2)}</td>
  <td style="text-align:right;">${module.moyenneDuModule >= 10 ? 'VALIDE' : 'NON VALIDE'}</td>
</tr>
$subjectRows''';
            }).join();

    return '''<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Releve de notes ${_escapeHtml(releve.semestre)}</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 32px; color: #0f172a; background: #f8fafc; }
    .sheet { background: #ffffff; border-radius: 20px; padding: 32px; box-shadow: 0 10px 30px rgba(15,23,42,0.08); }
    .top { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; margin-bottom:24px; }
    .title { font-size: 28px; font-weight: 700; margin: 0 0 8px; }
    .subtitle { color:#475569; margin:0; }
    .summary { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:16px; margin:24px 0; }
    .card { border:1px solid #e2e8f0; border-radius:16px; padding:16px 18px; background:#f8fafc; }
    .label { font-size:12px; text-transform:uppercase; color:#64748b; letter-spacing:0.08em; margin-bottom:8px; }
    .value { font-size:26px; font-weight:700; color:#4338ca; }
    .value small { font-size:15px; color:#64748b; }
    table { width:100%; border-collapse:collapse; margin-top:8px; }
    th, td { border-bottom:1px solid #e2e8f0; padding:12px 10px; font-size:14px; }
    th { text-align:left; color:#64748b; font-size:12px; text-transform:uppercase; letter-spacing:0.06em; }
  </style>
</head>
<body>
  <div class="sheet">
    <div class="top">
      <div>
        <h1 class="title">Releve de notes</h1>
        <p class="subtitle">Semestre ${_escapeHtml(releve.semestre)}</p>
      </div>
      <div class="subtitle">MyCampus</div>
    </div>

    <div class="summary">
      <div class="card">
        <div class="label">Moyenne session</div>
        <div class="value">${releve.moyenneSession.toStringAsFixed(2)}<small>/20</small></div>
      </div>
      <div class="card">
        <div class="label">Modules valides</div>
        <div class="value">${releve.nbModulesValider}<small>/${releve.nbModules}</small></div>
      </div>
      <div class="card">
        <div class="label">Resultat global</div>
        <div class="value">${releve.moyenneSession >= 10 ? 'VALIDE' : 'AJOURNE'}</div>
      </div>
    </div>

    <table>
      <thead>
        <tr>
          <th style="width:18%;">Code</th>
          <th style="width:52%;">Intitule</th>
          <th style="width:15%; text-align:center;">Note / 20</th>
          <th style="width:15%; text-align:right;">Resultat</th>
        </tr>
      </thead>
      <tbody>
        $moduleRows
      </tbody>
    </table>
  </div>
</body>
</html>''';
  }

  // ignore: unused_element
  String _escapeHtml(String value) {
    return value
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
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
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          "Releve de notes",
          style: TextStyle(
            fontWeight: FontWeight.w900,
            color: Color(0xFF0B1220),
          ),
        ),
        actions: [
          IconButton(
            tooltip: 'Telecharger le releve',
            onPressed: () => _telechargerReleveNotes(releve),
            icon: const Icon(Icons.download_rounded),
          ),
          const SizedBox(width: 4),
        ],
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _rafraichirPage,
          child: CustomScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              SliverToBoxAdapter(
                child: Column(
                  children: [
                    // barre telechargement (comme Cours)
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
                                    backgroundColor: Colors.black.withOpacity(
                                      0.08,
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    "Telechargement | ${(fichierCtrl.progression * 100).toStringAsFixed(0)}%",
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
                      onTelechargerReleve:
                          () => _telechargerReleveNotes(releve),
                    ),
                  ],
                ),
              ),
              if (modulesPublies.isEmpty)
                SliverFillRemaining(
                  hasScrollBody: false,
                  child: Center(
                    child: Text(
                      'Aucune note publiee',
                      style: TextStyle(
                        fontWeight: FontWeight.w800,
                        color: Colors.black.withOpacity(0.55),
                      ),
                    ),
                  ),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                  sliver: SliverList.separated(
                    itemCount: modulesPublies.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (_, i) {
                      final module = modulesPublies[i];

                      // si module 1 matiere => afficher juste le nom de la matiere
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
                                pathRelatif:
                                    '/api/etudiant/examens/${ex.id}/copie',
                                nomFichier:
                                    "copie_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                              ),
                          onOuvrirCorrige:
                              (ex) => _telechargerEtOuvrirPdf(
                                context: context,
                                pathRelatif:
                                    '/api/etudiant/examens/${ex.id}/corrige',
                                nomFichier:
                                    "corrige_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                              ),
                        );
                      }

                      // sinon expandable par module
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
    required this.onTelechargerReleve,
  });

  final String semestre;
  final double moyenneSession;
  final int nbValides;
  final int nbModules;
  final String semestreSelectionne;
  final List<String> semestresDisponibles;
  final ValueChanged<String> onChangerSemestre;
  final VoidCallback onTelechargerReleve;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  'Semestre $semestreSelectionne',
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    fontSize: 24,
                    color: Color(0xFF0B1220),
                  ),
                ),
              ),
              OutlinedButton.icon(
                onPressed: onTelechargerReleve,
                icon: const Icon(Icons.download_rounded, size: 18),
                label: const Text('Telecharger'),
                style: OutlinedButton.styleFrom(
                  foregroundColor: const Color(0xFF5B5BEA),
                  side: BorderSide(color: Colors.black.withOpacity(0.08)),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 10,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _HeaderStatCard(
                  label: 'Moyenne session',
                  value: moyenneSession.toStringAsFixed(2),
                  suffix: '/20',
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _HeaderStatCard(
                  label: 'Modules valides',
                  value: '$nbValides',
                  suffix: '/$nbModules',
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Align(
            alignment: Alignment.centerRight,
            child: Container(
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
                              (s) => PopupMenuItem<String>(
                                value: s,
                                child: Text(s),
                              ),
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
          ),
        ],
      ),
    );
  }
}

class _HeaderStatCard extends StatelessWidget {
  const _HeaderStatCard({
    required this.label,
    required this.value,
    required this.suffix,
  });

  final String label;
  final String value;
  final String suffix;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
        boxShadow: [
          BoxShadow(
            blurRadius: 16,
            offset: const Offset(0, 10),
            color: Colors.black.withOpacity(0.04),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label.toUpperCase(),
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w800,
              letterSpacing: 0.6,
              color: Colors.black.withOpacity(0.52),
            ),
          ),
          const SizedBox(height: 10),
          RichText(
            text: TextSpan(
              style: const TextStyle(
                color: Color(0xFF0B1220),
                fontWeight: FontWeight.w900,
              ),
              children: [
                TextSpan(text: value, style: const TextStyle(fontSize: 24)),
                TextSpan(
                  text: suffix,
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.black.withOpacity(0.50),
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
                                pathRelatif:
                                    '/api/etudiant/examens/${ex.id}/copie',
                                nomFichier:
                                    "copie_${mat.codeMatiere}_N${ex.numeroExamen}.pdf",
                              ),
                          onOuvrirCorrige:
                              (ex) => widget.onOuvrirPdf(
                                pathRelatif:
                                    '/api/etudiant/examens/${ex.id}/corrige',
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
    final examensList = [...widget.examens]..sort((a, b) {
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
                    Row(children: [_EtatChip(validee: widget.validee)]),
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
                                    (ex.copieEtudiantPath ?? '')
                                        .trim()
                                        .isNotEmpty,
                                onTap:
                                    (ex.copieEtudiantPath ?? '')
                                            .trim()
                                            .isNotEmpty
                                        ? () => widget.onOuvrirCopie(ex)
                                        : null,
                              ),
                              const SizedBox(width: 8),
                              _ActionFichier(
                                icon: Icons.task_alt_rounded,
                                enabled:
                                    (ex.corrigePath ?? '').trim().isNotEmpty,
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
        validee ? 'validé' : 'non validé',
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
