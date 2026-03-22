import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../network/client_dio.dart';

class PhotoSecurisee extends StatefulWidget {
  const PhotoSecurisee({
    super.key,
    required this.photoUrl,
    this.fit = BoxFit.cover,
    this.placeholder,
    this.error,
  });

  /// URL retournee par le backend, typiquement `/api/fichiers/photos/{nom}`.
  /// Peut aussi etre une URL absolue.
  final String? photoUrl;
  final BoxFit fit;
  final Widget? placeholder;
  final Widget? error;

  @override
  State<PhotoSecurisee> createState() => _PhotoSecuriseeState();
}

class _PhotoSecuriseeState extends State<PhotoSecurisee> {
  static final Map<String, Uint8List> _cache = <String, Uint8List>{};

  Future<Uint8List>? _future;
  String? _lastUrl;

  @override
  void initState() {
    super.initState();
    _syncFuture();
  }

  @override
  void didUpdateWidget(covariant PhotoSecurisee oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.photoUrl != widget.photoUrl) {
      _syncFuture();
    }
  }

  void _syncFuture() {
    final url = widget.photoUrl?.trim();
    _lastUrl = url;
    if (url == null || url.isEmpty) {
      _future = null;
      return;
    }

    final cached = _cache[url];
    if (cached != null) {
      _future = Future.value(cached);
      return;
    }

    _future = _downloadBytes(url);
  }

  Future<Uint8List> _downloadBytes(String url) async {
    final dio = ClientDio.dioInstance;
    final response = await dio.get<List<int>>(
      url,
      options: Options(responseType: ResponseType.bytes),
    );
    final bytes = Uint8List.fromList(response.data ?? const <int>[]);
    _cache[url] = bytes;
    return bytes;
  }

  @override
  Widget build(BuildContext context) {
    final url = widget.photoUrl?.trim();
    if (url == null || url.isEmpty || _future == null) {
      return widget.placeholder ?? const SizedBox.shrink();
    }

    return FutureBuilder<Uint8List>(
      future: _future,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return widget.placeholder ?? const SizedBox.shrink();
        }
        if (snapshot.hasError || !snapshot.hasData || snapshot.data!.isEmpty) {
          return widget.error ?? widget.placeholder ?? const SizedBox.shrink();
        }

        return Image.memory(
          snapshot.data!,
          fit: widget.fit,
          gaplessPlayback: true,
          errorBuilder: (_, __, ___) =>
              widget.error ?? widget.placeholder ?? const SizedBox.shrink(),
        );
      },
    );
  }
}

class AvatarPhotoSecurisee extends StatelessWidget {
  const AvatarPhotoSecurisee({
    super.key,
    required this.photoUrl,
    required this.radius,
    required this.accent,
  });

  final String? photoUrl;
  final double radius;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    final placeholder = Container(
      color: accent.withOpacity(0.12),
      alignment: Alignment.center,
      child: Icon(Icons.person, size: radius, color: accent),
    );

    return ClipOval(
      child: SizedBox(
        width: radius * 2,
        height: radius * 2,
        child: PhotoSecurisee(
          photoUrl: photoUrl,
          placeholder: placeholder,
          error: placeholder,
        ),
      ),
    );
  }
}

