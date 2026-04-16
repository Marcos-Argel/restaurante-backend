# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copiamos el pom y el código fuente
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto saltando los tests para acelerar el deploy
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiamos solo el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar restaurante-backend.jar

EXPOSE 8080

# Optimizamos el inicio de Java para entornos de nube
ENTRYPOINT ["java", "-Xmx512m", "-jar", "restaurante-backend.jar"]