import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../data/controller/ControleurFichier.dart';
import '../data/controller/controleur_cours_etudiant.dart';
import '../data/controller/controleur_profil_etudiant.dart';
import '../data/dto/cours_etudiant_dto.dart';

class PageCoursEtudiant extends StatefulWidget {
  const PageCoursEtudiant({super.key});

  @override
  State<PageCoursEtudiant> createState() => _PageCoursEtudiantState();
}

class _PageCoursEtudiantState extends State<PageCoursEtudiant> {
  final TextEditingController _rechercheController = TextEditingController();

  // ✅ Palette identique Dashboard
  static const Color _bg = Color(0xFFF5F7FB);
  static const Color _accent = Color(0xFF5B5BEA);
  static const Color _textDark = Color(0xFF0B1220);
  //static const Color _textMuted = Color(0xFF61708A);

  @override
  void dispose() {
    _rechercheController.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final ctrl = context.read<ControleurCoursEtudiant>();
      if (ctrl.modules.isEmpty && !ctrl.chargement) {
        ctrl.chargerModules();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final coursCtrl = context.watch<ControleurCoursEtudiant>();
    final profilCtrl = context.watch<ControleurProfilEtudiant>();
    final fichierCtrl = context.watch<ControleurFichier>();

    //  final photoUrl = profilCtrl.moi?.photoUrl;
    final prenom = profilCtrl.moi?.prenom ?? '';
    final nom = profilCtrl.moi?.nom ?? '';
    //  final initiales = _initialesPrenomNom(prenom, nom);

    if (coursCtrl.chargement) return const _EtatChargement();
    if (coursCtrl.erreur != null) {
      return _EtatErreur(
        message: coursCtrl.erreur!,
        onRetry: () => coursCtrl.reinitialiser(),
      );
    }

    return Scaffold(
      backgroundColor: _bg,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () => coursCtrl.reinitialiser(),
          child: CustomScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              // ✅ Téléchargement état global
              if (fichierCtrl.telechargementEnCours)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(18, 10, 18, 0),
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
                                color: _accent,
                                backgroundColor: Colors.black.withOpacity(0.08),
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                "Téléchargement… ${(fichierCtrl.progression * 100).toStringAsFixed(0)}%",
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w900,
                                  color: _textDark,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),

              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(18, 12, 18, 10),
                  child: _FiltersCard(
                    coursCtrl: coursCtrl,
                    rechercheController: _rechercheController,
                    onSearchChanged: coursCtrl.setRecherche,
                    onPickModule: () => _pickModule(context, coursCtrl),
                    onPickMatiere: () => _pickMatiere(context, coursCtrl),
                    onPickSeance: () => _pickSeance(context, coursCtrl),
                  ),
                ),
              ),

