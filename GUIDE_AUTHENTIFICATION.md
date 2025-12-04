# Guide d'Authentification FoodNow

Ce guide explique comment utiliser le nouveau système d'authentification sécurisé par JWT.

## 1. Inscription (Register)

Pour créer un nouveau compte utilisateur.

**Endpoint:** `POST /api/auth/register`

**Corps de la requête (JSON):**

```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "phoneNumber": "0612345678",
  "role": "CLIENT" 
}
```

*Note: Le rôle peut être `CLIENT`, `RESTAURANT`, ou `LIVREUR`.*

**Réponse (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "CLIENT"
}
```

## 2. Connexion (Login)

Pour se connecter et obtenir un token JWT.

**Endpoint:** `POST /api/auth/login`

**Corps de la requête (JSON):**

```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Réponse (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "CLIENT"
}
```

## 3. Accéder aux ressources protégées

Pour accéder aux autres endpoints de l'API (par exemple, créer une commande, voir les restaurants), vous devez inclure le token JWT dans l'en-tête de la requête.

**Header:**
`Authorization: Bearer <votre_token_jwt>`

Exemple :
`Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...`

## Résumé des composants créés

- **AuthController**: Gère les requêtes HTTP pour l'inscription et la connexion.
- **AuthService**: Contient la logique métier pour créer les utilisateurs et valider les identifiants.
- **JwtService**: Gère la création et la validation des tokens JWT.
- **SecurityConfig**: Configure Spring Security pour protéger l'application et autoriser l'accès public uniquement aux endpoints d'authentification.
- **JwtAuthenticationFilter**: Intercepte chaque requête pour vérifier la validité du token JWT.
- **DTOs**: `LoginRequest`, `RegisterRequest`, `AuthResponse` pour structurer les données.
