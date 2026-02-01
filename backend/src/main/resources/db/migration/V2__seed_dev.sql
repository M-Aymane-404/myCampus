-- ==========================================================
-- MyCampus - FULL RESET + FULL SEED (DEV / DEMO)
-- Compatible with your schema (JOINED inheritance)
-- ==========================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resultat;
TRUNCATE TABLE examen;
TRUNCATE TABLE presence;
TRUNCATE TABLE fichier_seance;
TRUNCATE TABLE seance;
TRUNCATE TABLE notification;
TRUNCATE TABLE affectation_enseignement;

TRUNCATE TABLE etudiant;
TRUNCATE TABLE professeur;
TRUNCATE TABLE administrateur;
TRUNCATE TABLE utilisateur;

TRUNCATE TABLE matiere;
TRUNCATE TABLE module;
TRUNCATE TABLE classe;
TRUNCATE TABLE filiere;
TRUNCATE TABLE departement;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- 1) UTILISATEURS (Admins + Profs + Etudiants)
-- ==========================================================

-- Admins (id 1..3)
INSERT INTO utilisateur (active, date_creation, date_derniere_connexion, id, cin, email, nom, password, photo, prenom, tel) VALUES
                                                                                                                                (b'1', NOW(6), NULL, 1, 'AD100001', 'admin1@mycampus.ma', 'Admin', 'password123', NULL, 'One', '0610000001'),
                                                                                                                                (b'1', NOW(6), NULL, 2, 'AD100002', 'admin2@mycampus.ma', 'Admin', 'password123', NULL, 'Two', '0610000002'),
                                                                                                                                (b'1', NOW(6), NULL, 3, 'AD100003', 'admin3@mycampus.ma', 'Admin', 'password123', NULL, 'Three', '0610000003');

INSERT INTO administrateur (utilisateur_id, fonction, grade, ppr) VALUES
                                                                      (1, 'Scolarité', 'PH',  'ADM00001'),
                                                                      (2, 'Direction', 'PES', 'ADM00002'),
                                                                      (3, 'Système',   'PH',  'ADM00003');

-- Profs (id 10..17)
INSERT INTO utilisateur (active, date_creation, date_derniere_connexion, id, cin, email, nom, password, photo, prenom, tel) VALUES
                                                                                                                                (b'1', NOW(6), NULL, 10, 'PR200010', 'prof.cyber@mycampus.ma', 'MARTIN',    'password123', NULL, 'Pierre',  '0620000010'),
                                                                                                                                (b'1', NOW(6), NULL, 11, 'PR200011', 'prof.math@mycampus.ma',  'ALAOUI',    'password123', NULL, 'Sara',    '0620000011'),
                                                                                                                                (b'1', NOW(6), NULL, 12, 'PR200012', 'prof.gi@mycampus.ma',    'HASSAN',    'password123', NULL, 'Omar',    '0620000012'),
                                                                                                                                (b'1', NOW(6), NULL, 13, 'PR200013', 'prof.java@mycampus.ma',  'BENALI',    'password123', NULL, 'Youssef', '0620000013'),
                                                                                                                                (b'1', NOW(6), NULL, 14, 'PR200014', 'prof.db@mycampus.ma',    'EL AMRANI', 'password123', NULL, 'Sara',    '0620000014'),
                                                                                                                                (b'1', NOW(6), NULL, 15, 'PR200015', 'prof.web@mycampus.ma',   'AIT SAID',  'password123', NULL, 'Imane',   '0620000015'),
                                                                                                                                (b'1', NOW(6), NULL, 16, 'PR200016', 'prof.elec@mycampus.ma',  'KADIRI',    'password123', NULL, 'Nabil',   '0620000016'),
                                                                                                                                (b'1', NOW(6), NULL, 17, 'PR200017', 'prof.reseau@mycampus.ma','OUAZZANI',  'password123', NULL, 'Salma',   '0620000017');

