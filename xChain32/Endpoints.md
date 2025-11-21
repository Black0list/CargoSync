# Endpoints sécurisés — Catalogue

Basé sur la configuration de sécurité définie dans [`com.spring.logitrack.config.SecurityConfig`](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java).

- /api/register
  - Méthodes : POST
  - Accès : PUBLIC (permitAll)
  - Usage : créer un nouvel utilisateur (nom d'utilisateur, mot de passe, rôles)
  - cURL :
    ```
    curl -X POST http://localhost:8080/api/register \
      -H "Content-Type: application/json" \
      -d '{"username":"alice","password":"pwd","roles":["USER"]}'
    ```

- /api/login
  - Méthodes : POST
  - Accès : PUBLIC (utile si vous implémentez un endpoint de vérif., sinon Basic Auth s’appuie directement sur l'en-tête Authorization)
  - Remarque : avec Basic Auth, l'en-tête Authorization est envoyé à chaque requête; `login` peut rester un endpoint d'aide.

- /api/products/**
  - Méthodes : GET → PUBLIC
  - Méthodes : POST, PUT, PATCH, DELETE → ROLE_ADMIN
  - Exemples :
    - GET public :
      ```
      curl http://localhost:8080/api/products
      ```
    - POST admin :
      ```
      curl -X POST -u admin:adminpwd http://localhost:8080/api/products \
        -H "Content-Type: application/json" \
        -d '{"name":"Item","price":9.99}'
      ```

- /api/users/**
  - Accès : ROLE_ADMIN
  - Exemple :
    ```
    curl -u admin:adminpwd http://localhost:8080/api/users
    ```

- /api/warehouses/**
  - Accès : ROLE_WAREHOUSE_MANAGER, ROLE_ADMIN
  - Exemple :
    ```
    curl -u manager:managerpwd http://localhost:8080/api/warehouses
    ```

Remarque : adaptez les noms d’utilisateur/mots de passe aux enregistrements présents dans votre base (seed SQL ou repository).

Fichier source de la configuration des routes : [xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java](xChain32/src/main/java/com/spring/logitrack/config/SecurityConfig.java)