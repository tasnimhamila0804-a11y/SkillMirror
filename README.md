# SkillMirror
SkillMirror is an AI-powered career guidance platform designed to help users understand their skills, identify skill gaps, and build a personalized path toward their professional goals.
# CAHIER DES CHARGES FONCTIONNEL ET TECHNIQUE
## Projet SkillMirror — Plateforme intelligente d’analyse et de développement des compétences professionnelles

**Version :** 1.0 — MVP  
**Type de projet :** Application web intégrant l’intelligence artificielle  
**Technologies principales :** Angular, Spring Boot, MySQL et API d’un modèle d’intelligence artificielle.

---

# 1. Présentation générale du projet

## 1.1. Contexte du projet

Dans un marché du travail en constante évolution, les étudiants et les jeunes diplômés rencontrent des difficultés pour identifier les compétences nécessaires à leur insertion professionnelle. Ils possèdent souvent des connaissances théoriques, des projets universitaires et des certifications, mais ne savent pas toujours si leur profil correspond aux exigences des offres d’emploi.

Les plateformes classiques de recrutement permettent principalement de consulter des offres d’emploi et de déposer des candidatures. Cependant, elles n’accompagnent pas nécessairement les candidats dans l’identification précise de leurs lacunes ni dans la construction d’un parcours personnalisé pour les combler.

Le projet SkillMirror vise à répondre à ce besoin en proposant une plateforme web intelligente capable d’analyser le profil d’un utilisateur, de le comparer aux exigences du marché du travail et de lui recommander des actions concrètes pour développer ses compétences.

## 1.2. Problématique

Comment aider un utilisateur à identifier ses compétences actuelles, à comprendre les compétences qui lui manquent pour atteindre un objectif professionnel et à construire un parcours d’apprentissage personnalisé lui permettant d’améliorer ses perspectives d’emploi ?

## 1.3. Solution proposée

SkillMirror est une plateforme web qui accompagne l’utilisateur dans un processus continu :

1. Répondre à un questionnaire initial pour renseigner son profil et ses compétences.
2. Identifier ses objectifs professionnels et les métiers qui l’intéressent.
3. Analyser une offre d’emploi réelle.
4. Comparer les compétences de l’utilisateur avec celles demandées par l’offre.
5. Identifier et hiérarchiser les compétences à améliorer.
6. Générer des projets pratiques personnalisés pour développer ces compétences.
7. Suivre la progression de l’utilisateur et actualiser son profil à partir des résultats obtenus.

L’objectif est de dépasser les simples recommandations de carrière en proposant un accompagnement fondé sur la pratique et sur des preuves concrètes de compétences.

# 2. Objectifs du projet

Le projet poursuit les objectifs suivants :

- Créer un profil professionnel personnalisé pour chaque utilisateur.
- Collecter les informations nécessaires grâce à un questionnaire initial interactif.
- Identifier les compétences déclarées, les expériences et les projets réalisés.
- Analyser les offres d’emploi afin d’en extraire les compétences recherchées.
- Calculer un indicateur de compatibilité entre le profil et une offre.
- Identifier les principales lacunes professionnelles.
- Générer des parcours d’apprentissage adaptés au niveau et aux objectifs de l’utilisateur.
- Proposer des projets pratiques permettant de développer les compétences manquantes.
- Permettre à l’utilisateur de suivre sa progression et de fournir des preuves de ses réalisations.
- Utiliser l’intelligence artificielle pour personnaliser les recommandations tout en garantissant leur transparence.

# 3. Périmètre du projet

Le développement sera divisé en deux phases.

## 3.1. Première phase : MVP (Minimum Viable Product)

La première version devra intégrer les fonctionnalités essentielles :

- Création de compte et authentification.
- Questionnaire initial de profilage.
- Gestion du profil professionnel et des compétences.
- Gestion des projets, expériences et certifications.
- Saisie manuelle d’une offre d’emploi.
- Extraction des compétences demandées par l’offre.
- Analyse de compatibilité et détection des lacunes.
- Génération de recommandations et de projets pratiques.
- Suivi des tâches et de la progression.
- Tableau de bord personnalisé.

