# Guide de Test Postman - API Restaurants

Ce guide vous explique comment tester toutes les APIs du contrôleur Restaurant avec Postman.

## 📋 Prérequis

1. **Démarrer l'application** : Assurez-vous que votre application Spring Boot est en cours d'exécution (port 8080 par défaut)
2. **Postman installé** : Téléchargez Postman depuis [postman.com](https://www.postman.com/)
3. **Authentification** : La plupart des endpoints nécessitent une authentification JWT

---

## 🔐 Étape 1 : Authentification

### 1.1 Se connecter en tant qu'ADMIN

**Endpoint** : `POST http://localhost:8080/api/auth/login`

**Headers** :
```
Content-Type: application/json
```

**Body (raw JSON)** :
```json
{
  "email": "admin@foodnow.com",
  "password": "admin123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@foodnow.com",
  "role": "ADMIN"
}
```

**Action** : Copiez le `token` pour l'utiliser dans les prochaines requêtes.

### 1.2 Se connecter en tant que RESTAURANT

**Endpoint** : `POST http://localhost:8080/api/auth/login`

**Body (raw JSON)** :
```json
{
  "email": "restaurant@example.com",
  "password": "password123"
}
```

---

## 📝 Étape 2 : Configuration de l'authentification dans Postman

Pour chaque requête nécessitant une authentification :

1. Allez dans l'onglet **Authorization**
2. Sélectionnez **Bearer Token**
3. Collez le token obtenu lors de la connexion

**OU** ajoutez manuellement dans **Headers** :
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🏪 Étape 3 : Tests des APIs Restaurant

### 3.1 ✅ Créer un Restaurant (ADMIN uniquement)

**Méthode** : `POST`  
**URL** : `http://localhost:8080/api/restaurants`  
**Authentification** : Bearer Token (ADMIN)

**Headers** :
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body (raw JSON)** :
```json
{
  "name": "Pizza Palace",
  "address": "123 Rue de la Pizza, Paris 75001",
  "description": "Les meilleures pizzas de Paris",
  "phone": "+33123456789",
  "imageUrl": "https://example.com/pizza-palace.jpg",
  "ownerEmail": "owner@pizzapalace.com",
  "ownerPassword": "SecurePass123!",
  "ownerFullName": "Jean Dupont",
  "ownerPhoneNumber": "+33987654321"
}
```

**Réponse attendue** : `201 Created`
```json
{
  "id": 1,
  "name": "Pizza Palace",
  "address": "123 Rue de la Pizza, Paris 75001",
  "description": "Les meilleures pizzas de Paris",
  "phone": "+33123456789",
  "imageUrl": "https://example.com/pizza-palace.jpg",
  "active": true,
  "createdAt": "2025-12-04T17:00:00"
}
```

---

### 3.2 ✏️ Modifier un Restaurant (ADMIN ou RESTAURANT)

**Méthode** : `PUT`  
**URL** : `http://localhost:8080/api/restaurants/{id}`  
**Exemple** : `http://localhost:8080/api/restaurants/1`  
**Authentification** : Bearer Token (ADMIN ou RESTAURANT)

**Headers** :
```
Content-Type: application/json
Authorization: Bearer <TOKEN>
```

**Body (raw JSON)** :
```json
{
  "name": "Pizza Palace Premium",
  "address": "123 Rue de la Pizza, Paris 75001",
  "description": "Les meilleures pizzas artisanales de Paris",
  "phone": "+33123456789",
  "imageUrl": "https://example.com/pizza-palace-new.jpg",
  "ownerEmail": "owner@pizzapalace.com",
  "ownerPassword": "NewSecurePass123!",
  "ownerFullName": "Jean Dupont",
  "ownerPhoneNumber": "+33987654321"
}
```

**Réponse attendue** : `200 OK`

---

### 3.3 🔄 Activer/Désactiver un Restaurant (ADMIN uniquement)

**Méthode** : `PATCH`  
**URL** : `http://localhost:8080/api/restaurants/{id}/status`  
**Exemple** : `http://localhost:8080/api/restaurants/1/status`  
**Authentification** : Bearer Token (ADMIN)

**Headers** :
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Body** : Aucun

**Réponse attendue** : `204 No Content`

---

### 3.4 🔍 Obtenir un Restaurant par ID (Public)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/{id}`  
**Exemple** : `http://localhost:8080/api/restaurants/1`  
**Authentification** : Aucune

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "name": "Pizza Palace",
  "address": "123 Rue de la Pizza, Paris 75001",
  "description": "Les meilleures pizzas de Paris",
  "phone": "+33123456789",
  "imageUrl": "https://example.com/pizza-palace.jpg",
  "active": true,
  "createdAt": "2025-12-04T17:00:00"
}
```

---

### 3.5 📋 Obtenir tous les Restaurants actifs (Public)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants`  
**Authentification** : Aucune

