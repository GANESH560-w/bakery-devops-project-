FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the source and libraries
COPY src/ /app/src/
COPY lib/ /app/lib/

# Compile the application
RUN mkdir -p /app/out
RUN javac -cp "/app/lib/*" -d /app/out /app/src/*.java

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-cp", "/app/out:/app/lib/*", "BackendServer"]
