import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/controller/controleur_annonce_professeur.dart';
import '../data/controller/controleur_notification_professeur.dart';
import '../data/dto/notification_professeur_dto.dart';
import 'page_detail_annonce_professeur.dart';

class PageNotificationsProfesseur extends StatefulWidget {
  const PageNotificationsProfesseur({super.key});

  @override
  State<PageNotificationsProfesseur> createState() =>
      _PageNotificationsProfesseurState();
}

class _PageNotificationsProfesseurState
    extends State<PageNotificationsProfesseur> {
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<ControleurNotificationProfesseur>().chargerNotifications();
    });
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurNotificationProfesseur>();

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Notifications',
          style: TextStyle(
            color: Color(0xFF0B1220),
            fontWeight: FontWeight.w900,
          ),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
        actions: [
          if (ctrl.aDesNotificationsNonLues)
            TextButton(
              onPressed: () => ctrl.marquerToutesCommeLues(),
              child: const Text('Tout lire'),
            ),
        ],
      ),
      body: Builder(
        builder: (_) {
          if (ctrl.chargement && ctrl.notifications.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (ctrl.erreur != null && ctrl.notifications.isEmpty) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Text(ctrl.erreur!, textAlign: TextAlign.center),
              ),
            );
          }

          if (ctrl.notifications.isEmpty) {
            return const Center(child: Text('Aucune notification.'));
          }

          return RefreshIndicator(
            onRefresh: () => ctrl.chargerNotifications(),
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: ctrl.notifications.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (_, i) {
                final notif = ctrl.notifications[i];
                return _NotificationTileProfesseur(
                  notification: notif,
                  onTap: () async {
                    if (!notif.lu) {
                      await ctrl.marquerCommeLue(notif.id);
                    }

                    final referenceType =
                        notif.referenceType?.toUpperCase() ?? '';
                    if (referenceType == 'ANNONCE' && notif.referenceId != null) {
                      final annonceCtrl =
                          context.read<ControleurAnnonceProfesseur>();
                      if (!annonceCtrl.chargement &&
                          annonceCtrl.annoncesPubliques.isEmpty) {
                        await annonceCtrl.chargerAnnoncesPubliques();
                      }
                      await annonceCtrl.chargerAnnonceParId(notif.referenceId!);
                      if (!context.mounted) return;
                      await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => PageDetailAnnonceProfesseur(
                            annonceId: notif.referenceId!,
                          ),
                        ),
                      );
                    }
                  },
                );
              },
            ),
          );
        },
      ),
    );
  }
}

class _NotificationTileProfesseur extends StatelessWidget {
  const _NotificationTileProfesseur({
    required this.notification,
    required this.onTap,
  });

  final NotificationProfesseurDTO notification;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final bg = notification.lu ? Colors.white : const Color(0xFFEEF2FF);

    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.black.withOpacity(0.06)),
        ),
        child: Row(
          children: [
            Container(
              width: 11,
              height: 11,
              decoration: BoxDecoration(
                color:
                    notification.lu
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
                    notification.titre,
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
                    notification.message,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 13,
                      color: Color(0xFF61708A),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    notification.dateEnvoi,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: Color(0xFF94A3B8),
                      fontWeight: FontWeight.w700,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 8),
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
