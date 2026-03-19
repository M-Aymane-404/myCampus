import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../etudiant/data/controller/ControleurFichier.dart';
import '../../data/controller/controleur_cours_professeur.dart';
import '../../data/dto/creation_examen_professeur_dto.dart';
import '../../data/dto/creation_seance_professeur_dto.dart';
import '../../data/dto/professeur_mobile_dto.dart';

const Color _kAccent = Color(0xFF5B5BEA);

ButtonStyle _primaryCtaButtonStyle({bool compact = false}) {
  final height = compact ? 40.0 : 46.0;
  return ElevatedButton.styleFrom(
    backgroundColor: _kAccent,
    foregroundColor: Colors.white,
    elevation: 0,
    minimumSize: Size(0, height),
    padding: const EdgeInsets.symmetric(horizontal: 14),
    textStyle: const TextStyle(fontSize: 13, fontWeight: FontWeight.w800),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
  );
}

Widget _ctaLabel(String text) {
  return Center(
    child: FittedBox(
      fit: BoxFit.scaleDown,
      child: Text(
        text,
        maxLines: 1,
        textAlign: TextAlign.center,
      ),
    ),
  );
}

class ActionRow extends StatelessWidget {
  const ActionRow({required this.title, required this.buttonLabel, this.onTap});

  final String title;
  final String buttonLabel;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                color: Color(0xFF0B1220),
              ),
            ),
          ),
          ElevatedButton(
            onPressed: onTap,
            style: _primaryCtaButtonStyle(compact: true),
            child: _ctaLabel(buttonLabel),
          ),
        ],
      ),
    );
  }
}

class SummaryChip extends StatelessWidget {
  const SummaryChip({required this.label, required this.value});

  final String label;
  final int value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(999),
        color: Colors.white,
        border: Border.all(color: const Color(0xFFEEF2FF)),
      ),
      child: Row(
        children: [
          Text(
            value.toString(),
            style: const TextStyle(
              fontWeight: FontWeight.w900,
              color: Color(0xFF0B1220),
            ),
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: const TextStyle(
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
        ],
      ),
    );
  }
}

class FieldWithPicker extends StatelessWidget {
  const FieldWithPicker({
    required this.label,
    required this.controller,
    required this.onTap,
  });

  final String label;
  final TextEditingController controller;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      readOnly: true,
      onTap: onTap,
      decoration: InputDecoration(
        labelText: label,
        border: const OutlineInputBorder(),
        suffixIcon: const Icon(Icons.calendar_today_rounded),
      ),
      validator:
          (value) => (value ?? '').trim().isEmpty ? 'Champ requis' : null,
    );
  }
}

class SessionCard extends StatelessWidget {
  const SessionCard({
    required this.seance,
    this.onVoirRessources,
    this.onUploaderSupport,
    this.showActions = true,
    this.isUploading = false,
  });

  final SeanceProfesseurMobileDTO seance;
  final VoidCallback? onVoirRessources;
  final VoidCallback? onUploaderSupport;
  final bool showActions;
  final bool isUploading;

