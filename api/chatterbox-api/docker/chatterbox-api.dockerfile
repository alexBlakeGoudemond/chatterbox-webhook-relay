FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# The base application.yml and other resources (like banner.txt) are already bundled inside the JAR
COPY target/chatterbox-*.jar chatterbox.jar

# Expose the port Spring Boot runs on
EXPOSE 1234

# Run the app with additional config path and prod profile
ENTRYPOINT ["java", "-Dspring.config.additional-location=file:/app/configuration/", "-Dspring.profiles.active=prod", "-jar", "chatterbox.jar"]