## 3.2. Deuxième phase : évolutions futures

Les fonctionnalités suivantes pourront être ajoutées après validation du MVP :

- Connexion avancée à GitHub pour analyser les projets et dépôts autorisés.
- Importation automatique d’offres d’emploi depuis des sources autorisées.
- Simulateur de parcours professionnels « What-If ».
- Espace destiné aux entreprises et aux recruteurs.
- Tableaux de bord pour les établissements de formation.
- Système avancé de vérification des compétences.
- Recommandations basées sur l’évolution des besoins du marché.

# 4. Acteurs du système

## 4.1. Utilisateur

L’utilisateur représente principalement un étudiant, un jeune diplômé ou une personne souhaitant évoluer professionnellement ou se reconvertir.

Il peut :

- Créer un compte et se connecter.
- Répondre au questionnaire initial.
- Consulter et modifier son profil.
- Ajouter ses compétences et ses expériences.
- Ajouter ses projets et certifications.
- Saisir des offres d’emploi.
- Lancer des analyses de compatibilité.
- Consulter les compétences manquantes.
- Recevoir des recommandations personnalisées.
- Générer et suivre des projets pratiques.
- Soumettre des preuves de réalisation.
- Consulter l’évolution de ses compétences.

## 4.2. Administrateur

L’administrateur assure la gestion technique et fonctionnelle de la plateforme.

Il peut :

- Gérer les comptes utilisateurs.
- Consulter les informations nécessaires à l’administration du système, dans le respect des règles de confidentialité.
- Gérer le référentiel des compétences et leurs catégories.
- Superviser les erreurs techniques.
- Contrôler l’utilisation du service d’intelligence artificielle.
- Configurer les limites d’utilisation et les paramètres généraux.

L’administrateur ne doit accéder aux données personnelles ou aux documents privés des utilisateurs que si cela est nécessaire, autorisé et encadré.

# 5. Description détaillée des fonctionnalités

## 5.1. Module 1 : Authentification et gestion des comptes

**Objectif :** permettre aux utilisateurs d’accéder à un espace personnel sécurisé.

Fonctionnalités :

- FR-01 : créer un compte à l’aide d’un nom, d’une adresse e-mail et d’un mot de passe.
- FR-02 : vérifier l’unicité de l’adresse e-mail.
- FR-03 : authentifier l’utilisateur.
- FR-04 : permettre la déconnexion.
- FR-05 : permettre la modification des informations personnelles.
- FR-06 : permettre la modification du mot de passe.
- FR-07 : gérer les rôles et les autorisations.
- FR-08 : protéger les routes et les données privées.

Après sa première connexion, l’utilisateur sera orienté vers le questionnaire initial s’il n’a pas encore terminé son profilage.

## 5.2. Module 2 : Questionnaire initial et profilage intelligent

**Objectif :** recueillir les informations nécessaires pour comprendre le profil, les compétences, le niveau et les objectifs de l’utilisateur avant de lui proposer des recommandations.

Ce module constitue la première étape importante de l’expérience SkillMirror.

### 5.2.1. Déroulement du questionnaire

Le questionnaire sera présenté sous la forme d’un assistant interactif composé de plusieurs étapes courtes. L’utilisateur pourra naviguer entre les étapes, revenir en arrière et sauvegarder ses réponses.

### Étape 1 : situation actuelle

Le système demande à l’utilisateur :

- Quelle est votre situation actuelle ?
  - Étudiant.
  - Jeune diplômé.
  - Salarié.
  - Personne en reconversion professionnelle.
  - En recherche d’emploi.
- Quel est votre domaine d’études ou d’activité ?
- Quel est votre niveau d’études ou votre niveau professionnel ?
- Avez-vous déjà effectué un stage ou occupé un emploi ?

### Étape 2 : identification des compétences

L’utilisateur sélectionne les compétences qu’il connaît et peut en ajouter d’autres.

Les catégories peuvent inclure :

