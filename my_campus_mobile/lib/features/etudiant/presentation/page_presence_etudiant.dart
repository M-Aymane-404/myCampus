import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../data/api/etudiant_api.dart';
import '../data/dto/presence_etudiant_dto.dart';

class PagePresenceEtudiant extends StatefulWidget {
  const PagePresenceEtudiant({super.key});

  @override
  State<PagePresenceEtudiant> createState() => _PagePresenceEtudiantState();
}

enum _ModeFiltre { historique, absence }

class _PagePresenceEtudiantState extends State<PagePresenceEtudiant> {
  bool _loading = true;
  String? _error;

  DateTime _selectedDate = DateTime.now();
  DateTime _displayedMonth = DateTime(DateTime.now().year, DateTime.now().month, 1);
  _ModeFiltre _mode = _ModeFiltre.historique;

  List<PresenceEtudiantDTO> _presences = [];
  final Map<String, _DayMarker> _markers = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final api = context.read<EtudiantApi>();
      final data = await api.recupererMesPresences();
      _presences = data;
      _buildMarkers();
      if (mounted) setState(() => _loading = false);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = e.toString();
      });
    }
  }

  void _buildMarkers() {
    _markers.clear();
    for (final p in _presences) {
      final d = p.seance?.date ?? p.dateDeclaration;
      if (d == null) continue;
      final key = _iso(d);
      final m = _markers[key] ?? const _DayMarker();
      _markers[key] = _DayMarker(
        hasPresent: m.hasPresent || p.status.toUpperCase() == 'PRESENT',
        hasAbsent: m.hasAbsent || p.status.toUpperCase() == 'ABSENT',
      );
    }
  }

  List<int> get _years {
    final years = <int>{DateTime.now().year};
    for (final p in _presences) {
      final d = p.seance?.date ?? p.dateDeclaration;
      if (d != null) years.add(d.year);
    }
    final min = years.reduce((a, b) => a < b ? a : b);
    final max = years.reduce((a, b) => a > b ? a : b);
    return [for (int y = min; y <= max; y++) y];
  }

  List<PresenceEtudiantDTO> get _sessionsForDay {
    final all = _presences.where((p) {
      final d = p.seance?.date ?? p.dateDeclaration;
      return d != null && _sameDay(d, _selectedDate);
    }).toList();
    return _mode == _ModeFiltre.absence ? all.where((e) => e.estAbsent).toList() : all;
  }

  int get _countHistorique =>
      _presences.where((p) => (p.seance?.date ?? p.dateDeclaration) != null && _sameDay((p.seance?.date ?? p.dateDeclaration)!, _selectedDate)).length;
  int get _countAbsences =>
      _presences.where((p) => p.estAbsent && (p.seance?.date ?? p.dateDeclaration) != null && _sameDay((p.seance?.date ?? p.dateDeclaration)!, _selectedDate)).length;

  Future<void> _openJustification(PresenceEtudiantDTO p) async {
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => _JustificationSheet(presence: p),
    );
    if (ok == true) await _load();
  }

  void _prevMonth() {
    final m = DateTime(_displayedMonth.year, _displayedMonth.month - 1, 1);
    setState(() {
      _displayedMonth = m;
      _selectedDate = DateTime(m.year, m.month, 1);
    });
  }

  void _nextMonth() {
    final m = DateTime(_displayedMonth.year, _displayedMonth.month + 1, 1);
    setState(() {
      _displayedMonth = m;
      _selectedDate = DateTime(m.year, m.month, 1);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        title: const Text('Presences'),
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView(
                    padding: const EdgeInsets.all(14),
                    children: [
                      _CalendarCard(
                        displayedMonth: _displayedMonth,
                        selectedDate: _selectedDate,
                        markers: _markers,
                        years: _years,
                        onSelectYear: (y) => setState(() {
                          _displayedMonth = DateTime(y, _displayedMonth.month, 1);
                          _selectedDate = DateTime(y, _displayedMonth.month, 1);
                        }),
                        onPrevMonth: _prevMonth,
                        onNextMonth: _nextMonth,
                        onSelectDate: (d) => setState(() => _selectedDate = d),
                      ),
                      const SizedBox(height: 14),
                      _Segment(
                        mode: _mode,
                        countHistorique: _countHistorique,
                        countAbsences: _countAbsences,
                        onMode: (m) => setState(() => _mode = m),
                      ),
                      const SizedBox(height: 14),
                      if (_sessionsForDay.isEmpty)
                        _EmptyCard(mode: _mode)
                      else
                        ..._sessionsForDay.map((p) => Padding(
                              padding: const EdgeInsets.only(bottom: 10),
                              child: _SessionCard(
                                presence: p,
                                onJustifier: p.estAbsent && p.peutAjouterJustification
                                    ? () => _openJustification(p)
                                    : null,
                              ),
                            )),
                    ],
                  ),
                ),
    );
  }
}

