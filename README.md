# reto-java

Reto técnico compuesto por microservicios en Spring Boot para un dominio bancario: gestión de clientes y gestión de cuentas/movimientos, comunicados de forma asíncrona vía RabbitMQ.

## Microservicios

| Servicio     | Descripción                                              | Puerto por defecto |
|--------------|-----------------------------------------------------------|---------------------|
| `ms-cliente` | Gestión de personas/clientes                               | `8081`              |
| `ms-cuenta`  | Gestión de cuentas, movimientos y reportes de estado de cuenta | `8082`              |

## Stack

- Java 17
- Spring Boot 3.5.16
- Spring Web, Spring Data JPA, Spring Validation
- Spring AMQP (RabbitMQ) — comunicación asíncrona entre microservicios
- PostgreSQL 16 (una base de datos por microservicio)
- Lombok
- Spring Boot Actuator
- Docker / Docker Compose

## Arquitectura

Cada microservicio es independiente, con su propia base de datos (patrón *database per service*) y expone una API REST. La única comunicación entre ellos es **asíncrona vía eventos de RabbitMQ**, no hay llamadas HTTP directas entre microservicios.

```
                 ┌──────────────────┐          ┌──────────────────┐
   HTTP :8081    │    ms-cliente    │          │    ms-cuenta     │   HTTP :8082
  ─────────────► │  (CRUD personas/ │          │ (cuentas, movi-  │ ◄─────────────
                 │    clientes)     │          │ mientos, reportes)│
                 └────────┬─────────┘          └─────────┬────────┘
                          │                               │
                    publica evento               escucha evento
                  cliente.created/                cliente.* (bind
                 updated/deleted                   por routing key)
                          │                               │
                          ▼                               ▼
                 ┌────────────────────────────────────────────────┐
                 │        RabbitMQ · exchange (topic)              │
                 │           cliente.events.exchange                │
                 └────────────────────────────────────────────────┘
                          │                               │
                          ▼                               ▼
                 ┌──────────────────┐          ┌──────────────────┐
                 │  cliente-db      │          │   cuenta-db       │
                 │  (PostgreSQL)    │          │   (PostgreSQL)    │
                 │  personas        │          │   clientes_replica│
                 │  clientes        │          │   cuentas         │
                 │                  │          │   movimientos     │
                 └──────────────────┘          └──────────────────┘
```

- `ms-cliente` es dueño de los datos de `personas`/`clientes`. Al crear, actualizar o eliminar un cliente, publica un evento (`cliente.created` / `cliente.updated` / `cliente.deleted`) en el exchange `cliente.events.exchange` (topic).
- `ms-cuenta` no llama a `ms-cliente` por HTTP: escucha esos eventos (`RabbitListener` sobre la cola `ms-cuenta.cliente.events.queue`, bindeada con el patrón `cliente.*`) y mantiene una **réplica local de solo lectura** (`clientes_replica`) para poder validar `clienteId` al crear cuentas/movimientos sin acoplarse al otro servicio.
- `ms-cuenta` expone además el endpoint de reportes (`/reportes`), que arma el estado de cuenta de un cliente a partir de `cuentas` y `movimientos` en un rango de fechas.

### Estructura por capas (ambos microservicios siguen el mismo patrón)

```
com.banco.<ms>/
├── controllers/    # Endpoints REST
├── services/       # Lógica de negocio
├── repositories/   # Spring Data JPA
├── entities/       # Entidades JPA
├── dto/            # Request/Response DTOs (con Bean Validation)
├── events/         # Payload de eventos de RabbitMQ (ClienteEvent)
├── messaging/       # Publisher (ms-cliente) / Listener (ms-cuenta) de RabbitMQ
├── config/          # RabbitMQConfig (exchange, queue, binding)
├── exceptions/       # GlobalExceptionHandler + excepciones de negocio
└── health/           # Endpoint de healthcheck propio (/health)
```

## API

### `ms-cliente` (`http://localhost:8081`)

| Método | Endpoint         | Descripción          |
|--------|-------------------|------------------------|
| GET    | `/clientes`        | Listar clientes         |
| GET    | `/clientes/{id}`   | Obtener cliente por id  |
| POST   | `/clientes`         | Crear cliente           |
| PUT    | `/clientes/{id}`    | Actualizar cliente      |
| DELETE | `/clientes/{id}`    | Eliminar cliente        |
| GET    | `/health`            | Healthcheck              |

### `ms-cuenta` (`http://localhost:8082`)

| Método | Endpoint                                     | Descripción                        |
|--------|-----------------------------------------------|--------------------------------------|
| GET    | `/cuentas`                                      | Listar cuentas                       |
| GET    | `/cuentas/{id}`                                 | Obtener cuenta por id                |
| POST   | `/cuentas`                                       | Crear cuenta                         |
| PUT    | `/cuentas/{id}`                                  | Actualizar cuenta                    |
| DELETE | `/cuentas/{id}`                                  | Eliminar cuenta                      |
| GET    | `/movimientos`                                    | Listar movimientos                   |
| GET    | `/movimientos/{id}`                               | Obtener movimiento por id            |
| POST   | `/movimientos`                                     | Registrar movimiento (depósito/retiro) |
| DELETE | `/movimientos/{id}`                                | Eliminar movimiento                  |
| GET    | `/reportes?clienteId={id}&fecha={rango}`            | Estado de cuenta / reporte de movimientos |
| GET    | `/health`                                            | Healthcheck                          |

Colección completa de pruebas (con ejemplos de body) disponible en [`postman/banco.postman.json`](postman/banco.postman.json).

## Requisitos

- JDK 17
- Docker y Docker Compose

## Cómo levantar el entorno

