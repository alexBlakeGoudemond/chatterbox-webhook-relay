FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Create log directories
RUN mkdir -p /var/log/chatterbox-api/archived

# Copy the built jar into the container
COPY target/chatterbox-*.jar chatterbox.jar

# Expose the port Spring Boot runs on
EXPOSE 1234

# Run the app
ENTRYPOINT ["java", "-jar", "chatterbox.jar"]
