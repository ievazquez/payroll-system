# Análisis: Problema TOTAL_EARNINGS

## 🔍 Problema Detectado

### Log Original:
```
-> Aplicando reglas: P002, P003, P004, D001, D002, D004
-> Contexto de Cálculo para empleado 3: {D003=500.0000, P001=3000.0000, UMA=108.5700}
-> HIRE_DATE: 2020-03-15
⚠️  Employee 3 - Missing variables (will use ZERO): [TOTAL_EARNINGS] in formula: #calcularISR(#TOTAL_EARNINGS)
   ✅ Percepciones: $2300.00
   ✅ Deducciones: $231.00
   ✅ Neto: $2069.00
```

### Síntomas:
1. **Warning**: Variable `TOTAL_EARNINGS` faltante en la fórmula de ISR
2. **ISR calculado incorrectamente**: Se usa `$0` en lugar del total real
3. **Percepciones incompletas**: Solo $2,300 en lugar de incluir P001 ($3,000)

---

## 🧩 Causa Raíz

### Flujo de Ejecución Anterior:

```
1. Cargar contexto inicial:
   - P001 = $3,000 (valor fijo del employee)
   - D003 = $500 (valor fijo del employee)
   - UMA = $108.57 (indicador económico)

2. Ejecutar reglas en orden:
   ├─ P002 (Aguinaldo: orden 20) → calcula y guarda en calculatedValues
   ├─ P003 (Prima Vac: orden 30) → calcula y guarda en calculatedValues
   ├─ P004 (Vales: orden 40) → calcula y guarda en calculatedValues
   ├─ D001 (IMSS: orden 200) → calcula sobre P001
   ├─ D002 (ISR: orden 210) → 🔴 BUSCA #TOTAL_EARNINGS → NO EXISTE → USA $0
   └─ D004 (Fondo: orden 230) → calcula sobre P001

3. Calcular totales:
   - Percepciones: suma solo calculatedValues con 'P' = P002 + P003 + P004
   - ❌ NO incluye P001 (está en fixedValues, no en calculatedValues)
```

### El Problema:

**Problema 1: P001 no aparece en los detalles del resultado**
- **P001** está en `fixedValues` (no tiene fórmula, viene del empleado)
- **P002, P003, P004** están en `calculatedValues` (tienen fórmulas)
- El código original solo agregaba `calculatedValues` al resultado
- Por eso `getTotalEarnings()` solo sumaba P002+P003+P004 = $2,300
- **Faltaba P001 ($3,000)** en el resultado

**Problema 2: TOTAL_EARNINGS no existe cuando se calcula ISR**
- **`TOTAL_EARNINGS`** nunca se calcula ni se inyecta al contexto
- **ISR (D002)** se ejecuta antes de que exista `TOTAL_EARNINGS`
- El FormulaEngine usa $0 por defecto → ISR incorrecto

---

## ✅ Solución Implementada

### Estrategia: Procesamiento en 3 Fases + Agregar todos los valores al resultado

**Parte 1: Calcular en 3 fases**
```java
// FASE 1: Calcular todas las PERCEPCIONES (orden < 100)
for (PayrollRule rule: rulesToApply) {
    if (rule.getOrder() < 100) {
        rule.execute(context);  // P002, P003, P004
    }
}

// FASE 2: Calcular TOTAL_EARNINGS y agregarlo al contexto
BigDecimal totalEarnings = calculateTotalEarnings(context);
context.setFixedValue("TOTAL_EARNINGS", totalEarnings);

// FASE 3: Calcular todas las DEDUCCIONES (orden >= 100)
for (PayrollRule rule: rulesToApply) {
    if (rule.getOrder() >= 100) {
        rule.execute(context);  // D001, D002, D004
    }
}
```

**Parte 2: Agregar TODOS los conceptos al resultado**
```java
// Agregar valores fijos (P001, D003, etc. del empleado)
context.getFixedValuesMap().forEach((code, amount) -> {
    if (code.startsWith("P") || code.startsWith("D")) {
        result.addDetail(code, amount, "Fixed value from employee");
    }
});

// Agregar valores calculados (P002, P003, P004, D001, D002, D004)
context.getCalculatedValues().forEach((code, amount) -> {
    result.addDetail(code, amount, "Calculated via formula");
});
```

