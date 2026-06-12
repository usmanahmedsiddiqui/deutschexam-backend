FROM gradle:8.11-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
COPY --from=builder /app/src/main/resources/db/migration /app/db/migration
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
