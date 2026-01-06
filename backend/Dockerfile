FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Maven wrapper and config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

# Download dependencies only once (cache)
RUN ./mvnw dependency:go-offline

# We DO NOT copy src here (important)
EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
