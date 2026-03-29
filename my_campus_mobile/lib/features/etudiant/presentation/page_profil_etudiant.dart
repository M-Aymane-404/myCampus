import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../core/widgets/photo_securisee.dart';
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
  final _telephone = TextEditingController();
  final _ancienMdp = TextEditingController();
  final _nouveauMdp = TextEditingController();
  bool _dejaCharge = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_dejaCharge) return;
    _dejaCharge = true;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final ctrl = context.read<ControleurProfilEtudiant>();
      if (ctrl.moi == null && !ctrl.chargement) {
        ctrl.chargerMoi();
      }
    });
  }

  @override
  void dispose() {
    _telephone.dispose();
    _ancienMdp.dispose();
    _nouveauMdp.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ctrl = context.watch<ControleurProfilEtudiant>();
    final moi = ctrl.moi;

    if (ctrl.chargement && moi == null) {
      return const Scaffold(
        backgroundColor: Color(0xFFF5F7FB),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (ctrl.erreur != null && moi == null) {
      return Scaffold(
        backgroundColor: const Color(0xFFF5F7FB),
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: _EmptyState(
                message: ctrl.erreur!,
                actionLabel: 'Reessayer',
                onTap: () => context.read<ControleurProfilEtudiant>().chargerMoi(),
              ),
            ),
          ),
        ),
      );
    }

    if (_telephone.text.isEmpty && moi?.tel.isNotEmpty == true) {
      _telephone.text = moi!.tel;
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FB),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () => context.read<ControleurProfilEtudiant>().chargerMoi(),
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 24),
            children: [
              const _SectionTitle(
                titre: 'Profil',
                sousTitre: 'Compte etudiant et informations principales',
              ),
              const SizedBox(height: 18),
              if (ctrl.erreur != null) ...[
                _ErrorBanner(message: ctrl.erreur!),
                const SizedBox(height: 14),
              ],
              _ProfileHeroCard(
                profil: moi,
                chargement: ctrl.chargement,
                onChangePhoto:
                    ctrl.chargement ? null : () => _changerPhoto(context),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: Column(
                  children: [
                    _ProfileAction(
                      icon: Icons.phone_rounded,
                      titre:
                          moi == null || moi.tel.trim().isEmpty
                              ? 'Telephone non renseigne'
                              : moi.tel,
                      sousTitre: 'Contact principal',
                    ),
                    const SizedBox(height: 10),
                    _ProfileAction(
                      icon: Icons.cake_rounded,
                      titre:
                          moi == null || moi.dateNaissance.trim().isEmpty
                              ? 'Date de naissance non renseignee'
                              : moi.dateNaissance,
                      sousTitre: 'Date de naissance',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Profil et securite',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF101828),
                        ),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Modifier votre telephone et votre mot de passe',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF667085),
                        ),
                      ),
                      const SizedBox(height: 14),
                      _Field(
                        label: 'Telephone',
                        controller: _telephone,
                        keyboardType: TextInputType.phone,
                        validator: (v) {
                          final value = (v ?? '').trim();
                          if (value.isEmpty) return null;
                          if (value.length < 8) {
                            return 'Numero de telephone invalide';
                          }
                          return null;
                        },
                      ),
                      _Field(
                        label: 'Ancien mot de passe',
                        controller: _ancienMdp,
                        obscure: true,
                        validator: (v) {
                          final value = (v ?? '').trim();
                          final hasNew = _nouveauMdp.text.trim().isNotEmpty;
                          if (value.isEmpty) {
                            return hasNew ? 'Ancien mot de passe requis' : null;
                          }
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
                          if (value.isEmpty) {
                            return hasOld ? 'Nouveau mot de passe requis' : null;
                          }
                          if (value.length < 8) return 'Minimum 8 caracteres';
                          return null;
                        },
                      ),
                      const SizedBox(height: 14),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed:
                              ctrl.chargement ? null : () => _enregistrer(context),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF5B5BEA),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(18),
                            ),
                          ),
                          child:
                              ctrl.chargement
                                  ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  )
                                  : const Text(
                                    'Enregistrer',
                                    style: TextStyle(fontWeight: FontWeight.w800),
                                  ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),
              _SurfaceCard(
                child: SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed:
                        ctrl.chargement
                            ? null
                            : () async {
                              await context
                                  .read<ControleurAuthentification>()
                                  .seDeconnecter();
                            },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF5B5BEA),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(18),
                      ),
                    ),
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text(
                      'Se deconnecter',
                      style: TextStyle(fontWeight: FontWeight.w800),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _enregistrer(BuildContext context) async {
    if (!_formKey.currentState!.validate()) return;

    final dto = ModifierProfilEtudiantDTO(
      tel: _telephone.text.trim().isEmpty ? null : _telephone.text.trim(),
      ancienPassword:
          _ancienMdp.text.trim().isEmpty ? null : _ancienMdp.text.trim(),
      nouveauPassword:
          _nouveauMdp.text.trim().isEmpty ? null : _nouveauMdp.text.trim(),
    );

    await context.read<ControleurProfilEtudiant>().modifierProfil(dto);

    if (!mounted) return;
    final err = context.read<ControleurProfilEtudiant>().erreur;
    if (err == null) {
      _ancienMdp.clear();
      _nouveauMdp.clear();
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Profil mis a jour')));
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(err)));
    }
  }

  Future<void> _changerPhoto(BuildContext context) async {
    try {
      final picker = ImagePicker();
      final picked = await picker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 85,
      );
      if (picked == null || !mounted) return;

      await context
          .read<ControleurProfilEtudiant>()
          .changerPhoto(File(picked.path));

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
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.titre, required this.sousTitre});

  final String titre;
  final String sousTitre;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          titre,
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w900,
            color: Color(0xFF101828),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          sousTitre,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: Color(0xFF667085),
          ),
        ),
      ],
    );
  }
}

