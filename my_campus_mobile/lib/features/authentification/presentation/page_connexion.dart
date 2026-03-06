import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'controleur_authentification.dart';

class PageConnexion extends StatefulWidget {
  const PageConnexion({super.key});

  @override
  State<PageConnexion> createState() => _PageConnexionState();
}

class _PageConnexionState extends State<PageConnexion> {
  final _cleFormulaire = GlobalKey<FormState>();

  final _controleurEmail = TextEditingController();
  final _controleurMotDePasse = TextEditingController();

  bool _motDePasseVisible = false;

  @override
  void dispose() {
    _controleurEmail.dispose();
    _controleurMotDePasse.dispose();
    super.dispose();
  }

  String? _validerEmail(String? valeur) {
    final v = valeur?.trim() ?? '';
    if (v.isEmpty) return 'Veuillez saisir votre email';
    final regex = RegExp(r'^[^@]+@[^@]+\.[^@]+$');
    if (!regex.hasMatch(v)) return 'Email invalide';
    return null;
  }

  String? _validerMotDePasse(String? valeur) {
    final v = valeur ?? '';
    if (v.isEmpty) return 'Veuillez saisir votre mot de passe';
    if (v.length < 4) return 'Mot de passe trop court';
    return null;
  }

  Future<void> _soumettre() async {
    final estValide = _cleFormulaire.currentState?.validate() ?? false;
    if (!estValide) return;

    FocusScope.of(context).unfocus();

    final email = _controleurEmail.text.trim();
    final motDePasse = _controleurMotDePasse.text;

    try {
      await context.read<ControleurAuthentification>().seConnecter(
        email,
        motDePasse,
      );
    } catch (_) {
      if (!mounted) return;
      final message =
          context.read<ControleurAuthentification>().erreur ??
          'Erreur de connexion';

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(message),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  InputDecoration _decorChamp({
    required String hint,
    required IconData icon,
    Widget? suffix,
  }) {
    return InputDecoration(
      hintText: hint,
      prefixIcon: Icon(icon),
      suffixIcon: suffix,
      filled: true,
      fillColor: const Color(0xFFF4F6FB),
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 16),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: Colors.black.withOpacity(0.06)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: Colors.black.withOpacity(0.18)),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: Colors.redAccent),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: Colors.redAccent),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<ControleurAuthentification>();

    return Scaffold(
      backgroundColor: const Color(0xFFF5F6FB),
      body: SafeArea(
        child: Stack(
          children: [
            // Background blobs (no interaction)
            Positioned(
              top: -120,
              left: -120,
              child: IgnorePointer(
                child: _Blob(
                  size: 260,
                  color: const Color(0xFF5B5BEA).withOpacity(0.16),
                ),
              ),
            ),
            Positioned(
              right: -120,
              bottom: -140,
              child: IgnorePointer(
                child: _Blob(
                  size: 300,
                  color: const Color(0xFF2B2E8F).withOpacity(0.14),
                ),
              ),
            ),

            // Full screen content (scrollable)
            Positioned.fill(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(22, 22, 22, 28),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Brand header
                    Row(
                      children: [
                        Container(
                          width: 42,
                          height: 42,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(14),
                            boxShadow: [
                              BoxShadow(
                                blurRadius: 18,
                                offset: const Offset(0, 10),
                                color: Colors.black.withOpacity(0.08),
                              ),
                            ],
                          ),
                          child: const Center(
                            child: Text(
                              'M',
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                fontSize: 18,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'MyCampus',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 28),

                    const Text(
                      'Bon retour !',
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.w900,
                        letterSpacing: -0.4,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      "Connectez-vous pour continuer l'aventure MyCampus.",
                      style: TextStyle(
                        fontSize: 14,
                        height: 1.35,
                        color: Colors.black.withOpacity(0.62),
                      ),
                    ),

                    const SizedBox(height: 26),

                    // Form
                    Form(
                      key: _cleFormulaire,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          const _Label('E-MAIL INSTITUTIONNEL'),
                          const SizedBox(height: 8),
                          TextFormField(
                            controller: _controleurEmail,
                            validator: _validerEmail,
                            keyboardType: TextInputType.emailAddress,
                            textInputAction: TextInputAction.next,
                            decoration: _decorChamp(
                              hint: 'demo@mycampus.edu',
                              icon: Icons.mail_outline,
                            ),
                          ),

                          const SizedBox(height: 16),

                          const _Label('MOT DE PASSE'),
                          const SizedBox(height: 8),
                          TextFormField(
                            controller: _controleurMotDePasse,
                            validator: _validerMotDePasse,
                            obscureText: !_motDePasseVisible,
                            textInputAction: TextInputAction.done,
                            onFieldSubmitted: (_) => _soumettre(),
                            decoration: _decorChamp(
                              hint: '••••••••',
                              icon: Icons.lock_outline,
                              suffix: IconButton(
                                tooltip:
                                    _motDePasseVisible ? 'Masquer' : 'Afficher',
                                onPressed:
                                    () => setState(
                                      () =>
                                          _motDePasseVisible =
                                              !_motDePasseVisible,
                                    ),
                                icon: Icon(
                                  _motDePasseVisible
                                      ? Icons.visibility_outlined
                                      : Icons.visibility_off_outlined,
                                ),
                              ),
                            ),
                          ),

                          const SizedBox(height: 22),

                          SizedBox(
                            height: 54,
                            child: ElevatedButton(
                              onPressed: auth.chargement ? null : _soumettre,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFF0B1220),
                                foregroundColor: Colors.white,
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(16),
                                ),
                              ),
                              child:
                                  auth.chargement
                                      ? const SizedBox(
                                        width: 22,
                                        height: 22,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                        ),
                                      )
                                      : Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        children: const [
                                          Text(
                                            'Se connecter',
                                            style: TextStyle(
                                              fontSize: 15.5,
                                              fontWeight: FontWeight.w800,
                                            ),
                                          ),
                                          SizedBox(width: 10),
                                          Icon(
                                            Icons.arrow_forward_rounded,
                                            size: 18,
                                          ),
                                        ],
                                      ),
                            ),
                          ),

                          const SizedBox(height: 14),

                          AnimatedSwitcher(
                            duration: const Duration(milliseconds: 180),
                            child:
                                auth.estAuthentifie
                                    ? const Text(
                                      'Authentifié ✅',
                                      key: ValueKey('ok'),
                                      textAlign: TextAlign.center,
                                      style: TextStyle(
                                        color: Colors.green,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    )
                                    : const SizedBox.shrink(
                                      key: ValueKey('empty'),
                                    ),
                          ),

                          const SizedBox(height: 18),

                          Text(
                            "Besoin d'aide ? Accéder au support informatique",
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 12.5,
                              color: Colors.black.withOpacity(0.55),
                              decoration: TextDecoration.underline,
                            ),
                          ),

                          const SizedBox(height: 26),

                          Text(
                            '© ${DateTime.now().year} MyCampus',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.black.withOpacity(0.45),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Label extends StatelessWidget {
  const _Label(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: TextStyle(
        fontSize: 11,
        fontWeight: FontWeight.w800,
        letterSpacing: 0.7,
        color: Colors.black.withOpacity(0.55),
      ),
    );
  }
}

class _Blob extends StatelessWidget {
  const _Blob({required this.size, required this.color});
  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: DecoratedBox(
        decoration: BoxDecoration(color: color, shape: BoxShape.circle),
      ),
    );
  }
}
