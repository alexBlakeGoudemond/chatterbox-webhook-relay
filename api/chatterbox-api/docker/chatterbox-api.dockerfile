FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/chatterbox-*.jar chatterbox.jar

COPY src/main/resources/application.yml configuration/application.yml
COPY src/main/resources/application-prod.yml configuration/application-prod.yml

# Expose the port Spring Boot runs on
EXPOSE 1234

# Run the app with absolute config path and prod profile
ENTRYPOINT ["java", "-Dspring.config.location=/app/configuration/application-prod.yml", "-Dspring.profiles.active=prod", "-jar", "chatterbox.jar"]
