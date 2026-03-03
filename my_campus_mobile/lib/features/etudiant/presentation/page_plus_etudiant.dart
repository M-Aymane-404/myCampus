import 'package:flutter/material.dart';

class PagePlusEtudiant extends StatelessWidget {
  const PagePlusEtudiant({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        title: const Text(
          "Plus",
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _Tile(
              icon: Icons.calendar_month_outlined,
              title: "Calendrier",
              onTap:
                  () => Navigator.of(context).pushNamed('/etudiant/calendrier'),
            ),
            const SizedBox(height: 10),
            _Tile(
              icon: Icons.campaign_outlined,
              title: "Annonces",
              onTap:
                  () => Navigator.of(context).pushNamed('/etudiant/annonces'),
            ),
          ],
        ),
      ),
    );
  }
}

class _Tile extends StatelessWidget {
  const _Tile({required this.icon, required this.title, required this.onTap});

  final IconData icon;
  final String title;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.black.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: const Color(0xFFEEF2FF),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: const Color(0xFF5B5BEA)),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                title,
                style: const TextStyle(fontWeight: FontWeight.w900),
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
