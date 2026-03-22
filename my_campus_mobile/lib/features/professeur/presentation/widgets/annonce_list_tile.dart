import 'package:flutter/material.dart';

import '../../data/dto/annonce_professeur_dto.dart';

class AnnonceListTileProfesseur extends StatelessWidget {
  const AnnonceListTileProfesseur({
    super.key,
    required this.annonce,
    required this.onTap,
    this.onSupprimer,
    this.onBasculerPublication,
    this.actionsEnCours = false,
  });

  final AnnonceProfesseurDTO annonce;
  final VoidCallback onTap;
  final VoidCallback? onSupprimer;
  final VoidCallback? onBasculerPublication;
  final bool actionsEnCours;

  @override
  Widget build(BuildContext context) {
    final unread = !annonce.estLue;
    final urgent = annonce.urgent;
    final type = (annonce.typeAnnonce ?? '').trim();
    final classe = (annonce.codeClasse ?? '').trim();
    final dateLabel = _formatPublicationDate(annonce.datePublication);
    final extrait = annonce.contenu.trim();

    final borderColor =
        urgent
            ? const Color(0xFFEF4444).withOpacity(0.22)
            : unread
            ? const Color(0xFF5B5BEA).withOpacity(0.18)
            : Colors.black.withOpacity(0.05);

    return InkWell(
      borderRadius: BorderRadius.circular(22),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: unread ? const Color(0xFFF4F6FF) : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: borderColor),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 14,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 10,
                  height: 10,
                  decoration: BoxDecoration(
                    color:
                        urgent
                            ? const Color(0xFFEF4444)
                            : unread
                            ? const Color(0xFF5B5BEA)
                            : const Color(0xFFCBD5E1),
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    annonce.titre,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF0B1220),
                    ),
                  ),
                ),
                if (urgent) ...[
                  const SizedBox(width: 8),
                  const _Pill(
                    label: 'Urgent',
                    fg: Color(0xFFDC2626),
                    bg: Color(0xFFFEE2E2),
                  ),
                ] else if (unread) ...[
                  const SizedBox(width: 8),
                  const _Pill(
                    label: 'Nouveau',
                    fg: Color(0xFF4F46E5),
                    bg: Color(0xFFEFF6FF),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                if (type.isNotEmpty)
                  _Pill(
                    label: type == 'AUTRES'
                        ? 'Autres'
                        : type[0] + type.substring(1).toLowerCase(),
                    fg: const Color(0xFF5B5BEA),
                    bg: const Color(0xFFEEF2FF),
                  ),
                if (classe.isNotEmpty)
                  _Pill(
                    label: classe,
                    fg: const Color(0xFF0B1220),
                    bg: const Color(0xFFF3F4F6),
                  ),
              ],
            ),
            if (extrait.isNotEmpty) ...[
              const SizedBox(height: 10),
              Text(
                extrait,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 12.5,
                  height: 1.35,
                  fontWeight: FontWeight.w600,
                  color: Color(0xFF667085),
                ),
              ),
            ],
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(
                  Icons.schedule_rounded,
                  size: 16,
                  color: Color(0xFF64748B),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    dateLabel,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: Color(0xFF61708A),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                if (onSupprimer == null && onBasculerPublication == null)
                  const Icon(
                    Icons.chevron_right_rounded,
                    color: Color(0xFF94A3B8),
                  ),
              ],
            ),
            if (onSupprimer != null || onBasculerPublication != null) ...[
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  if (actionsEnCours)
                    const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  else ...[
                    if (onBasculerPublication != null)
                      TextButton.icon(
                        onPressed: onBasculerPublication,
                        icon: Icon(
                          annonce.publiee
                              ? Icons.visibility_off_rounded
                              : Icons.visibility_rounded,
                          size: 18,
                        ),
                        label: Text(annonce.publiee ? 'Depublier' : 'Publier'),
                        style: TextButton.styleFrom(
                          foregroundColor: const Color(0xFF4F46E5),
                          textStyle: const TextStyle(fontWeight: FontWeight.w900),
                          visualDensity: VisualDensity.compact,
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 8,
                          ),
                        ),
                      ),
                    if (onSupprimer != null) ...[
                      const SizedBox(width: 8),
                      TextButton.icon(
                        onPressed: onSupprimer,
                        icon: const Icon(
                          Icons.delete_outline_rounded,
                          size: 18,
                        ),
                        label: const Text('Supprimer'),
                        style: TextButton.styleFrom(
                          foregroundColor: const Color(0xFFDC2626),
                          textStyle: const TextStyle(fontWeight: FontWeight.w900),
                          visualDensity: VisualDensity.compact,
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 8,
                          ),
                        ),
                      ),
                    ],
                  ],
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _Pill extends StatelessWidget {
  const _Pill({required this.label, required this.fg, required this.bg});

  final String label;
  final Color fg;
  final Color bg;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: fg.withOpacity(0.18)),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w900,
          color: fg,
        ),
      ),
    );
  }
}

String _formatPublicationDate(String raw) {
  final value = raw.trim();
  if (value.isEmpty) return '—';

  final iso = RegExp(r'(\d{4}-\d{2}-\d{2})').firstMatch(value);
  if (iso != null) {
    final parsed = DateTime.tryParse(iso.group(1)!);
    if (parsed != null) {
      final dd = parsed.day.toString().padLeft(2, '0');
      final mm = parsed.month.toString().padLeft(2, '0');
      final yyyy = parsed.year.toString();
      return '$dd/$mm/$yyyy';
    }
  }

  final direct = DateTime.tryParse(value);
  if (direct != null) {
    final dd = direct.day.toString().padLeft(2, '0');
    final mm = direct.month.toString().padLeft(2, '0');
    final yyyy = direct.year.toString();
    return '$dd/$mm/$yyyy';
  }

  return value;
}
