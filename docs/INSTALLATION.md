# SkillMirror — Installation et guide de lancement

> Plateforme intelligente d'analyse et de développement des compétences professionnelles.
> Signature : **See your skills. Unlock your potential.**

## 1. Vue d'ensemble

| Bloc | Technologie | Port par défaut |
|---|---|---|
| Frontend | Angular 22 (standalone, lazy loading, Vitest) | `4200` (dev server, proxy `/api`) |
| Backend | Java 21 · Spring Boot 3.5 (REST, Security, JPA, Validation, Actuator) | `8080` (`SERVER_PORT`) |
| Base de données | MySQL 8 (ou H2 en mode `dev`) | `3306` |
| IA | API compatible OpenAI (optionnelle) — **mode démonstration sans clé** | — |

Le frontend ne parle jamais directement à MySQL ni à l'API d'IA : tous les appels passent par
`/api` (proxy en développement, même origine en production).

## 2. Prérequis

- **Java 21+** (backend et tests)
- **Node.js 20+ / npm 10+** (frontend)
- **MySQL 8** — soit via Docker (`docker compose up -d`), soit une instance locale
- Maven n'est pas nécessaire : le **Maven Wrapper** (`backend/mvnw`) est fourni

## 3. Base de données

### Option A — Docker (recommandée)

```bash
docker compose up -d        # depuis la racine du dépôt
```

Le conteneur `skillmirror-mysql` crée la base `skillmirror` et l'utilisateur
`skillmirror / skillmirror` (identique aux valeurs par défaut du backend).

### Option B — MySQL déjà installé

Créez la base et un utilisateur dédié (moindre privilège) :

```sql
CREATE DATABASE IF NOT EXISTS skillmirror
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'skillmirror'@'localhost' IDENTIFIED BY 'skillmirror';
GRANT ALL PRIVILEGES ON skillmirror.* TO 'skillmirror'@'localhost';
FLUSH PRIVILEGES;
```

Les tables sont créées automatiquement au démarrage par **Flyway**
(`backend/src/main/resources/db/migration`), y compris le référentiel de compétences.

### Option C — sans MySQL (démarrage instantané)

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Le profil `dev` utilise un H2 en mémoire en mode compatibilité MySQL, avec les **mêmes
migrations**. Idéal pour tester ; utilisez MySQL pour une vraie démonstration.

## 4. Lancer le backend

```bash
cd backend
./mvnw spring-boot:run                        # MySQL, port 8080
# ou
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # H2, port 8080
# ou avec des variables d'environnement
SERVER_PORT=8085 MYSQL_PASSWORD=secret ./mvnw spring-boot:run
```

Vérification : `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`.

### Mode démonstration (données de présentation)

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Crée deux comptes **clairement identifiés** et un parcours complet (profilage, offre,
analyse, projet, preuve validée) :

| Compte | Mot de passe | Rôle |
|---|---|---|
| `demo@skillmirror.local` | `Demo1234!` | Utilisateur |
| `admin@skillmirror.local` | `Admin1234!` | Administrateur |

> Ces comptes sont des données de démonstration : supprimez-les (ou la base) avant toute mise
> en production.

## 5. Lancer le frontend

```bash
cd frontend
npm ci
npm start                 # http://localhost:4200 (proxy /api → http://localhost:8080)
```

Si le backend tourne sur un autre port :

```bash
SKILLMIRROR_API=http://localhost:8085 npm start
```

Build de production : `npm run build` (sortie dans `frontend/dist/`).

## 6. Variables d'environnement

Le fichier [`backend/.env.example`](../backend/.env.example) documente toutes les variables :