class _CalendarCard extends StatelessWidget {
  const _CalendarCard({
    required this.displayedMonth,
    required this.selectedDate,
    required this.markers,
    required this.years,
    required this.onSelectYear,
    required this.onPrevMonth,
    required this.onNextMonth,
    required this.onSelectDate,
  });

  final DateTime displayedMonth;
  final DateTime selectedDate;
  final Map<String, _DayMarker> markers;
  final List<int> years;
  final ValueChanged<int> onSelectYear;
  final VoidCallback onPrevMonth;
  final VoidCallback onNextMonth;
  final ValueChanged<DateTime> onSelectDate;

  @override
  Widget build(BuildContext context) {
    final first = DateTime(displayedMonth.year, displayedMonth.month, 1);
    final days = DateTime(displayedMonth.year, displayedMonth.month + 1, 0).day;
    final offset = first.weekday - 1;
    final cells = ((offset + days + 6) ~/ 7) * 7;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: [Color(0xFF111827), Color(0xFF1F2937)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Row(children: [
          Icon(Icons.bolt_rounded, color: Color(0xFF5B5BEA)),
          SizedBox(width: 8),
          Text('Calendrier Presences', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 18)),
        ]),
        const SizedBox(height: 4),
        const Text('Suivi temporel interactif', style: TextStyle(color: Color(0xFF9CA3AF))),
        const SizedBox(height: 12),
        Row(children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            height: 40,
            decoration: BoxDecoration(color: const Color(0xFF374151), borderRadius: BorderRadius.circular(12)),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<int>(
                value: displayedMonth.year,
                dropdownColor: const Color(0xFF374151),
                style: const TextStyle(color: Colors.white),
                iconEnabledColor: Colors.white,
                onChanged: (y) => y == null ? null : onSelectYear(y),
                items: years.map((y) => DropdownMenuItem(value: y, child: Text('$y'))).toList(),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Container(
              height: 40,
              decoration: BoxDecoration(color: const Color(0xFF374151), borderRadius: BorderRadius.circular(12)),
              child: Row(children: [
                IconButton(onPressed: onPrevMonth, icon: const Icon(Icons.chevron_left_rounded, color: Colors.white)),
                Expanded(child: Center(child: Text(_monthLabel(displayedMonth.month), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w900)))),
                IconButton(onPressed: onNextMonth, icon: const Icon(Icons.chevron_right_rounded, color: Colors.white)),
              ]),
            ),
          ),
        ]),
        const SizedBox(height: 10),
        Row(children: const ['L', 'M', 'M', 'J', 'V', 'S', 'D'].map((d) => Expanded(child: Center(child: Text(d, style: TextStyle(color: Color(0xFF9CA3AF)))))).toList()),
        const SizedBox(height: 6),
        GridView.builder(
          itemCount: cells,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 7, mainAxisExtent: 42),
          itemBuilder: (_, i) {
            final day = i - offset + 1;
            if (day < 1 || day > days) return const SizedBox.shrink();
            final date = DateTime(displayedMonth.year, displayedMonth.month, day);
            final marker = markers[_iso(date)] ?? const _DayMarker();
            final selected = _sameDay(date, selectedDate);
            return InkWell(
              borderRadius: BorderRadius.circular(10),
              onTap: () => onSelectDate(date),
              child: Container(
                margin: const EdgeInsets.all(2),
                decoration: BoxDecoration(
                  color: selected ? const Color(0xFF5B5BEA) : const Color(0xFF111827),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
                  Text('$day', style: TextStyle(color: selected ? Colors.white : const Color(0xFFE5E7EB), fontSize: 12, fontWeight: FontWeight.w800)),
                  const SizedBox(height: 2),
                  Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                    if (marker.hasPresent) const _Dot(color: Color(0xFF22C55E)),
                    if (marker.hasPresent && marker.hasAbsent) const SizedBox(width: 4),
                    if (marker.hasAbsent) const _Dot(color: Color(0xFFEF4444)),
                  ]),
                ]),
              ),
            );
          },
        ),
      ]),
    );
  }
}

