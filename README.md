### 📝 About project:

This is a backend app which was meant for notes service. Such features was implemented: 

**Auth**: You can be logged in via localauth or google OAuth2 provider.  
**Friendships**: You can send friendship request to users and look at each other's notes.  
**Notes**: You can save save notes and set its status: 

  - PUBLIC - each user can see this note.  
  - PROTECTED - only friends can see this note.  
  - PRIVATE - only you can see this note.

**User avatars**: You can change account's avatar and it will be saved in AWS s3 cloud storage


### 🛠 Tech stack:

- **Core**: Java, Spring framework, Spring Boot

- **Database & Persistence**: Spring Data JPA (Hybernate), PostgreSQL

- **Security**: Spring Security, jjwt, Spring Oauth2 client

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

    **Jwt local auth:**
    ```
    JWT_SECRET
    ISSUER
    JWT_EXPIRE_IN
    REFRESH_TOKEN_SECRET
    REFRESH_TOKEN_TTL
    DOMAIN
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
    ```

    **Oauth2 client:**
    ```
    GOOGLE_CLIENT_ID
    GOOGLE_CLIENT_SECRET
    ```

    3.Run required docker containers: ```  docker compose up  ```
    
    4.Enter to the comand line: ```  ./gradlew bootRun  ``` to start application
