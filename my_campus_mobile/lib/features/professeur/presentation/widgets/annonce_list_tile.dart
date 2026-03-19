import 'package:flutter/material.dart';

import '../../data/dto/annonce_professeur_dto.dart';

class AnnonceListTileProfesseur extends StatelessWidget {
  const AnnonceListTileProfesseur({
    super.key,
    required this.annonce,
    required this.onTap,
  });

  final AnnonceProfesseurDTO annonce;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final bg = annonce.estLue ? Colors.white : const Color(0xFFEEF2FF);

    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: annonce.estLue
                ? Colors.black.withOpacity(0.05)
                : const Color(0xFF5B5BEA).withOpacity(0.18),
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 11,
              height: 11,
              decoration: BoxDecoration(
                color: annonce.estLue
                    ? const Color(0xFFCBD5E1)
                    : const Color(0xFFEF4444),
                shape: BoxShape.circle,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    annonce.titre,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF0B1220),
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    annonce.datePublication,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: Color(0xFF61708A),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
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