class _Segment extends StatelessWidget {
  const _Segment({required this.mode, required this.countHistorique, required this.countAbsences, required this.onMode});
  final _ModeFiltre mode;
  final int countHistorique;
  final int countAbsences;
  final ValueChanged<_ModeFiltre> onMode;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(color: const Color(0xFFE5E7EB), borderRadius: BorderRadius.circular(14)),
      child: Row(children: [
        Expanded(child: _SegmentBtn(active: mode == _ModeFiltre.historique, label: 'Historique ($countHistorique)', onTap: () => onMode(_ModeFiltre.historique))),
        const SizedBox(width: 6),
        Expanded(child: _SegmentBtn(active: mode == _ModeFiltre.absence, label: 'Absences ($countAbsences)', onTap: () => onMode(_ModeFiltre.absence))),
      ]),
    );
  }
}

class _SegmentBtn extends StatelessWidget {
  const _SegmentBtn({required this.active, required this.label, required this.onTap});
  final bool active;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        alignment: Alignment.center,
        height: 40,
        decoration: BoxDecoration(color: active ? const Color(0xFF5B5BEA) : Colors.transparent, borderRadius: BorderRadius.circular(10)),
        child: Text(label, style: TextStyle(color: active ? Colors.white : const Color(0xFF334155), fontWeight: FontWeight.w900, fontSize: 12)),
      ),
    );
  }
}

class _SessionCard extends StatelessWidget {
  const _SessionCard({required this.presence, required this.onJustifier});
  final PresenceEtudiantDTO presence;
  final VoidCallback? onJustifier;

  String _justificationLabel() {
    switch (presence.justificationState) {
      case 'validated':
        return 'Justification validée';
      case 'rejected':
        return 'Justification refusée';
      case 'pending':
        return 'Justification en attente';
      default:
        return 'Justification envoyée';
    }
  }

  Color _justificationColor() {
    switch (presence.justificationState) {
      case 'validated':
        return const Color(0xFF16A34A);
      case 'rejected':
        return const Color(0xFFEF4444);
      case 'pending':
        return const Color(0xFF5B5BEA);
      default:
        return const Color(0xFF5B5BEA);
    }
  }