-- ==========================================================
-- 2) DEPARTEMENTS (chef_departement_id sera mis après)
-- ==========================================================
INSERT INTO departement (date_creation, chef_departement_id, id, code_departement, description, type_departement, etat) VALUES
                                                                                                                            (CURDATE(), NULL, 1, 'INFO',  'Département Informatique & Réseaux', 'Informatique & Réseaux', 'OUVERTE'),
                                                                                                                            (CURDATE(), NULL, 2, 'INDUS', 'Département Génie Industriel',       'Génie Industriel',       'OUVERTE'),
                                                                                                                            (CURDATE(), NULL, 3, 'ELEC',  'Département Génie Électrique',       'Génie Électrique',       'OUVERTE');

-- Professeurs (JOINED) => departement_ou_travail_id
INSERT INTO professeur (date_recrutement, departement_ou_travail_id, utilisateur_id, grade, ppr, specialite, statut) VALUES
                                                                                                                         ('2020-09-01', 1, 10, 'PES', 'PPR200010', 'Cybersécurité',          'Titulaire'),
                                                                                                                         ('2021-09-01', 1, 11, 'PH',  'PPR200011', 'Mathématiques',          'Vacataire'),
                                                                                                                         ('2019-09-01', 2, 12, 'PES', 'PPR200012', 'Génie industriel',       'Titulaire'),
                                                                                                                         ('2020-10-10', 1, 13, 'PA',  'PPR200013', 'Java / POO',             'Titulaire'),
                                                                                                                         ('2022-09-01', 1, 14, 'PH',  'PPR200014', 'Bases de données',       'Titulaire'),
                                                                                                                         ('2020-02-15', 1, 15, 'PA',  'PPR200015', 'Web / UI',               'Titulaire'),
                                                                                                                         ('2018-03-12', 3, 16, 'PH',  'PPR200016', 'Électronique',           'Titulaire'),
                                                                                                                         ('2017-04-22', 1, 17, 'PA',  'PPR200017', 'Réseaux',                'Titulaire');

-- Set chefs departements (unique)
UPDATE departement SET chef_departement_id = 10 WHERE id = 1; -- INFO -> Pierre
UPDATE departement SET chef_departement_id = 12 WHERE id = 2; -- INDUS -> Omar
UPDATE departement SET chef_departement_id = 16 WHERE id = 3; -- ELEC -> Nabil

-- ==========================================================
-- 3) FILIERES (chef_filiere_id sera mis après)
-- ==========================================================
INSERT INTO filiere (chef_filiere_id, departement_id, id, code_filiere, description, duree_formation, niveau_diplome, nom, etat) VALUES
                                                                                                                                     (NULL, 1, 1, 'ILCS', 'Cyber sécurité',              '3 Ans', 'Licence', 'ILCS - Cyber Sécurité',     'OUVERTE'),
                                                                                                                                     (NULL, 1, 2, 'GL',   'Génie logiciel',              '3 Ans', 'Licence', 'Génie Logiciel',           'OUVERTE'),
                                                                                                                                     (NULL, 2, 3, 'GIND', 'Industrie & optimisation',    '3 Ans', 'Licence', 'Génie Industriel',         'OUVERTE'),
                                                                                                                                     (NULL, 3, 4, 'GEII', 'Électrique & informatique ind','3 Ans','BUT',     'BUT GEII',                 'OUVERTE'),
                                                                                                                                     (NULL, 1, 5, 'ISI',  'Informatique & ingénierie',   '3 Ans', 'Licence', 'Informatique (ISI)',       'OUVERTE');

-- Set chefs filières (unique)
UPDATE filiere SET chef_filiere_id = 10 WHERE id = 1; -- ILCS -> Pierre
UPDATE filiere SET chef_filiere_id = 13 WHERE id = 2; -- GL   -> Youssef
UPDATE filiere SET chef_filiere_id = 12 WHERE id = 3; -- GIND -> Omar
UPDATE filiere SET chef_filiere_id = 16 WHERE id = 4; -- GEII -> Nabil
UPDATE filiere SET chef_filiere_id = 14 WHERE id = 5; -- ISI  -> Sara(DB)