### Opción A — Todo dockerizado (recomendado)

Levanta las bases de datos, RabbitMQ y ambos microservicios (build de las imágenes incluido):

```bash
docker-compose up -d --build
```

Esto crea:
- `ms-cliente-db` (Postgres, puerto host `5433`) y `ms-cuenta-db` (Postgres, puerto host `5434`)
- `rabbitmq` (AMQP en `5672`, panel de administración en `15672`, usuario/clave `guest`/`guest`)
- `ms-cliente` (puerto `8081`) y `ms-cuenta` (puerto `8082`), construidos desde sus respectivos `Dockerfile`

Ver logs:

```bash
docker-compose logs -f ms-cliente ms-cuenta
```

Detener y limpiar:

```bash
docker-compose down          # detiene los contenedores
docker-compose down -v       # además elimina los volúmenes (borra los datos de las DB)
```

### Opción B — Solo infraestructura en Docker, microservicios en local

Útil para desarrollar con recarga rápida (sin rebuild de imagen en cada cambio).

1. Levantar solo las bases de datos y RabbitMQ:
   ```bash
   docker-compose up -d cliente-db cuenta-db rabbitmq
   ```
2. Ejecutar cada microservicio en una terminal distinta (usan los defaults de `application.yaml`, que ya apuntan a `localhost:5433`/`5434` y `localhost:5672`):
   ```bash
   cd ms-cliente && ./mvnw spring-boot:run
   cd ms-cuenta && ./mvnw spring-boot:run
   ```

### Probar que todo esté arriba

```bash
curl -i http://localhost:8081/health   # ms-cliente
curl -i http://localhost:8082/health   # ms-cuenta
```

## Datos de prueba

[`db/database.sql`](db/database.sql) contiene el DDL y datos semilla (personas, clientes, cuentas y movimientos de ejemplo) usados en los casos de uso del reto. No se aplica automáticamente vía `docker-compose` (no está montado como script de inicialización), así que hay que ejecutarlo manualmente contra cada base una vez que los contenedores estén arriba, por ejemplo:

```bash
docker exec -i ms-cliente-db psql -U ms_cliente -d ms_cliente < db/database.sql
docker exec -i ms-cuenta-db  psql -U ms_cuenta  -d ms_cuenta  < db/database.sql
```

> El script incluye las secciones de `cliente` y `cuenta` en un mismo archivo; al ejecutarlo contra cada contenedor solo importan las tablas de su propio dominio (las demás se crean vacías por los `CREATE TABLE IF NOT EXISTS`, sin datos aplicables). Alternativamente, dado que `ms-cliente` publica eventos al crear/editar/eliminar clientes, `clientes_replica` en `ms-cuenta` también puede poblarse dejando correr ambos servicios y usando la API en vez del seed manual.

## Variables de entorno

### `ms-cliente`

| Variable                    | Default                                              | Descripción              |
|------------------------------|-------------------------------------------------------|---------------------------|
| `SERVER_PORT`                | `8081`                                                 | Puerto de la aplicación   |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5433/ms_cliente`          | URL de conexión a la DB   |
| `SPRING_DATASOURCE_USERNAME` | `ms_cliente`                                           | Usuario de la DB          |
| `SPRING_DATASOURCE_PASSWORD` | `ms_cliente`                                           | Password de la DB         |
| `SPRING_JPA_DDL_AUTO`        | `update`                                               | Estrategia de DDL de JPA  |
| `RABBITMQ_HOST`              | `localhost`                                            | Host de RabbitMQ          |
| `RABBITMQ_PORT`              | `5672`                                                 | Puerto AMQP de RabbitMQ   |
| `RABBITMQ_USER`              | `guest`                                                | Usuario de RabbitMQ       |
| `RABBITMQ_PASSWORD`          | `guest`                                                | Password de RabbitMQ      |

### `ms-cuenta`

| Variable                    | Default                                              | Descripción              |
|------------------------------|-------------------------------------------------------|---------------------------|
| `SERVER_PORT`                | `8082`                                                 | Puerto de la aplicación   |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5434/ms_cuenta`           | URL de conexión a la DB   |
| `SPRING_DATASOURCE_USERNAME` | `ms_cuenta`                                            | Usuario de la DB          |
| `SPRING_DATASOURCE_PASSWORD` | `ms_cuenta`                                            | Password de la DB         |
| `SPRING_JPA_DDL_AUTO`        | `update`                                               | Estrategia de DDL de JPA  |
| `RABBITMQ_HOST`              | `localhost`                                            | Host de RabbitMQ          |
| `RABBITMQ_PORT`              | `5672`                                                 | Puerto AMQP de RabbitMQ   |
| `RABBITMQ_USER`              | `guest`                                                | Usuario de RabbitMQ       |
| `RABBITMQ_PASSWORD`          | `guest`                                                | Password de RabbitMQ      |

> Nota: dentro de `docker-compose.yaml` los microservicios reciben las variables `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`, pero `application.yaml` de ambos servicios solo lee `SPRING_DATASOURCE_*` y `RABBITMQ_*`. Si vas a correr los contenedores de la app (no solo la infraestructura) revisa que esas variables lleguen con el nombre que Spring espera.

## Estructura del repo

```
reto-java/
├── docker-compose.yaml   # Bases de datos, RabbitMQ y ambos microservicios
├── db/
│   └── database.sql      # DDL + datos semilla (personas, clientes, cuentas, movimientos)
├── postman/
│   └── banco.postman.json  # Colección Postman con todos los endpoints
├── ms-cliente/            # Microservicio de clientes (puerto 8081)
└── ms-cuenta/             # Microservicio de cuentas, movimientos y reportes (puerto 8082)
```