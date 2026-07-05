# Build stage
FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B || true
# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install Python, pip, curl, and required packages
RUN apt-get update && \
    apt-get install -y python3 python3-pip curl && \
    rm -rf /var/lib/apt/lists/*

# Install keyring and keyrings.alt
RUN pip3 install --no-cache-dir keyring keyrings.alt --break-system-packages || pip3 install --no-cache-dir keyring keyrings.alt

# Download and install agy CLI for Linux
RUN curl -fsSL https://antigravity.google/cli/install.sh | bash && \
    ln -s /root/.local/bin/agy /usr/local/bin/agy

# Copy the built jar from the builder stage
COPY --from=builder /app/target/antigravity-overlay-backend-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8085

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
