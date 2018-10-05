# fpl-ccd-configuration

[![Build Status](https://travis-ci.org/hmcts/spring-boot-template.svg?branch=master)](https://travis-ci.org/hmcts/spring-boot-template)
 ## Building and deploying the application
 ### Building the application
 The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.
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
 Run the distribution (created in `build/install/spring-boot-template` directory)
by executing the following command:
 ```bash
  docker-compose up
```
 This will start the API container exposing the application's port
(set to `4000` in this template app).
 In order to test if the application is up, you can call its health endpoint:
 ```bash
  curl http://localhost:4000/health
```
 You should get a response similar to this:
 ```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```
 ### Alternative script to run application
 To skip all the setting up and building, just execute the following command:
 ```bash
./bin/run-in-docker.sh
```
 For more information:
 ```bash
./bin/run-in-docker.sh -h
```
 Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:
 ```bash
docker-compose rm
```
 It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:
 ```bash
docker images
 docker image rm <image-id>
```
 There is no need to remove postgres and java or similar core images.
 ## License
 This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
