import 'package:flutter/material.dart';

class AccueilEtudiant extends StatelessWidget {
  const AccueilEtudiant({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Accueil Étudiant'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              // Logout functionality
            },
          ),
        ],
      ),
      body: const Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Bienvenue sur MyCampus',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 24),
            Card(
              child: ListTile(
                leading: Icon(Icons.schedule),
                title: Text('Emploi du temps'),
                subtitle: Text('Consulter vos cours'),
              ),
            ),
            Card(
              child: ListTile(
                leading: Icon(Icons.assignment),
                title: Text('Devoirs'),
                subtitle: Text('Vos devoirs à rendre'),
              ),
            ),
            Card(
              child: ListTile(
                leading: Icon(Icons.grade),
                title: Text('Notes'),
                subtitle: Text('Consulter vos résultats'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
