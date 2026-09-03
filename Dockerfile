FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradle.lockfile ./
COPY gradle/ ./gradle/

RUN gradle dependencies --no-daemon

COPY src/ ./src/

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]