-- ==========================================================
-- 4) CLASSES
-- ==========================================================
INSERT INTO classe (effectif_max, filiere_id, id, annee_universitaire, code_classe, emploi_temps, niveau, nom) VALUES
                                                                                                                   (35, 1, 1, '2025/2026', 'ILCS1-A', NULL, '1ère année', 'ILCS1 - Groupe A'),
                                                                                                                   (35, 1, 2, '2025/2026', 'ILCS1-B', NULL, '1ère année', 'ILCS1 - Groupe B'),
                                                                                                                   (35, 2, 3, '2025/2026', 'GL1-A',   NULL, '1ère année', 'GL1 - Groupe A'),
                                                                                                                   (35, 2, 4, '2025/2026', 'GL2-A',   NULL, '2ème année', 'GL2 - Groupe A'),
                                                                                                                   (30, 3, 5, '2025/2026', 'GIND1-A', NULL, '1ère année', 'GIND1 - Groupe A'),
                                                                                                                   (30, 3, 6, '2025/2026', 'GIND2-A', NULL, '2ème année', 'GIND2 - Groupe A'),
                                                                                                                   (30, 4, 7, '2025/2026', 'GEII2-A', NULL, '2ème année', 'GEII2 - Groupe A'),
                                                                                                                   (40, 5, 8, '2025/2026', 'ISI2-A',  NULL, '2ème année', 'ISI2 - Groupe A'),
                                                                                                                   (40, 5, 9, '2025/2026', 'ISI2-B',  NULL, '2ème année', 'ISI2 - Groupe B');

-- ==========================================================
-- 5) MODULES (S1..S4) + MATIERES
-- ==========================================================

-- Modules ILCS (S1, S3)
INSERT INTO module (coefficient, filiere_id, id, code_module, description, nom, semestre) VALUES
                                                                                              (3, 1, 1,  'ILCS-MATH-S1', 'Maths de base',              'Maths 1',            'S1'),
                                                                                              (3, 1, 2,  'ILCS-SEC-S1',  'Introduction sécurité',      'Sécurité 1',         'S1'),
                                                                                              (2, 1, 3,  'ILCS-NET-S3',  'Réseaux et sécurité',        'Réseaux S3',         'S3'),
                                                                                              (2, 1, 4,  'ILCS-CRYPTO-S3','Cryptographie',             'Crypto S3',          'S3');

-- Modules GL (S1, S3)
INSERT INTO module (coefficient, filiere_id, id, code_module, description, nom, semestre) VALUES
                                                                                              (3, 2, 10, 'GL-JAVA-S1',   'Programmation Java',         'Java 1',             'S1'),
                                                                                              (2, 2, 11, 'GL-DB-S3',     'Bases de données',           'DB S3',              'S3'),
                                                                                              (2, 2, 12, 'GL-WEB-S3',    'Web',                        'Web S3',             'S3');

-- Modules GIND (S1, S3)
INSERT INTO module (coefficient, filiere_id, id, code_module, description, nom, semestre) VALUES
                                                                                              (3, 3, 20, 'GIND-STAT-S1', 'Statistiques',               'Stats 1',            'S1'),
                                                                                              (3, 3, 21, 'GIND-OPT-S3',  'Optimisation',               'Optimisation S3',    'S3');

-- Modules GEII (S3, S4)
INSERT INTO module (coefficient, filiere_id, id, code_module, description, nom, semestre) VALUES
                                                                                              (2, 4, 30, 'GEII-ELEC-S3', 'Électronique',               'Électronique S3',    'S3'),
                                                                                              (2, 4, 31, 'GEII-EMB-S4',  'Systèmes embarqués',         'Embarqué S4',        'S4');

-- Modules ISI (S3, S4)
INSERT INTO module (coefficient, filiere_id, id, code_module, description, nom, semestre) VALUES
                                                                                              (2, 5, 40, 'ISI-DB-S3',    'SQL & modélisation',         'Bases de données S3','S3'),
                                                                                              (2, 5, 41, 'ISI-JAVA-S3',  'POO Java + Spring',          'Java/Spring S3',     'S3'),
                                                                                              (2, 5, 42, 'ISI-ARCH-S4',  'Architecture logicielle',    'Architecture S4',    'S4');

