import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'features/authentification/presentation/controleur_authentification.dart';
import 'features/authentification/presentation/page_connexion.dart';
import 'features/etudiant/presentation/accueil_etudiant.dart';
import 'features/professeur/presentation/accueil_professeur.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ControleurAuthentification()),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'MyCampus',
        theme: ThemeData(
          primarySwatch: Colors.blue,
          useMaterial3: true,
        ),
        home: const RouteurAuthentification(),
      ),
    );
  }
}

class RouteurAuthentification extends StatefulWidget {
  const RouteurAuthentification({super.key});

  @override
  State<RouteurAuthentification> createState() => _RouteurAuthentificationState();
}

class _RouteurAuthentificationState extends State<RouteurAuthentification> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ControleurAuthentification>().initialiser();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ControleurAuthentification>(
      builder: (context, controleur, child) {
        if (controleur.chargement) {
          return const Scaffold(
            body: Center(
              child: CircularProgressIndicator(),
            ),
          );
        }

        if (!controleur.estInitialise) {
          return const Scaffold(
            body: Center(
              child: Text('Initialisation...'),
            ),
          );
        }

        if (!controleur.estAuthentifie) {
          return const PageConnexion();
        }

        switch (controleur.roleUtilisateur) {
          case 'ETUDIANT':
            return const AccueilEtudiant();
          case 'PROFESSEUR':
            return const AccueilProfesseur();
          default:
            return const PageConnexion();
        }
      },
    );
  }
}