- Langages de programmation : Java, Python, C, JavaScript, etc.
- Développement web : HTML, CSS, Angular, Spring Boot, etc.
- Bases de données : MySQL, MongoDB, etc.
- Analyse de données et statistiques.
- Intelligence artificielle et apprentissage automatique.
- Outils de développement : Git, GitHub, Docker, etc.
- Compétences transversales : communication, travail en équipe, résolution de problèmes, gestion du temps.

Les compétences proposées doivent être adaptées au domaine de l’utilisateur, sans l’empêcher d’ajouter une compétence absente du référentiel.

### Étape 3 : autoévaluation du niveau

Pour chaque compétence sélectionnée, l’utilisateur indique son niveau estimé :

- Débutant : connaît les notions fondamentales.
- Intermédiaire : peut réaliser des exercices et des projets simples.
- Avancé : peut réaliser des projets plus complexes de manière autonome.
- Expert : possède une expérience approfondie et peut résoudre des problèmes complexes.

L’utilisateur peut également indiquer qu’il ne connaît pas suffisamment son niveau.

Ces niveaux représentent une autoévaluation initiale et ne constituent pas une certification.

### Étape 4 : expériences et preuves

Le système demande :

- Avez-vous déjà utilisé ces compétences dans un projet ?
- Avez-vous réalisé des projets universitaires ou personnels ?
- Avez-vous effectué un stage ou une expérience professionnelle ?
- Possédez-vous des certifications ?
- Disposez-vous d’un portfolio ou d’un compte GitHub ?
- Souhaitez-vous joindre un CV ?

Les réponses sont facultatives lorsque les informations ne sont pas disponibles.

### Étape 5 : objectif professionnel

L’utilisateur indique :

- Quel métier souhaite-t-il exercer ?
- Quel domaine l’intéresse le plus ?
- Quel est son objectif principal : obtenir un premier emploi, améliorer son profil ou changer de métier ?
- A-t-il une entreprise ou une offre d’emploi en tête ?
- Quel type de poste recherche-t-il : stage, emploi junior ou poste expérimenté ?

Les métiers proposés peuvent inclure développeur Full Stack, Data Analyst, ingénieur IA junior ou spécialiste en cybersécurité. L’utilisateur peut aussi choisir un autre métier ou indiquer qu’il n’a pas encore défini son objectif.

### Étape 6 : préférences d’apprentissage

Le système demande :

- Combien d’heures par semaine l’utilisateur peut-il consacrer à l’apprentissage ?
- Préfère-t-il les projets pratiques, les exercices ou les cours théoriques ?
- Quel niveau de difficulté souhaite-t-il ?
- Dispose-t-il d’une échéance pour atteindre son objectif ?

Ces informations serviront à adapter les projets et le rythme du parcours d’apprentissage.

### 5.2.2. Traitement des réponses

Après validation du questionnaire, le système doit :

1. Enregistrer les réponses dans la base de données.
2. Créer le profil professionnel initial.
3. Enregistrer les compétences déclarées et les niveaux autoévalués.
4. Identifier les informations manquantes.
5. Proposer des évaluations complémentaires si nécessaire.
6. Déterminer les compétences généralement associées à l’objectif professionnel.
7. Préparer une première recommandation personnalisée.
8. Orienter l’utilisateur vers son tableau de bord.

L’intelligence artificielle peut aider à interpréter les réponses et à personnaliser les recommandations. Cependant, les données structurées et les résultats doivent être validés par le backend.

### 5.2.3. Règles de gestion

- RG-01 : le questionnaire est proposé lors de la première utilisation.
- RG-02 : l’utilisateur peut sauvegarder ses réponses et reprendre plus tard.
- RG-03 : les réponses peuvent être modifiées après validation.
- RG-04 : les compétences déclarées restent distinctes des compétences vérifiées.
- RG-05 : l’absence d’expérience ou de projet ne doit pas empêcher l’utilisation de la plateforme.
- RG-06 : les recommandations doivent tenir compte de l’objectif professionnel et du temps disponible.
- RG-07 : les informations personnelles collectées doivent être limitées au nécessaire.

## 5.3. Module 3 : gestion du profil professionnel (Skill DNA)

**Objectif :** centraliser les informations professionnelles de l’utilisateur.

Le profil doit contenir :