  @override
  Widget build(BuildContext context) {
    final status = presence.status.toUpperCase();
    final absent = status == 'ABSENT';
    final hasJustification = presence.aJustification;
    final justificationColor = _justificationColor();
    final d = presence.seance?.date ?? presence.dateDeclaration;
    final date = d == null ? '-' : '${_two(d.day)}/${_two(d.month)}/${d.year}';
    final time = d == null ? '-' : _time(d);
    final badge = absent ? const Color(0xFFEF4444) : const Color(0xFF22C55E);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [BoxShadow(blurRadius: 16, offset: const Offset(0, 8), color: Colors.black.withOpacity(0.06))],
      ),
      child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(date, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 15)),
            const SizedBox(height: 3),
            Text(time, style: const TextStyle(color: Color(0xFF64748B), fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            Text((presence.seance?.type ?? '-').toUpperCase(), style: const TextStyle(color: Color(0xFF5B5BEA), fontWeight: FontWeight.w900)),
            const SizedBox(height: 6),
            Text('Prof: ${presence.seance?.prof ?? '-'}', style: const TextStyle(color: Color(0xFF334155))),
            Text('Salle: ${presence.seance?.salle ?? '-'}', style: const TextStyle(color: Color(0xFF334155))),
            if (hasJustification) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                decoration: BoxDecoration(
                  color: const Color(0xFFF1F5F9),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _justificationLabel(),
                      style: TextStyle(
                        color: justificationColor,
                        fontWeight: FontWeight.w800,
                        fontSize: 12,
                      ),
                    ),
                    if ((presence.descriptionDeJustification ?? '').trim().isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(top: 3),
                        child: Text(
                          presence.descriptionDeJustification!,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            color: Color(0xFF334155),
                            fontSize: 12.5,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ]),
        ),
        const SizedBox(width: 10),
        Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(color: badge.withOpacity(0.12), borderRadius: BorderRadius.circular(999)),
            child: Text(status, style: TextStyle(color: badge, fontWeight: FontWeight.w900, fontSize: 12)),
          ),
          if (absent) ...[
            const SizedBox(height: 10),
            if (onJustifier != null)
              SizedBox(
                height: 34,
                child: ElevatedButton(
                  onPressed: onJustifier,
                  style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF5B5BEA), foregroundColor: Colors.white, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10))),
                  child: Text(
                    hasJustification ? 'Changer la justification' : 'Justifier',
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                ),
              )
            else if (hasJustification)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
                decoration: BoxDecoration(
                  color: justificationColor.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text(
                  presence.justificationState == 'validated'
                    ? 'Validée'
                    : presence.justificationState == 'rejected'
                      ? 'Refusée'
                      : 'En attente',
                  style: TextStyle(
                    color: justificationColor,
                    fontWeight: FontWeight.w800,
                    fontSize: 12,
                  ),
                ),
              )
            else
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
                decoration: BoxDecoration(
                  color: const Color(0xFFFEF2F2),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Text(
                  'Delai depasse',
                  style: TextStyle(
                    color: Color(0xFFEF4444),
                    fontWeight: FontWeight.w800,
                    fontSize: 12,
                  ),
                ),
              ),
          ],
        ]),
      ]),
    );
  }
}

class _EmptyCard extends StatelessWidget {
  const _EmptyCard({required this.mode});
  final _ModeFiltre mode;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(20)),
      child: Center(child: Text(mode == _ModeFiltre.absence ? 'Aucune absence pour cette date.' : 'Aucune session pour cette date.')),
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.color});
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(width: 6, height: 6, decoration: BoxDecoration(color: color, shape: BoxShape.circle));
  }
}

class _DayMarker {
  final bool hasPresent;
  final bool hasAbsent;
  const _DayMarker({this.hasPresent = false, this.hasAbsent = false});
}

class _JustificationSheet extends StatefulWidget {
  const _JustificationSheet({required this.presence});
  final PresenceEtudiantDTO presence;

  @override
  State<_JustificationSheet> createState() => _JustificationSheetState();
}

class _JustificationSheetState extends State<_JustificationSheet> {
  static const Map<String, String> _types = {
    'CERTIFICAT_MEDICAL': 'Certificat medical',
    'PROBLEME_TRANSPORT': 'Probleme transport',
    'AUTRE': 'Autre',
  };

  String? _type;
  final TextEditingController _description = TextEditingController();
  PlatformFile? _file;
  String? _existingFileName;
  bool _sending = false;

  @override
  void initState() {
    super.initState();
    final backendType = widget.presence.typeMotif?.toUpperCase();
    if (backendType == 'RAISON_FAMILIALE') {
      _type = 'PROBLEME_TRANSPORT';
    } else if (_types.containsKey(backendType)) {
      _type = backendType;
    }
    _description.text = widget.presence.descriptionDeJustification ?? '';
    _existingFileName = _extractFileName(
      widget.presence.motifNom ?? widget.presence.motifUrl,
    );
  }