-- Matières ILCS
INSERT INTO matiere (id, module_id, code_matiere, description, nom) VALUES
                                                                        (1, 1, 'ALGEBRE', 'Algèbre linéaire',     'Algèbre'),
                                                                        (2, 1, 'ANALYSE', 'Analyse 1',            'Analyse'),
                                                                        (3, 2, 'SEC_BASE','Bases sécurité',       'Sécurité - Bases'),
                                                                        (4, 3, 'RESEAU',  'Routage & TCP/IP',     'Réseaux'),
                                                                        (5, 4, 'CRYPTO',  'Chiffrement & hash',   'Cryptographie');

-- Matières GL
INSERT INTO matiere (id, module_id, code_matiere, description, nom) VALUES
                                                                        (10, 10, 'JAVA_BASICS', 'OOP + syntaxe',           'Java Basics'),
                                                                        (11, 11, 'SQL_ADV',     'JOIN, index, transac',   'SQL Avancé'),
                                                                        (12, 11, 'MODELE',      'UML/MERISE',             'Modélisation'),
                                                                        (13, 12, 'WEB_BASE',    'HTML/CSS/JS',            'Web de base');

-- Matières GIND
INSERT INTO matiere (id, module_id, code_matiere, description, nom) VALUES
                                                                        (20, 20, 'STAT',   'Probabilités & stats',        'Statistiques'),
                                                                        (21, 21, 'OPT',    'PL, heuristiques',            'Optimisation');

-- Matières GEII
INSERT INTO matiere (id, module_id, code_matiere, description, nom) VALUES
                                                                        (30, 30, 'ELEC_AN', 'Analogique',                 'Électronique Analogique'),
                                                                        (31, 30, 'ELEC_DI', 'Digitale',                   'Électronique Digitale'),
                                                                        (32, 31, 'EMB',     'Microcontrôleurs',           'Systèmes embarqués');

-- Matières ISI
INSERT INTO matiere (id, module_id, code_matiere, description, nom) VALUES
                                                                        (40, 40, 'SQL_ISI',     'SQL avancé',             'SQL ISI'),
                                                                        (41, 40, 'MODELE_ISI',  'UML + normalisation',    'Modélisation ISI'),
                                                                        (42, 41, 'JAVA_OOP',    'POO Java',               'Java POO'),
                                                                        (43, 41, 'SPRING',      'Spring Boot REST + JPA', 'Spring Boot'),
                                                                        (44, 42, 'ARCH',        'Clean arch + patterns',  'Architecture');

