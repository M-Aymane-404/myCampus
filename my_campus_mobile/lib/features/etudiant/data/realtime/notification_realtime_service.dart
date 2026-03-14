import 'dart:convert';

import 'package:stomp_dart_client/stomp_dart_client.dart';

import '../../../../core/config/configuration_app.dart';
import '../../../../core/storage/stockage_securise.dart';
import '../dto/notification_etudiant_dto.dart';

class NotificationRealtimeService {
  StompClient? _client;
  bool _isActive = false;

  bool get isActive => _isActive;

  Future<void> connect({
    required void Function(NotificationEtudiantDTO notification) onNotification,
  }) async {
    if (_isActive) return;

    final token = await StockageSecurise.recupererToken();
    if (token == null || token.isEmpty) return;

    final url = _buildWebSocketUrl(ConfigurationApp.apiBaseUrl);

    _client = StompClient(
      config: StompConfig(
        url: url,
        reconnectDelay: const Duration(seconds: 5),
        stompConnectHeaders: {'Authorization': 'Bearer $token'},
        webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
        onConnect: (frame) {
          _isActive = true;
          _client?.subscribe(
            destination: '/user/queue/notifications',
            callback: (message) {
              final body = message.body;
              if (body == null || body.isEmpty) return;

              final decoded = jsonDecode(body);
              if (decoded is Map<String, dynamic>) {
                onNotification(NotificationEtudiantDTO.fromJson(decoded));
              } else if (decoded is Map) {
                onNotification(
                  NotificationEtudiantDTO.fromJson(
                    decoded.cast<String, dynamic>(),
                  ),
                );
              }
            },
          );
        },
        onDisconnect: (_) => _isActive = false,
        onWebSocketDone: () => _isActive = false,
        onWebSocketError: (_) => _isActive = false,
        onStompError: (_) => _isActive = false,
      ),
    );

    _client?.activate();
  }

  void disconnect() {
    _client?.deactivate();
    _client = null;
    _isActive = false;
  }

  String _buildWebSocketUrl(String baseUrl) {
    final uri = Uri.parse(baseUrl);
    final scheme = uri.scheme == 'https' ? 'wss' : 'ws';
    final host = uri.host;
    final hasPort = uri.hasPort;
    final port = hasPort ? ':${uri.port}' : '';
    return '$scheme://$host$port/ws';
  }
}
