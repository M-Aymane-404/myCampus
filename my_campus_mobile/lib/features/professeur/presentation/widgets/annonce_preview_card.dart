import 'package:flutter/material.dart';

import '../../data/dto/annonce_professeur_dto.dart';

class AnnoncePreviewCard extends StatelessWidget {
  const AnnoncePreviewCard({
    super.key,
    required this.annonce,
    required this.onTap,
  });

  final AnnonceProfesseurDTO annonce;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final chips = <Widget>[];
    if (annonce.typeAnnonce != null && annonce.typeAnnonce!.trim().isNotEmpty) {
      chips.add(
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.2),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            annonce.typeAnnonce!,
            style: const TextStyle(
              color: Colors.white70,
              fontWeight: FontWeight.w700,
              fontSize: 12,
            ),
          ),
        ),
      );
    }

    if ((annonce.codeClasse ?? '').trim().isNotEmpty) {
      chips.add(
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.2),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            'Classe ${annonce.codeClasse}',
            style: const TextStyle(
              color: Colors.white70,
              fontWeight: FontWeight.w700,
              fontSize: 12,
            ),
          ),
        ),
      );
    }

    return InkWell(
      borderRadius: BorderRadius.circular(22),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFFEFEFFF), Color(0xFFD5DBFF)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          borderRadius: BorderRadius.circular(22),
          border: Border.all(
            color: const Color(0xFFD2D9FF),
            width: 1.5,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    annonce.titre,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF0B1220),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                if (annonce.urgent)
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFF1F2),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text(
                      'URGENT',
                      style: TextStyle(
                        color: Color(0xFFDC2626),
                        fontWeight: FontWeight.w900,
                        fontSize: 12,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Text(
                  annonce.datePublication,
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFF475569),
                  ),
                ),
                const Spacer(),
                if (chips.isNotEmpty)
                  Row(
                    children: chips
                        .map((chip) => Padding(
                              padding: const EdgeInsets.only(left: 6),
                              child: chip,
                            ))
                        .toList(),
                  ),
              ],
            ),
            if ((annonce.codeClasse ?? '').trim().isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(
                'Classe concernée : ${annonce.codeClasse}',
                style: const TextStyle(
                  color: Color(0xFF475569),
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
