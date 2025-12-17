# PayrollSystem - Sistema de Nómina Mexicano

Sistema de nómina para México construido con Spring Boot 3.4.1, Java 17, PostgreSQL y RabbitMQ.

## Inicio Rápido

### Iniciar el sistema

```bash
docker-compose up --build
```

### Reiniciar Base de Datos

El script `reset-db.sh` ofrece dos opciones:

```bash
./reset-db.sh
```

**Opción 1: Reset Completo**
- Elimina TODOS los datos (empleados, conceptos, resultados)
- Recarga desde `schema.sql` y `data.sql`
- Útil para empezar desde cero con datos de prueba

**Opción 2: Limpiar Solo Resultados**
- Elimina: `payroll_results`, `payroll_result_details`, `payroll_periods`
- Mantiene: empleados, conceptos, fórmulas, valores de empleados
- Útil para reprocesar nómina sin perder empleados reales

## Servicios

- **Aplicación**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **PostgreSQL**: localhost:5432/payroll_db

## Endpoints Principales

```bash
# Crear periodo de nómina
POST http://localhost:8080/api/payroll/periods
Content-Type: application/json
{
  "name": "PERIOD-2024-12",
  "startDate": "2024-12-01",
  "endDate": "2024-12-31"
}

# Crear empleado
POST http://localhost:8080/api/employees
Content-Type: application/json
{
  "employeeNumber": "EMP-001",
  "fullName": "Juan Pérez",
  "hireDate": "2024-01-15",
  "active": true
}

# Ver progreso de procesamiento
GET http://localhost:8080/api/monitor/progress/{periodId}
```

## Comandos Útiles

```bash
# Ver logs en tiempo real
docker-compose logs -f payroll_app

# Detener servicios
docker-compose down

# Reiniciar solo la aplicación
docker-compose restart payroll_app

# Acceder a PostgreSQL
docker-compose exec postgres_db psql -U payroll_user -d payroll_db

# Ver colas de RabbitMQ
docker-compose exec rabbitmq_broker rabbitmqctl list_queues
```

## Desarrollo

Ver [CLAUDE.md](CLAUDE.md) para guía completa de desarrollo y arquitectura.

Ver [docs/HOWTO.md](docs/HOWTO.md) para instrucciones detalladas.

## Conceptos de Nómina Mexicana

El sistema maneja:
- **ISR**: Impuesto Sobre la Renta (con tablas 2024)
- **IMSS**: Seguro Social
- **Aguinaldo**: Mínimo 15 días
- **Prima Vacacional**: 25% sobre días de vacaciones
- **UMA**: Unidad de Medida y Actualización
- **Indicadores Económicos**: Valores históricos

## Arquitectura

- **Domain-Driven Design (DDD)**
- **Motor de fórmulas SpEL** para cálculos dinámicos
- **Procesamiento asíncrono** con RabbitMQ
- **Paginación por lotes** (100 empleados/lote)
- **Versionamiento de fórmulas** con fechas efectivas

## Licencia

Proyecto educativo - Sistema de Nómina para México