  @override
  Widget build(BuildContext context) {
    final dateLabel = _formatDateJourMoisAnnee(seance.jour);
    final timeLabel = _formatTimeRange(seance.heureDebut, seance.heureFin);
    final metaParts = <String>[
      if (dateLabel.isNotEmpty) dateLabel,
      if (timeLabel.isNotEmpty) timeLabel,
      if (seance.classeNom.isNotEmpty) seance.classeNom,
    ];

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFF5B5BEA).withOpacity(0.08)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                seance.matiereNom,
                style: const TextStyle(fontWeight: FontWeight.w900),
              ),
              Text(
                seance.typeSeance,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: Color(0xFF5B5BEA),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            metaParts.join(' · '),
            style: const TextStyle(color: Color(0xFF667085)),
          ),
          const SizedBox(height: 4),
          Text(
            seance.salle.isEmpty ? 'Salle non renseignée' : seance.salle,
            style: const TextStyle(color: Color(0xFF94A3B8)),
          ),
          if (showActions) ...[
            const SizedBox(height: 12),
            Row(
              children: [
                TextButton.icon(
                  onPressed: onVoirRessources,
                  icon: const Icon(Icons.folder_open_rounded),
                  label: const Text('Ressources'),
                ),
                const SizedBox(width: 8),
                isUploading
                    ? const SizedBox(
                      width: 24,
                      height: 24,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                    : TextButton.icon(
                      onPressed: onUploaderSupport,
                      icon: const Icon(Icons.upload_file_rounded),
                      label: const Text('Uploader'),
                    ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class ExamCard extends StatelessWidget {
  const ExamCard({required this.examen});

  final ExamenProfesseurMobileDTO examen;

  @override
  Widget build(BuildContext context) {
    final dateLabel =
        examen.dateExamen.trim().isEmpty
            ? 'Date non renseignée'
            : _formatDateJourMoisAnnee(examen.dateExamen);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFF5B5BEA).withOpacity(0.08)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            examen.label,
            style: const TextStyle(
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 6),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                dateLabel,
                style: const TextStyle(color: Color(0xFF667085)),
              ),
              Text(
                examen.etat,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: Color(0xFF5B5BEA),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Corrigé ${examen.corrigeDisponible ? 'disponible' : 'non disponible'}',
            style: const TextStyle(color: Color(0xFF94A3B8)),
          ),
        ],
      ),
    );
  }
}

class RessourceSheet extends StatelessWidget {
  const RessourceSheet({required this.seanceId, required this.fichierCtrl});

  final int seanceId;
  final ControleurFichier fichierCtrl;

  @override
  Widget build(BuildContext context) {
    final ctrlCours = context.watch<ControleurCoursProfesseur>();
    final ressources = ctrlCours.ressources;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.black.withOpacity(0.15),
              borderRadius: BorderRadius.circular(999),
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            'Ressources',
            style: TextStyle(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 14),
          if (ctrlCours.chargementRessources)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 28),
              child: CircularProgressIndicator(),
            )
          else if (ressources.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 24),
              child: Text('Aucune ressource disponible.'),
            )
          else
            Flexible(
              child: ListView.separated(
                shrinkWrap: true,
                itemCount: ressources.length,
                separatorBuilder: (_, __) => const Divider(height: 1),
                itemBuilder: (_, index) {
                  final ressource = ressources[index];
                  return ListTile(
                    title: Text(ressource.nomOriginal),
                    subtitle: Text(
                      '${ressource.typeMime} · ${_formatTaille(ressource.taille)}',
                    ),
                    trailing:
                        fichierCtrl.telechargementEnCours
                            ? const SizedBox(
                              width: 24,
                              height: 24,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                            : IconButton(
                              icon: const Icon(Icons.download_rounded),
                              onPressed: () async {
                                try {
                                  await fichierCtrl.telechargerEtOuvrir(
                                    urlDownload:
                                        '/api/professeur/seances/$seanceId/ressources/${ressource.id}/telecharger',
                                    nomFichier: ressource.nomOriginal,
                                  );
                                } catch (_) {}
                              },
                            ),
                  );
                },
              ),
            ),
          const SizedBox(height: 18),
        ],
      ),
    );
  }

  String _formatTaille(int? bytes) {
    if (bytes == null || bytes <= 0) return 'Taille inconnue';
    const units = ['o', 'Ko', 'Mo', 'Go'];
    var value = bytes.toDouble();
    var index = 0;
    while (value >= 1024 && index < units.length - 1) {
      value /= 1024;
      index++;
    }
    final precision = index == 0 ? 0 : 1;
    return '${value.toStringAsFixed(precision)} ${units[index]}';
  }
}

class CreateSeanceModal extends StatefulWidget {
  const CreateSeanceModal({required this.enseignement});

  final EnseignementProfesseurMobileDTO enseignement;

  @override
  State<CreateSeanceModal> createState() => _CreateSeanceModalState();
}

class _CreateSeanceModalState extends State<CreateSeanceModal> {
  late final TextEditingController _dateCtrl;
  late final TextEditingController _debutCtrl;
  late final TextEditingController _finCtrl;
  late final TextEditingController _typeCtrl;
  late final TextEditingController _salleCtrl;
  final _formKey = GlobalKey<FormState>();
  bool _envoiEnCours = false;