- Informations personnelles.
- Situation académique ou professionnelle.
- Objectif de carrière.
- Compétences et niveaux déclarés.
- Expériences professionnelles.
- Projets réalisés.
- Certifications.
- Liens vers les réalisations.
- Preuves de compétences.
- Historique des analyses.

Fonctionnalités :

- FR-09 : consulter et modifier le profil.
- FR-10 : ajouter, modifier et supprimer des compétences.
- FR-11 : gérer les projets et expériences.
- FR-12 : ajouter des certifications et des liens de preuve.
- FR-13 : importer un CV si cette fonctionnalité est activée.
- FR-14 : extraire des compétences potentielles à partir du CV.
- FR-15 : distinguer les compétences déclarées, évaluées et démontrées.

Le système devra associer chaque compétence vérifiée à une source identifiable, telle qu’un projet, un test ou une certification.

## 5.4. Module 4 : analyse des offres d’emploi

**Objectif :** identifier les exigences d’une offre d’emploi.

Dans le MVP, l’utilisateur pourra copier le texte d’une offre et le coller dans un formulaire.

Les données pourront inclure :

- Intitulé du poste.
- Nom de l’entreprise.
- Description du poste.
- Compétences techniques demandées.
- Compétences transversales.
- Niveau d’expérience.
- Formation ou certifications exigées.

Fonctionnalités :

- FR-16 : ajouter et sauvegarder une offre.
- FR-17 : extraire les compétences du texte grâce à l’intelligence artificielle.
- FR-18 : classer les compétences par catégorie.
- FR-19 : identifier les exigences obligatoires et souhaitées lorsque le texte permet de les distinguer.
- FR-20 : permettre à l’utilisateur de vérifier et de corriger les compétences extraites.

L’importation automatique depuis des plateformes externes sera considérée comme une évolution future.

## 5.5. Module 5 : moteur d’analyse des écarts (Career Gap Engine)

**Objectif :** comparer les exigences d’un poste aux compétences de l’utilisateur.

Le moteur devra classer les compétences dans les catégories suivantes :

- Compétences démontrées par des preuves.
- Compétences déclarées mais non vérifiées.
- Compétences partiellement maîtrisées.
- Compétences manquantes ou insuffisamment documentées.

Fonctionnalités :

- FR-21 : comparer les compétences du profil avec celles de l’offre.
- FR-22 : calculer un indicateur de compatibilité.
- FR-23 : identifier les compétences prioritaires à améliorer.
- FR-24 : expliquer les écarts détectés.
- FR-25 : sauvegarder les résultats de chaque analyse.
- FR-26 : permettre de consulter l’historique des analyses.

### Calcul indicatif du score de compatibilité

Le score pourra être calculé à l’aide d’une formule pondérée :

Score de compatibilité = (somme des poids multipliés par les scores de maîtrise) / (somme des poids) × 100.

Le poids représente l’importance d’une compétence dans l’offre. Le score de maîtrise représente le degré auquel l’utilisateur satisfait le niveau demandé, selon les règles définies par le système.

Les compétences dont la maîtrise n’est pas suffisamment documentée ne doivent pas être automatiquement considérées comme acquises.

Le score constitue un indicateur interne de compatibilité, et non une probabilité réelle d’obtenir le poste.

## 5.6. Module 6 : génération de projets pratiques (Project-to-Proof)

**Objectif :** transformer les compétences manquantes en actions d’apprentissage concrètes.

Pour chaque compétence prioritaire, le système peut générer un projet adapté au niveau de l’utilisateur.

Exemple : pour améliorer SQL, le système peut proposer un projet d’analyse de données comprenant la création d’une base de données, la rédaction de requêtes, l’analyse des résultats et la rédaction d’un rapport.

Chaque projet devra comporter :

- Titre et description.
- Compétence cible.
- Objectifs pédagogiques.
- Niveau de difficulté.
- Outils nécessaires.
- Étapes de réalisation.
- Liste des tâches.
- Critères d’évaluation.
- Résultat attendu.
- Preuve à fournir.

Fonctionnalités :

