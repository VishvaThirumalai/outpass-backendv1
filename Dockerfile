# Use Java 17 base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy everything
COPY . .

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Build project using Maven wrapper
RUN ./mvnw clean package -DskipTests

# Expose application port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "target/outpass-management-1.0.0.jar"]
