### 📝 About project:

This is a backend app which was meant for notes service. Such features was implemented: 

**Auth**: You can be logged in via localauth or google OAuth2 provider.  
**Friendships**: You can send friendship request to users and look at each other's notes.  
**Notes**: You can save save notes and set its status: 

  - PUBLIC - each user can see this note.  
  - PROTECTED - only friends can see this note.  
  - PRIVATE - only you can see this note.

**User avatars**: You can change account's avatar and it will be saved in AWS s3 cloud storage.  
**Real-Time communiction**: Each frienship requesting or accepting request corresponding user will receive notification about this actions. It was implemented via Websockets (STOMP)


### 🛠 Tech stack:

- **Core**: Java, Spring framework, Spring Boot

- **Database & Persistence**: Spring Data JPA (Hybernate), PostgreSQL, LiquidBase

- **Security**: Spring Security, Spring Resource Server, Spring Oauth2 client

- **Cloud & Storage**: AWS s3 (to store user avatars)

- **Testing**: JUnit, Mockito, Spring Test, Jacoco(coverage reports)

- **Real-Time Communication**: STOMP over WebSocket 

- **Other**: Docker, Gradle (Groovy)   



### 🚀 To start this application:

 1. Copy this repo to your local machine

 2. Specify this env variables:

    **DB:**
    ```
    DB_PASSWORD,
    PG_ADMIN_PASSWORD
    ```

    **Local auth:**
    ```
    JWT_SECRET
    JWT_ISSUER
    JWT_EXPIRE_TIME
    REFRESH_TOKEN_SECRET
    REFRESH_TOKEN_TTL
    JWT_HEADER_ALG
    NIMBUS_ALG
    ```

    **Email sender:**
    ```
    EMAIL_SENDER_PASSWORD
    EMAIL_SENDER_USERNAME
    ```

    **Aws s3:**
    ```
    AWS_ACCESS_KEY
    AWS_SECRET_ACCESS_KEY
    AWS_REGION
    AWS_AVATAR_BUCKET_NAME
    ```

    **Oauth2 client:**
    ```
    GOOGLE_CLIENT_ID
    GOOGLE_CLIENT_SECRET
    ```

    3.Run required docker containers: ```  docker compose up  ```
    
    4.Enter to the comand line: ```  ./gradlew bootRun  ``` to start application
