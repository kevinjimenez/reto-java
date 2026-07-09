# reto-java

Reto técnico compuesto por microservicios en Spring Boot para un dominio bancario.

## Microservicios

| Servicio     | Descripción                                  | Puerto por defecto | Estado |
|--------------|-----------------------------------------------|---------------------|--------|
| `ms-cliente` | Gestión de clientes                           | `8081`              | En desarrollo (health check) |
| `ms-cuenta`  | Gestión de cuentas                            | `8082`              | En desarrollo (health check) |

## Stack

- Java 17
- Spring Boot 3.5.16
- Spring Web, Spring Data JPA, Spring Validation
- Spring AMQP (RabbitMQ) — comunicación entre microservicios
- PostgreSQL
- Spring Boot Actuator

## Requisitos

- JDK 17
- Docker (para levantar las bases de datos vía `docker-compose`)

## Cómo levantar el entorno

1. Levantar las bases de datos de ambos microservicios (desde la raíz del repo):
   ```bash
   docker-compose up -d
   ```
2. Ejecutar cada microservicio (en terminales separadas):
   ```bash
   cd ms-cliente && ./mvnw spring-boot:run
   cd ms-cuenta && ./mvnw spring-boot:run
   ```
3. Probar los health checks:
   ```bash
   curl -i http://localhost:8081/health   # ms-cliente
   curl -i http://localhost:8082/health   # ms-cuenta
   ```

### Variables de entorno (`ms-cliente`)

| Variable                    | Default                                              | Descripción              |
|------------------------------|-------------------------------------------------------|---------------------------|
| `SERVER_PORT`                | `8081`                                                 | Puerto de la aplicación   |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5433/ms_cliente`          | URL de conexión a la DB   |
| `SPRING_DATASOURCE_USERNAME` | `ms_cliente`                                           | Usuario de la DB          |
| `SPRING_DATASOURCE_PASSWORD` | `ms_cliente`                                           | Password de la DB         |
| `SPRING_JPA_DDL_AUTO`        | `update`                                               | Estrategia de DDL de JPA  |

### Variables de entorno (`ms-cuenta`)

| Variable                    | Default                                              | Descripción              |
|------------------------------|-------------------------------------------------------|---------------------------|
| `SERVER_PORT`                | `8082`                                                 | Puerto de la aplicación   |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5434/ms_cuenta`           | URL de conexión a la DB   |
| `SPRING_DATASOURCE_USERNAME` | `ms_cuenta`                                            | Usuario de la DB          |
| `SPRING_DATASOURCE_PASSWORD` | `ms_cuenta`                                            | Password de la DB         |
| `SPRING_JPA_DDL_AUTO`        | `update`                                               | Estrategia de DDL de JPA  |

## Estructura del repo

```
reto-java/
├── docker-compose.yaml   # Bases de datos de ambos microservicios
├── db/                    # Scripts/recursos de base de datos (pendiente)
├── postman/               # Colección de Postman para pruebas (pendiente)
├── ms-cliente/            # Microservicio de clientes (puerto 8081)
└── ms-cuenta/             # Microservicio de cuentas (puerto 8082)
```