# IoT Monitoring System

Spring Boot backend for collecting sensor readings, storing alerts and calculating measurements.
The application supports temperature, humidity and atmospheric pressure sensors. Data is stored in
PostgreSQL using Spring Data JPA.

This is a take-home assignment. [TASKS.md](./TASKS.md) contains the original requirements.

## Implemented Features

- Sensor types stored as strings and sensor relationships mapped with JPA.
- Sensor registration with name validation and duplicate-name handling.
- Sensor readings received through REST or Kafka and saved to PostgreSQL.
- Latest alert lookup for a sensor.
- Incoming alerts saved and published to the `alerts` Kafka topic.
- Average temperature calculation and measurement lookup by sensor type and time period.
- Filtering measurement values by their allowed deviation from the average.
- Automatic alerts for readings outside the limits defined in Task 10.

## Prerequisites

- Java 21. The Maven wrapper is included.
- Docker for the PostgreSQL integration tests and the local Compose setup.
- A separately configured Kafka broker for running the messaging flow.

## Build and Test

```bash
./mvnw clean verify
./mvnw test -Dtest=Task4Tests
./mvnw test -Dtest=Task9Tests
```

The supplied task suite contains 43 tests. The last full run passed all 43 tests.
The supplied test classes and SQL fixtures have been kept unchanged.

Integration tests use the `docker` profile and start PostgreSQL 17.6 through Testcontainers.
Start Docker before running them. Kafka publishing is mocked in the relevant tests, so a Kafka broker
is not required for the suite. `Task9Tests` tests the calculator without Spring or Docker.

Task 0 in [TASKS.md](./TASKS.md) also describes a local PostgreSQL alternative. The default test setup
uses Testcontainers.

## Running Locally

Start PostgreSQL and provide the datasource settings at startup:

```bash
docker compose up -d
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/monitoring \
SPRING_DATASOURCE_USERNAME=monitoring_user \
SPRING_DATASOURCE_PASSWORD='IOT123!' \
./mvnw spring-boot:run
```

The application uses port `8080` by default. The Compose file includes only PostgreSQL.
Kafka defaults to `localhost:9092`; use `SPRING_KAFKA_BOOTSTRAP_SERVERS` to change the broker address.
JSON serialization and deserialization must also be configured for the DTO payloads before using
Kafka end to end. The task tests do not verify a real broker connection.

Hibernate uses `ddl-auto=create-drop`, so application tables are recreated at startup and dropped
on normal shutdown. This setup is intended for the assignment and local development.

## REST Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/sensors` | Register a sensor; returns 400 for a blank name and 409 for a duplicate name |
| POST | `/sensor-readings` | Save a reading and create an alert if it breaks the sensor's rule |
| GET | `/alerts/latest?sensorId=<UUID>` | Get the latest alert; returns 404 when none is found |

Example sensor registration:

```bash
curl -i -X POST http://localhost:8080/sensors \
  -H 'Content-Type: application/json' \
  -d '{"name":"warehouse-temperature","type":"TEMPERATURE"}'
```

Use the returned sensor ID when submitting a reading. Timestamps represent UTC:

```json
{
  "sensorId": "<sensor UUID>",
  "value": 45.0,
  "timestamp": "2026-11-19T12:00:00"
}
```

## Kafka Topics

| Direction | Topic | Payload |
|-----------|-------|---------|
| Consumed | `sensor-reading` | `SensorReadingDto` |
| Consumed | `sensor-alerts` | `AlertDto` |
| Published | `alerts` | `AlertDto` |

Incoming alerts are handled by `AlertService`. Automatic alerts created while saving readings are
stored directly in the database; they are not published to Kafka by that path.

## Design Decisions and Limits

- DTOs are Java records. Mapping uses direct constructor calls because the current mappings are small
  and do not need a separate mapper layer.
- Average calculations and measurement ordering are done in the database. Measurement queries return
  values without loading full reading entities.
- Saving a reading and its automatic alert uses one database transaction.
- Kafka publishing does not currently handle asynchronous delivery failures. Database persistence and
  Kafka delivery are not atomic. Reliable delivery in a production system would need an explicit
  retry and consistency strategy, such as a transactional outbox.
- Sensor names are checked before insertion, as allowed by the assignment. There is no database unique
  constraint, so concurrent requests can still create duplicates.
- Some REST responses expose JPA entities. A production API could use dedicated response DTOs to
  control its public contract independently of persistence.
- Further hardening would include complete input validation, consistent error responses, Kafka
  redelivery handling and database migrations instead of `create-drop`.

## Project Structure

```
src/main/java/com/aldisued/iot/monitoring/
  controller/     REST endpoints (sensors, sensor readings, alerts)
  dto/            Request/response records
  entity/         JPA entities: Sensor, SensorReading, Alert, SensorType
  messaging/      Kafka listeners for readings and alerts
  repository/     Spring Data JPA repositories
  service/        Business logic
src/main/resources/
  application.properties
src/test/java/com/aldisued/iot/monitoring/
  IntegrationTestBase.java          Shared setup for integration tests
  TestcontainersConfiguration.java  PostgreSQL Testcontainer
  tasks/                            Task0Test ... Task10Tests
src/test/resources/
  application-docker.yml / application-local.yml
  sql/                              Seed data for the task tests
docker-compose.yml                  Local PostgreSQL
TASKS.md                            Task descriptions
```