  @override
  void dispose() {
    _description.dispose();
    super.dispose();
  }

  bool get _descRequired => _type == 'AUTRE';

  bool get _valid {
    if (_type == null) return false;
    if (_fileRequiredForType && (_file == null || _file!.path == null || _file!.path!.isEmpty)) {
      return false;
    }
    if (_descRequired && _description.text.trim().isEmpty) return false;
    return true;
  }

  bool get _fileRequiredForType =>
      _type == 'CERTIFICAT_MEDICAL' || _type == 'PROBLEME_TRANSPORT';

  Future<void> _pickFile() async {
    try {
      final res = await FilePicker.platform.pickFiles(
        allowMultiple: false,
        type: FileType.custom,
        allowedExtensions: ['pdf', 'jpg', 'jpeg', 'png'],
        withData: false,
      );
      if (res == null || res.files.isEmpty) return;
      final f = res.files.first;
      if (f.path == null || f.path!.isEmpty) return;
      if (f.size > 5 * 1024 * 1024) {
        _toast('Fichier trop grand (max 5Mo).');
        return;
      }
      setState(() {
        _file = f;
        _existingFileName = null;
      });
    } on MissingPluginException {
      final picker = ImagePicker();
      final image = await picker.pickImage(source: ImageSource.gallery);
      if (image == null) return;
      final path = image.path;
      final name = path.split(RegExp(r'[\\/]')).last;
      setState(
        () {
          _file = PlatformFile(
            name: name,
            path: path,
            size: 0,
          );
          _existingFileName = null;
        },
      );
      _toast('Mode image active (plugin PDF indisponible).');
    }
  }

  Future<void> _submit() async {
    if (!_valid || _sending) return;

    final backendType = _type == 'PROBLEME_TRANSPORT' ? 'RAISON_FAMILIALE' : _type!;

    setState(() => _sending = true);
    try {
      await context.read<EtudiantApi>().ajouterJustificationPresence(
            presenceId: widget.presence.id,
            typeMotif: backendType,
            description: _description.text.trim(),
            fichierPath: _file?.path,
          );
      if (!mounted) return;
      Navigator.pop(context, true);
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Justification envoyee.')));
    } catch (e) {
      _toast('Echec envoi justification: $e');
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  void _toast(String m) => ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(m)));

