# fpl-service

NOTE: All commands have to be executed from project root directory.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains `./gradlew` wrapper script, so there's no need to install Gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application (as a Docker container)

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/service` directory) by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port (set to `4000` in this app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4000/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

Change the variable for CCD_DEF_CASE_SERVICE_BASE_URL in bin/configurer/utils/fpl-process-definition.sh
 to http://fpl-service:4000 - this ensures the CCD use the docker networking to reach
the service.

### Run the application (from IntelliiJ)
 
Ensure the Spring Boot is started with local profile 
(add environment variable spring.profiles.active=local when starting the main class).

Configure the notify.api_key if necessary (you can pass them as environment variables when IntelliJ 
starts the application). 

Ensure the variable CCD_DEF_CASE_SERVICE_BASE_URL bin/configurer/utils/fpl-process-definition.sh is set to
http://docker.for.mac.localhost:4000 - this will use the host machine for CCD, reaching 
locally running application. 


## Application Mappings

### Email domain to Local Authority code

Example:
```
FPL_LOCAL_AUTHORITY_EMAIL_TO_CODE_MAPPING:
example.gov.uk=>EX
```

### Local Authority code to Local Authority name

Example:
```
FPL_LOCAL_AUTHORITY_CODE_TO_NAME_MAPPING:
EX=>Example Local Authority
```

### Local Authority code to Local Authority users

Example:
```
FPL_LOCAL_AUTHORITY_USER_MAPPING:
EX=>1,2,3
```

### Local Authority Code to HMCTS Court

Example:
```
FPL_LOCAL_AUTHORITY_CODE_TO_HMCTS_COURT_MAPPING:
EX=>Example Family Court: FamilyCourt@email.com
```

## Feature Toggle

For local development feature toggle will use default flag values defined in `FeatureToggleService.java`.

### Use Test flag values
In order to use `Test` environment values locally, `sdk_key` needs to be specified. To do that,
create `application-feature-toggle.yaml` file with following data:

```
spring:
  profiles: feature-toggle

ld:
  sdk_key: (get from key vault)
```

`feature-toggle` value needs to be added to your run profiles in `spring.profiles.active` variable.

### Custom flag values

In order to test your feature with custom flag values `user_key` needs to be added to `application-feature-toggle.yaml`:

```
spring:
  profiles: feature-toggle

ld:
  sdk_key: (get from key vault)
  user_key: my-local-key
```

Your key will be added on first `FeatureToggleService` call and will be available on LaunchDarkly panel in Users tab.
You will be able to set your own flag values there without affecting other environments.


### Scheduler

In order to enable quartz scheduler
- run ./bin/utils/create-scheduler-db.sh
- set scheduler.enabled:true in application.yml local profile  

Upcoming hearing jobs can be configured with environment variables
UPCOMING_HEARINGS_CRON[default 0 0 2 ? * MON-FRI] - quartz expression, e.g 0/30 * * ? * MON-FRI
UPCOMING_HEARINGS_DAYS[default 2] - number of working days notification is sent before hearing
Elastic search must be enable in ccd-docker for Upcoming hearings job to work
