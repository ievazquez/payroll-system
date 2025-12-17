# --- Etapa 1: Build (Compilación) ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
# Copiamos solo el pom.xml primero para aprovechar el caché de dependencias de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
# Compilamos sin tests (los tests requieren BD que aún no está disponible durante build)
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# --- Etapa 2: Run (Ejecución) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el JAR generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
