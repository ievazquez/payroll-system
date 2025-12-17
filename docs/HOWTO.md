# Guía de Ejecución - PayrollSystem

## Modo 1: Ejecutar todo con Docker (Recomendado)

### Iniciar todos los servicios
```bash
docker-compose up --build
```

### Reiniciar solo la aplicación
```bash
docker-compose up --build -d payroll_app
```

### Ver logs
```bash
docker-compose logs -f payroll_app
```

### Detener todo
```bash
docker-compose down
# O eliminar volúmenes también
docker-compose down -v
```

---

## Modo 2: Ejecutar en consola (sin servidor web)

La aplicación está configurada con `spring.main.web-application-type=none` para ejecutarse en modo consola.

### Paso 1: Iniciar PostgreSQL y RabbitMQ
```bash
docker-compose up -d postgres_db rabbitmq_broker
```

### Paso 2: Ejecutar la aplicación en modo consola

**Opción A: Usar el script**
```bash
./run-console.sh
```

**Opción B: Manualmente con variables de entorno**
```bash
SPRING_DATASOURCE_USERNAME=payroll_user \
SPRING_DATASOURCE_PASSWORD=secret_password \
./mvnw spring-boot:run
```

### Credenciales importantes:
- **Docker PostgreSQL**: `payroll_user` / `secret_password`
- **Local PostgreSQL (por defecto)**: `postgres` / `postgres`

---

## Modo 3: Ejecutar en modo servidor web

### Paso 1: Comentar la línea en application.properties
```properties
# spring.main.web-application-type=none
```

### Paso 2: Iniciar servicios
```bash
docker-compose up -d postgres_db rabbitmq_broker
```

### Paso 3: Ejecutar aplicación
```bash
SPRING_DATASOURCE_USERNAME=payroll_user \
SPRING_DATASOURCE_PASSWORD=secret_password \
./mvnw spring-boot:run
```

### Paso 4: Probar endpoints
```bash
curl http://localhost:8080/api/payroll/periods
curl http://localhost:8080/api/employees
```



  ### 
  docker exec -it payroll_postgres psql -U payroll_user -d payroll_db -c "\dt"

  docker exec payroll_postgres psql -U payroll_user -d payroll_db -c "SELECT code, name, type FROM concepts LIMIT 5;"


  ##Ejecutar pruebas
  ./mvnw test -Dtest=DynamicFormulaTest



## 

curl -s http://localhost:8080/api/employees/Juan/concepts | jq .

docker-compose up --build -d payroll_app

docker cp target/payroll-system-0.0.1-SNAPSHOT.jar payroll_engine:/app/app.jar && docker restart payroll_engine


####
  Otros endpoints disponibles:
  - http://207.246.110.78:8080/api/employees - Lista todos los empleados
  - http://207.246.110.78:8080/api/employees/{id} - Detalles de un empleado específico
  - http://207.246.110.78:8080/api/employees/{name}/concepts - Conceptos aplicables a un empleado




curl http://207.246.110.78:8080/api/monitor/1/progress | jq .
curl -s http://localhost:8080/api/monitor/123/progress | python3 -m json.tool

curl -s "http://localhost:8080/api/employees?page=0&size=10" | python3 -m json.tool | head -40


  # Listar empleados (paginado)
  curl http://localhost:8080/api/employees?page=0&size=20

  # Buscar empleado por número
  curl http://localhost:8080/api/employees/number/EMP-000100

  # Buscar empleados por nombre
  curl "http://localhost:8080/api/employees/search?name=García"

  # Ver conceptos de un empleado
  curl http://localhost:8080/api/employees/Juan/concepts

  Archivo Generado

  El script está guardado en:
  src/main/resources/seed-5000-employees.sql

  Para regenerar los datos en Vultr, solo necesitas ejecutar:
  psql -h localhost -U payroll_user -d payroll_db -f seed-5000-employees.sql



## Probar

