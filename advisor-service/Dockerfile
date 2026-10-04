# Build stage
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /workspace
COPY . .

# Make wrapper executable
RUN chmod +x gradlew

# Build jar
RUN ./gradlew clean bootJar --no-daemon

#RUN ./gradlew flywayClean flywayMigrate --no-daemon

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]