#!/bin/bash
# Script para ejecutar la aplicación en modo consola con las credenciales correctas

echo "🚀 Iniciando aplicación en modo consola..."
echo "📋 Usando credenciales de Docker: payroll_user/secret_password"
echo ""

# Variables de entorno para conectarse a PostgreSQL y RabbitMQ en Docker
export SPRING_DATASOURCE_USERNAME=payroll_user
export SPRING_DATASOURCE_PASSWORD=secret_password
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payroll_db
export SPRING_RABBITMQ_HOST=localhost
export SPRING_RABBITMQ_PORT=5672

# Ejecutar la aplicación
./mvnw spring-boot:run