curl -X POST http://localhost:8080/api/payroll/periods \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nómina Enero 2025",
    "startDate": "2025-01-01",
    "endDate": "2025-01-31"
  }'


#trigger
curl -X POST http://localhost:8080/api/payroll/periods/1/calculate

curl -X GET http://localhost:8080/api/monitor/1/progress

#logs
 docker-compose logs --tail=100 payroll_app


##logs
 docker-compose logs payroll_app | grep "Job pushed"


 docker-compose logs payroll_app | grep "Exception"
 docker-compose logs payroll_app | grep "Worker"
 docker-compose logs payroll_app | grep "finalizado"

 docker exec payroll_postgres psql postgresql://payroll_user:secret_password@localhost/payroll_db -c "SELECT │
│ count(*) FROM payroll_results;"



  docker-compose exec -T postgres_db psql -U payroll_user -d payroll_db <<EOF
  -- Ver empleados duplicados en el periodo 2
  SELECT
      employee_id,
      COUNT(*) as veces_procesado,
      array_agg(id) as result_ids
  FROM payroll_results
  WHERE period_id = '2'
  GROUP BY employee_id
  HAVING COUNT(*) > 1
  ORDER BY COUNT(*) DESC
  LIMIT 20;

  -- Estadísticas generales
  SELECT
      COUNT(DISTINCT employee_id) as empleados_unicos,
      COUNT(*) as total_registros,
      COUNT(*) - COUNT(DISTINCT employee_id) as duplicados
  FROM payroll_results
  WHERE period_id = '2';
  EOF




   docker-compose exec -T postgres_db psql -U payroll_user -d payroll_db <<'EOF'
   -- Ver cuántos empleados únicos se procesaron vs esperados
   SELECT
       'Total esperado' as tipo,
       total_expected as cantidad
   FROM payroll_periods
   WHERE period_identifier = 'Nómina Enero 2025 - Q1'

   UNION ALL

   SELECT
       'Empleados únicos procesados' as tipo,
       COUNT(DISTINCT employee_id) as cantidad
   FROM payroll_results
   WHERE period_id = 'Nómina Enero 2025 - Q1'

   UNION ALL

   SELECT
       'Total registros creados' as tipo,
       COUNT(*) as cantidad
   FROM payroll_results
   WHERE period_id = 'Nómina Enero 2025 - Q1'

   UNION ALL

   SELECT
       'Empleados activos en BD' as tipo,
       COUNT(*) as cantidad
   FROM employees
   WHERE active = true;

   -- Ver si hay empleados procesados que no estén activos
   SELECT
       'Empleados inactivos procesados' as tipo,
       COUNT(DISTINCT pr.employee_id) as cantidad
   FROM payroll_results pr
   LEFT JOIN employees e ON pr.employee_id = e.id
   WHERE pr.period_id = 'Nómina Enero 2025 - Q1'
   AND (e.active = false OR e.id IS NULL);
   EOF


   docker-compose logs payroll_app | grep -E "(Iniciando Dispatch|Worker procesando lote|Lote.*finalizado)"


   docker-compose logs payroll_app 2>&1 | grep -A 2 "Missing variables" | head -15



   docker-compose logs payroll_app 2>&1 | grep "Missing variables.*TOTAL_EARNINGS" |

   docker-compose logs payroll_app 2>&1 | grep "Missing variables.*TOTAL_EARNINGS" |




##ejecutar
./mvnw spring-boot:run


docker-compose up -d postgres_db rabbitmq_broker



  Ahora el script debería ejecutarse sin errores. Para probar:

  # Ejecutar el seed desde Docker
  docker exec payroll_postgres psql -U payroll_user -d payroll_db -f /path/to/seed-5000-employees.sql

  # O si está en el contenedor:
  docker cp src/main/resources/seed-5000-employees.sql payroll_postgres:/tmp/
  docker exec payroll_postgres psql -U payroll_user -d payroll_db -f /tmp/seed-5000-employees.sql
