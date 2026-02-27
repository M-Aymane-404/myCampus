import 'package:flutter/material.dart';

class AccueilProfesseur extends StatelessWidget {
  const AccueilProfesseur({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Accueil Professeur'),
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
              'Espace Professeur',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 24),
            Card(
              child: ListTile(
                leading: Icon(Icons.schedule),
                title: Text('Mes cours'),
                subtitle: Text('Gérer mes cours'),
              ),
            ),
            Card(
              child: ListTile(
                leading: Icon(Icons.assignment),
                title: Text('Devoirs à corriger'),
                subtitle: Text('Corriger les devoirs'),
              ),
            ),
            Card(
              child: ListTile(
                leading: Icon(Icons.people),
                title: Text('Étudiants'),
                subtitle: Text('Liste des étudiants'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
