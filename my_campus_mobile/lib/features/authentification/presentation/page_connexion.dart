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

      // Rien à faire ici : le routing dans main.dart va basculer automatiquement.
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Échec connexion: $e'),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<ControleurAuthentification>();

    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: Form(
              key: _cleFormulaire,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'MyCampus',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 34, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Connexion',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                  ),
                  const SizedBox(height: 32),

                  TextFormField(
                    controller: _controleurEmail,
                    decoration: const InputDecoration(
                      labelText: 'Email',
                      border: OutlineInputBorder(),
                      prefixIcon: Icon(Icons.email),
                    ),
                    keyboardType: TextInputType.emailAddress,
                    textInputAction: TextInputAction.next,
                    validator: _validerEmail,
                  ),
                  const SizedBox(height: 16),

                  TextFormField(
                    controller: _controleurMotDePasse,
                    decoration: InputDecoration(
                      labelText: 'Mot de passe',
                      border: const OutlineInputBorder(),
                      prefixIcon: const Icon(Icons.lock),
                      suffixIcon: IconButton(
                        onPressed:
                            () => setState(
                              () => _motDePasseVisible = !_motDePasseVisible,
                            ),
                        icon: Icon(
                          _motDePasseVisible
                              ? Icons.visibility
                              : Icons.visibility_off,
                        ),
                      ),
                    ),
                    obscureText: !_motDePasseVisible,
                    textInputAction: TextInputAction.done,
                    validator: _validerMotDePasse,
                    onFieldSubmitted: (_) => _soumettre(),
                  ),
                  const SizedBox(height: 24),

                  SizedBox(
                    height: 48,
                    child: ElevatedButton(
                      onPressed: auth.chargement ? null : _soumettre,
                      child:
                          auth.chargement
                              ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                ),
                              )
                              : const Text('Se connecter'),
                    ),
                  ),

                  const SizedBox(height: 12),

                  Text(
                    auth.estAuthentifie ? 'Authentifié ✅' : '',
                    textAlign: TextAlign.center,
                    style: const TextStyle(color: Colors.green),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