              SliverPadding(
                padding: const EdgeInsets.fromLTRB(18, 0, 18, 24),
                sliver: _ListeRessources(
                  ressources: coursCtrl.ressourcesFiltrees,
                  onTelecharger: (f) => _telechargerFichier(context, f),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // -----------------------
  // PICKERS
  // -----------------------

  Future<void> _pickModule(
    BuildContext context,
    ControleurCoursEtudiant ctrl,
  ) async {
    final selected = await _showPickerSheet<ModuleSortieEtudiantDTO>(
      context: context,
      title: "Choisir un module",
      items: ctrl.modules,
      selected: ctrl.moduleSelectionne,
      label: (m) => m.nom,
      equals: (a, b) => a.codeModule == b.codeModule,
    );
    if (selected != null) ctrl.selectionnerModule(selected);
  }

  Future<void> _pickMatiere(
    BuildContext context,
    ControleurCoursEtudiant ctrl,
  ) async {
    if (ctrl.matieres.isEmpty) {
      _toast(context, "Choisis d’abord un module");
      return;
    }

    final selected = await _showPickerSheet<MatiereSortieEtudiantDTO>(
      context: context,
      title: "Choisir une matière",
      items: ctrl.matieres,
      selected: ctrl.matiereSelectionnee,
      label: (m) => m.nom,
      equals: (a, b) => a.codeMatiere == b.codeMatiere,
    );
    if (selected != null) ctrl.selectionnerMatiere(selected);
  }

  Future<void> _pickSeance(
    BuildContext context,
    ControleurCoursEtudiant ctrl,
  ) async {
    if (ctrl.seances.isEmpty) {
      _toast(context, "Aucune séance disponible");
      return;
    }

    final selected = await _showPickerSheet<SeanceSortieEtudiantDTO>(
      context: context,
      title: "Choisir une séance",
      items: ctrl.seances,
      selected: ctrl.seanceSelectionnee,
      label: (s) => _labelSeance(s),
      equals: (a, b) => a.id == b.id,
    );
    if (selected != null) ctrl.selectionnerSeance(selected);
  }

  Future<T?> _showPickerSheet<T>({
    required BuildContext context,
    required String title,
    required List<T> items,
    required T? selected,
    required String Function(T) label,
    required bool Function(T a, T b) equals,
  }) {
    return showModalBottomSheet<T>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      builder: (_) {
        return FractionallySizedBox(
          heightFactor: 0.8,
          child: Container(
            decoration: const BoxDecoration(
              color: _bg,
              borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
            ),
            child: Column(
              children: [
                const SizedBox(height: 10),
                Container(
                  width: 44,
                  height: 5,
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
                const SizedBox(height: 10),
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 6, 16, 10),
                  child: Row(
                    children: [
                      Expanded(
                        child: Text(
                          title,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w900,
                            color: _textDark,
                          ),
                        ),
                      ),
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(),
                        style: TextButton.styleFrom(foregroundColor: _accent),
                        child: const Text("Fermer"),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Container(
                    color: Colors.white,
                    child: ListView.separated(
                      itemCount: items.length,
                      separatorBuilder:
                          (_, __) => Divider(
                            height: 1,
                            thickness: 1,
                            color: Colors.black.withOpacity(0.05),
                          ),
                      itemBuilder: (context, i) {
                        final it = items[i];
                        final isSel =
                            (selected != null) && equals(it, selected);
                        return ListTile(
                          title: Text(
                            label(it),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                              fontWeight:
                                  isSel ? FontWeight.w900 : FontWeight.w700,
                              color: _textDark,
                            ),
                          ),
                          trailing:
                              isSel
                                  ? const Icon(
                                    Icons.check_rounded,
                                    color: _accent,
                                  )
                                  : null,
                          onTap: () => Navigator.of(context).pop(it),
                        );
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 10),
              ],
            ),
          ),
        );
      },
    );
  }

  // -----------------------
  // DOWNLOAD
  // -----------------------
  Future<void> _telechargerFichier(
    BuildContext context,
    FichierSeanceSortieEtudiantDTO fichier,
  ) async {
    try {
      final urlDownload =
          "/api/etudiant/fichiers-seance/${fichier.id}/telecharger";

      await context.read<ControleurFichier>().telechargerEtOuvrir(
        urlDownload: urlDownload,
        nomFichier: fichier.nomOriginal,
      );

      if (!context.mounted) return;
      _toast(context, "Téléchargé : ${fichier.nomOriginal}");
    } catch (e) {
      if (!context.mounted) return;
      _toast(context, "Erreur téléchargement: $e");
    }
  }
}

/* =========================
   TopBar Dashboard + Dropdown Profil
   ========================= */

/* =========================
   Filters card
   ========================= */

class _FiltersCard extends StatelessWidget {
  const _FiltersCard({
    required this.coursCtrl,
    required this.rechercheController,
    required this.onSearchChanged,
    required this.onPickModule,
    required this.onPickMatiere,
    required this.onPickSeance,
  });

  final ControleurCoursEtudiant coursCtrl;
  final TextEditingController rechercheController;
  final ValueChanged<String> onSearchChanged;
  final VoidCallback onPickModule;
  final VoidCallback onPickMatiere;
  final VoidCallback onPickSeance;

  @override
  Widget build(BuildContext context) {
    final moduleLabel = coursCtrl.moduleSelectionne?.nom ?? "Choisir";
    final matiereLabel = coursCtrl.matiereSelectionnee?.nom ?? "Choisir";
    final seanceLabel =
        (coursCtrl.seanceSelectionnee == null)
            ? "Choisir"
            : _labelSeance(coursCtrl.seanceSelectionnee!);

    return _CardBase(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            _PickerTile(
              title: "Module",
              value: moduleLabel,
              onTap: onPickModule,
            ),
            const SizedBox(height: 10),
            _PickerTile(
              title: "Matière",
              value: matiereLabel,
              onTap: onPickMatiere,
            ),
            const SizedBox(height: 12),
            _SegmentMinimal(
              value: coursCtrl.filtreTypeSeance,
              onChanged: coursCtrl.changerFiltreTypeSeance,
            ),
            const SizedBox(height: 12),
            _PickerTile(
              title: "Séance",
              value: seanceLabel,
              onTap: onPickSeance,
            ),
            const SizedBox(height: 12),
            _SearchField(
              controller: rechercheController,
              onChanged: onSearchChanged,
            ),
          ],
        ),
      ),
    );
  }
}

class _PickerTile extends StatelessWidget {
  const _PickerTile({
    required this.title,
    required this.value,
    required this.onTap,
  });

  final String title;
  final String value;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final subtle = Colors.black.withOpacity(0.55);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        decoration: BoxDecoration(
          color: const Color(0xFFF4F6FB),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.black.withOpacity(0.06)),
        ),
        child: Row(
          children: [
            SizedBox(
              width: 66,
              child: Text(
                title,
                style: TextStyle(fontWeight: FontWeight.w800, color: subtle),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                value,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontWeight: FontWeight.w900,
                  color: Color(0xFF0B1220),
                ),
              ),
            ),
            const SizedBox(width: 6),
            Icon(Icons.chevron_right_rounded, color: subtle),
          ],
        ),
      ),
    );
  }
}

