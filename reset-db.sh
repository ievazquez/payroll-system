#!/bin/bash

# Script para reiniciar la base de datos de nómina
# Opciones: Reset completo o solo limpiar resultados de procesamiento

set -e  # Salir si hay algún error

echo "=========================================="
echo "  Script de Reinicio de Base de Datos"
echo "=========================================="
echo ""
echo "Selecciona una opción:"
echo ""
echo "  1) Reset Completo (elimina TODO y recarga desde schema.sql + data.sql)"
echo "     - Elimina: empleados, conceptos, fórmulas, resultados"
echo "     - Útil para: empezar desde cero con datos de prueba"
echo ""
echo "  2) Limpiar Solo Resultados de Nómina"
echo "     - Elimina: payroll_results, payroll_result_details, payroll_periods"
echo "     - Mantiene: empleados, conceptos, fórmulas, valores de empleados"
echo "     - Útil para: reprocesar nómina sin perder empleados reales"
echo ""
read -p "Ingresa tu opción (1 o 2): " option

case $option in
    1)
        echo ""
        echo "⚠️  ADVERTENCIA: Esto eliminará TODOS los datos y recargará desde SQL"
        read -p "¿Estás seguro? (si/no): " confirm

        if [ "$confirm" != "si" ]; then
            echo "❌ Operación cancelada"
            exit 0
        fi

        echo ""
        echo "=========================================="
        echo "  Ejecutando Reset Completo"
        echo "=========================================="

        echo ""
        echo "1. Deteniendo contenedores..."
        docker-compose down

        echo ""
        echo "2. Eliminando volúmenes de PostgreSQL..."
        docker-compose down -v

        echo ""
        echo "3. Iniciando PostgreSQL..."
        docker-compose up -d postgres_db

        echo ""
        echo "4. Esperando a que PostgreSQL esté listo..."
        sleep 10

        until docker-compose exec -T postgres_db pg_isready -U payroll_user -d payroll_db &> /dev/null; do
            echo "   Esperando PostgreSQL..."
            sleep 2
        done

        echo "   ✅ PostgreSQL listo"

        echo ""
        echo "5. Iniciando RabbitMQ..."
        docker-compose up -d rabbitmq_broker
        sleep 5

        echo ""
        echo "6. Reconstruyendo y iniciando aplicación..."
        docker-compose up -d --build payroll_app

        echo ""
        echo "7. Esperando a que la aplicación inicie..."
        sleep 10

        echo ""
        echo "=========================================="
        echo "  ✅ Reset Completo Exitoso"
        echo "=========================================="
        echo ""
        echo "La base de datos se ha recreado con:"
        echo "  - Schema (tablas vacías)"
        echo "  - Data (catálogos y datos de prueba)"
        ;;

    2)
        echo ""
        echo "=========================================="
        echo "  Limpiando Solo Resultados de Nómina"
        echo "=========================================="

        # Verificar que PostgreSQL esté corriendo
        if ! docker-compose ps postgres_db | grep -q "Up"; then
            echo "❌ Error: PostgreSQL no está corriendo"
            echo "Ejecuta: docker-compose up -d postgres_db"
            exit 1
        fi

        echo ""
        echo "Ejecutando limpieza de tablas..."

        # Script SQL para limpiar solo las tablas de resultados
        docker-compose exec -T postgres_db psql -U payroll_user -d payroll_db <<EOF
-- Limpiar resultados de nómina (mantiene empleados y catálogos)
TRUNCATE TABLE payroll_result_details CASCADE;
TRUNCATE TABLE payroll_results CASCADE;
TRUNCATE TABLE payroll_periods CASCADE;

-- Mostrar conteo de registros eliminados
SELECT 'Tablas limpiadas exitosamente' AS status;

-- Mostrar qué datos se mantuvieron
SELECT
    (SELECT COUNT(*) FROM employees) AS empleados_mantenidos,
    (SELECT COUNT(*) FROM concepts) AS conceptos_mantenidos,
    (SELECT COUNT(*) FROM concept_formulas) AS formulas_mantenidas,
    (SELECT COUNT(*) FROM employee_concept_values) AS valores_empleado_mantenidos,
    (SELECT COUNT(*) FROM payroll_results) AS resultados_eliminados,
    (SELECT COUNT(*) FROM payroll_periods) AS periodos_eliminados;
EOF

        echo ""
        echo "=========================================="
        echo "  ✅ Limpieza Exitosa"
        echo "=========================================="
        echo ""
        echo "Tablas limpiadas:"
        echo "  - payroll_results"
        echo "  - payroll_result_details"
        echo "  - payroll_periods"
        echo ""
        echo "Tablas mantenidas (sin cambios):"
        echo "  - employees"
        echo "  - concepts"
        echo "  - concept_formulas"
        echo "  - employee_concept_values"
        echo "  - economic_indicators"
        echo ""
        echo "Puedes procesar nómina nuevamente desde la aplicación."
        ;;

    *)
        echo ""
        echo "❌ Opción inválida. Usa 1 o 2"
        exit 1
        ;;
esac

echo ""
echo "Servicios disponibles:"
echo "  - Aplicación:     http://localhost:8080"
echo "  - RabbitMQ UI:    http://localhost:15672 (guest/guest)"
echo "  - PostgreSQL:     localhost:5432/payroll_db"
echo ""
echo "Comandos útiles:"
echo "  Ver logs:         docker-compose logs -f payroll_app"
echo "  Detener todo:     docker-compose down"
echo "  Reiniciar app:    docker-compose restart payroll_app"
echo ""
