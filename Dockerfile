# Use an official Gradle image from the Docker Hub
FROM --platform=linux/amd64 gradle:jdk17-jammy AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy the Gradle configuration files first for leveraging Docker cache
# and avoid the downloading of dependencies on each build
COPY build.gradle settings.gradle gradlew ./

# Copy the gradle wrapper JAR and properties files
COPY gradle ./gradle

# Copy the source code
COPY ./core ./core

# Now run gradle assemble to download dependencies and build the application
RUN chmod +x ./gradlew
RUN ./gradlew build

# Use a JDK base image for running the gradle task
FROM amazoncorretto:17-al2-native-headless
WORKDIR /app

# Copy the build output from the builder stage
COPY --from=build /home/gradle/project/build /app/build

# Copy the source code
COPY --from=build /home/gradle/project/core /app/core

# Copy the gradlew and settings files
COPY --from=build /home/gradle/project/gradlew /home/gradle/project/settings.gradle /home/gradle/project/build.gradle /app/
COPY --from=build /home/gradle/project/gradle /app/gradle

# Run the specified task
CMD ["sh", "-c", "./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker"]
