# Use an official Maven image as the base image
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and the project files to the container
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/BookBliss-0.0.1-SNAPSHOT.jar .

#Expose Port 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar","/app/BookBliss-0.0.1-SNAPSHOT.jar"]