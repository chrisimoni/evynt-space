# Use the OpenJDK 21 JDK Slim image
FROM openjdk:21-jdk-slim
# Set working directory inside the container
WORKDIR /app
# Copy the compiled Java application JAR file into the container and rename it
COPY ./target/*.jar /app/app.jar
# Expose the port the Spring Boot application will run on
EXPOSE 8080
# Specify the entrypoint command
ENTRYPOINT ["java", "-jar", "/app/app.jar"]