| Variable | Rôle | Défaut |
|---|---|---|
| `SERVER_PORT` | Port HTTP du backend | `8080` |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` | Cible MySQL | `localhost` / `3306` / `skillmirror` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Identifiants MySQL | `skillmirror` / `skillmirror` |
| `JWT_SECRET` | Clé de signature HS256 (**à changer en production**) | valeur de dev |
| `JWT_EXPIRATION_SECONDS` | Durée de vie du jeton | `86400` |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées | `http://localhost:4200` |
| `AI_MODE` | `auto` \| `live` \| `demo` | `auto` |
| `AI_BASE_URL` / `AI_API_KEY` / `AI_MODEL` | Fournisseur IA compatible OpenAI | OpenAI v1 / vide / `gpt-4o-mini` |
| `AI_DAILY_USER_LIMIT` | Quota d'appels IA par utilisateur et par jour | `40` |
| `AI_MONTHLY_TOKEN_BUDGET` | Budget mensuel de tokens avant repli en démo | `2000000` |
| `UPLOAD_DIR` / `UPLOAD_MAX_BYTES` | Stockage des preuves téléversées | `./uploads` / 5 Mo |

**Aucune clé API n'est présente dans le frontend.**

## 7. Mode IA : live ou démonstration

- **Sans `AI_API_KEY`** : l'extraction de compétences, la génération de projets et les
  résumés sont produits par un moteur **déterministe et explicable**, et chaque résultat
  porte la mention **« simulé » / « IA simulée »** dans l'interface.
- **Avec une clé** (`AI_API_KEY=sk-...`, `AI_MODE=auto|live`) : les mêmes appels passent par
  le fournisseur ; en cas d'échec ou de dépassement de quota, le système **replie
  automatiquement** sur le moteur de démonstration et le signale.
- Tous les appels (mode, tokens, latence, statut) sont journalisés dans `ai_usage_logs` et
  visibles dans la console d'administration. Le score, les contrôles d'accès et la
  validation des preuves restent **toujours calculés côté backend**, jamais par l'IA.

## 8. Tests

```bash
cd backend  && ./mvnw test      # 10 tests : gap engine, validation des preuves, sécurité
cd frontend && npm test         # tests unitaires (Vitest)
cd frontend && npm run build    # build de production
```

Les tests d'intégration s'exécutent sur H2 (profil `dev`) : migrations Flyway, règles de
score, critères de validation et protections d'endpoints sont vérifiés.

## 9. Principaux endpoints API

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` · `/api/auth/login` | Création de compte, connexion (JWT) |
| GET/PUT | `/api/questionnaire/responses` · POST `/api/questionnaire/complete` | Brouillon et finalisation du profilage |
| GET/PUT | `/api/profiles/me` · `/api/profiles/me/skills` | Profil et compétences (Skill DNA) |
| POST | `/api/jobs` · `/api/jobs/{id}/extract` · `/api/jobs/{id}/skills` | Offre, extraction IA, validation des compétences |
| POST | `/api/analyses` · GET `/api/analyses/{id}` · GET `/api/career/gaps` | Career Gap Engine et historique |
| POST | `/api/learning-paths/generate` · POST `/api/learning-paths/{id}/complete` | Projets générés et validation sous critères |
| PATCH | `/api/learning-tasks/{id}` | État des tâches et notes |
| POST | `/api/evidence` · `/api/evidence/upload` · GET `/api/evidence/{id}/file` | Preuves (lien, rapport, fichier) |
| GET | `/api/dashboard` · `/api/progress` | Statistiques réelles et progression |
| * | `/api/admin/**` | Console d'administration (rôle `ADMIN`) |

## 10. Limites connues (MVP)

- L'importation automatique de CV et l'import d'offres depuis des plateformes externes ne
  sont **pas activés** (saisie manuelle uniquement, conformément au périmètre MVP).
- Le simulateur de carrière « What-If », l'intégration GitHub et l'espace recruteurs sont
  des **évolutions futures**, hors MVP.
- Les fichiers de preuve sont stockés sur le disque du serveur (`UPLOAD_DIR`), accessibles
  uniquement à leur propriétaire ; pour un déploiement multi-instances, brancher un stockage
  objet.