class _ProfileHeroCard extends StatelessWidget {
  const _ProfileHeroCard({
    required this.profil,
    required this.chargement,
    required this.onChangePhoto,
  });

  final ProfilEtudiantDTO? profil;
  final bool chargement;
  final VoidCallback? onChangePhoto;

  @override
  Widget build(BuildContext context) {
    final fullName = _buildNomComplet(profil);

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          AvatarPhotoSecurisee(
            photoUrl: profil?.photoUrl,
            radius: 34,
            accent: const Color(0xFF5B5BEA),
          ),
          const SizedBox(height: 14),
          Text(
            fullName,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w900,
              color: Color(0xFF101828),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            profil?.email ?? '',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: Color(0xFF667085),
            ),
          ),
          const SizedBox(height: 14),
          OutlinedButton.icon(
            onPressed: onChangePhoto,
            style: OutlinedButton.styleFrom(
              foregroundColor: const Color(0xFF5B5BEA),
              side: BorderSide(color: const Color(0xFF5B5BEA).withOpacity(0.2)),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
              ),
            ),
            icon:
                chargement
                    ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                    : const Icon(Icons.photo_camera_rounded),
            label: const Text(
              'Changer la photo',
              style: TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
        ],
      ),
    );
  }
}

class _SurfaceCard extends StatelessWidget {
  const _SurfaceCard({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: child,
    );
  }
}

class _ProfileAction extends StatelessWidget {
  const _ProfileAction({
    required this.icon,
    required this.titre,
    required this.sousTitre,
  });

  final IconData icon;
  final String titre;
  final String sousTitre;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F8FC),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: const Color(0xFF5B5BEA)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  titre,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w800,
                    color: Color(0xFF101828),
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  sousTitre,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF667085),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Field extends StatelessWidget {
  const _Field({
    required this.label,
    required this.controller,
    this.obscure = false,
    this.validator,
    this.keyboardType,
  });

  final String label;
  final TextEditingController controller;
  final bool obscure;
  final String? Function(String?)? validator;
  final TextInputType? keyboardType;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: TextFormField(
        controller: controller,
        obscureText: obscure,
        keyboardType: keyboardType,
        decoration: InputDecoration(
          labelText: label,
          filled: true,
          fillColor: const Color(0xFFF7F9FF),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide(color: Colors.black.withOpacity(0.08)),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide(color: Colors.black.withOpacity(0.06)),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: const BorderSide(color: Color(0xFF5B5BEA)),
          ),
        ),
        validator: validator,
      ),
    );
  }
}

class _ErrorBanner extends StatelessWidget {
  const _ErrorBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF1F2),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.red.withOpacity(0.15)),
      ),
      child: Row(
        children: [
          const Icon(Icons.error_outline_rounded, color: Colors.red),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({
    required this.message,
    this.actionLabel,
    this.onTap,
  });

  final String message;
  final String? actionLabel;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.inbox_rounded, size: 34, color: Color(0xFF98A2B3)),
          const SizedBox(height: 12),
          Text(
            message,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: Color(0xFF667085),
            ),
          ),
          if (actionLabel != null && onTap != null) ...[
            const SizedBox(height: 12),
            OutlinedButton(onPressed: onTap, child: Text(actionLabel!)),
          ],
        ],
      ),
    );
  }
}

String _buildNomComplet(ProfilEtudiantDTO? profil) {
  if (profil == null) return 'Etudiant';
  final value = '${profil.prenom} ${profil.nom}'.trim();
  return value.isEmpty ? 'Etudiant' : value;
}
