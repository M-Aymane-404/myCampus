import 'package:dwm_bot/features/etudiant/presentation/page_notes_etudiant.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/network/client_dio.dart';
import 'core/storage/app_file_storage.dart';
import 'features/authentification/presentation/controleur_authentification.dart';
import 'features/authentification/presentation/page_connexion.dart';
import 'features/etudiant/data/api/etudiant_api.dart';
import 'features/etudiant/data/controller/ControleurFichier.dart';
import 'features/etudiant/data/controller/controleur_accueil_etudiant.dart';
import 'features/etudiant/data/controller/controleur_cours_etudiant.dart';
import 'features/etudiant/data/controller/controleur_emploi_etudiant.dart';
import 'features/etudiant/data/controller/controleur_profil_etudiant.dart';
import 'features/etudiant/data/controller/controleur_releve_notes_etudiant.dart';
import 'features/etudiant/data/repository/etudiant_repository.dart';
import 'features/etudiant/data/repository/fichier_repository.dart';
import 'features/etudiant/data/repository/releve_notes_repo.dart';
import 'features/etudiant/presentation/accueil_etudiant.dart';
import 'features/etudiant/presentation/etudiant_shell.dart';
import 'features/etudiant/presentation/page_cours_etudiant.dart';
import 'features/professeur/presentation/accueil_professeur.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
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
          create: (ctx) => ControleurAuthentification()..initialiser(),
        ),
        // API étudiant
        Provider<EtudiantApi>(
          create: (_) => EtudiantApi(ClientDio.dioInstance),
        ),
        ChangeNotifierProvider(
          create:
              (ctx) => ControleurReleveNotesEtudiant(
                repo: ReleveNotesRepo(api: ctx.read<EtudiantApi>()),
              ),
        ),
        // Storage option A
        Provider<AppFileStorage>(create: (_) => AppFileStorage()),

        // Repo téléchargement fichiers (Option A)
        Provider<FichierRepository>(
          create: (ctx) {
            final dio = ClientDio.dioInstance;
            final storage = ctx.read<AppFileStorage>();
            return FichierRepository(dio, storage);
          },
        ),

        // Controller téléchargement fichiers
        ChangeNotifierProxyProvider<FichierRepository, ControleurFichier>(
          create: (ctx) => ControleurFichier(ctx.read<FichierRepository>()),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurFichier(repo),
        ),

        // Repo étudiant
        Provider<EtudiantRepository>(
          create: (ctx) {
            final dio = ClientDio.dioInstance;
            final api = ctx.read<EtudiantApi>();
            return EtudiantRepository(api, dio);
          },
        ),

        ChangeNotifierProxyProvider<
          EtudiantRepository,
          ControleurAccueilEtudiant
        >(
          create:
              (ctx) =>
                  ControleurAccueilEtudiant(ctx.read<EtudiantRepository>()),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurAccueilEtudiant(repo),
        ),

        ChangeNotifierProxyProvider<
          EtudiantRepository,
          ControleurProfilEtudiant
        >(
          create:
              (ctx) => ControleurProfilEtudiant(ctx.read<EtudiantRepository>()),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurProfilEtudiant(repo),
        ),

        ChangeNotifierProxyProvider<
          EtudiantRepository,
          ControleurCoursEtudiant
        >(
          create:
              (ctx) => ControleurCoursEtudiant(ctx.read<EtudiantRepository>()),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurCoursEtudiant(repo),
        ),

        ChangeNotifierProxyProvider<EtudiantApi, ControleurEmploiEtudiant>(
          create:
              (ctx) => ControleurEmploiEtudiant(api: ctx.read<EtudiantApi>()),
          update:
              (ctx, api, ctrl) => ctrl ?? ControleurEmploiEtudiant(api: api),
        ),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'MyCampus',
        theme: ThemeData(useMaterial3: true),
        home: const RouteurAuthentification(),
        onGenerateRoute: (settings) {
          switch (settings.name) {
            case '/etudiant/tableau-de-bord':
              return MaterialPageRoute(
                settings: settings,
                builder: (ctx) => const PageAccueilEtudiant(),
              );

            case '/etudiant/cours':
              return MaterialPageRoute(
                settings: settings,
                builder: (ctx) => const PageCoursEtudiant(),
              );

            case '/etudiant/notes':
              return MaterialPageRoute(
                settings: settings,
                builder: (ctx) => const PageNotesEtudiant(),
              );

            case '/etudiant/presence':
              return MaterialPageRoute(
                settings: settings,
                builder:
                    (_) => const Scaffold(
                      body: Center(child: Text('Page Présence')),
                    ),
              );

            case '/etudiant/calendrier':
              return MaterialPageRoute(
                settings: settings,
                builder:
                    (_) => const Scaffold(
                      body: Center(child: Text('Page Calendrier')),
                    ),
              );

            case '/etudiant/annonces':
              return MaterialPageRoute(
                settings: settings,
                builder:
                    (_) => const Scaffold(
                      body: Center(child: Text('Page Annonces')),
                    ),
              );
            case '/etudiant/profil':
              return MaterialPageRoute(
                settings: settings,
                builder:
                    (_) => const Scaffold(
                      body: Center(child: Text('Page Profil')),
                    ),
              );

            case '/connexion':
              return MaterialPageRoute(
                settings: settings,
                builder: (ctx) => const PageConnexion(),
              );

            default:
              return null;
          }
        },
      ),
    );
  }
}

class RouteurAuthentification extends StatefulWidget {
  const RouteurAuthentification({super.key});

  @override
  State<RouteurAuthentification> createState() =>
      _RouteurAuthentificationState();
}

class _RouteurAuthentificationState extends State<RouteurAuthentification> {
  bool _initEtudiantFait = false;

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
          _initEtudiantFait = false;
          return const PageConnexion();
        }

        switch (controleur.roleUtilisateur) {
          case 'ETUDIANT':
            if (!_initEtudiantFait) {
              _initEtudiantFait = true;
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                context.read<ControleurProfilEtudiant>().chargerMoi();
                context.read<ControleurAccueilEtudiant>().chargerDashboard();
              });
            }
            return const EtudiantShell();

          case 'PROFESSEUR':
            _initEtudiantFait = false;
            return const AccueilProfesseur();

          default:
            _initEtudiantFait = false;
            return const PageConnexion();
        }
      },
    );
  }
}