- FR-27 : générer un projet personnalisé.
- FR-28 : définir les étapes et les tâches.
- FR-29 : permettre à l’utilisateur de suivre l’avancement.
- FR-30 : permettre l’ajout de fichiers, de rapports ou de liens de dépôt.
- FR-31 : enregistrer les preuves soumises.
- FR-32 : mettre à jour le statut d’une compétence lorsque les critères de validation sont satisfaits.

L’IA peut générer les consignes et proposer une évaluation, mais la validation doit reposer sur des critères explicites. Une compétence ne doit pas être automatiquement considérée comme maîtrisée simplement parce qu’un projet a été déclaré terminé.

## 5.7. Module 7 : suivi de la progression

**Objectif :** permettre à l’utilisateur de mesurer son évolution.

Fonctionnalités :

- FR-33 : afficher les parcours en cours et terminés.
- FR-34 : afficher l’état de chaque tâche.
- FR-35 : conserver l’historique des compétences évaluées.
- FR-36 : afficher l’évolution des indicateurs de compatibilité.
- FR-37 : permettre de mettre à jour le profil après une nouvelle réalisation.
- FR-38 : recommander les prochaines actions selon les progrès réalisés.

## 5.8. Module 8 : tableau de bord

Le tableau de bord doit fournir une vue synthétique du profil et des objectifs.

Il pourra afficher :

- Nombre d’offres analysées.
- Dernier score de compatibilité.
- Principales compétences à améliorer.
- Nombre de projets en cours.
- Nombre de projets terminés.
- Taux d’avancement des parcours.
- Historique des analyses.
- Recommandations prioritaires.

Les indicateurs doivent être calculés à partir des données réelles de l’utilisateur.

## 5.9. Module 9 : simulateur de parcours professionnel (évolution future)

Ce module permettra de comparer plusieurs stratégies d’apprentissage.

Exemple : comparer un parcours axé sur SQL et Power BI à un parcours axé sur Python et les statistiques pour viser un poste de Data Analyst.

Le système pourra estimer :

- Les compétences susceptibles d’être améliorées.
- Les projets à réaliser.
- Le temps nécessaire selon les hypothèses retenues.
- L’évolution possible de l’indicateur de compatibilité.

Les résultats seront présentés comme des estimations et non comme des prédictions certaines de recrutement.

# 6. Exigences non fonctionnelles

Le système devra respecter les exigences suivantes :

- **Sécurité :** protéger les comptes, les API et les données personnelles.
- **Confidentialité :** limiter l’accès aux documents et aux profils.
- **Performance :** limiter les temps de réponse et éviter les appels inutiles aux services d’IA.
- **Ergonomie :** proposer une interface intuitive et adaptée aux ordinateurs et aux téléphones.
- **Fiabilité :** gérer les erreurs et vérifier les résultats générés.
- **Maintenabilité :** structurer le code et documenter les composants.
- **Évolutivité :** permettre l’ajout de nouvelles fonctionnalités.
- **Traçabilité :** conserver les informations nécessaires pour expliquer les résultats.
- **Transparence :** indiquer les limites des évaluations et des recommandations.

# 7. Architecture technique

## 7.1. Frontend : Angular

Le frontend sera responsable de :

- L’affichage des pages.
- Le questionnaire initial.
- La gestion des formulaires.
- Le profil professionnel.
- La saisie des offres d’emploi.
- L’affichage des analyses.
- La présentation des projets et des indicateurs de progression.
- La communication avec le backend via REST API.

## 7.2. Backend : Spring Boot

Le backend assurera :

- La logique métier.
- L’authentification et l’autorisation.
- La validation des données.
- La gestion du questionnaire et des profils.
- Le traitement des offres et des analyses.
- Le calcul des scores.
- L’orchestration des appels à l’intelligence artificielle.
- La gestion des projets, des tâches et des preuves.
- La communication avec la base de données.

Les composants pourront être organisés en couches : contrôleurs REST, services métier, repositories et entités.

## 7.3. Base de données : MySQL

MySQL servira à stocker les informations structurées de l’application : comptes, réponses au questionnaire, compétences, offres d’emploi, résultats d’analyse, parcours et preuves.

## 7.4. Intelligence artificielle

