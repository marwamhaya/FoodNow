# Email Notification Setup and Testing Guide

This guide explains how to configure the SMTP server and how to test the email sending functionality when creating Restaurants or Livreurs.

## 1. SMTP Configuration

To send real emails, you need an SMTP server. For development/testing, we recommend **Mailtrap** (https://mailtrap.io). It captures emails sent from your local machine so you don't spam real people.

### Configure `application.properties`

Open `src/main/resources/application.properties` and configure the following settings.
**Note**: The project currently contains placeholders. You MUST replace them with valid credentials.

```properties
# ===================================
# Configuration Email (SMTP)
# ===================================
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 2. API Testing Guide

Since you removed the public registration requests, **only Admins** can create Restaurants and Livreurs. This action triggers the email.

### Prerequisites
- Ensure you have an ADMIN token.
- Ensure the server is running: `./mvnw spring-boot:run`

### Test Case A: Admin Creates a Restaurant (Triggers Email)

**Endpoint:** `POST http://localhost:8080/api/restaurants`
**Auth:** Bearer Token (Admin)

**Body:**
```json
{
  "name": "Pizza Palace",
  "address": "123 Flavor St",
  "description": "Best pizza in town",
  "phone": "0123456789",
  "ownerFullName": "Mario Owner",
  "ownerEmail": "mario@pizzapalace.com",
  "ownerPassword": "securePassword123",
  "ownerPhoneNumber": "0600000002"
}
```

**Verification:**
1. Check the server logs. You should see a log entry confirming the email attempt.
2. Check your SMTP inbox (e.g., Mailtrap). You should receive an email with the subject "Bienvenue sur FoodNow".

### Test Case B: Admin Creates a Livreur (Triggers Email)

**Endpoint:** `POST http://localhost:8080/api/livreurs`
**Auth:** Bearer Token (Admin)

**Body:**
```json
{
  "userFullName": "Fast Driver",
  "userEmail": "driver@test.com",
  "userPassword": "driverPassword123",
  "userPhoneNumber": "0700000003",
  "vehicleType": "BIKE"
}
```

**Verification:**
1. Check server logs.
2. Check SMTP inbox for "Bienvenue sur FoodNow" email.

## 3. Current Implementation Details (What I Did)

- **Added Dependency**: `spring-boot-starter-mail` to `pom.xml`.
- **Created Service**: `EmailService.java` to handle SMTP logic.
- **Modified Services**: `RestaurantService` and `LivreurService` now inject `EmailService` and call `sendSimpleMessage` upon successful account creation.
