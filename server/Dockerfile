# ── server/Dockerfile ──
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy Maven wrapper & pom first (layer caching for dependencies)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source
COPY src src

# Run with spring-boot:run so devtools works
CMD ["./mvnw", "spring-boot:run"]