  @override
  void initState() {
    super.initState();
    _dateCtrl = TextEditingController();
    _debutCtrl = TextEditingController();
    _finCtrl = TextEditingController();
    _typeCtrl = TextEditingController(text: 'COURS');
    _salleCtrl = TextEditingController();
  }

  @override
  void dispose() {
    _dateCtrl.dispose();
    _debutCtrl.dispose();
    _finCtrl.dispose();
    _typeCtrl.dispose();
    _salleCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
        left: 18,
        right: 18,
        top: 20,
      ),
      child: Wrap(
        children: [
          Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Créer une séance',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 14),
                FieldWithPicker(
                  label: 'Date',
                  controller: _dateCtrl,
                  onTap: () => _pickDate(_dateCtrl),
                ),
                const SizedBox(height: 12),
                FieldWithPicker(
                  label: 'Heure début',
                  controller: _debutCtrl,
                  onTap: () => _pickTime(_debutCtrl),
                ),
                const SizedBox(height: 12),
                FieldWithPicker(
                  label: 'Heure fin',
                  controller: _finCtrl,
                  onTap: () => _pickTime(_finCtrl),
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _typeCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Type de séance',
                    border: OutlineInputBorder(),
                  ),
                  validator:
                      (value) =>
                          (value ?? '').trim().isEmpty ? 'Type requis' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _salleCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Salle',
                    border: OutlineInputBorder(),
                  ),
                  validator:
                      (value) =>
                          (value ?? '').trim().isEmpty ? 'Salle requise' : null,
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 46,
                  child: ElevatedButton(
                    onPressed: _envoiEnCours ? null : _soumettre,
                    style: _primaryCtaButtonStyle(),
                    child: _ctaLabel('Créer'),
                  ),
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _soumettre() async {
    if (!_formKey.currentState!.validate()) return;
    if (_dateCtrl.text.isEmpty ||
        _debutCtrl.text.isEmpty ||
        _finCtrl.text.isEmpty) {
      _showSnack('Date et horaires requis', isError: true);
      return;
    }

    final annee =
        widget.enseignement.anneeUniversitaire.trim().isNotEmpty
            ? widget.enseignement.anneeUniversitaire
            : '2025-2026';

    final dto = CreationSeanceProfesseurDTO(
      codeClasse: widget.enseignement.codeClasse,
      codeMatiere: widget.enseignement.codeMatiere,
      anneeUniversitaire: annee,
      date: _dateCtrl.text,
      heureDebut: _toIsoDateTime(_dateCtrl.text, _debutCtrl.text),
      heureFin: _toIsoDateTime(_dateCtrl.text, _finCtrl.text),
      type: _typeCtrl.text.trim().toUpperCase(),
      etat: 'PLANIFIEE',
      salle: _salleCtrl.text.trim(),
      contenu: null,
    );
    final ctrlCours = context.read<ControleurCoursProfesseur>();
    setState(() => _envoiEnCours = true);
    try {
      await ctrlCours.creerSeance(dto);
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (e) {
      _showSnack(e.toString(), isError: true);
    } finally {
      if (mounted) setState(() => _envoiEnCours = false);
    }
  }

  Future<void> _pickDate(TextEditingController controller) async {
    final now = DateTime.now();
    final selected = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: now.subtract(const Duration(days: 365)),
      lastDate: now.add(const Duration(days: 365)),
    );
    if (selected == null) return;
    controller.text =
        '${selected.year}-${selected.month.toString().padLeft(2, '0')}-${selected.day.toString().padLeft(2, '0')}';
  }

  Future<void> _pickTime(TextEditingController controller) async {
    final selected = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    if (selected == null) return;
    controller.text =
        '${selected.hour.toString().padLeft(2, '0')}:${selected.minute.toString().padLeft(2, '0')}';
  }

  String _toIsoDateTime(String date, String time) {
    if (date.isEmpty || time.isEmpty) return '';
    return '${date}T$time:00';
  }

  void _showSnack(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor:
            isError ? const Color(0xFFE11D48) : const Color(0xFF0B1220),
      ),
    );
  }
}