**Paramètres de requête (Query Params)** :
- `page` : Numéro de page (défaut: 0)
- `size` : Nombre d'éléments par page (défaut: 10)
- `sortBy` : Champ de tri (défaut: id)
- `sortDir` : Direction du tri (asc/desc, défaut: asc)

**Exemples d'URLs** :
```
http://localhost:8080/api/restaurants
http://localhost:8080/api/restaurants?page=0&size=5
http://localhost:8080/api/restaurants?page=0&size=10&sortBy=name&sortDir=asc
```

**Réponse attendue** : `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Pizza Palace",
      "address": "123 Rue de la Pizza, Paris 75001",
      "active": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### 3.6 📋 Obtenir tous les Restaurants (ADMIN - incluant inactifs)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/admin`  
**Authentification** : Bearer Token (ADMIN)

**Headers** :
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Paramètres de requête** : Identiques à 3.5

**Exemple** :
```
http://localhost:8080/api/restaurants/admin?page=0&size=10
```

---

### 3.7 🏪 Obtenir mon Restaurant (RESTAURANT uniquement)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/my-restaurant`  
**Authentification** : Bearer Token (RESTAURANT)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "name": "Pizza Palace",
  "address": "123 Rue de la Pizza, Paris 75001",
  "description": "Les meilleures pizzas de Paris",
  "active": true
}
```

---

## 📦 Étape 4 : Tests des APIs de Commandes

### 4.1 📋 Obtenir les commandes de mon Restaurant (RESTAURANT)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/my-restaurant/orders`  
**Authentification** : Bearer Token (RESTAURANT)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Paramètres de requête** :
- `page` : 0
- `size` : 10
- `sortBy` : createdAt
- `sortDir` : desc

**Exemple** :
```
http://localhost:8080/api/restaurants/my-restaurant/orders?page=0&size=10&sortBy=createdAt&sortDir=desc
```

**Réponse attendue** : `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "status": "PENDING",
      "totalPrice": 25.50,
      "createdAt": "2025-12-04T16:30:00",
      "items": [...]
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 5,
  "totalPages": 1
}
```

---

### 4.2 🔍 Filtrer les commandes par statut (RESTAURANT)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/my-restaurant/orders/status/{status}`  
**Authentification** : Bearer Token (RESTAURANT)

**Statuts possibles** :
- `PENDING` : En attente
- `ACCEPTED` : Acceptée
- `PREPARING` : En préparation
- `READY_FOR_PICKUP` : Prête pour livraison
- `OUT_FOR_DELIVERY` : En cours de livraison
- `DELIVERED` : Livrée
- `CANCELLED` : Annulée

**Exemples d'URLs** :
```
http://localhost:8080/api/restaurants/my-restaurant/orders/status/PENDING
http://localhost:8080/api/restaurants/my-restaurant/orders/status/ACCEPTED?page=0&size=5
```

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

---

### 4.3 🔍 Obtenir une commande spécifique (RESTAURANT ou ADMIN)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Authorization: Bearer <TOKEN>
```

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "restaurantId": 1,
  "restaurantName": "Pizza Palace",
  "customerId": 5,
  "customerName": "Marie Martin",
  "status": "PENDING",
  "totalPrice": 25.50,
  "deliveryAddress": "456 Rue Client, Paris",
  "createdAt": "2025-12-04T16:30:00",
  "items": [
    {
      "id": 1,
      "menuItemName": "Pizza Margherita",
      "quantity": 2,
      "price": 12.00
    }
  ]
}
```

---

### 4.4 📋 Obtenir les articles d'une commande (RESTAURANT ou ADMIN)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}/items`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1/items`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Authorization: Bearer <TOKEN>
```

**Réponse attendue** : `200 OK`
```json
[
  {
    "id": 1,
    "menuItemId": 10,
    "menuItemName": "Pizza Margherita",
    "quantity": 2,
    "price": 12.00,
    "subtotal": 24.00
  },
  {
    "id": 2,
    "menuItemId": 15,
    "menuItemName": "Coca Cola",
    "quantity": 1,
    "price": 1.50,
    "subtotal": 1.50
  }
]
```

---

### 4.5 📋 Obtenir les commandes d'un Restaurant spécifique (ADMIN)

**Méthode** : `GET`  
**URL** : `http://localhost:8080/api/restaurants/{restaurantId}/orders`  
**Exemple** : `http://localhost:8080/api/restaurants/1/orders`  
**Authentification** : Bearer Token (ADMIN)

