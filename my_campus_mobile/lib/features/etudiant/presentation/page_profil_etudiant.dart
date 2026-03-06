import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../core/config/configuration_app.dart';
import '../../authentification/presentation/controleur_authentification.dart';
import '../data/controller/controleur_profil_etudiant.dart';
import '../data/dto/profil_etudiant_dto.dart';

class PageProfilEtudiant extends StatefulWidget {
  const PageProfilEtudiant({super.key});

  @override
  State<PageProfilEtudiant> createState() => _PageProfilEtudiantState();
}

class _PageProfilEtudiantState extends State<PageProfilEtudiant> {
  final _formKey = GlobalKey<FormState>();
  final _ancienMdp = TextEditingController();
  final _nouveauMdp = TextEditingController();

  @override
  void dispose() {
    _ancienMdp.dispose();
    _nouveauMdp.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurProfilEtudiant>();
    final moi = ctrl.moi;
    final photoUrl = _buildPhotoUrl(moi?.photoUrl);

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Profil',
          style: TextStyle(fontWeight: FontWeight.w900, color: Color(0xFF0B1220)),
        ),
        iconTheme: const IconThemeData(color: Color(0xFF0B1220)),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(18),
          children: [
            if (ctrl.erreur != null) _ErreurBox(message: ctrl.erreur!),
            _Card(
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 32,
                    backgroundColor: const Color(0xFFEEF2FF),
                    backgroundImage: photoUrl != null ? NetworkImage(photoUrl) : null,
                    child:
                        photoUrl == null
                            ? const Icon(Icons.person, color: Color(0xFF5B5BEA), size: 32)
                            : null,
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${moi?.prenom ?? ''} ${moi?.nom ?? ''}'.trim().isEmpty
                              ? 'Etudiant'
                              : '${moi!.prenom} ${moi.nom}',
                          style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          moi?.email ?? '',
                          style: const TextStyle(
                            color: Color(0xFF61708A),
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 10),
                  ElevatedButton.icon(
                    onPressed: ctrl.chargement ? null : () => _changerPhoto(context),
                    icon: const Icon(Icons.photo_camera),
                    label: const Text('Photo'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 14),
            _Card(
              child: Form(
                key: _formKey,
                child: Column(
                  children: [
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Changer mot de passe (optionnel)',
                        style: TextStyle(fontWeight: FontWeight.w900),
                      ),
                    ),
                    const SizedBox(height: 10),
                    _Field(
                      label: 'Ancien mot de passe',
                      controller: _ancienMdp,
                      obscure: true,
                      validator: (v) {
                        final value = (v ?? '').trim();
                        final hasNew = _nouveauMdp.text.trim().isNotEmpty;
                        if (value.isEmpty) return hasNew ? 'Ancien mot de passe requis' : null;
                        if (value.length < 8) return 'Minimum 8 caracteres';
                        return null;
                      },
                    ),
                    _Field(
                      label: 'Nouveau mot de passe',
                      controller: _nouveauMdp,
                      obscure: true,
                      validator: (v) {
                        final value = (v ?? '').trim();
                        final hasOld = _ancienMdp.text.trim().isNotEmpty;
                        if (value.isEmpty) return hasOld ? 'Nouveau mot de passe requis' : null;
                        if (value.length < 8) return 'Minimum 8 caracteres';
                        return null;
                      },
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      height: 46,
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: ctrl.chargement ? null : () => _enregistrer(context),
                        child:
                            ctrl.chargement
                                ? const SizedBox(
                                  width: 18,
                                  height: 18,
                                  child: CircularProgressIndicator(strokeWidth: 2),
                                )
                                : const Text(
                                  'Enregistrer',
                                  style: TextStyle(fontWeight: FontWeight.w900),
                                ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            _Card(
              child: SizedBox(
                height: 46,
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed:
                      ctrl.chargement
                          ? null
                          : () async {
                            await context.read<ControleurAuthentification>().seDeconnecter();
                          },
                  icon: const Icon(Icons.logout),
                  label: const Text('Deconnexion', style: TextStyle(fontWeight: FontWeight.w900)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _enregistrer(BuildContext context) async {
    if (!_formKey.currentState!.validate()) return;

    final dto = ModifierProfilEtudiantDTO(
      ancienPassword: _ancienMdp.text.trim().isEmpty ? null : _ancienMdp.text.trim(),
      nouveauPassword: _nouveauMdp.text.trim().isEmpty ? null : _nouveauMdp.text.trim(),
    );

    await context.read<ControleurProfilEtudiant>().modifierProfil(dto);

    if (!mounted) return;
    final err = context.read<ControleurProfilEtudiant>().erreur;
    if (err == null) {
      _ancienMdp.clear();
      _nouveauMdp.clear();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Profil mis a jour')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(err)),
      );
    }
  }

  Future<void> _changerPhoto(BuildContext context) async {
    try {
      final picker = ImagePicker();
      final picked = await picker.pickImage(source: ImageSource.gallery, imageQuality: 85);
      if (picked == null || !mounted) return;

      await context.read<ControleurProfilEtudiant>().changerPhoto(File(picked.path));

      if (!mounted) return;
      final err = context.read<ControleurProfilEtudiant>().erreur;
      if (err == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Photo de profil mise a jour')),
        );
      }
    } on PlatformException {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Service photo indisponible. Redemarrez completement l app puis reessayez.',
          ),
        ),
      );
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Erreur lors de la selection de la photo')),
      );
    }
  }

  String? _buildPhotoUrl(String? photoUrl) {
    if (photoUrl == null || photoUrl.trim().isEmpty) return null;
    return '${ConfigurationApp.apiBaseUrl}$photoUrl';
  }
}

class _Card extends StatelessWidget {
  const _Card({required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.black.withOpacity(0.05)),
      ),
      child: child,
    );
  }
}

class _Field extends StatelessWidget {
  const _Field({
    required this.label,
    required this.controller,
    this.obscure = false,
    this.validator,
  });

  final String label;
  final TextEditingController controller;
  final bool obscure;
  final String? Function(String?)? validator;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: TextFormField(
        controller: controller,
        obscureText: obscure,
        decoration: InputDecoration(
          labelText: label,
          filled: true,
          fillColor: const Color(0xFFF7F9FF),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
        ),
        validator: validator,
      ),
    );
  }
}

class _ErreurBox extends StatelessWidget {
  const _ErreurBox({required this.message});
  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF1F2),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.red.withOpacity(0.2)),
      ),
      child: Row(
        children: [
          const Icon(Icons.error_outline, color: Colors.red),
          const SizedBox(width: 10),
          Expanded(child: Text(message)),
        ],
      ),
    );
  }
}
