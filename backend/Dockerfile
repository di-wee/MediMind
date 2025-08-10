# Use the official OpenJDK 21 runtime as the base image
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables with defaults (these will be overridden by docker-compose)
ENV DB_USERNAME=admin \
    DB_PASSWORD=medimind2025 \
    DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=medimind \
    APP_ENV=production \
    APP_PORT=8080 \
    CORS_ALLOWED_ORIGIN=http://localhost:5173 \
    SERVER_ADDRESS=0.0.0.0 \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Create a non-root user to run the application
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to the non-root user
USER appuser

# Run the application with optimized JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

