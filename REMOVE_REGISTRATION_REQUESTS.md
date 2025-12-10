# Removal of Online Registration Requests

## Code Removed
The following components have been removed to eliminate the online "waiting list" style registration for Restaurants and Livreurs:
- `com.example.foodNow.controller.RegistrationRequestController`
- `com.example.foodNow.service.RegistrationRequestService`
- `com.example.foodNow.repository.RegistrationRequestRepository`
- `com.example.foodNow.model.RegistrationRequest`

## Code Modified
- **`com.example.foodNow.service.AuthService`**: The `register` method is now strictly limited to creating `CLIENT` accounts. Any attempt to register as RESTAURANT or LIVREUR via the public endpoint will result in an error.
- **`com.example.foodNow.service.RestaurantService`**: Added logging to the `createRestaurant` method to confirm that credentials are effectively "sent" (simulated) when an Admin creates a restaurant.
- **`com.example.foodNow.service.LivreurService`**: Added logging to the `createLivreur` method to confirm that credentials are effectively "sent" (simulated) when an Admin creates a livreur.

## New Admin Workflow
1. **Restaurants**: Admin uses the existing `POST /api/restaurants` endpoint. The system creates the user, assigns the RESTAURANT role, creates the Restaurant profile, and logs the credential "email".
2. **Livreurs**: Admin uses the existing `POST /api/livreurs` endpoint. The system creates the user, assigns the LIVREUR role, creates the Livreur profile, and logs the credential "email".
3. **Clients**: Clients continue to use `POST /api/auth/register` to create their own accounts immediately.

## Migrations
No database migrations were altered. If the `registration_requests` table was created by `ddl-auto=update`, it may remain in the database but will no longer be used. This is safe and preserves data integrity.
