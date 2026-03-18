import 'package:dwm_bot/features/etudiant/presentation/page_notes_etudiant.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/network/client_dio.dart';
import 'core/storage/app_file_storage.dart';
import 'features/authentification/presentation/controleur_authentification.dart';
import 'features/authentification/presentation/page_connexion.dart';
import 'features/etudiant/data/api/annonce_etudiant_api.dart';
import 'features/etudiant/data/api/etudiant_api.dart';
import 'features/etudiant/data/api/notification_etudiant_api.dart';
import 'features/etudiant/data/api/profil_etudiant_api.dart';
import 'features/etudiant/data/controller/ControleurFichier.dart';
import 'features/etudiant/data/controller/controleur_accueil_etudiant.dart';
import 'features/etudiant/data/controller/controleur_annonce_etudiant.dart';
import 'features/etudiant/data/controller/controleur_cours_etudiant.dart';
import 'features/etudiant/data/controller/controleur_emploi_etudiant.dart';
import 'features/etudiant/data/controller/controleur_notification_etudiant.dart';
import 'features/etudiant/data/controller/controleur_profil_etudiant.dart';
import 'features/etudiant/data/controller/controleur_releve_notes_etudiant.dart';
import 'features/etudiant/data/realtime/notification_realtime_service.dart';
import 'features/etudiant/data/repository/annonce_etudiant_repository.dart';
import 'features/etudiant/data/repository/etudiant_repository.dart';
import 'features/etudiant/data/repository/fichier_repository.dart';
import 'features/etudiant/data/repository/notification_etudiant_repository.dart';
import 'features/etudiant/data/repository/profil_etudiant_repository.dart';
import 'features/etudiant/data/repository/releve_notes_repo.dart';
import 'features/etudiant/presentation/accueil_etudiant.dart';
import 'features/etudiant/presentation/etudiant_shell.dart';
import 'features/etudiant/presentation/page_cours_etudiant.dart';
import 'features/etudiant/presentation/page_emploi_etudiant.dart';
import 'features/etudiant/presentation/page_liste_annonces_etudiant.dart';
import 'features/etudiant/presentation/page_notifications_etudiant.dart';
import 'features/etudiant/presentation/page_presence_etudiant.dart';
import 'features/etudiant/presentation/page_profil_etudiant.dart';
import 'features/professeur/data/api/professeur_api.dart';
import 'features/professeur/data/api/notification_professeur_api.dart';
import 'features/professeur/data/api/ressource_seance_professeur_api.dart';
import 'features/professeur/data/controller/controleur_cours_professeur.dart';
import 'features/professeur/data/controller/controleur_espace_professeur.dart';
import 'features/professeur/data/controller/controleur_notification_professeur.dart';
import 'features/professeur/data/repository/notification_professeur_repository.dart';
import 'features/professeur/data/repository/professeur_repository.dart';
import 'features/professeur/data/repository/ressource_seance_professeur_repository.dart';
import 'features/professeur/presentation/professeur_shell.dart';

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
        Provider<AnnonceEtudiantApi>(
          create: (_) => AnnonceEtudiantApi(ClientDio.dioInstance),
        ),
        Provider<NotificationEtudiantApi>(
          create: (_) => NotificationEtudiantApi(ClientDio.dioInstance),
        ),
        Provider<ProfilEtudiantApi>(
          create: (_) => ProfilEtudiantApi(ClientDio.dioInstance),
        ),
        Provider<ProfesseurApi>(
          create: (_) => ProfesseurApi(ClientDio.dioInstance),
        ),
        Provider<NotificationProfesseurApi>(
          create: (_) => NotificationProfesseurApi(ClientDio.dioInstance),
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
        Provider<ProfilEtudiantRepository>(
          create:
              (ctx) => ProfilEtudiantRepository(ctx.read<ProfilEtudiantApi>()),
        ),
        Provider<ProfesseurRepository>(
          create: (ctx) => ProfesseurRepository(ctx.read<ProfesseurApi>()),
        ),
        Provider<RessourceSeanceProfesseurApi>(
          create: (_) => RessourceSeanceProfesseurApi(ClientDio.dioInstance),
        ),
        Provider<RessourceSeanceProfesseurRepository>(
          create: (ctx) => RessourceSeanceProfesseurRepository(
            ctx.read<RessourceSeanceProfesseurApi>(),
          ),
        ),
        Provider<NotificationProfesseurRepository>(
          create:
              (ctx) => NotificationProfesseurRepository(
                ctx.read<NotificationProfesseurApi>(),
              ),
        ),
        Provider<AnnonceEtudiantRepository>(
          create:
              (ctx) =>
                  AnnonceEtudiantRepository(ctx.read<AnnonceEtudiantApi>()),
        ),
        Provider<NotificationEtudiantRepository>(
          create:
              (ctx) => NotificationEtudiantRepository(
                ctx.read<NotificationEtudiantApi>(),
              ),
        ),
        Provider<NotificationRealtimeService>(
          create: (_) => NotificationRealtimeService(),
          dispose: (_, service) => service.disconnect(),
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
          ProfilEtudiantRepository,
          ControleurProfilEtudiant
        >(
          create:
              (ctx) => ControleurProfilEtudiant(
                ctx.read<ProfilEtudiantRepository>(),
              ),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurProfilEtudiant(repo),
        ),
        ChangeNotifierProxyProvider<
          ProfesseurRepository,
          ControleurEspaceProfesseur
        >(
          create:
              (ctx) =>
                  ControleurEspaceProfesseur(ctx.read<ProfesseurRepository>()),
          update:
              (ctx, repo, ctrl) => ctrl ?? ControleurEspaceProfesseur(repo),
        ),
        ChangeNotifierProxyProvider2<
          ProfesseurRepository,
          RessourceSeanceProfesseurRepository,
          ControleurCoursProfesseur
        >(
          create: (ctx) => ControleurCoursProfesseur(
            ctx.read<ProfesseurRepository>(),
            ctx.read<RessourceSeanceProfesseurRepository>(),
          ),
          update: (ctx, repo, resRepo, ctrl) =>
              ctrl ?? ControleurCoursProfesseur(repo, resRepo),
        ),
        ChangeNotifierProxyProvider<
          AnnonceEtudiantRepository,
          ControleurAnnonceEtudiant
        >(
          create:
              (ctx) => ControleurAnnonceEtudiant(
                ctx.read<AnnonceEtudiantRepository>(),
              ),
          update: (ctx, repo, ctrl) => ctrl ?? ControleurAnnonceEtudiant(repo),
        ),
        ChangeNotifierProxyProvider<
          NotificationEtudiantRepository,
          ControleurNotificationEtudiant
        >(
          create:
              (ctx) => ControleurNotificationEtudiant(
                ctx.read<NotificationEtudiantRepository>(),
              ),
          update:
              (ctx, repo, ctrl) => ctrl ?? ControleurNotificationEtudiant(repo),
        ),
        ChangeNotifierProxyProvider<
          NotificationProfesseurRepository,
          ControleurNotificationProfesseur
        >(
          create:
              (ctx) => ControleurNotificationProfesseur(
                ctx.read<NotificationProfesseurRepository>(),
              ),
          update:
              (ctx, repo, ctrl) =>
                  ctrl ?? ControleurNotificationProfesseur(repo),
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
                builder: (_) => const PagePresenceEtudiant(),
              );

            case '/etudiant/calendrier':
              return MaterialPageRoute(
                settings: settings,
                builder: (_) => const PageEmploiTimelineEtudiant(),
              );

            case '/etudiant/annonces':
              return MaterialPageRoute(
                settings: settings,
                builder: (_) => const PageListeAnnoncesEtudiant(),
              );
            case '/etudiant/notifications':
              return MaterialPageRoute(
                settings: settings,
                builder: (_) => const PageNotificationsEtudiant(),
              );
            case '/etudiant/profil':
              return MaterialPageRoute(
                settings: settings,
                builder: (_) => const PageProfilEtudiant(),
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
  bool _initProfesseurFait = false;
  bool _wsEtudiantConnecte = false;

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
          _initProfesseurFait = false;
          if (_wsEtudiantConnecte) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (!mounted) return;
              context.read<NotificationRealtimeService>().disconnect();
              _wsEtudiantConnecte = false;
            });
          }
          return const PageConnexion();
        }

        switch (controleur.roleUtilisateur) {
          case 'ETUDIANT':
            _initProfesseurFait = false;
            if (!_initEtudiantFait) {
              _initEtudiantFait = true;
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                context.read<ControleurProfilEtudiant>().chargerMoi();
                context.read<ControleurAccueilEtudiant>().chargerDashboard();
                context.read<ControleurAnnonceEtudiant>().chargerAnnonces();
                final ctrlNotification =
                    context.read<ControleurNotificationEtudiant>();
                ctrlNotification.rafraichirCompteurNonLues();

                context.read<NotificationRealtimeService>().connect(
                  onNotification: ctrlNotification.ajouterNotificationRealtime,
                );
                _wsEtudiantConnecte = true;
              });
            }
            return const EtudiantShell();

          case 'PROFESSEUR':
            _initEtudiantFait = false;
            if (!_initProfesseurFait) {
              _initProfesseurFait = true;
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                context
                    .read<ControleurNotificationProfesseur>()
                    .rafraichirCompteurNonLues();
              });
            }
            if (_wsEtudiantConnecte) {
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                context.read<NotificationRealtimeService>().disconnect();
                _wsEtudiantConnecte = false;
              });
            }
            return const ProfesseurShell();

          default:
            _initEtudiantFait = false;
            _initProfesseurFait = false;
            if (_wsEtudiantConnecte) {
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                context.read<NotificationRealtimeService>().disconnect();
                _wsEtudiantConnecte = false;
              });
            }
            return const PageConnexion();
        }
      },
    );
  }
}