Une API de modèle de langage sera utilisée pour :

- Extraire les compétences des CV et des offres.
- Interpréter les réponses en langage naturel.
- Générer des projets adaptés.
- Produire des explications et des recommandations.

Les règles de calcul des scores, les contrôles d’accès et la validation des données resteront sous le contrôle du backend.

## 7.5. Technologies complémentaires

- Spring Security pour la sécurité.
- JWT pour la gestion des jetons d’authentification si cette approche est retenue.
- Spring Data JPA pour l’accès aux données.
- Git et GitHub pour la gestion du code source.
- Docker pour faciliter la reproductibilité de l’environnement.
- Python, éventuellement, pour des traitements spécialisés futurs.

# 8. Conception de la base de données

Les principales tables proposées sont les suivantes :

| Table | Rôle |
|---|---|
| `users` | Comptes et rôles des utilisateurs |
| `profiles` | Informations professionnelles et objectifs |
| `questionnaire_responses` | Réponses au questionnaire initial |
| `skills` | Référentiel des compétences |
| `user_skills` | Compétences déclarées, niveaux et statuts |
| `projects` | Projets personnels et professionnels |
| `experiences` | Expériences et stages |
| `certificates` | Certifications |
| `job_offers` | Offres d’emploi enregistrées |
| `job_skills` | Compétences demandées par les offres |
| `analyses` | Résultats des comparaisons |
| `analysis_details` | Détail des écarts par compétence |
| `learning_paths` | Parcours d’apprentissage |
| `learning_tasks` | Tâches associées aux parcours |
| `skill_evidence` | Preuves liées aux compétences |

Les relations entre les tables devront être définies avec des clés primaires, des clés étrangères et des contraintes d’intégrité.

Les réponses au questionnaire devront pouvoir être modifiées et réutilisées pour actualiser le profil sans créer de doublons.

# 9. API REST prévisionnelle

| Méthode | Endpoint | Fonction |
|---|---|---|
| POST | `/api/auth/register` | Créer un compte |
| POST | `/api/auth/login` | Se connecter |
| POST | `/api/auth/logout` | Se déconnecter |
| GET | `/api/questionnaire` | Obtenir les questions |
| POST | `/api/questionnaire/responses` | Enregistrer les réponses |
| GET | `/api/profiles/me` | Consulter son profil |
| PUT | `/api/profiles/me` | Modifier son profil |
| POST | `/api/skills/analyze` | Analyser les compétences |
| POST | `/api/jobs` | Ajouter une offre |
| POST | `/api/jobs/analyze` | Analyser une offre |
| GET | `/api/career/gaps` | Consulter les écarts |
| POST | `/api/projects/generate` | Générer un projet |
| GET | `/api/learning-paths` | Consulter ses parcours |
| PATCH | `/api/learning-tasks/{id}` | Modifier l’état d’une tâche |
| POST | `/api/evidence` | Ajouter une preuve |
| GET | `/api/progress` | Consulter sa progression |

Ces endpoints constituent une proposition initiale. Leur contrat précis devra être défini pendant la conception technique.

# 10. Sécurité et protection des données

Le système devra :

- Hacher les mots de passe avant leur stockage.
- Protéger les endpoints selon le rôle et l’identité de l’utilisateur.
- Valider les données reçues par l’API.
- Ne jamais exposer les clés API de l’intelligence artificielle dans le frontend.
- Limiter les données personnelles envoyées aux services externes.
- Prévoir une politique de conservation et de suppression des données.
- Empêcher les accès non autorisés aux CV, aux projets et aux résultats.
- Gérer les erreurs sans divulguer d’informations techniques sensibles.
- Informer l’utilisateur des traitements réalisés par l’IA.

# 11. Planification du développement

Le planning indicatif suivant prévoit six semaines de travail.

**Semaine 1 : conception et fondations**
- Définition de l’architecture.
- Initialisation Angular, Spring Boot et MySQL.
- Conception de la base de données.
- Authentification.

**Semaine 2 : questionnaire et profil**
- Développement du questionnaire initial.
- Enregistrement et modification des réponses.
- Création du profil professionnel.
- Gestion des compétences et des projets.

