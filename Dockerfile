# Use OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY . .

# Package the application
RUN ./mvnw clean package -DskipTests

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","target/*.jar"]
