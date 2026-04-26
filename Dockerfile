# Backend Dockerfile
FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project files
COPY ai_interview/pom.xml ./
COPY ai_interview/src ./src

# Download dependencies and build the application
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the Maven build stage
COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]