**Semaine 3 : gestion des offres**
- Formulaire de saisie des offres.
- Intégration de l’API d’IA pour l’extraction.
- Vérification et sauvegarde des compétences extraites.

**Semaine 4 : analyse des écarts**
- Implémentation de l’algorithme de comparaison.
- Calcul du score de compatibilité.
- Affichage des compétences manquantes.
- Génération des recommandations.

**Semaine 5 : projets et progression**
- Génération des projets pratiques.
- Gestion des tâches.
- Soumission des preuves.
- Tableau de bord.

**Semaine 6 : tests et déploiement**
- Tests fonctionnels et techniques.
- Vérification des permissions.
- Correction des erreurs.
- Déploiement de la version pilote.

Ce planning peut être ajusté selon la complexité retenue, le niveau d’expérience du développeur et les services externes utilisés.

# 12. Tests et critères d’acceptation

Le MVP sera considéré comme fonctionnel lorsque les critères suivants seront satisfaits :

- Un utilisateur peut créer un compte et s’authentifier.
- Le questionnaire initial est affiché et ses réponses sont enregistrées.
- Le profil professionnel est généré à partir des réponses.
- Les compétences peuvent être ajoutées et modifiées.
- Une offre d’emploi peut être saisie et sauvegardée.
- Les compétences demandées peuvent être extraites et vérifiées.
- Le système compare les exigences avec les données du profil.
- Les résultats sont accompagnés d’explications.
- Les projets pratiques peuvent être générés et suivis.
- Les preuves de réalisation peuvent être enregistrées.
- Le tableau de bord présente des données cohérentes.
- Les données privées ne sont accessibles qu’aux utilisateurs autorisés.
- Les erreurs de base de données et d’IA sont correctement gérées.

Les tests comprendront des tests unitaires, des tests d’intégration, des tests API, des tests de sécurité et des tests fonctionnels avec des utilisateurs pilotes.

# 13. Risques et mesures préventives

| Risque | Mesure préventive |
|---|---|
| Autoévaluation imprécise | Proposer des tests et des projets pratiques |
| Résultats incorrects de l’IA | Vérifier les résultats et conserver des règles déterministes |
| Coût élevé des appels IA | Limiter les appels, les tailles de requêtes et les usages |
| Données sensibles | Réduire les données collectées et sécuriser leur stockage |
| Accès aux offres externes | Commencer par une saisie manuelle |
| Projet trop ambitieux | Limiter la première version aux fonctionnalités essentielles |
| Recommandations peu pertinentes | Tester la plateforme auprès d’étudiants et ajuster les parcours |

# 14. Livrables attendus

À la fin du projet, les livrables seront :

1. Une application web Angular fonctionnelle.
2. Un backend Spring Boot exposant les API nécessaires.
3. Une base de données MySQL structurée.
4. Un questionnaire initial intégré.
5. Un module d’analyse des compétences et des offres.
6. Un module de génération de projets pratiques.
7. Un tableau de bord de progression.
8. Une documentation technique.
9. Une documentation des endpoints API.
10. Des tests et leurs résultats.
11. Une version déployée et accessible pour les essais.
12. Un guide d’utilisation.

# 15. Conclusion

SkillMirror est une plateforme destinée à accompagner les utilisateurs dans le développement de leurs compétences et la préparation de leurs objectifs professionnels.

La première étape du parcours est un questionnaire interactif permettant de recueillir les compétences, le niveau estimé, les expériences et les ambitions de l’utilisateur. Ces informations servent à créer un profil initial personnalisé.

La plateforme permet ensuite d’analyser des offres d’emploi, de comparer leurs exigences au profil de l’utilisateur, d’identifier les compétences à développer et de proposer des projets pratiques pour progresser.

La valeur ajoutée de SkillMirror repose sur la continuité entre l’analyse des besoins professionnels, l’apprentissage pratique et la constitution de preuves de compétences.

La priorité du développement sera de réaliser un MVP fiable, explicable et sécurisé, avant d’intégrer des fonctionnalités avancées comme le simulateur de carrière, l’analyse approfondie de GitHub ou les outils destinés aux entreprises.
