# fpl-service

NOTE: All commands have to be executed from project root directory.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains `./gradlew` wrapper script, so there's no need to install Gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

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
