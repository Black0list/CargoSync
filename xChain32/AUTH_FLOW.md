# Flux d'authentification Basic Auth — Chaîne de traitement

Ce document décrit le traitement d'une requête authentifiée Basic Auth dans la Security Filter Chain.

1. Requête HTTP entrante
   - Le client inclut l’en-tête :
     ```
     Authorization: Basic <base64(username:password)>
     ```

2. Security Filter Chain (extrait)
   - La chaîne est configurée dans [`com.spring.logitrack.config.SecurityConfig.securityFilterChain`](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java).
   - Principaux filtres impliqués (ordre conceptuel) :
     - ChannelProcessingFilter
     - SecurityContextPersistenceFilter
     - CorsFilter (si activé)
     - BasicAuthenticationFilter (ou un équivalent custom) : décode `Authorization: Basic`, crée un `Authentication` token.
     - UsernamePasswordAuthenticationFilter (si utilisé pour form login)
     - ExceptionTranslationFilter
     - FilterSecurityInterceptor : décide l’accès aux ressources en fonction des rôles.

3. Validation des credentials
   - BasicAuthenticationFilter transmet le token au `AuthenticationManager`.
   - L’`AuthenticationManager` utilise un `UserDetailsService` (ou repository) pour charger l’utilisateur.
   - Le `PasswordEncoder` (BCrypt) compare le mot de passe encodé.

4. Autorisation
   - Si l’authentification réussit, le `SecurityContext` est peuplé avec un `Authentication` contenant les `GrantedAuthority`.
   - Le filtre d’autorisation vérifie les règles définies dans `securityFilterChain` : `permitAll`, `hasRole`, `hasAnyRole`.

5. Réponse
   - Si autorisé → exécution du contrôleur.
   - Si refusé → 401 Unauthorized (auth) ou 403 Forbidden (auth ok mais pas de rôle).

Schéma (texte)
Client -> Serveur
  |- Requête (Authorization: Basic ...)
  -> BasicAuthenticationFilter -> AuthenticationManager -> UserDetailsService -> PasswordEncoder (BCrypt)
  -> SecurityContext (authentifié) -> FilterSecurityInterceptor -> Contrôleur

Fichier clé : [xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java)