# Use the official Maven image as the base image
FROM maven:3.9.2 AS maven

# Copy the application source code to the container
COPY . /api

# Set the working directory in the container
WORKDIR /api

# Download the dependencies specified in the Maven project file
RUN mvn clean install

# Use the official JDK 8 image as the base image
FROM openjdk:19-jdk-alpine

ARG JAR_FILE=embapi-0.0.1-SNAPSHOT.jar

WORKDIR /api

# Copy the built JAR file from the previous stage
COPY --from=maven api/target/${JAR_FILE} /api

# Set the startup command to run the Spring Boot application
CMD ["java", "-jar", "embapi-0.0.1-SNAPSHOT.jar"]