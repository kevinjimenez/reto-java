# reto-java

Reto técnico compuesto por microservicios en Spring Boot para un dominio bancario.

## Microservicios

| Servicio     | Descripción                                  | Puerto por defecto | Estado |
|--------------|-----------------------------------------------|---------------------|--------|
| `ms-cliente` | Gestión de clientes                           | `8081`              | En desarrollo |
| `ms-cuenta`  | Gestión de cuentas                            | *(pendiente)*       | Esqueleto inicial (sin implementar) |

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

## Cómo levantar `ms-cliente`

1. Levantar la base de datos:
   ```bash
   cd ms-cliente
   docker-compose up -d
   ```
2. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Probar el health check:
   ```bash
   curl -i http://localhost:8081/health
   ```

### Variables de entorno (`ms-cliente`)

| Variable                    | Default                                              | Descripción              |
|------------------------------|-------------------------------------------------------|---------------------------|
| `SERVER_PORT`                | `8081`                                                 | Puerto de la aplicación   |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5433/ms_client`           | URL de conexión a la DB   |
| `SPRING_DATASOURCE_USERNAME` | `ms_client`                                            | Usuario de la DB          |
| `SPRING_DATASOURCE_PASSWORD` | `ms_client`                                            | Password de la DB         |
| `SPRING_JPA_DDL_AUTO`        | `update`                                               | Estrategia de DDL de JPA  |

## `ms-cuenta`

Todavía es el esqueleto generado por Spring Initializr, sin lógica implementada.

## Estructura del repo

```
reto-java/
├── ms-cliente/   # Microservicio de clientes
└── ms-cuenta/    # Microservicio de cuentas (pendiente)
```