#!/bin/bash

curl http://localhost:8080/api/payroll/periods

curl -X POST http://localhost:8080/api/payroll/periods \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nómina Enero 2025",
    "startDate": "2025-01-01",
    "endDate": "2025-01-31"
  }'

curl http://localhost:8080/api/payroll/periods | jq .

#trigger
curl -X POST http://localhost:8080/api/payroll/periods/1/calculate

curl http://207.246.110.78:8080/api/monitor/1/progress| jq .
