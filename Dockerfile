FROM gradle:jdk-21-and-23-jammy AS build

WORKDIR /home/gradle/project

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew clean build --no-daemon

FROM openjdk:23

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]