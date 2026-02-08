FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar into the container
COPY target/chatterbox-*.jar chatterbox.jar
COPY src/main/resources/application-prod.yml app/configuration/application-prod.yml

# Expose the port Spring Boot runs on
EXPOSE 1234

# Run the app
ENTRYPOINT ["java", "-Dspring.config.location=app/configuration/application-prod.yml", "-jar", "chatterbox.jar"]
