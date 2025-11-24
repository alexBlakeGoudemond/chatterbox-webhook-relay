# Use Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar into the container
COPY target/chatterbox-*.jar chatterbox.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "chatterbox.jar"]
