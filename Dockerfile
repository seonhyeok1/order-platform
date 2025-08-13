# Stage 1: Build the application
FROM gradle:jdk17 AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle src ./src
# Gradle Chmod
RUN chmod +x gradlew
RUN ./gradlew build -x test

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /home/gradle/src/build/libs/order-platform-backend-0.0.1-SNAPSHOT.jar /app/application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]

