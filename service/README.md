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

Ensure that the Spring Boot application is started with local, user-mappings and the feature-toggle profiles
(add environment variable spring.profiles.active=feature-toggle,local,user-mappings when starting the main class).

Configure the notify.api_key if necessary (you can pass them as environment variables when IntelliJ
starts the application).

Ensure the variable CCD_DEF_CASE_SERVICE_BASE_URL bin/configurer/utils/fpl-process-definition.sh is set to
http://docker.for.mac.localhost:4000 - this will use the host machine for CCD, reaching
the locally running application.

## Config

The FPL Service (as any other HMCTS Reform services) uses three sources of configuration for the placeholders:
* application.yaml (profile-driven, test and production systems use only default profile)
* ENV variables (passed in via charts in the cluster or docker-compose for local)
* The following [library](https://github.com/hmcts/properties-volume-spring-boot-starter) provides secrets as configuration placeholders.
  [This file](src/main/resources/bootstrap.yaml) configures mapping from a secret name to Spring's configuration property.

Custom configuration parameters:

|Property name|Configuration place|Description|
|---|---|---|
|fees-register.api.url|ENV|URL of the Fees Register service, used for obtaining fee of applications|
|fees-register.parameters|application.yaml|Mapping of FPL's possible Fees to required parameters by Fee Register. Configuration stored in class *FeesConfig*|
|payment.site_id|application.yaml|Identifier required when communicating with Payment service to determine payments coming from FPL |
|payment.api.url|ENV|URL of service providing PBA payments functionality|
|bankHolidays.api.url|application.yaml|URL of the service which can return this years Bank Holidays|
|ld.sdk_key|SECRET|Launch Darkly API key for feature toggles|
|ld.user_key|ENV|Optional parameter which is send to LD to enable per env toggles|
|SCHEDULER_ENABLED|ENV|(Optional) Determines if Quartz scheduler should be used. Enabled by default (disabled in local profile).|
|SCHEDULER_DB_HOST|ENV|(Optional) Host for Scheduler DB, default is `localhost`|
|SCHEDULER_DB_PORT|ENV|(Optional) Port for Scheduler DB, default is `5050`|
|SCHEDULER_DB_NAME|ENV|(Optional) Name for Scheduler DB, default is `fpl_scheduler`|
|SCHEDULER_DB_USER|ENV|(Optional) Username for Scheduler DB, default is `fpl_scheduler`|
|SCHEDULER_DB_PASSWORD|SECRET|(Optional) Password for scheduler db, default (for local) is `fpl_scheduler`|
|idam.s2s-auth.microservice|application.yaml|Name of FPL service when communicating with other Reform services.|
|idam.s2s-auth.url|ENV|URL of Service responsible for Service to Service authentication (s2s token provider)|
|idam.s2s-auth.totp_secret|SECRET|One time password code secret required when communicating with s2s service|
|idam.api.url|ENV|URL to IDAM service used to authenticate requests and obtain further user details.|
|idam.client.id|ENV|Client identifier used for OAuth2 login via IDAM.|
|idam.client.redirect_uri|ENV|Redirect URI used for OAuth2 login via IDAM.|
|idam.client.secret|SECRET|Secret portion of OAuth 2 flow.|
|auth.idam.client.baseUrl|ENV|Configuration required by auth-checker-library to communicate with IDAM |
|auth.provider.service.client.baseUrl|ENV|Configuration required by auth-checker-library to communicate with s2s auth service|
|spring.mail.host|ENV|Mail server host used for robotics (non Gov notify emails)|
|spring.mail.port|ENV|Port of the mail server|
|spring.mail.properties.mail.smtp.starttls.enable|ENV|Is mail TLS enabled|
|spring.mail.properties.mail.smtp.ssl.trust|ENV|TLS mail config|
|spring.security.enabled|ENV|Indicates if spring security should be enabled|
|spring.security.oauth2.resourceserver.jwt.issuer-uri|ENV|Expected IDAM issuer-uri|
|spring.security.oauth2.resourceserver.jwt.jwk-set-uri|ENV|Expected IDAM jwk-set-uri|
|core_case_data.api.url|ENV|CCD Data store API URL|
|docmosis.tornado.url|ENV|Docmosis Tornado (document generation service) URL|
|docmosis.tornado.key|SECRET|Docmosis Tornado (document generation service) API key|
|document_management.url|ENV|URL for service responsible for storing documents|
|notify.api_key|SECRET|Gov notify API Key for sending emails|
|gateway.url|ENV|URL for CCD gateway, currently used to prepare URL leading directly to the document, used in notifications|
|rd_professional.api.url|ENV|URL for Professional Reference Data service|
|send-letter.url|ENV|URL for service delivering Bulk Print functionality|
|ccd.ui.base.url|ENV|URL for CCD Web UI, used in notifications|
|fpl.local_authority_email_to_code.mapping|SECRET|Explained below.|
|fpl.local_authority_code_to_name.mapping|SECRET|Explained below.|
|fpl.local_authority_code_to_hmcts_court.mapping|SECRET|Explained below.|
|fpl.local_authority_code_to_cafcass.mapping|SECRET|Explained below.|
|fpl.local_authority_code_to_shared_inbox.mapping|SECRET|Explained below.|
|fpl.local_authority_fallback_inbox|SECRET|Fallback notification inbox when the system cannot determine where the LA notification should be delivered|
|fpl.system_update.username|SECRET|System user username, used for automated state transitions and data modifications|
|fpl.system_update.password|SECRET|System user password, used for automated state transitions and data modifications|
|fpl.ctsc_inbox|SECRET|CTSC mail inbox|
|robotics.notification.sender|SECRET|FROM field when sending emails to robotics|
|robotics.notification.recipient|SECRET|Robotics mailbox address|
|feature.toggle.robotics.case-number.notification.enabled|ENV|Determines if JSON file should be send to robotics when Family Man case number is added to the case'
|feature.toggle.robotics.support.api.enabled|ENV|Enables API to retrigger robotics notification for particular case|
|appinsights.instrumentationkey|SECRET|Key used to connect to Azure AppInsights|

Notes:
* When using env variables '.', '-' are replaced with '_', i.e. `idam.s2s-auth.url` is configured as ENV var: IDAM_S2S_AUTH_URL
* Secrets are stored in the appropriate Azure Key vault. As mentioned above name of the secrets to property name can be found in [this file](src/main/resources/bootstrap.yaml)

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

### Local Authority Code to HMCTS Court

Example:
```
FPL_LOCAL_AUTHORITY_CODE_TO_HMCTS_COURT_MAPPING:
EX=>Example Family Court: FamilyCourt@email.com
```

### Local Authority Code to Cafcass

Example:
```
FPL_LOCAL_AUTHORITY_CODE_TO_CAFCASS_MAPPING:
EX=>cafcass:cafcass@cafcass.com
```

### Local Authority code to Local Authority users

Example:
```
FPL_LOCAL_AUTHORITY_USER_MAPPING:
EX=>1,2,3
```

### Local Authority code to Shared Inbox

Example:
```
FPL_LOCAL_AUTHORITY_CODE_TO_SHARED_INBOX_MAPPING:
EX=>local-authority@local-authority.com
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
