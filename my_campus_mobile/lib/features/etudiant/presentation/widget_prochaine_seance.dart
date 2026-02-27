import 'package:flutter/material.dart';

class WidgetProchaineSeance extends StatelessWidget {
  final String matiere;
  final String heure;
  final String salle;

  const WidgetProchaineSeance({
    super.key,
    required this.matiere,
    required this.heure,
    required this.salle,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            const Icon(Icons.schedule, color: Colors.blue),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    matiere,
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                    ),
                  ),
                  Text('$heure • $salle'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
