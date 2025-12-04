# Service de Consultation des Commandes pour Restaurants

## 📋 Vue d'ensemble

Ce service permet aux propriétaires de restaurants de consulter toutes les commandes reçues par leur restaurant via l'API REST.

## 🆕 Fichiers créés

### 1. **Repository**
- `OrderRepository.java` - Interface pour accéder aux données des commandes
  - Méthodes de recherche par restaurant avec pagination
  - Filtrage par statut de commande
  - Recherche par client

### 2. **DTOs (Data Transfer Objects)**
- `OrderResponse.java` - Représente une commande complète avec toutes ses informations
- `OrderItemResponse.java` - Représente un article dans une commande

### 3. **Service**
Méthodes ajoutées dans `RestaurantService.java` :
- `getMyRestaurantOrders()` - Récupère toutes les commandes du restaurant de l'utilisateur connecté
- `getRestaurantOrders()` - Récupère les commandes d'un restaurant spécifique (admin ou propriétaire)
- `getMyRestaurantOrdersByStatus()` - Filtre les commandes par statut
- `getOrderById()` - Récupère les détails d'une commande spécifique
- `getOrderItems()` - **NOUVEAU** - Récupère uniquement les articles d'une commande
- `mapToOrderResponse()` - Convertit une entité Order en OrderResponse
- `mapToOrderItemResponse()` - Convertit un OrderItem en OrderItemResponse

### 4. **Controller**
Endpoints ajoutés dans `RestaurantController.java` :

## 🔌 Endpoints API

### Pour les propriétaires de restaurants (ROLE: RESTAURANT)

#### 1. Consulter toutes les commandes de mon restaurant
```http
GET /api/restaurants/my-restaurant/orders
```

**Paramètres de requête (optionnels):**
- `page` (default: 0) - Numéro de la page
- `size` (default: 10) - Nombre d'éléments par page
- `sortBy` (default: "createdAt") - Champ de tri
- `sortDir` (default: "desc") - Direction du tri (asc/desc)

**Exemple de requête:**
```http
GET /api/restaurants/my-restaurant/orders?page=0&size=20&sortBy=createdAt&sortDir=desc
```

**Réponse (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "clientId": 5,
      "clientName": "Jean Dupont",
      "clientPhone": "+33612345678",
      "restaurantId": 3,
      "restaurantName": "Le Bon Goût",
      "totalAmount": 45.50,
      "status": "PENDING",
      "deliveryAddress": "123 Rue de la Paix, Paris",
      "createdAt": "2025-12-04T15:30:00",
      "updatedAt": null,
      "orderItems": [
        {
          "id": 1,
          "menuItemId": 10,
          "menuItemName": "Pizza Margherita",
          "quantity": 2,
          "unitPrice": 12.50,
          "subtotal": 25.00
        },
        {
          "id": 2,
          "menuItemId": 15,
          "menuItemName": "Salade César",
          "quantity": 1,
          "unitPrice": 8.50,
          "subtotal": 8.50
        }
      ]
    }
  ],
  "pageNo": 0,
  "pageSize": 20,
  "totalElements": 45,
  "totalPages": 3,
  "last": false
}
```

#### 2. Filtrer les commandes par statut
```http
GET /api/restaurants/my-restaurant/orders/status/{status}
```

**Statuts disponibles:**
- `PENDING` - En attente
- `ACCEPTED` - Acceptée
- `PREPARING` - En préparation
- `READY_FOR_PICKUP` - Prête pour le ramassage
- `IN_DELIVERY` - En livraison
- `DELIVERED` - Livrée
- `CANCELLED` - Annulée

**Exemple de requête:**
```http
GET /api/restaurants/my-restaurant/orders/status/PENDING?page=0&size=10
```

#### 3. Consulter les détails d'une commande spécifique
```http
GET /api/restaurants/orders/{orderId}
```

**Exemple:**
```http
GET /api/restaurants/orders/123
```

#### 4. Consulter uniquement les articles d'une commande
```http
GET /api/restaurants/orders/{orderId}/items
```

**Description:** Récupère uniquement la liste des articles (items) d'une commande spécifique, sans les autres informations de la commande.

**Exemple:**
```http
GET /api/restaurants/orders/123/items
```

**Réponse (200 OK):**
```json
[
  {
    "id": 1,
    "menuItemId": 10,
    "menuItemName": "Pizza Margherita",
    "quantity": 2,
    "unitPrice": 12.50,
    "subtotal": 25.00
  },
  {
    "id": 2,
    "menuItemId": 15,
    "menuItemName": "Salade César",
    "quantity": 1,
    "unitPrice": 8.50,
    "subtotal": 8.50
  },
  {
    "id": 3,
    "menuItemId": 20,
    "menuItemName": "Tiramisu",
    "quantity": 2,
    "unitPrice": 6.00,
    "subtotal": 12.00
  }
]
```

**Cas d'utilisation:**
- Afficher uniquement les articles dans une vue détaillée
- Générer un ticket de cuisine avec juste les items
- Calculer des statistiques sur les articles commandés

### Pour les administrateurs (ROLE: ADMIN)

#### 4. Consulter les commandes d'un restaurant spécifique
```http
GET /api/restaurants/{restaurantId}/orders
```

**Exemple:**
```http
GET /api/restaurants/5/orders?page=0&size=10&sortBy=createdAt&sortDir=desc
```

## 🔒 Sécurité

- **Authentification requise** : Tous les endpoints nécessitent une authentification JWT
- **Autorisation** :
  - Les propriétaires de restaurants peuvent uniquement voir les commandes de LEUR restaurant
  - Les administrateurs peuvent voir les commandes de n'importe quel restaurant
- **Validation** : Le service vérifie automatiquement que l'utilisateur a les droits d'accès

## 📊 Informations retournées

Pour chaque commande, vous recevez :
- **Informations client** : ID, nom, téléphone
- **Informations restaurant** : ID, nom
- **Détails de la commande** : montant total, statut, adresse de livraison
- **Articles commandés** : liste complète avec nom, quantité, prix unitaire et sous-total
- **Horodatage** : date de création et de dernière mise à jour

## 🎯 Cas d'utilisation

1. **Tableau de bord restaurant** : Afficher toutes les commandes en cours
2. **Gestion des commandes** : Filtrer par statut (ex: voir uniquement les commandes PENDING)
3. **Historique** : Consulter toutes les commandes passées avec pagination
4. **Détails de commande** : Voir tous les articles d'une commande spécifique
5. **Administration** : Les admins peuvent surveiller les commandes de tous les restaurants

## 🔄 Workflow typique

1. Un client passe une commande → statut `PENDING`
2. Le restaurant consulte ses nouvelles commandes via `/my-restaurant/orders/status/PENDING`
3. Le restaurant accepte la commande → statut `ACCEPTED`
4. Le restaurant prépare la commande → statut `PREPARING`
5. La commande est prête → statut `READY_FOR_PICKUP`
6. Un livreur prend la commande → statut `IN_DELIVERY`
7. La commande est livrée → statut `DELIVERED`

## ✅ Tests de compilation

Le projet a été compilé avec succès :
```
[INFO] BUILD SUCCESS
[INFO] Compiling 34 source files
```

Tous les nouveaux fichiers ont été intégrés sans erreur.
