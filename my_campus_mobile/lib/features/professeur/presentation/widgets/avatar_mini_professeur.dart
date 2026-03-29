import 'package:flutter/material.dart';

import '../../../../core/widgets/photo_securisee.dart';

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
    final url = photoUrl?.trim();
    final fallback = Container(
      decoration: BoxDecoration(
        color: accent.withOpacity(0.12),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Icon(Icons.person, size: 14, color: accent),
    );

    if (url == null || url.isEmpty) {
      return fallback;
    }

    return ClipOval(
      child: SizedBox.expand(
        child: PhotoSecurisee(
          photoUrl: url,
          placeholder: fallback,
          error: fallback,
        ),
      ),
    );
  }
}