String _formatTimeRange(String start, String end) {
  final debut = start.trim();
  final fin = end.trim();
  if (debut.isEmpty && fin.isEmpty) return '';
  if (fin.isEmpty) return debut;
  if (debut.isEmpty) return fin;
  return '$debut - $fin';
}

String _formatDateJourMoisAnnee(String raw) {
  final parsed = _tryParseDate(raw);
  if (parsed == null) return raw.trim();
  final dd = parsed.day.toString().padLeft(2, '0');
  final mm = parsed.month.toString().padLeft(2, '0');
  final yyyy = parsed.year.toString();
  return '$dd/$mm/$yyyy';
}

DateTime? _tryParseDate(String raw) {
  final value = raw.trim();
  if (value.isEmpty) return null;

  final embeddedIso = RegExp(r'(\d{4}-\d{2}-\d{2})').firstMatch(value);
  if (embeddedIso != null) {
    final parsed = DateTime.tryParse(embeddedIso.group(1)!);
    if (parsed != null) return parsed;
  }

  final direct = DateTime.tryParse(value);
  if (direct != null) return direct;

  final fr = RegExp(r'^(\d{2})/(\d{2})/(\d{4})$').firstMatch(value);
  if (fr != null) {
    final day = int.parse(fr.group(1)!);
    final month = int.parse(fr.group(2)!);
    final year = int.parse(fr.group(3)!);
    return DateTime(year, month, day);
  }

  final dash = RegExp(r'^(\d{2})-(\d{2})-(\d{4})$').firstMatch(value);
  if (dash != null) {
    final day = int.parse(dash.group(1)!);
    final month = int.parse(dash.group(2)!);
    final year = int.parse(dash.group(3)!);
    return DateTime(year, month, day);
  }

  final ymdSlash = RegExp(r'^(\d{4})/(\d{2})/(\d{2})$').firstMatch(value);
  if (ymdSlash != null) {
    final year = int.parse(ymdSlash.group(1)!);
    final month = int.parse(ymdSlash.group(2)!);
    final day = int.parse(ymdSlash.group(3)!);
    return DateTime(year, month, day);
  }

  return null;
}

class CreateExamenModal extends StatefulWidget {
  const CreateExamenModal({required this.enseignement});

  final EnseignementProfesseurMobileDTO enseignement;

  @override
  State<CreateExamenModal> createState() => _CreateExamenModalState();
}

class _CreateExamenModalState extends State<CreateExamenModal> {
  static const List<String> _semestres = ['S1', 'S2', 'S3', 'S4'];
  static const List<String> _typesExamen = ['NORMAL', 'RATTRAPAGE'];
  late final TextEditingController _dateCtrl;
  late final TextEditingController _dureeCtrl;
  late final TextEditingController _numeroCtrl;
  String _selectedSemestre = 'S1';
  String _selectedType = 'NORMAL';
  final _formKey = GlobalKey<FormState>();
  bool _envoiEnCours = false;

  @override
  void initState() {
    super.initState();
    _dateCtrl = TextEditingController();
    _dureeCtrl = TextEditingController(text: '90');
    _numeroCtrl = TextEditingController(text: '1');
  }