-- ==========================================================
-- 6) ETUDIANTS (24 étudiants)
-- ==========================================================
-- Utilisateurs étudiants (100..123)
INSERT INTO utilisateur (active, date_creation, date_derniere_connexion, id, cin, email, nom, password, photo, prenom, tel) VALUES
                                                                                                                                (b'1', NOW(6), NULL, 100, 'ET300100', 'etud100@mycampus.ma', 'RAMZI',     'password123', NULL, 'Norhane', '0601000100'),
                                                                                                                                (b'1', NOW(6), NULL, 101, 'ET300101', 'etud101@mycampus.ma', 'EL IDRISSI','password123', NULL, 'Amal',    '0601000101'),
                                                                                                                                (b'1', NOW(6), NULL, 102, 'ET300102', 'etud102@mycampus.ma', 'HASSANI',   'password123', NULL, 'Aya',     '0601000102'),
                                                                                                                                (b'1', NOW(6), NULL, 103, 'ET300103', 'etud103@mycampus.ma', 'ZIANI',     'password123', NULL, 'Oussama', '0601000103'),
                                                                                                                                (b'1', NOW(6), NULL, 104, 'ET300104', 'etud104@mycampus.ma', 'FARAH',     'password123', NULL, 'Salma',   '0601000104'),
                                                                                                                                (b'1', NOW(6), NULL, 105, 'ET300105', 'etud105@mycampus.ma', 'KAMAL',     'password123', NULL, 'Ahmed',   '0601000105'),

                                                                                                                                (b'1', NOW(6), NULL, 106, 'ET300106', 'etud106@mycampus.ma', 'BENJELLOUN','password123', NULL, 'Imane',   '0601000106'),
                                                                                                                                (b'1', NOW(6), NULL, 107, 'ET300107', 'etud107@mycampus.ma', 'BOUAZZA',   'password123', NULL, 'Yassine', '0601000107'),
                                                                                                                                (b'1', NOW(6), NULL, 108, 'ET300108', 'etud108@mycampus.ma', 'EL FASSI',  'password123', NULL, 'Sara',    '0601000108'),

                                                                                                                                (b'1', NOW(6), NULL, 109, 'ET300109', 'etud109@mycampus.ma', 'LOUARDI',   'password123', NULL, 'Rania',   '0601000109'),
                                                                                                                                (b'1', NOW(6), NULL, 110, 'ET300110', 'etud110@mycampus.ma', 'AMRANI',    'password123', NULL, 'Omar',    '0601000110'),
                                                                                                                                (b'1', NOW(6), NULL, 111, 'ET300111', 'etud111@mycampus.ma', 'KHOUYA',    'password123', NULL, 'Hiba',    '0601000111'),

                                                                                                                                (b'1', NOW(6), NULL, 112, 'ET300112', 'etud112@mycampus.ma', 'NAJI',      'password123', NULL, 'Anas',    '0601000112'),
                                                                                                                                (b'1', NOW(6), NULL, 113, 'ET300113', 'etud113@mycampus.ma', 'RIFAI',     'password123', NULL, 'Nada',    '0601000113'),
                                                                                                                                (b'1', NOW(6), NULL, 114, 'ET300114', 'etud114@mycampus.ma', 'EL HANI',   'password123', NULL, 'Soukaina','0601000114'),

                                                                                                                                (b'1', NOW(6), NULL, 115, 'ET300115', 'etud115@mycampus.ma', 'TAZI',      'password123', NULL, 'Mehdi',   '0601000115'),
                                                                                                                                (b'1', NOW(6), NULL, 116, 'ET300116', 'etud116@mycampus.ma', 'BERRADA',   'password123', NULL, 'Youssef', '0601000116'),
                                                                                                                                (b'1', NOW(6), NULL, 117, 'ET300117', 'etud117@mycampus.ma', 'EL KHALDI', 'password123', NULL, 'Hajar',   '0601000117'),

                                                                                                                                (b'1', NOW(6), NULL, 118, 'ET300118', 'etud118@mycampus.ma', 'SAAD',      'password123', NULL, 'Nabil',   '0601000118'),
                                                                                                                                (b'1', NOW(6), NULL, 119, 'ET300119', 'etud119@mycampus.ma', 'MAHJOUB',   'password123', NULL, 'Asmae',   '0601000119'),
                                                                                                                                (b'1', NOW(6), NULL, 120, 'ET300120', 'etud120@mycampus.ma', 'CHAFI',     'password123', NULL, 'Kawtar',  '0601000120'),

                                                                                                                                (b'1', NOW(6), NULL, 121, 'ET300121', 'etud121@mycampus.ma', 'KARRA',     'password123', NULL, 'Adam',    '0601000121'),
                                                                                                                                (b'1', NOW(6), NULL, 122, 'ET300122', 'etud122@mycampus.ma', 'EL MANSOURI','password123',NULL, 'Lina',    '0601000122'),
                                                                                                                                (b'1', NOW(6), NULL, 123, 'ET300123', 'etud123@mycampus.ma', 'FADILI',    'password123', NULL, 'Ilyas',   '0601000123');

