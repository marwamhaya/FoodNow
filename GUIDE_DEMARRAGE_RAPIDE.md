# 🚀 Guide de Démarrage Rapide - Consultation des Commandes

## ⚡ En 5 minutes

Ce guide vous permet de tester rapidement la nouvelle fonctionnalité de consultation des articles de commande.

---

## 📋 Prérequis

- ✅ Application FoodNow démarrée
- ✅ Base de données configurée
- ✅ Un compte restaurant créé
- ✅ Token JWT valide

---

## 🎯 Étape 1 : Démarrer l'application

```bash
cd c:\FoodNow\FoodNow\FoodNow
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`

---

## 🔑 Étape 2 : Obtenir un token JWT

### Option A : Via Postman
1. Ouvrez Postman
2. Créez une requête POST vers `/api/auth/login`
3. Body (JSON) :
```json
{
  "email": "restaurant@example.com",
  "password": "votre_mot_de_passe"
}
```
4. Copiez le token de la réponse

### Option B : Via cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "restaurant@example.com",
    "password": "votre_mot_de_passe"
  }'
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "role": "RESTAURANT"
}
```

💾 **Sauvegardez le token** pour les prochaines requêtes !

---

## 🧪 Étape 3 : Tester les endpoints

### Test 1 : Récupérer toutes vos commandes

```bash
curl -X GET "http://localhost:8080/api/restaurants/my-restaurant/orders" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Réponse attendue :**
```json
{
  "content": [
    {
      "id": 1,
      "clientName": "Jean Dupont",
      "totalAmount": 45.50,
      "status": "PENDING",
      "orderItems": [...]
    }
  ],
  "pageNo": 0,
  "pageSize": 10,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```

---

### Test 2 : Filtrer par statut PENDING

```bash
curl -X GET "http://localhost:8080/api/restaurants/my-restaurant/orders/status/PENDING" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

---

### Test 3 : ⭐ Récupérer uniquement les items d'une commande (NOUVEAU)

```bash
curl -X GET "http://localhost:8080/api/restaurants/orders/1/items" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Réponse attendue :**
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
  }
]
```

✅ **Succès !** Vous avez récupéré uniquement les items !

---

## 📦 Étape 4 : Importer la collection Postman

1. Ouvrez Postman
2. Cliquez sur **Import**
3. Sélectionnez le fichier : `FoodNow_Consultation_Commandes.postman_collection.json`
4. La collection est importée avec tous les endpoints prêts à l'emploi !

### Configuration de la collection :

1. Cliquez sur la collection
2. Allez dans **Variables**
3. Modifiez :
   - `baseUrl` : `http://localhost:8080/api/restaurants`
   - `token` : Collez votre JWT token
   - `orderId` : ID d'une commande existante
   - `restaurantId` : ID de votre restaurant

4. Testez tous les endpoints en un clic ! 🎉

---

## 💻 Étape 5 : Intégrer dans votre frontend

### Exemple JavaScript simple :

```javascript
// Configuration
const API_BASE_URL = 'http://localhost:8080/api/restaurants';
const token = localStorage.getItem('jwtToken');

// Fonction pour récupérer les items d'une commande
async function getOrderItems(orderId) {
  try {
    const response = await fetch(
      `${API_BASE_URL}/orders/${orderId}/items`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Erreur HTTP: ${response.status}`);
    }

    const items = await response.json();
    console.log('Articles de la commande:', items);
    
    // Afficher les items
    displayItems(items);
    
    return items;
  } catch (error) {
    console.error('Erreur:', error);
  }
}

// Fonction pour afficher les items
function displayItems(items) {
  const container = document.getElementById('order-items');
  
  const html = items.map(item => `
    <div class="item">
      <h4>${item.menuItemName}</h4>
      <p>Quantité: ${item.quantity}</p>
      <p>Prix unitaire: ${item.unitPrice} €</p>
      <p><strong>Sous-total: ${item.subtotal} €</strong></p>
    </div>
  `).join('');
  
  container.innerHTML = html;
}

// Utilisation
getOrderItems(1);
```

### HTML correspondant :

```html
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Articles de commande</title>
  <style>
    .item {
      border: 1px solid #ddd;
      padding: 15px;
      margin: 10px 0;
      border-radius: 5px;
    }
  </style>
</head>
<body>
  <h1>Articles de la commande</h1>
  <div id="order-items"></div>
  
  <script src="app.js"></script>
</body>
</html>
```

---

## 🎨 Étape 6 : Exemple avec React

```jsx
import React, { useState, useEffect } from 'react';

function OrderItems({ orderId }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`http://localhost:8080/api/restaurants/orders/${orderId}/items`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    .then(res => res.json())
    .then(data => {
      setItems(data);
      setLoading(false);
    })
    .catch(err => console.error(err));
  }, [orderId]);

  if (loading) return <p>Chargement...</p>;

  return (
    <div>
      <h2>Articles de la commande #{orderId}</h2>
      {items.map(item => (
        <div key={item.id} className="item-card">
          <h3>{item.menuItemName}</h3>
          <p>Quantité: {item.quantity}</p>
          <p>Prix: {item.unitPrice} €</p>
          <p><strong>Total: {item.subtotal} €</strong></p>
        </div>
      ))}
    </div>
  );
}

export default OrderItems;
```

---

## 🔍 Vérification rapide

### ✅ Checklist de test

- [ ] L'application démarre sans erreur
- [ ] Je peux me connecter et obtenir un token
- [ ] Je peux récupérer mes commandes
- [ ] Je peux filtrer par statut
- [ ] Je peux récupérer les items d'une commande
- [ ] Les réponses sont au format JSON correct
- [ ] La sécurité fonctionne (401 sans token)

---

## 🐛 Résolution des problèmes courants

### Problème 1 : Erreur 401 Unauthorized
**Solution :** Vérifiez que votre token JWT est valide et correctement formaté dans le header `Authorization: Bearer TOKEN`

### Problème 2 : Erreur 403 Forbidden
**Solution :** Vérifiez que vous avez le rôle RESTAURANT ou ADMIN

### Problème 3 : Erreur 404 Not Found
**Solution :** Vérifiez que l'ID de la commande existe et appartient à votre restaurant

### Problème 4 : Réponse vide []
**Solution :** Il n'y a pas de commandes pour votre restaurant. Créez-en une d'abord !

---

## 📚 Documentation complète

Pour plus de détails, consultez :

- 📘 [Documentation principale](./CONSULTATION_COMMANDES_README.md)
- ⭐ [Nouvelle fonctionnalité](./NOUVELLE_FONCTIONNALITE_ORDER_ITEMS.md)
- 💻 [Exemples de code](./EXEMPLES_ORDER_ITEMS.md)
- 🏗️ [Architecture](./ARCHITECTURE_CONSULTATION_COMMANDES.md)
- 📚 [Index](./INDEX_DOCUMENTATION.md)

---

## 🎯 Prochaines étapes

1. ✅ Tester tous les endpoints
2. ✅ Intégrer dans votre frontend
3. ✅ Créer une interface de gestion des commandes
4. ✅ Ajouter des notifications en temps réel
5. ✅ Implémenter la mise à jour du statut

---

## 💡 Astuce Pro

Utilisez les **variables d'environnement** dans Postman pour basculer facilement entre :
- Développement (`localhost:8080`)
- Production (`votre-domaine.com`)

---

**Bon développement ! 🚀**

Si vous avez des questions, consultez la documentation complète ou les exemples de code.
