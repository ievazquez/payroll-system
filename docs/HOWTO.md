  Configuración creada:
  - PostgreSQL: localhost:5432/payroll_db
  - RabbitMQ: localhost:5672
  - Puerto servidor: 8080

  Próximos pasos:

  1. Iniciar PostgreSQL:
  docker run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=payroll_db -p 5432:5432 -d postgres

  2. Iniciar RabbitMQ:
  docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:management

  3. Ejecutar la aplicación:
  cd /home/docker/containers/payroll-system
  ./mvnw spring-boot:run


###
 🚀 Para probar:

  Con Docker:
  docker-compose down -v  # Elimina la BD anterior
  docker-compose up --build

  Local:
  # Asegúrate que PostgreSQL esté corriendo
  ./mvnw spring-boot:run



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

#logs
 docker-compose logs --tail=100 payroll_app


##logs
 docker-compose logs payroll_app | grep "Job pushed"


 docker-compose logs payroll_app | grep "Exception"
 docker-compose logs payroll_app | grep "Worker"
 docker-compose logs payroll_app | grep "finalizado"

 docker exec payroll_postgres psql postgresql://payroll_user:secret_password@localhost/payroll_db -c "SELECT │
│ count(*) FROM payroll_results;"
