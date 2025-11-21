# Authentification Basic Auth — Guide d'utilisation

## Objectif
Ce document décrit le fonctionnement de l'authentification Basic Auth implémentée dans le projet, les composants Spring Security impliqués, et les étapes pour tester l'API via Postman ou cURL.

## Composants clés
- Bean PasswordEncoder : défini dans [`com.spring.logitrack.config.SecurityConfig`](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java) via la méthode `passwordEncoder`.
- SecurityFilterChain : défini dans [`com.spring.logitrack.config.SecurityConfig.securityFilterChain`](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java) — c’est la chaîne de filtres qui applique les règles d’accès, le CSRF, et la gestion de session.
- Méthodes sécurisées : annotation `@EnableMethodSecurity` active la sécurité au niveau des méthodes (ex. `@PreAuthorize`).

## Principe Basic Auth (rappel)
- Chaque requête HTTP contient l’en-tête `Authorization: Basic <base64(username:password)>`.
- Le serveur décode cet en-tête, vérifie les credentials et utilise le `UserDetailsService` / repository pour charger l’utilisateur et ses rôles.
- Aucun token n’est stocké côté serveur (stateless) si la configuration utilise `SessionCreationPolicy.STATELESS`.

## Tester avec cURL
- Requête GET publique (produits) :
  ```
  curl -v http://localhost:8080/api/products
  ```
- Requête GET protégée avec Basic Auth :
  ```
  curl -v -u alice:password123 http://localhost:8080/api/users
  ```
- Exemple d'en-tête explicite :
  ```
  curl -v -H "Authorization: Basic $(echo -n 'alice:password123' | base64)" http://localhost:8080/api/secure-route
  ```

## Tester avec Postman
1. Créer une nouvelle requête.
2. Onglet "Authorization" → Type = "Basic Auth".
3. Saisir Username / Password.
4. Envoyer la requête vers l’endpoint voulu.

## Références rapides
- Fichier de configuration principal : [xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java)
- README projet : [xChain32/README.md](xChain32/README.md)
- Documentation API : [xChain32/API_DOCUMENTATION.md](xChain32/API_DOCUMENTATION.md)