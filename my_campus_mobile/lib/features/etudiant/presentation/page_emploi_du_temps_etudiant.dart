import 'package:flutter/material.dart';

class PageEmploiDuTempsEtudiant extends StatelessWidget {
  const PageEmploiDuTempsEtudiant({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Emploi du temps'),
      ),
      body: const Center(
        child: Text('Emploi du temps - En développement'),
      ),
    );
  }
}