class _SegmentMinimal extends StatelessWidget {
  const _SegmentMinimal({required this.value, required this.onChanged});

  final String value;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    const children = <String, Widget>{
      'TOUS': Padding(
        padding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        child: Text('Tous'),
      ),
      'COURS': Padding(
        padding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        child: Text('Cours'),
      ),
      'TD': Padding(
        padding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        child: Text('TD'),
      ),
      'TP': Padding(
        padding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        child: Text('TP'),
      ),
    };

    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFF4F6FB),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.black.withOpacity(0.06)),
      ),
      padding: const EdgeInsets.all(6),
      child: CupertinoSlidingSegmentedControl<String>(
        groupValue: value,
        children: children,
        onValueChanged: (v) {
          if (v != null) onChanged(v);
        },
        thumbColor: const Color(0xFF5B5BEA),
        backgroundColor: const Color(0xFFF4F6FB),
      ),
    );
  }
}

class _SearchField extends StatelessWidget {
  const _SearchField({required this.controller, required this.onChanged});

  final TextEditingController controller;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      onChanged: onChanged,
      decoration: InputDecoration(
        hintText: "Rechercher une ressource…",
        prefixIcon: const Icon(Icons.search_rounded),
        filled: true,
        fillColor: const Color(0xFFF4F6FB),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: Colors.black.withOpacity(0.06)),
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 12,
          vertical: 12,
        ),
      ),
    );
  }
}

/* =========================
   Liste ressources
   ========================= */

class _ListeRessources extends StatelessWidget {
  const _ListeRessources({
    required this.ressources,
    required this.onTelecharger,
  });

  final List<FichierSeanceSortieEtudiantDTO> ressources;
  final ValueChanged<FichierSeanceSortieEtudiantDTO> onTelecharger;

  @override
  Widget build(BuildContext context) {
    if (ressources.isEmpty) {
      return SliverToBoxAdapter(
        child: Padding(
          padding: const EdgeInsets.only(top: 8),
          child: _CardBase(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    Icons.folder_open_rounded,
                    color: Colors.black.withOpacity(0.35),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      "Aucune ressource",
                      style: TextStyle(
                        color: Colors.black.withOpacity(0.55),
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
    }

    return SliverList.separated(
      itemCount: ressources.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, i) {
        final f = ressources[i];
        final dl = context.watch<ControleurFichier>();

        return _CardBase(
          child: ListTile(
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 14,
              vertical: 6,
            ),
            leading: Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: const Color(0xFFEEF2FF),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.picture_as_pdf_rounded,
                color: Color(0xFF5B5BEA),
              ),
            ),
            title: Text(
              f.nomOriginal,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontWeight: FontWeight.w900,
                color: Color(0xFF0B1220),
              ),
            ),
            subtitle: Text(
              "${f.tailleFormatee} • ${f.dateUpload}",
              style: const TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w700,
                color: Color(0xFF61708A),
              ),
            ),
            trailing:
                dl.telechargementEnCours
                    ? SizedBox(
                      width: 34,
                      height: 34,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.4,
                        value: dl.progression > 0 ? dl.progression : null,
                        color: const Color(0xFF5B5BEA),
                        backgroundColor: Colors.black.withOpacity(0.08),
                      ),
                    )
                    : IconButton(
                      onPressed: () => onTelecharger(f),
                      icon: const Icon(
                        Icons.download_rounded,
                        color: Color(0xFF5B5BEA),
                      ),
                    ),
          ),
        );
      },
    );
  }
}

/* =========================
   Card Base
   ========================= */

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

/* =========================
   States
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
          child: _CardBase(
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
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: onRetry,
                      child: const Text("Réessayer"),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/* =========================
   Helpers
   ========================= */

String _initialesPrenomNom(String prenom, String nom) {
  final p = prenom.trim();
  final n = nom.trim();

  String firstLetter(String s) =>
      s.isEmpty ? '' : s.substring(0, 1).toUpperCase();

  final res = (firstLetter(p) + firstLetter(n)).trim();
  return res.isEmpty ? "U" : res;
}

String? _construireUrlPhoto(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return "${ConfigurationApp.apiBaseUrl}$photoUrl";
}

String _labelSeance(SeanceSortieEtudiantDTO s) {
  final type = (s.type.name).toUpperCase(); // COURS/TD/TP
  final date = (s.date == null || s.date!.isEmpty) ? "Date ?" : s.date!;

  final salle = (s.salle == null || s.salle!.isEmpty) ? "Salle ?" : s.salle!;
  return "$type • $date • $salle";
}

void _toast(BuildContext context, String msg) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
}