### Método `calculateTotalEarnings()`:
```java
private BigDecimal calculateTotalEarnings(PayrollContext context) {
    BigDecimal total = BigDecimal.ZERO;

    // Sumar valores FIJOS de percepciones (P001 del employee)
    for (Entry<String, BigDecimal> entry : context.getFixedValuesMap().entrySet()) {
        if (entry.getKey().startsWith("P")) {
            total = total.add(entry.getValue());
        }
    }

    // Sumar valores CALCULADOS de percepciones (P002, P003, P004 de fórmulas)
    for (Entry<String, BigDecimal> entry : context.getCalculatedValues().entrySet()) {
        if (entry.getKey().startsWith("P")) {
            total = total.add(entry.getValue());
        }
    }

    return total;
}
```

---

## 📊 Resultados Esperados

### Antes (Incorrecto):
```
Contexto inicial: P001=$3,000, D003=$500, UMA=$108.57
Aplicando reglas: P002, P003, P004, D001, D002, D004

❌ Percepciones: $2,300 (solo P002+P003+P004, FALTA P001!)
⚠️  Missing variable TOTAL_EARNINGS (usa $0)
   Deducciones: $231 (ISR calculado sobre $0)
   Neto: $2,069
```

### Después (Correcto):
```
Contexto inicial: P001=$3,000, D003=$500, UMA=$108.57

FASE 1 - Calcular percepciones:
  P002 (Aguinaldo) = calculado
  P003 (Prima Vac) = calculado
  P004 (Vales) = calculado

FASE 2 - Calcular TOTAL_EARNINGS:
  💰 TOTAL_EARNINGS = P001 + P002 + P003 + P004 = $X,XXX

FASE 3 - Calcular deducciones:
  D001 (IMSS) = sobre P001
  D002 (ISR) = sobre TOTAL_EARNINGS ✅
  D004 (Fondo) = sobre P001

Resultado final:
✅ Percepciones: $X,XXX (P001 + P002 + P003 + P004 - TODOS INCLUIDOS)
✅ Deducciones: $XXX (ISR correcto + IMSS + Fondo + D003)
✅ Neto: $XXX
```

---

## 🎯 Beneficios de la Solución

1. **✅ Todos los conceptos aparecen en el resultado**:
   - Valores fijos (P001, D003) ahora se agregan a `details`
   - Valores calculados (P002, P003, etc.) también se agregan
   - `getTotalEarnings()` suma TODOS los conceptos 'P'

2. **✅ Orden de ejecución garantizado**:
   - Primero percepciones (orden < 100)
   - Luego deducciones (orden >= 100)

3. **✅ TOTAL_EARNINGS siempre disponible**:
   - Se calcula entre fases
   - Incluye valores fijos Y calculados
   - Disponible para fórmulas de deducciones

4. **✅ ISR correcto**:
   - Ya no usa $0
   - Calcula sobre el total real de percepciones
   - Incluye P001 en la base gravable

5. **✅ Separación lógica**:
   - Percepciones: orden 10-99
   - Deducciones: orden 100-299
   - Base gravable virtual: orden 100-199 (futuro)

---

## 🧪 Cómo Probar

### Ejecutar la aplicación:
```bash
./run-console.sh
```

### Buscar en el log:
```bash
# Debe aparecer:
💰 TOTAL_EARNINGS calculado: $3,XXX

# Ya NO debe aparecer:
⚠️  Employee X - Missing variables (will use ZERO): [TOTAL_EARNINGS]
```

### Verificar resultado:
```bash
# El ISR ahora debe ser > $0
# Las percepciones deben incluir P001
# El neto debe ser menor (más ISR)
```

---

## 📝 Notas Adicionales

### Convención de Códigos:
- **P001-P099**: Percepciones (EARNING)
- **D001-D299**: Deducciones (DEDUCTION)
- **G001-G099**: Base Gravable (futura implementación)

### Variables Especiales:
- `TOTAL_EARNINGS`: Total de percepciones (P*)
- `TOTAL_DEDUCTIONS`: Total de deducciones (D*) - puede agregarse después
- `UMA`: Unidad de Medida y Actualización
- `HIRE_DATE`: Fecha de contratación del empleado

### Fórmulas que dependen de TOTAL_EARNINGS:
```sql
-- ISR (D002)
'#calcularISR(#TOTAL_EARNINGS)'

-- Futuras fórmulas:
-- Subsidio al empleo: '#subsidioEmpleo(#TOTAL_EARNINGS)'
-- IMSS patronal: '#calcularIMSSPatronal(#TOTAL_EARNINGS)'
```

---

## 🔄 Próximas Mejoras

1. **Agregar TOTAL_DEDUCTIONS**: Similar a TOTAL_EARNINGS
2. **Base gravable exenta**: Calcular percepciones exentas de ISR
3. **Logging mejorado**: Registrar cada fase del cálculo
4. **Tests unitarios**: Validar que TOTAL_EARNINGS se calcula correctamente
