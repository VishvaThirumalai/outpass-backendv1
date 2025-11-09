# Use stable Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy everything
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose the app port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "target/outpass-management-1.0.0.jar"]