-- Affectation des étudiants à des classes (S1 pour 1ère année, S3 pour 2ème année)
INSERT INTO etudiant (annee_inscription, date_naissance, classe_id, utilisateur_id, annee_fin_etude, apogee, niveau, etat_inscription, semestre_actuel) VALUES
                                                                                                                                                            (2025, '2004-05-11', 8, 100, '2027', 'APO300100', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2004-03-02', 8, 101, '2027', 'APO300101', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2004-11-19', 8, 102, '2027', 'APO300102', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2004-01-21', 9, 103, '2027', 'APO300103', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2004-07-15', 9, 104, '2027', 'APO300104', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2004-09-09', 9, 105, '2027', 'APO300105', '2ème année', 'INSCRIT', 'S3'),

                                                                                                                                                            (2025, '2005-01-01', 1, 106, '2028', 'APO300106', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-02-02', 1, 107, '2028', 'APO300107', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-03-03', 2, 108, '2028', 'APO300108', '1ère année', 'INSCRIT', 'S1'),

                                                                                                                                                            (2025, '2005-04-04', 3, 109, '2028', 'APO300109', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-05-05', 3, 110, '2028', 'APO300110', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-06-06', 4, 111, '2028', 'APO300111', '2ème année', 'INSCRIT', 'S3'),

                                                                                                                                                            (2025, '2005-07-07', 5, 112, '2028', 'APO300112', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-08-08', 6, 113, '2028', 'APO300113', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-09-09', 6, 114, '2028', 'APO300114', '2ème année', 'INSCRIT', 'S3'),

                                                                                                                                                            (2025, '2005-10-10', 7, 115, '2028', 'APO300115', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-11-11', 7, 116, '2028', 'APO300116', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-12-12', 7, 117, '2028', 'APO300117', '2ème année', 'INSCRIT', 'S3'),

                                                                                                                                                            (2025, '2005-01-13', 4, 118, '2028', 'APO300118', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-02-14', 5, 119, '2028', 'APO300119', '1ère année', 'INSCRIT', 'S1'),
                                                                                                                                                            (2025, '2005-03-15', 2, 120, '2028', 'APO300120', '1ère année', 'INSCRIT', 'S1'),

                                                                                                                                                            (2025, '2005-04-16', 8, 121, '2027', 'APO300121', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-05-17', 8, 122, '2027', 'APO300122', '2ème année', 'INSCRIT', 'S3'),
                                                                                                                                                            (2025, '2005-06-18', 9, 123, '2027', 'APO300123', '2ème année', 'INSCRIT', 'S3');

-- ==========================================================
-- 7) AFFECTATIONS (Prof + Classe + Matière)
-- ==========================================================
-- ISI2-A (classe 8) : DB & MODELE par prof 14 ; JAVA & SPRING par prof 13 ; WEB par prof 15
INSERT INTO affectation_enseignement (active, classe_id, id, matiere_id, professeur_id) VALUES
                                                                                            (b'1', 8, 1, 40, 14),
                                                                                            (b'1', 8, 2, 41, 14),
                                                                                            (b'1', 8, 3, 42, 13),
                                                                                            (b'1', 8, 4, 43, 13),
                                                                                            (b'1', 8, 5, 13, 15);

-- ISI2-B (classe 9)
INSERT INTO affectation_enseignement (active, classe_id, id, matiere_id, professeur_id) VALUES
                                                                                            (b'1', 9, 6, 40, 14),
                                                                                            (b'1', 9, 7, 41, 14),
                                                                                            (b'1', 9, 8, 42, 13),
                                                                                            (b'1', 9, 9, 43, 13),
                                                                                            (b'1', 9, 10, 13, 15);

-- ILCS1-A (classe 1) : maths/secure
INSERT INTO affectation_enseignement (active, classe_id, id, matiere_id, professeur_id) VALUES
                                                                                            (b'1', 1, 11, 1, 11),
                                                                                            (b'1', 1, 12, 2, 11),
                                                                                            (b'1', 1, 13, 3, 10);

-- GL1-A (classe 3) : Java basics + web base
INSERT INTO affectation_enseignement (active, classe_id, id, matiere_id, professeur_id) VALUES
                                                                                            (b'1', 3, 14, 10, 13),
                                                                                            (b'1', 3, 15, 13, 15);

-- GEII2-A (classe 7) : elec + embarqué
INSERT INTO affectation_enseignement (active, classe_id, id, matiere_id, professeur_id) VALUES
                                                                                            (b'1', 7, 16, 30, 16),
                                                                                            (b'1', 7, 17, 31, 16),
                                                                                            (b'1', 7, 18, 32, 16);

-- ==========================================================
-- 8) SEANCES + FICHIERS + PRESENCES
-- ==========================================================
-- Séances ISI2-A (affectation id 1..5)
INSERT INTO seance (date, affectation_enseignement_id, heure_debut, heure_fin, id, contenu, salle, etat, type) VALUES
                                                                                                                   ('2025-11-01', 1, '2025-11-01 08:30:00.000000', '2025-11-01 10:30:00.000000', 1, 'Intro SQL',      'B12', 'TERMINEE', 'COURS'),
                                                                                                                   ('2025-11-03', 3, '2025-11-03 10:30:00.000000', '2025-11-03 12:30:00.000000', 2, 'Java Classes',   'A05', 'TERMINEE', 'TD'),
                                                                                                                   ('2025-11-05', 4, '2025-11-05 14:30:00.000000', '2025-11-05 16:30:00.000000', 3, 'REST Controllers','A07','PLANIFIEE','COURS');

-- Fichiers pour séance 1
INSERT INTO fichier_seance (date_upload, id, seance_id, taille, chemin, nom_original, type_mime) VALUES
                                                                                                     (NOW(6), 1, 1, 245760, '/files/seances/1/cours-sql.pdf', 'cours-sql.pdf', 'application/pdf'),
                                                                                                     (NOW(6), 2, 1, 102400, '/files/seances/1/tp-sql.sql',   'tp-sql.sql',   'text/plain');

-- Présences (quelques étudiants ISI2-A : 100,101,102,121,122)
INSERT INTO presence (justifiee, date_declaration, etudiant_id, id, seance_id, motif, status) VALUES
                                                                                                  (b'0', NOW(6), 100, 1, 1, NULL,        'PRESENT'),
                                                                                                  (b'1', NOW(6), 101, 2, 1, 'Maladie',   'ABSENT'),
                                                                                                  (b'0', NOW(6), 102, 3, 1, NULL,        'PRESENT'),
                                                                                                  (b'0', NOW(6), 121, 4, 1, NULL,        'PRESENT'),
                                                                                                  (b'0', NOW(6), 122, 5, 1, NULL,        'PRESENT');

-- ==========================================================
-- 9) EXAMENS (NORMAL + RATTRAPAGE) + RESULTATS (pour relevé)
-- ==========================================================
-- ISI - SQL_ISI (matiere 40): NORMAL + RATTRAPAGE (S3)
INSERT INTO examen (duree, numero_examen, date_examen, id, matiere_id, corrige_path, etat, semestre, type_examen) VALUES
                                                                                                                      (90, 1, '2025-12-10 09:00:00.000000', 1, 40, '/corriges/isi-sql-normal.pdf', 'CORRIGE', 'S3', 'NORMAL'),
                                                                                                                      (90, 2, '2026-01-20 09:00:00.000000', 2, 40, '/corriges/isi-sql-ratt.pdf',   'CORRIGE', 'S3', 'RATTRAPAGE');

