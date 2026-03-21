import 'package:flutter_test/flutter_test.dart';

import 'package:dwm_bot/features/etudiant/data/dto/releve_notes_dto.dart';

void main() {
  test('ReleveNotesDTO recalcule nbModules/nbModulesValider depuis modules', () {
    final dto = ReleveNotesDTO.fromJson({
      'semestre': 'S1',
      'moyenneSession': 12.5,
      // Valeurs backend incohérentes (bug observé)
      'nbModules': 10,
      'nbModulesValider': 1,
      'modules': [
        {
          'id': 1,
          'nomDuModule': 'Module 1',
          'codeModule': 'M1',
          'moyenneDuModule': 10.0,
          'matieres': [],
        },
        {
          'id': 2,
          'nomDuModule': 'Module 2',
          'codeModule': 'M2',
          'moyenneDuModule': 11.25,
          'matieres': [],
        },
        {
          'id': 3,
          'nomDuModule': 'Module 3',
          'codeModule': 'M3',
          'moyenneDuModule': 14,
          'matieres': [],
        },
        {
          'id': 4,
          'nomDuModule': 'Module 4',
          'codeModule': 'M4',
          'moyenneDuModule': 10,
          'matieres': [],
        },
      ],
    });

    expect(dto.nbModules, 4);
    expect(dto.nbModulesValider, 4);
  });

  test('ReleveNotesDTO garde les compteurs API si modules vide', () {
    final dto = ReleveNotesDTO.fromJson({
      'semestre': 'S2',
      'moyenneSession': 0,
      'nbModules': 6,
      'nbModulesValider': 2,
      'modules': [],
    });

    expect(dto.nbModules, 6);
    expect(dto.nbModulesValider, 2);
  });
}

