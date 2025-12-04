# Guide de Test Postman - API Menu Items

Ce guide vous explique comment tester toutes les APIs du contrôleur MenuItem avec Postman.

## 📋 Prérequis

1. **Démarrer l'application** : Assurez-vous que votre application Spring Boot est en cours d'exécution (port 8080 par défaut)
2. **Postman installé** : Téléchargez Postman depuis [postman.com](https://www.postman.com/)
3. **Authentification** : Les endpoints de modification nécessitent une authentification JWT avec le rôle RESTAURANT.

---

## 🔐 Étape 1 : Authentification

### 1.1 Se connecter en tant que RESTAURANT

**Endpoint** : `POST http://localhost:8080/api/auth/login`

**Headers** :
```
Content-Type: application/json
```

**Body (raw JSON)** :
```json
{
  "email": "restaurant@example.com",
  "password": "password123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "restaurant@example.com",
  "role": "RESTAURANT"
}
```

**Action** : Copiez le `token` pour l'utiliser dans les prochaines requêtes.

---

## 📝 Étape 2 : Configuration de l'authentification dans Postman

Pour chaque requête nécessitant une authentification (création, modification, suppression) :

1. Allez dans l'onglet **Authorization**
2. Sélectionnez **Bearer Token**
3. Collez le token obtenu lors de la connexion

**OU** ajoutez manuellement dans **Headers** :
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🍔 Étape 3 : Tests des APIs Menu Items

### 3.1 ✅ Créer un Article de Menu (RESTAURANT uniquement)

**Méthode** : `POST`  
**URL** : `http://localhost:8080/api/menu-items`  
**Authentification** : Bearer Token (RESTAURANT)

**Headers** :
```
Content-Type: application/json
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body (raw JSON)** :
```json
{
  "name": "Pizza Margherita",
  "description": "Tomate, mozzarella, basilic frais",
  "price": 12.50,
  "imageUrl": "https://example.com/margherita.jpg",
  "category": "PIZZA",
  "isAvailable": true
}
```

**Réponse attendue** : `201 Created`
```json
{
  "id": 1,
  "restaurantId": 1,
  "name": "Pizza Margherita",
  "description": "Tomate, mozzarella, basilic frais",
  "price": 12.50,
  "imageUrl": "https://example.com/margherita.jpg",
  "category": "PIZZA",
  "isAvailable": true,
  "createdAt": "2025-12-05T10:00:00",
  "updatedAt": "2025-12-05T10:00:00"
}
```

---

### 3.2 ✏️ Modifier un Article de Menu (RESTAURANT uniquement)

**Méthode** : `PUT`  
**URL** : `http://localhost:8080/api/menu-items/{id}`  
**Exemple** : `http://localhost:8080/api/menu-items/1`  
**Authentification** : Bearer Token (RESTAURANT)

**Headers** :
```
Content-Type: application/json
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body (raw JSON)** :
```json
{
  "name": "Pizza Margherita Royal",
  "description": "Tomate, mozzarella di bufala, basilic frais, huile d'olive",
  "price": 14.50,
  "imageUrl": "https://example.com/margherita-royal.jpg",
  "category": "PIZZA",
  "isAvailable": true
}
```

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "restaurantId": 1,
  "name": "Pizza Margherita Royal",
  "description": "Tomate, mozzarella di bufala, basilic frais, huile d'olive",
  "price": 14.50,
  "imageUrl": "https://example.com/margherita-royal.jpg",
  "category": "PIZZA",
  "isAvailable": true,
  "createdAt": "2025-12-05T10:00:00",
  "updatedAt": "2025-12-05T10:15:00"
}
```

---

### 3.3 🗑️ Supprimer un Article de Menu (RESTAURANT uniquement)

**Méthode** : `DELETE`  
**URL** : `http://localhost:8080/api/menu-items/{id}`  
**Exemple** : `http://localhost:8080/api/menu-items/1`  
**Authentification** : Bearer Token (RESTAURANT)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body** : Aucun

**Réponse attendue** : `204 No Content`

---

### 3.4 📋 Obtenir les Articles de Menu d'un Restaurant (Public)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/{restaurantId}/menu-items`  
**Exemple** : `http://localhost:8080/api/restaurants/1/menu-items`  
**Authentification** : Aucune

**Paramètres de requête (Query Params)** :
- `activeOnly` : `true` (par défaut) pour voir seulement les articles disponibles, `false` pour tout voir.

**Exemples d'URLs** :
```
http://localhost:8080/api/restaurants/1/menu-items
http://localhost:8080/api/restaurants/1/menu-items?activeOnly=false
```

**Réponse attendue** : `200 OK`
```json
[
  {
    "id": 2,
    "restaurantId": 1,
    "name": "Pasta Carbonara",
    "description": "Pâtes fraîches, guanciale, oeuf, pecorino",
    "price": 13.00,
    "imageUrl": "https://example.com/carbonara.jpg",
    "category": "PASTA",
    "isAvailable": true,
    "createdAt": "2025-12-05T10:05:00",
    "updatedAt": "2025-12-05T10:05:00"
  }
]
```

---

## 🔄 Étape 4 : Scénario de Test Complet

### Scénario : Gestion du Menu

1. **Se connecter en tant que RESTAURANT**
   ```
   POST /api/auth/login
   ```

2. **Ajouter un nouvel article**
   ```
   POST /api/menu-items
   ```

3. **Vérifier que l'article apparaît dans la liste du restaurant**
   ```
   GET /api/restaurants/{myRestaurantId}/menu-items
   ```

4. **Mettre à jour le prix de l'article**
   ```
   PUT /api/menu-items/{itemId}
   ```

5. **Supprimer l'article (si nécessaire)**
   ```
   DELETE /api/menu-items/{itemId}
   ```

---

## 🛠️ Conseils Postman

### Créer une Collection
1. Cliquez sur **New** → **Collection**
2. Nommez-la "FoodNow - Menu Items API"
3. Ajoutez toutes les requêtes dans cette collection

### Utiliser des Variables
Utilisez les mêmes variables que pour l'API Restaurants :
- `baseUrl` : `http://localhost:8080`
- `restaurantToken` : (à remplir après login)

---

## ❗ Codes d'erreur courants

| Code | Signification | Solution |
|------|---------------|----------|
| 401 | Non autorisé | Vérifiez votre token JWT |
| 403 | Accès refusé | Vérifiez que vous avez le rôle RESTAURANT |
| 404 | Non trouvé | Vérifiez l'ID de l'article ou du restaurant |
| 400 | Requête invalide | Vérifiez le format JSON (prix positif, nom non vide, etc.) |
| 500 | Erreur serveur | Vérifiez les logs de l'application |

---

## 📞 Support

Si vous rencontrez des problèmes, vérifiez d'abord que vous êtes bien connecté avec un compte qui possède un restaurant associé. L'ajout d'un article de menu lie automatiquement cet article au restaurant de l'utilisateur connecté.
