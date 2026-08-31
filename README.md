# Migración de procesos batch - Banco XYZ

Actividad sumativa semana 3, Desarrollo Backend III (PBY2203).

Migra tres procesos batch del sistema legacy del Banco XYZ a Spring Batch. Los datos vienen
de https://github.com/KariVillagran/bank_legacy_data (carpeta `data/semana_3`) y traen errores
a propósito: fechas en varios formatos, montos vacíos o negativos, tipos inválidos y duplicados.

Requisitos: Java 21, Docker y el wrapper `./mvnw`.

## Ejecutar

```
docker compose up -d                              # PostgreSQL, puerto 5433
./mvnw clean package
java -jar target/bank-batch-0.0.1-SNAPSHOT.jar    # corre los 3 jobs
```

Un job solo:

```
java -jar target/bank-batch-0.0.1-SNAPSHOT.jar --job=calculoInteresesMensuales
```

Ver resultados:

```
docker exec -it bank-batch-db psql -U bank -d bankxyz
```

Para repetir desde cero: `docker compose down -v` y volver a levantar.

## Jobs

- **reporteTransaccionesDiarias**: valida las transacciones y las guarda en `transaccion`,
  después arma el resumen por fecha en `resumen_transaccion_diaria`. Quedan 785 de 1000 y
  322 fechas.
- **calculoInteresesMensuales**: calcula el interés según el tipo de cuenta (ahorro 2,5%,
  préstamo 1,8%, hipoteca 1,2%) y guarda el saldo final en `cuenta_interes`. Quedan 524.
- **generacionEstadosAnuales**: guarda los movimientos en `movimiento_anual` y genera el
  estado anual por cuenta en `estado_cuenta_anual`. Quedan 952 movimientos y 20 estados.

## Datos malos y tolerancia a fallos

Si falta un campo necesario para el cálculo (monto o saldo vacío, tipo inválido, fecha
inexistente) se descarta el registro. Si el problema no impide procesarlo (monto negativo,
descripción vacía, edad rara, nombre "Unknown") se guarda igual pero marcado como anomalía.
Los descartes quedan en `registro_rechazado` con el motivo.

La clase `PoliticaSkipPersonalizada` revisa el tipo de excepción: si es un problema del dato
lo salta, pero si es un error de base de datos reintenta en vez de saltarlo, para no perder
registros que sí eran válidos.

## Escalamiento

Multi-thread en los jobs 1 y 2 (el lector va envuelto en `SynchronizedItemStreamReader` porque
no es thread-safe) y particiones por rango de `cuenta_id` en el job 3.

Probé varios parámetros, están en `evidencia/pruebas_escalado.txt`. Con hilos baja de 471 ms
a 350 ms, pero de 4 a 8 casi no cambia, así que dejé 4. Con particiones es al revés: sube de
248 ms a 390 ms, porque son solo 20 cuentas y coordinar los hilos cuesta más que lo que se gana.

Se cambian en `application.yml` o por consola con `--bank.scaling.threads=8`.

## Estructura

```
src/main/java/cl/duoc/bankbatch/
    config/          propiedades, executor y lanzador de jobs
    support/         parser de fechas, política de skip, listeners y auditoría
    transacciones/   job 1
    intereses/       job 2
    anuales/         job 3
data/                CSV de entrada
evidencia/           capturas, salidas de consola y pruebas de escalamiento
```

Tests: `./mvnw test` (14 tests sobre el parseo de fechas, la política de skip y el processor).
