# ===== Build Maven =====
FROM eclipse-temurin:21-jdk-jammy
ARG JAR_FILE=target/restaurante-backend-1.0.0.jar
COPY ${JAR_FILE} restaurante-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "restaurante-backend.jar"]