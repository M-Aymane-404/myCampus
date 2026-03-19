import 'package:flutter/material.dart';

import '../../../../core/config/configuration_app.dart';

class AvatarMiniProfesseur extends StatelessWidget {
  const AvatarMiniProfesseur({
    super.key,
    required this.photoUrl,
    required this.accent,
  });

  final String? photoUrl;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    final url = _buildPhotoUrl(photoUrl);
    if (url != null) {
      return ClipOval(
        child: SizedBox.expand(
          child: Image.network(
            url,
            fit: BoxFit.cover,
            errorBuilder:
                (_, __, ___) => Container(
                  color: accent.withOpacity(0.12),
                  alignment: Alignment.center,
                  child: Icon(Icons.person, size: 14, color: accent),
                ),
          ),
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        color: accent.withOpacity(0.12),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Icon(Icons.person, size: 14, color: accent),
    );
  }
}

String? _buildPhotoUrl(String? photoUrl) {
  if (photoUrl == null || photoUrl.trim().isEmpty) return null;
  return '${ConfigurationApp.apiBaseUrl}$photoUrl';
}
