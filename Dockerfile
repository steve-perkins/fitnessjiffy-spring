# =================================================================================
# Stage 1: The Builder
#
# This stage uses a Gradle image to build the application. It will download
# dependencies, compile the code, and then extract the application layers.
# The result is a set of directories that can be copied into the final image.
# =================================================================================
# UPDATED: Using the exact Gradle version to match the Gradle wrapper
FROM gradle:8.14.3-jdk21-alpine AS builder

# Set the working directory in the container
WORKDIR /workspace

# Copy the entire project context into the builder stage
# This includes build.gradle, settings.gradle, and the src directory
COPY . .

# Execute the Gradle build to create the layered JAR.
# --no-daemon is recommended for CI/CD and containerized environments.
RUN gradle bootJar --no-daemon

# Use Spring Boot's layertools mode to extract the application layers
# into distinct directories. This is the key to efficient Docker caching.
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# =================================================================================
# Stage 2: The Final Image
#
# This stage builds the final, lightweight production image. It starts from a
# minimal JRE base, creates a non-root user, and copies only the necessary
# application layers from the 'builder' stage.
# =================================================================================
FROM azul/zulu-openjdk-alpine:21-jre

# Set the working directory
WORKDIR /app

# Create a dedicated system group and user to run the application
# for enhanced security, instead of running as root.
RUN addgroup -S --gid 1001 spring && \
    adduser -S --uid 1001 -G spring spring

# Switch to the newly created non-root user
USER spring

# Copy the extracted layers from the 'builder' stage into the final image.
# The order is important for Docker's layer caching. Dependencies, which
# change infrequently, are copied first. Your application code, which changes
# frequently, is copied last.
COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

# UPDATED: Expose the port specified in application.properties
EXPOSE 8080

# The entrypoint command to start the application.
# This uses the Spring Boot loader, which knows how to launch the
# application from the layered structure.
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
