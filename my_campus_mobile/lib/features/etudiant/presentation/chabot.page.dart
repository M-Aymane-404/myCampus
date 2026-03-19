import 'package:flutter/material.dart';

class ChabotPage extends StatelessWidget {
  const ChabotPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Assistant MyCampus'),
      ),
      body: const Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            'Le chatbot mobile n utilise plus de secret embarque dans l application. Configurez un endpoint backend securise avant de reactiver cette fonctionnalite.',
            textAlign: TextAlign.center,
          ),
        ),
      ),
    );
  }
}