  @override
  Widget build(BuildContext context) {
    final insets = MediaQuery.of(context).viewInsets.bottom;
    final d = widget.presence.seance?.date ?? widget.presence.dateDeclaration;
    final date = d == null ? '-' : '${_two(d.day)}/${_two(d.month)}/${d.year}';
    final type = (widget.presence.seance?.type ?? '-').toUpperCase();

    return Container(
      color: const Color(0xFFF5F7FB),
      padding: EdgeInsets.fromLTRB(14, 14, 14, 14 + insets),
      child: SingleChildScrollView(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            const Expanded(child: Text('Justification', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900))),
            IconButton(onPressed: _sending ? null : () => Navigator.pop(context, false), icon: const Icon(Icons.close_rounded)),
          ]),
          const SizedBox(height: 10),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(20), boxShadow: [BoxShadow(blurRadius: 16, offset: const Offset(0, 8), color: Colors.black.withOpacity(0.06))]),
            child: Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(color: const Color(0xFFF1F5F9), borderRadius: BorderRadius.circular(14)),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(date, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w900)),
                const SizedBox(height: 4),
                Text(type, style: const TextStyle(color: Color(0xFF5B5BEA), fontWeight: FontWeight.w900)),
              ]),
            ),
          ),
          const SizedBox(height: 12),
          const Text('Type de motif', style: TextStyle(fontWeight: FontWeight.w700, color: Color(0xFF334155))),
          const SizedBox(height: 6),
          DropdownButtonFormField<String>(
            value: _type,
            hint: const Text('Selectionner un motif'),
            decoration: InputDecoration(filled: true, fillColor: Colors.white, border: OutlineInputBorder(borderRadius: BorderRadius.circular(14))),
            items: _types.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
            onChanged: _sending ? null : (v) => setState(() => _type = v),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _description,
            minLines: 2,
            maxLines: 4,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              labelText: _descRequired ? 'Description (obligatoire si AUTRE)' : 'Description (optionnelle)',
              filled: true,
              fillColor: Colors.white,
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
            ),
          ),
          const SizedBox(height: 12),
          GestureDetector(
            onTap: _sending ? null : _pickFile,
            child: CustomPaint(
              painter: _DashedPainter(color: const Color(0xFF94A3B8), radius: 14),
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 12),
                decoration: BoxDecoration(color: const Color(0xFFF8FAFC), borderRadius: BorderRadius.circular(14)),
                child: Column(children: [
                  const Icon(Icons.upload_file_rounded, color: Color(0xFF5B5BEA), size: 30),
                  const SizedBox(height: 8),
                  const Text('IMPORTER PDF (MAX 5MO)', style: TextStyle(fontWeight: FontWeight.w900)),
                      if (_file != null || _existingFileName != null) ...[
                        const SizedBox(height: 10),
                        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                          Flexible(
                            child: Text(
                              _file?.name ?? _existingFileName ?? '',
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Color(0xFF334155),
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          InkWell(
                            onTap: _sending
                                ? null
                                : () => setState(() {
                                      _file = null;
                                      _existingFileName = null;
                                    }),
                            child: const Icon(
                              Icons.close_rounded,
                              size: 18,
                              color: Color(0xFFEF4444),
                            ),
                          ),
                        ]),
                      ],
                ]),
              ),
            ),
          ),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: (!_valid || _sending) ? null : _submit,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF5B5BEA),
                foregroundColor: Colors.white,
                disabledBackgroundColor: const Color(0xFFCBD5E1),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                padding: const EdgeInsets.symmetric(vertical: 14),
              ),
              child: _sending
                  ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Text('SOUMETTRE LE DOCUMENT', style: TextStyle(fontWeight: FontWeight.w900)),
            ),
          ),
        ]),
      ),
    );
  }
}

class _DashedPainter extends CustomPainter {
  _DashedPainter({required this.color, required this.radius});
  final Color color;
  final double radius;

  @override
  void paint(Canvas canvas, Size size) {
    final rrect = RRect.fromRectAndRadius(Offset.zero & size, Radius.circular(radius));
    final paint = Paint()..color = color..style = PaintingStyle.stroke..strokeWidth = 1.2;
    const dash = 6.0;
    const gap = 4.0;
    final path = Path()..addRRect(rrect);
    for (final metric in path.computeMetrics()) {
      double dist = 0;
      while (dist < metric.length) {
        final end = (dist + dash).clamp(0, metric.length).toDouble();
        canvas.drawPath(metric.extractPath(dist, end), paint);
        dist += dash + gap;
      }
    }
  }

  @override
  bool shouldRepaint(covariant _DashedPainter oldDelegate) => oldDelegate.color != color || oldDelegate.radius != radius;
}

String? _extractFileName(String? raw) {
  if (raw == null || raw.trim().isEmpty) return null;
  final v = raw.trim();
  if (!v.contains('/')) return v;
  return v.split('/').last;
}

bool _sameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;
String _two(int v) => v.toString().padLeft(2, '0');
String _iso(DateTime d) => '${d.year}-${_two(d.month)}-${_two(d.day)}';
String _monthLabel(int m) => const ['Janvier','Fevrier','Mars','Avril','Mai','Juin','Juillet','Aout','Septembre','Octobre','Novembre','Decembre'][(m - 1).clamp(0, 11)];
String _time(DateTime d) {
  final h24 = d.hour;
  final min = _two(d.minute);
  final suffix = h24 >= 12 ? 'PM' : 'AM';
  final h12 = h24 % 12 == 0 ? 12 : h24 % 12;
  return '${_two(h12)}:$min $suffix';
}

