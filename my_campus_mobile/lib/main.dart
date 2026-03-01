import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/network/client_dio.dart';
import 'features/authentification/presentation/controleur_authentification.dart';
import 'features/authentification/presentation/page_connexion.dart';
import 'features/etudiant/data/api/etudiant_api.dart';
import 'features/etudiant/data/repository/etudiant_repository.dart';
import 'features/etudiant/presentation/accueil_etudiant/accueil_etudiant.dart';
import 'features/etudiant/presentation/accueil_etudiant/controleur_accueil_etudiant.dart';
import 'features/professeur/presentation/accueil_professeur.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Init Dio (interceptors, cookies, etc.)
  ClientDio().initialiser();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => ControleurAuthentification()..initialiser(),
        ),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'MyCampus',
        theme: ThemeData(useMaterial3: true),
        home: const RouteurAuthentification(),
      ),
    );
  }
}

class RouteurAuthentification extends StatelessWidget {
  const RouteurAuthentification({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<ControleurAuthentification>(
      builder: (context, controleur, child) {
        if (!controleur.estInitialise) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        if (!controleur.estAuthentifie) {
          return const PageConnexion();
        }

        switch (controleur.roleUtilisateur) {
          case 'ETUDIANT':
            return ChangeNotifierProvider(
              create: (_) {
                final dio = ClientDio.dioInstance;
                final api = EtudiantApi(dio);
                final repo = EtudiantRepository(api);
                return ControleurAccueilEtudiant(repo)..chargerDashboard();
              },
              child: const PageAccueilEtudiant(),
            );

          case 'PROFESSEUR':
            return const AccueilProfesseur();

          default:
            return const PageConnexion();
        }
      },
    );
  }
}