**Headers** :
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Paramètres de requête** : Identiques aux autres endpoints de liste

---

## ⚙️ Étape 5 : Gestion des Commandes

### 5.1 ✅ Accepter une commande (PENDING → ACCEPTED)

**Méthode** : `PATCH`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}/accept`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1/accept`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body** : Aucun

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "status": "ACCEPTED",
  "totalPrice": 25.50,
  "acceptedAt": "2025-12-04T16:35:00"
}
```

---

### 5.2 ❌ Rejeter une commande (PENDING/ACCEPTED → CANCELLED)

**Méthode** : `PATCH`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}/reject`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1/reject`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Content-Type: application/json
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body (raw JSON)** - Optionnel :
```json
{
  "reason": "Ingrédients manquants"
}
```

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "status": "CANCELLED",
  "cancellationReason": "Ingrédients manquants",
  "cancelledAt": "2025-12-04T16:40:00"
}
```

---

### 5.3 👨‍🍳 Commencer la préparation (ACCEPTED → PREPARING)

**Méthode** : `PATCH`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}/prepare`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1/prepare`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body** : Aucun

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "status": "PREPARING",
  "preparingStartedAt": "2025-12-04T16:45:00"
}
```

---

### 5.4 ✅ Marquer comme prête (PREPARING → READY_FOR_PICKUP)

**Méthode** : `PATCH`  
**URL** : `http://localhost:8080/api/restaurants/orders/{orderId}/ready`  
**Exemple** : `http://localhost:8080/api/restaurants/orders/1/ready`  
**Authentification** : Bearer Token (RESTAURANT ou ADMIN)

**Headers** :
```
Authorization: Bearer <RESTAURANT_TOKEN>
```

**Body** : Aucun

**Réponse attendue** : `200 OK`
```json
{
  "id": 1,
  "status": "READY_FOR_PICKUP",
  "readyAt": "2025-12-04T17:00:00"
}
```

---

## 🔄 Étape 6 : Scénario de Test Complet

### Scénario : Cycle de vie d'une commande

1. **Se connecter en tant que RESTAURANT**
   ```
   POST /api/auth/login
   ```

2. **Voir toutes les commandes en attente**
   ```
   GET /api/restaurants/my-restaurant/orders/status/PENDING
   ```

3. **Voir les détails d'une commande**
   ```
   GET /api/restaurants/orders/1
   ```

4. **Accepter la commande**
   ```
   PATCH /api/restaurants/orders/1/accept
   ```

5. **Commencer la préparation**
   ```
   PATCH /api/restaurants/orders/1/prepare
   ```

6. **Marquer comme prête**
   ```
   PATCH /api/restaurants/orders/1/ready
   ```

---

## 🛠️ Conseils Postman

### Créer une Collection

1. Cliquez sur **New** → **Collection**
2. Nommez-la "FoodNow - Restaurants API"
3. Ajoutez toutes les requêtes dans cette collection

### Utiliser des Variables d'environnement

1. Créez un environnement "FoodNow Local"
2. Ajoutez ces variables :
   - `baseUrl` : `http://localhost:8080`
   - `adminToken` : (à remplir après login)
   - `restaurantToken` : (à remplir après login)
   - `customerId` : (à remplir après login)

3. Utilisez-les dans vos requêtes :
   ```
   {{baseUrl}}/api/restaurants
   Authorization: Bearer {{adminToken}}
   ```

### Sauvegarder les réponses

Après chaque requête réussie, notez les IDs retournés pour les utiliser dans les tests suivants.

---

## ❗ Codes d'erreur courants

| Code | Signification | Solution |
|------|---------------|----------|
| 401 | Non autorisé | Vérifiez votre token JWT |
| 403 | Accès refusé | Vérifiez que vous avez le bon rôle |
| 404 | Non trouvé | Vérifiez l'ID du restaurant/commande |
| 400 | Requête invalide | Vérifiez le format JSON et les champs requis |
| 500 | Erreur serveur | Vérifiez les logs de l'application |

---

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez que l'application est démarrée
2. Vérifiez les logs dans la console
3. Vérifiez que la base de données est accessible
4. Vérifiez le format de vos requêtes JSON

Bon test ! 🚀
