# Delius API Testing Strategy

API endpoints should have unit and integration test coverage for development
purposes and end-to-end coverage for inclusion in ongoing CI smoke tests.

## Unit Testing

Unit tests are written in Kotlin using JUnit 5. Each class should have
associated unit tests covering business logic.

## Integration Testing

Integration tests are written in Kotlin using JUnit 5. Each class should have
associated integration tests covering API functionality.

## End-to-End Testing

End-to-end tests are provided via the `e2e` gradle task:

```bash
./gradlew e2e
```

You must have a spring profile activated e.g. `SPRING_PROFILES_ACTIVE=test`

Process:

1. **generateApiSpec** API started and OAPI 3.0 document generated & saved in `build/generated`.
2. **openApiGenerate** OAPI document used to generate a kotlin API client in `src/generated`.
3. **e2e** JUnit tests written in Kotlin against the generated client.

The `e2e` task actually spins up the delius API Spring boot application but with the API disabled.
This is for configuration & DI in order to connect to the database for test setup & teardown.

Environment configuration is handled by Spring configuration.
The `e2e` profile is always loaded so defaults can be found: `src/e2e/resources/application-e2e.yml`.

### Running against local API

> TODO: once we have a way of specifying database username via configuration/profiles then simplify this section

```bash
docker-compose -f docker-compose.yml -f docker-compose.oracle.yml up -d --build --force-recreate
```

The oauth client must have the database username claim setup but this is not configured in the auth service by default.
1. Browse to <http://localhost:9090/auth/ui>.
2. Login with:
   * **Username** AUTH_ADM
   * **Password** password123456
3. Select a client that has the `client_credentials` grant type
   (`community-api-client` is configured  by default)
3. Set the 'Database Username' field to `NationalUser`
4. Save

```bash
SPRING_PROFILES_ACTIVE=local-oracle ./gradlew e2e
```