-- ISI - SPRING (matiere 43): NORMAL + RATTRAPAGE (S3)
INSERT INTO examen (duree, numero_examen, date_examen, id, matiere_id, corrige_path, etat, semestre, type_examen) VALUES
                                                                                                                      (90, 1, '2025-12-14 10:00:00.000000', 3, 43, '/corriges/isi-spring-normal.pdf', 'CORRIGE', 'S3', 'NORMAL'),
                                                                                                                      (90, 2, '2026-01-22 10:00:00.000000', 4, 43, '/corriges/isi-spring-ratt.pdf',   'CORRIGE', 'S3', 'RATTRAPAGE');

-- ISI - MODELE_ISI (matiere 41): NORMAL (S3)
INSERT INTO examen (duree, numero_examen, date_examen, id, matiere_id, corrige_path, etat, semestre, type_examen) VALUES
    (90, 1, '2025-12-11 09:00:00.000000', 5, 41, '/corriges/isi-modele.pdf', 'CORRIGE', 'S3', 'NORMAL');

-- ISI - JAVA_OOP (matiere 42): NORMAL (S3)
INSERT INTO examen (duree, numero_examen, date_examen, id, matiere_id, corrige_path, etat, semestre, type_examen) VALUES
    (90, 1, '2025-12-12 10:00:00.000000', 6, 42, '/corriges/isi-java-oop.pdf', 'CORRIGE', 'S3', 'NORMAL');