  @override
  void dispose() {
    _dateCtrl.dispose();
    _dureeCtrl.dispose();
    _numeroCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
        left: 18,
        right: 18,
        top: 20,
      ),
      child: Wrap(
        children: [
          Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Créer un examen',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 14),
                FieldWithPicker(
                  label: 'Date de l\'examen',
                  controller: _dateCtrl,
                  onTap: () => _pickDate(_dateCtrl),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  value: _selectedType,
                  decoration: const InputDecoration(
                    labelText: 'Type d\'examen',
                    border: OutlineInputBorder(),
                  ),
                  items:
                      _typesExamen
                          .map(
                            (type) => DropdownMenuItem(
                              value: type,
                              child: Text(
                                type == 'NORMAL' ? 'Normal' : 'Rattrapage',
                              ),
                            ),
                          )
                          .toList(),
                  onChanged: (value) {
                    if (value != null) {
                      setState(() {
                        _selectedType = value;
                      });
                    }
                  },
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  value: _selectedSemestre,
                  decoration: const InputDecoration(
                    labelText: 'Semestre',
                    border: OutlineInputBorder(),
                  ),
                  items:
                      _semestres
                          .map(
                            (sem) =>
                                DropdownMenuItem(value: sem, child: Text(sem)),
                          )
                          .toList(),
                  onChanged: (value) {
                    if (value != null) {
                      setState(() {
                        _selectedSemestre = value;
                      });
                    }
                  },
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _dureeCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Durée (minutes)',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _numeroCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Numéro',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 46,
                  child: ElevatedButton(
                    onPressed: _envoiEnCours ? null : _soumettre,
                    style: _primaryCtaButtonStyle(),
                    child: _ctaLabel('Créer'),
                  ),
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _soumettre() async {
    if (!_formKey.currentState!.validate()) return;
    if (_dateCtrl.text.isEmpty) {
      _showSnack('Date requise', isError: true);
      return;
    }

    final duree = int.tryParse(_dureeCtrl.text.trim());
    if (duree == null || duree <= 0) {
      _showSnack('Durée requise (minutes > 0)', isError: true);
      return;
    }

    final numero = int.tryParse(_numeroCtrl.text.trim());
    if (numero == null || numero <= 0) {
      _showSnack('Numéro requis (> 0)', isError: true);
      return;
    }

    final dto = CreationExamenProfesseurDTO(
      typeExamen: _selectedType,
      semestre: _selectedSemestre,
      // Backend expects a date-only value (same convention as seance creation).
      dateExamen: _dateCtrl.text.trim(),
      duree: duree,
      numeroExamen: numero,
    );
    final ctrlCours = context.read<ControleurCoursProfesseur>();
    setState(() => _envoiEnCours = true);
    try {
      await ctrlCours.creerExamen(
        dto,
        codeClasse: widget.enseignement.codeClasse,
        codeMatiere: widget.enseignement.codeMatiere,
      );
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (e) {
      _showSnack(_formatApiError(e), isError: true);
    } finally {
      if (mounted) setState(() => _envoiEnCours = false);
    }
  }

  Future<void> _pickDate(TextEditingController controller) async {
    final now = DateTime.now();
    final selected = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: now.subtract(const Duration(days: 365)),
      lastDate: now.add(const Duration(days: 365)),
    );
    if (selected == null) return;
    controller.text =
        '${selected.year}-${selected.month.toString().padLeft(2, '0')}-${selected.day.toString().padLeft(2, '0')}';
  }

  String _formatApiError(Object error) {
    if (error is DioException) {
      final message = _extractMessageFromErrorBody(error.response?.data);
      if (message != null) return message;

      final fallback = (error.message ?? '').trim();
      if (fallback.isNotEmpty) return fallback;
    }

    return error.toString();
  }

  String? _extractMessageFromErrorBody(dynamic data) {
    if (data == null) return null;

    if (data is Map) {
      final rawMessage =
          data['message'] ?? data['error'] ?? data['detail'] ?? data['title'];
      final msg = (rawMessage ?? '').toString().trim();
      if (msg.isNotEmpty) return _truncate(msg);

      final fieldErrors = data['fieldErrors'];
      if (fieldErrors is List && fieldErrors.isNotEmpty) {
        final first = fieldErrors.first;
        if (first is Map) {
          final raw = first['defaultMessage'] ?? first['message'];
          final fe = (raw ?? '').toString().trim();
          if (fe.isNotEmpty) return _truncate(fe);
        }
      }
    }

    if (data is String) {
      final msg = data.trim();
      if (msg.isNotEmpty) return _truncate(msg);
    }

    return null;
  }

  String _truncate(String value, {int max = 220}) {
    if (value.length <= max) return value;
    return '${value.substring(0, max)}...';
  }

  void _showSnack(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor:
            isError ? const Color(0xFFE11D48) : const Color(0xFF0B1220),
      ),
    );
  }
}