-- WEB_BASE (matiere 13): NORMAL (S3)
INSERT INTO examen (duree, numero_examen, date_examen, id, matiere_id, corrige_path, etat, semestre, type_examen) VALUES
    (60, 1, '2025-12-15 08:30:00.000000', 7, 13, '/corriges/web-base.pdf', 'CORRIGE', 'S3', 'NORMAL');

-- Résultats (examen_id, etudiant_id) unique -> OK
-- Etudiant 100 (Norhane) : rattrapage > normal => moyenne matière = rattrapage
INSERT INTO resultat (note, date_publication, etudiant_id, examen_id, id, copie_etudiant_path) VALUES
                                                                                                   ( 9.0, NOW(6), 100, 1, 1, '/copies/100-sql-normal.pdf'),
                                                                                                   (14.0, NOW(6), 100, 2, 2, '/copies/100-sql-ratt.pdf'),
                                                                                                   ( 8.0, NOW(6), 100, 3, 3, '/copies/100-spring-normal.pdf'),
                                                                                                   (12.0, NOW(6), 100, 4, 4, '/copies/100-spring-ratt.pdf'),
                                                                                                   (13.5, NOW(6), 100, 5, 5, '/copies/100-modele.pdf'),
                                                                                                   (16.0, NOW(6), 100, 6, 6, '/copies/100-java.pdf'),
                                                                                                   (15.0, NOW(6), 100, 7, 7, '/copies/100-web.pdf');

-- Etudiant 101 (Amal) : rattrapage non noté => moyenne = normal
INSERT INTO resultat (note, date_publication, etudiant_id, examen_id, id, copie_etudiant_path) VALUES
                                                                                                   (12.0, NOW(6), 101, 1, 8,  '/copies/101-sql-normal.pdf'),
                                                                                                   (11.0, NOW(6), 101, 5, 9,  '/copies/101-modele.pdf'),
                                                                                                   (13.0, NOW(6), 101, 6, 10, '/copies/101-java.pdf'),
                                                                                                   (10.0, NOW(6), 101, 7, 11, '/copies/101-web.pdf');

-- Etudiant 102 (Aya) : notes publiées (pas NULL)
INSERT INTO resultat (note, date_publication, etudiant_id, examen_id, id, copie_etudiant_path) VALUES
                                                                                                   (7.0, NOW(6), 102, 1, 12, '/copies/102-sql-normal.pdf'),
                                                                                                   (9.0, NOW(6), 102, 2, 13, '/copies/102-sql-ratt.pdf');


-- Etudiants ISI2-B : 103,104,105,123 (quelques notes)
INSERT INTO resultat (note, date_publication, etudiant_id, examen_id, id, copie_etudiant_path) VALUES
                                                                                                   ( 7.0, NOW(6), 103, 1, 14, '/copies/103-sql-normal.pdf'),
                                                                                                   (11.0, NOW(6), 103, 2, 15, '/copies/103-sql-ratt.pdf'),
                                                                                                   (16.5, NOW(6), 104, 6, 16, '/copies/104-java.pdf'),
                                                                                                   (18.0, NOW(6), 105, 7, 17, '/copies/105-web.pdf'),
                                                                                                   (14.0, NOW(6), 123, 5, 18, '/copies/123-modele.pdf');

-- ==========================================================
-- 10) NOTIFICATIONS (plusieurs)
-- ==========================================================
INSERT INTO notification (lu, date_envoi, date_lecture, id, utilisateur_id, message, titre, type) VALUES
                                                                                                      (b'0', NOW(6), NULL, 1, 100, 'Votre résultat SQL (S3) est publié.', 'Résultat publié', 'RESULTAT_PUBLIE'),
                                                                                                      (b'0', NOW(6), NULL, 2, 101, 'Examen Spring prévu demain.',         'Rappel examen',   'EXAMEN_DEMAIN'),
                                                                                                      (b'0', NOW(6), NULL, 3, 10,  'Nouvelle séance ajoutée pour SQL.',   'Séance ajoutée',  'SEANCE_AJOUTEE'),
                                                                                                      (b'0', NOW(6), NULL, 4, 15,  'Cours uploadé: Web de base',          'Cours uploadé',   'COURS_UPLOADE');
