
> Configure project :service
Runs pact consumer Tests
Runs PIT mutation Tests

> Task :service:dependencies

------------------------------------------------------------
Project ':service'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
\--- org.projectlombok:lombok:1.18.30

api - API dependencies for source set 'main'. (n)
No dependencies

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

bootArchives - Configuration for Spring Boot archive artifacts. (n)
No dependencies

cftlibAnnotationProcessor - Annotation processors and their dependencies for source set 'cftlib'.
No dependencies

cftlibCompileClasspath - Compile classpath for source set 'cftlib'.
+--- com.github.hmcts.rse-cft-lib:bootstrapper:0.19.842
+--- com.github.hmcts.rse-cft-lib:cftlib-agent:0.19.842
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    \--- commons-configuration:commons-configuration:1.8
|    |         \--- commons-lang:commons-lang:2.6
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre
|    |         +--- com.google.guava:failureaccess:1.0.1
|    |         +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         +--- com.google.code.findbugs:jsr305:3.0.2
|    |         +--- org.checkerframework:checker-qual:3.37.0
|    |         +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         \--- com.google.j2objc:j2objc-annotations:2.8
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
\--- net.minidev:json-smart:2.5.0
     \--- net.minidev:accessors-smart:2.5.0
          \--- org.ow2.asm:asm:9.3

cftlibCompileOnly - Compile only dependencies for source set 'cftlib'. (n)
No dependencies

cftlibIDEAnnotationProcessor - Annotation processors and their dependencies for source set 'cftlib ide'.
No dependencies

cftlibIDECompileClasspath - Compile classpath for source set 'cftlib ide'.
+--- com.github.hmcts.rse-cft-lib:application:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:excel-importer:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:rest-api:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:elastic-search-support:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:domain:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:app-insights:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:repository:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:commons:0.19.842
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.7.11 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
|    +--- io.github.openfeign:feign-httpclient:11.6 -> 10.12
|    +--- com.github.hmcts:befta-fw:8.7.11
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.1 -> 7.12.1
|    +--- org.elasticsearch.client:elasticsearch-rest-client:7.17.1 -> 7.12.1
|    +--- io.rest-assured:rest-assured:4.5.1 -> 4.3.3
|    +--- org.apache.httpcomponents:httpmime:4.5.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.poi:poi-ooxml:5.2.2 -> 5.2.4
|    +--- org.apache.commons:commons-compress:1.21 -> 1.24.0
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.4.1 -> 2.6.1
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.11 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.apache.poi:poi-scratchpad:5.2.2 -> 5.2.3
|    +--- org.apache.poi:poi:5.2.2 -> 5.2.4
|    +--- org.apache.commons:commons-collections4:4.4
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- com.google.code.gson:gson:2.9.0 -> 2.8.9
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.4.1 -> 2.6.4
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- com.zaxxer:HikariCP:4.0.2 -> 4.0.3
|    +--- org.jooq:jool-java-8:0.9.14
|    +--- org.elasticsearch:elasticsearch:7.17.1 -> 7.12.1
|    +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
|    +--- org.flywaydb:flyway-core:6.5.7 -> 7.7.3
|    +--- javax.inject:javax.inject:1
|    +--- com.microsoft.azure:azure-storage:8.0.0
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- io.cucumber:cucumber-junit:5.7.0
|    +--- junit:junit:4.13.1 -> 4.13.2
|    +--- com.github.rholder:guava-retrying:2.0.0
|    +--- com.google.guava:guava:31.1-jre -> 32.0.1-jre
|    +--- org.hibernate.validator:hibernate-validator:6.0.20.Final -> 6.2.5.Final
|    +--- javax.validation:validation-api:2.0.1.Final
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.swagger:swagger-models:1.5.20
|    +--- io.swagger:swagger-annotations:1.6.6
|    +--- com.vladmihalcea:hibernate-types-52:2.16.3
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- commons-validator:commons-validator:1.6
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.postgresql:postgresql:42.5.1 -> 42.2.27
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.74 -> 9.0.75
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-logging:commons-logging:1.2
|    +--- com.auth0:java-jwt:3.12.0
|    +--- commons-codec:commons-codec:1.15
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.springframework.data:spring-data-jpa:2.7.11 -> 2.5.12
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.data:spring-data-commons:2.7.11 -> 2.5.12
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.apache.commons:commons-math3:3.6.1
|    +--- com.zaxxer:SparseBitSet:1.2 -> 1.3
|    +--- org.apache.poi:poi-ooxml-lite:5.2.2 -> 5.2.4
|    +--- org.apache.xmlbeans:xmlbeans:5.0.3 -> 5.1.1
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- com.github.virtuald:curvesapi:1.07 -> 1.08
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- com.microsoft.azure:applicationinsights-core:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.4.1 -> 2.6.4
|    +--- io.github.openfeign:feign-jackson:11.6 -> 10.12
|    +--- org.elasticsearch:elasticsearch-x-content:7.17.1
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.springframework:spring-web:5.3.27
|    +--- org.springframework:spring-orm:5.3.27
|    +--- org.springframework:spring-jdbc:5.3.27
|    +--- org.springframework:spring-tx:5.3.27
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- org.hibernate:hibernate-core:5.6.15.Final -> 5.4.33
|    +--- org.springframework:spring-aspects:5.3.27
|    +--- org.elasticsearch:elasticsearch-lz4:7.17.1
|    +--- org.elasticsearch:elasticsearch-cli:7.17.1
|    +--- org.elasticsearch:elasticsearch-core:7.17.1
|    +--- org.elasticsearch:elasticsearch-secure-sm:7.17.1
|    +--- org.elasticsearch:elasticsearch-geo:7.17.1
|    +--- org.apache.lucene:lucene-core:8.11.1
|    +--- org.apache.lucene:lucene-analyzers-common:8.11.1
|    +--- org.apache.lucene:lucene-backward-codecs:8.11.1
|    +--- org.apache.lucene:lucene-grouping:8.11.1
|    +--- org.apache.lucene:lucene-highlighter:8.11.1
|    +--- org.apache.lucene:lucene-join:8.11.1
|    +--- org.apache.lucene:lucene-memory:8.11.1
|    +--- org.apache.lucene:lucene-misc:8.11.1
|    +--- org.apache.lucene:lucene-queries:8.11.1
|    +--- org.apache.lucene:lucene-queryparser:8.11.1
|    +--- org.apache.lucene:lucene-sandbox:8.11.1
|    +--- org.apache.lucene:lucene-spatial3d:8.11.1
|    +--- org.apache.lucene:lucene-suggest:8.11.1
|    +--- com.carrotsearch:hppc:0.8.1
|    +--- joda-time:joda-time:2.10.10
|    +--- com.tdunning:t-digest:3.2
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- net.java.dev.jna:jna:5.10.0
|    +--- org.elasticsearch:elasticsearch-plugin-classloader:7.17.1
|    +--- org.elasticsearch.plugin:mapper-extras-client:7.17.1
|    +--- org.elasticsearch.plugin:parent-join-client:7.17.1
|    +--- org.elasticsearch.plugin:aggs-matrix-stats-client:7.17.1
|    +--- org.elasticsearch.plugin:rank-eval-client:7.17.1
|    +--- org.elasticsearch.plugin:lang-mustache-client:7.17.1
|    +--- org.checkerframework:checker-qual:3.12.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.11.0 -> 2.21.1
|    +--- com.microsoft.azure:azure-keyvault-core:1.0.0
|    +--- io.rest-assured:xml-path:4.5.1 -> 4.3.3
|    +--- io.rest-assured:json-path:4.5.1 -> 4.3.3
|    +--- io.rest-assured:rest-assured-common:4.5.1
|    +--- org.apache.commons:commons-lang3:3.12.0
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- org.projectlombok:lombok:1.18.26
|    +--- org.json:json:20200518 -> 20230227
|    +--- io.cucumber:cucumber-java:5.7.0
|    +--- org.hamcrest:hamcrest-core:2.2
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.glassfish:javax.el:3.0.0
|    +--- org.mapstruct:mapstruct-processor:1.3.0.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.14.1 -> 2.12.7
|    +--- org.lz4:lz4-java:1.8.0
|    +--- net.sf.jopt-simple:jopt-simple:5.0.2
|    +--- org.apache.httpcomponents:httpasyncclient:4.1.5
|    +--- org.apache.httpcomponents:httpcore-nio:4.4.16
|    +--- com.github.spullara.mustache.java:compiler:0.9.6
|    +--- org.codehaus.groovy:groovy-xml:3.0.17
|    +--- org.codehaus.groovy:groovy-json:3.0.17
|    +--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    +--- io.cucumber:cucumber-core:5.7.0
|    +--- io.cucumber:cucumber-expressions:8.3.1
|    +--- io.cucumber:datatable:3.3.1
|    +--- io.cucumber:cucumber-gherkin-vintage:5.7.0
|    +--- io.cucumber:cucumber-gherkin:5.7.0
|    +--- io.cucumber:cucumber-plugin:5.7.0
|    +--- io.cucumber:docstring:5.7.0
|    +--- org.apiguardian:apiguardian-api:1.1.0
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- commons-digester:commons-digester:1.8.1
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- io.cucumber:tag-expressions:2.0.4
|    \--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
+--- com.github.hmcts.rse-cft-lib:ccd-case-document-am-api:0.19.842
|    +--- com.github.hmcts:service-auth-provider-java-client:3.1.4 -> 4.0.3
|    +--- com.github.hmcts:idam-java-client:1.5.5 -> 2.1.1
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- org.springframework.boot:spring-boot-starter-hateoas:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-validation:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.json:json:20200518 -> 20230227
|    +--- org.projectlombok:lombok:1.18.20 -> 1.18.26
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- org.springframework.retry:spring-retry:1.3.4
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.5.14 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- javax.inject:javax.inject:1
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- org.springframework.hateoas:spring-hateoas:1.5.4 -> 1.3.7
|    +--- com.jayway.jsonpath:json-path:2.7.0 -> 2.5.0
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- io.vavr:vavr:0.10.4
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-web:5.3.27
|    +--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- io.swagger:swagger-models:1.5.20
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- io.github.openfeign:feign-jackson:10.12
|    +--- com.auth0:java-jwt:3.12.0
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- org.checkerframework:checker-qual:2.6.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.3.3 -> 2.21.1
|    +--- commons-logging:commons-logging:1.2
|    +--- commons-collections:commons-collections:3.2.2
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-codec:commons-codec:1.15
|    +--- com.fasterxml:classmate:1.5.1
|    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.1 -> 2.17.2
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- io.vavr:vavr-match:0.10.4
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.74 -> 9.0.75
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- com.microsoft.azure:applicationinsights-web:2.6.4
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- jakarta.validation:jakarta.validation-api:2.0.2
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    \--- org.bouncycastle:bcprov-jdk15on:1.70
+--- com.github.hmcts.rse-cft-lib:user-profile-api:0.19.842
|    +--- org.projectlombok:lombok:1.18.28 -> 1.18.26
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.github.hmcts.java-logging:logging-appinsights:6.0.1
|    +--- org.slf4j:slf4j-simple:2.0.7 -> 1.7.36
|    +--- org.slf4j:jcl-over-slf4j:2.0.7 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-jdbc:3.1.2 -> 2.5.15
|    +--- com.zaxxer:HikariCP:5.0.1 -> 4.0.3
|    +--- org.springframework.data:spring-data-jpa:3.1.2 -> 2.5.12
|    +--- org.springframework.data:spring-data-commons:3.1.2 -> 2.5.12
|    +--- org.springframework.boot:spring-boot-starter-actuator:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:3.1.2 -> 2.5.15
|    +--- com.github.hmcts:auth-checker-lib:2.1.5
|    +--- org.springframework.boot:spring-boot-starter-security:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:3.1.2 -> 2.5.15
|    +--- org.slf4j:jul-to-slf4j:2.0.7 -> 1.7.36
|    +--- org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0
|    +--- org.springdoc:springdoc-openapi-starter-webmvc-api:2.2.0
|    +--- org.springdoc:springdoc-openapi-starter-common:2.2.0
|    +--- io.swagger.core.v3:swagger-core-jakarta:2.2.15
|    +--- org.slf4j:slf4j-api:2.0.7 -> 1.7.36
|    +--- org.flywaydb:flyway-core:9.21.1 -> 7.7.3
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.1 -> 2.12.7
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:3.1.2 -> 2.5.15
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models-jakarta:2.2.15
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- org.yaml:snakeyaml:2.1 -> 1.28
|    +--- javax.inject:javax.inject:1
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.commons:commons-lang3:3.13.0 -> 3.12.0
|    +--- com.sun.mail:mailapi:2.0.1
|    +--- org.apache.httpcomponents:httpclient:4.5.14
|    +--- net.jcip:jcip-annotations:1.0
|    +--- org.springframework.security:spring-security-config:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-crypto:6.1.2 -> 5.5.8
|    +--- org.glassfish:jakarta.el:4.0.2 -> 3.0.4
|    +--- org.postgresql:postgresql:42.6.0 -> 42.2.27
|    +--- org.springframework.boot:spring-boot-autoconfigure:3.1.2 -> 2.5.15
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1 -> 2.6.4
|    +--- org.hibernate.orm:hibernate-core:6.2.6.Final
|    +--- org.springframework:spring-aspects:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-orm:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-jdbc:6.0.11 -> 5.3.27
|    +--- io.micrometer:micrometer-core:1.11.2 -> 1.7.12
|    +--- org.springframework:spring-webmvc:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-web:6.0.11 -> 5.3.27
|    +--- io.micrometer:micrometer-observation:1.11.2
|    +--- org.springframework.boot:spring-boot-starter-tomcat:3.1.2 -> 2.5.15
|    +--- org.webjars:swagger-ui:5.2.0
|    +--- com.google.code.gson:gson:2.10.1 -> 2.8.9
|    +--- com.google.guava:guava:28.0-jre -> 32.0.1-jre
|    +--- commons-io:commons-io:2.11.0
|    +--- com.sun.activation:jakarta.activation:2.0.1 -> 1.2.2
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-codec:commons-codec:1.15
|    +--- org.springframework.boot:spring-boot-actuator:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:3.1.2 -> 2.5.15
|    +--- org.springframework:spring-context:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-aop:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-tx:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-beans:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-expression:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-core:6.0.11 -> 5.3.27
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
|    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.6.1 -> 2.6.4
|    +--- org.aspectj:aspectjweaver:1.9.19 -> 1.9.7
|    +--- jakarta.persistence:jakarta.persistence-api:3.1.0 -> 2.2.3
|    +--- jakarta.transaction:jakarta.transaction-api:2.0.1 -> 1.3.3
|    +--- org.jboss.logging:jboss-logging:3.5.3.Final -> 3.4.3.Final
|    +--- org.hibernate.common:hibernate-commons-annotations:6.0.6.Final
|    +--- io.smallrye:jandex:3.0.5
|    +--- com.fasterxml:classmate:1.5.1
|    +--- net.bytebuddy:byte-buddy:1.14.5 -> 1.10.22
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.3 -> 2.3.8
|    +--- org.glassfish.jaxb:jaxb-core:4.0.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 2.3.3
|    +--- jakarta.inject:jakarta.inject-api:2.0.1
|    +--- org.antlr:antlr4-runtime:4.10.1
|    +--- jakarta.annotation:jakarta.annotation-api:2.1.1 -> 1.3.5
|    +--- io.micrometer:micrometer-commons:1.11.2
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.11 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.11 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.11 -> 9.0.75
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.errorprone:error_prone_annotations:2.3.2 -> 2.21.1
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.codehaus.mojo:animal-sniffer-annotations:1.17
|    +--- org.springframework:spring-jcl:6.0.11 -> 5.3.27
|    +--- org.eclipse.angus:angus-activation:2.0.1
|    +--- jakarta.activation:jakarta.activation-api:2.1.2 -> 1.2.2
|    +--- org.glassfish.jaxb:txw2:4.0.3 -> 2.3.8
|    +--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- io.swagger.core.v3:swagger-annotations-jakarta:2.2.15
|    \--- jakarta.validation:jakarta.validation-api:3.0.2 -> 2.0.2
+--- com.github.hmcts.rse-cft-lib:ccd-data-store-api:0.19.842
|    +--- pl.jalokim.propertiestojson:java-properties-to-json:5.1.3
|    +--- io.searchbox:jest:6.3.1
|    +--- io.searchbox:jest-common:6.3.1
|    +--- com.google.code.gson:gson:2.8.9
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.3 -> 3.0.6
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
|    +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
|    +--- com.github.hmcts:ccd-case-document-am-client:1.7.1
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.4.1 -> 2.6.4
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-hateoas:2.7.11 -> 2.5.15
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- org.springframework.hateoas:spring-hateoas:1.5.4 -> 1.3.7
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.retry:spring-retry:1.3.1 -> 1.3.4
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- com.github.ben-manes.caffeine:caffeine:3.1.6 -> 2.9.3
|    +--- javax.inject:javax.inject:1
|    +--- pl.jalokim.utils:java-utils:1.1.1
|    +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- org.flywaydb:flyway-core:8.5.13 -> 7.7.3
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-validation:2.7.11 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.elasticsearch:elasticsearch:7.16.2 -> 7.12.1
|    +--- org.elasticsearch:elasticsearch-x-content:7.16.2 -> 7.17.1
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- com.jayway.jsonpath:json-path:2.4.0 -> 2.5.0
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- io.vavr:vavr:0.10.4
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- com.sun.mail:mailapi:1.6.1 -> 2.0.1
|    +--- commons-lang:commons-lang:2.6
|    +--- commons-validator:commons-validator:1.6
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.awaitility:awaitility:3.1.6 -> 4.0.3
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- com.vladmihalcea:hibernate-types-52:2.9.13 -> 2.16.3
|    +--- org.hibernate:hibernate-core:5.6.10.Final -> 5.4.33
|    +--- org.apache.commons:commons-jexl3:3.1
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.jooq:jool-java-8:0.9.14
|    +--- org.postgresql:postgresql:42.5.1 -> 42.2.27
|    +--- com.zaxxer:HikariCP:4.0.2 -> 4.0.3
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework:spring-web:5.3.27
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- io.github.openfeign:feign-jackson:11.8 -> 10.12
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- com.microsoft.azure:applicationinsights-core:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.4.1 -> 2.6.4
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- org.springframework.data:spring-data-jpa:2.7.11 -> 2.5.12
|    +--- org.springframework:spring-aspects:5.3.27
|    +--- org.springframework:spring-orm:5.3.27
|    +--- org.springframework:spring-jdbc:5.3.27
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- org.reflections:reflections:0.9.11
|    +--- com.google.guava:guava:30.1-jre -> 32.0.1-jre
|    +--- org.checkerframework:checker-qual:3.33.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.18.0 -> 2.21.1
|    +--- org.apache.httpcomponents:httpcore-nio:4.4.16
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- org.apache.httpcomponents:httpasyncclient:4.1.5
|    +--- commons-logging:commons-logging:1.2
|    +--- com.auth0:java-jwt:3.12.0
|    +--- commons-codec:commons-codec:1.15
|    +--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.springframework.data:spring-data-commons:2.7.11 -> 2.5.12
|    +--- io.swagger:swagger-models:1.5.20
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.springframework:spring-tx:5.3.27
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- io.vavr:vavr-match:0.10.4
|    +--- javax.activation:activation:1.1
|    +--- commons-digester:commons-digester:1.8.1
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.hamcrest:hamcrest-library:2.2
|    +--- org.hamcrest:hamcrest-core:2.2
|    +--- org.objenesis:objenesis:2.6
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- javax.persistence:javax.persistence-api:2.2
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    +--- javax.activation:javax.activation-api:1.2.0
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- org.projectlombok:lombok:1.18.26
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- org.elasticsearch:elasticsearch-lz4:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-cli:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-core:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-secure-sm:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-geo:7.16.2 -> 7.17.1
|    +--- org.apache.lucene:lucene-core:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-analyzers-common:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-backward-codecs:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-grouping:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-highlighter:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-join:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-memory:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-misc:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-queries:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-queryparser:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-sandbox:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-spatial3d:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-suggest:8.10.1 -> 8.11.1
|    +--- com.carrotsearch:hppc:0.8.1
|    +--- joda-time:joda-time:2.10.10
|    +--- com.tdunning:t-digest:3.2
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- net.java.dev.jna:jna:5.10.0
|    +--- org.elasticsearch:elasticsearch-plugin-classloader:7.16.2 -> 7.17.1
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- org.hamcrest:hamcrest:2.2
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.codehaus.groovy:groovy-all:2.4.15
|    +--- jakarta.validation:jakarta.validation-api:2.0.2
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.14.1 -> 2.12.7
|    +--- org.lz4:lz4-java:1.8.0
|    +--- net.sf.jopt-simple:jopt-simple:5.0.2
|    +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
|    \--- org.javassist:javassist:3.21.0-GA
+--- com.github.hmcts.rse-cft-lib:am-role-assignment-service:0.19.842
|    +--- com.github.hmcts:properties-volume-spring-boot-starter:0.1.1
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.16 -> 2.5.15
|    +--- com.github.hmcts:idam-java-client:2.1.1
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.2 -> 4.0.3
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.16 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.10.RELEASE -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:2.2.10.RELEASE -> 3.0.7
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-security:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.16 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.10 -> 5.5.8
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-context:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.7 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.10 -> 5.5.8
|    +--- org.springframework.retry:spring-retry:2.0.2 -> 1.3.4
|    +--- org.drools:drools-decisiontables:7.73.0.Final
|    +--- org.apache.poi:poi-ooxml:5.2.4
|    +--- org.apache.poi:poi-scratchpad:5.2.3
|    +--- org.apache.poi:poi:5.2.4
|    +--- org.springframework:spring-context-support:5.3.29 -> 5.3.27
|    +--- org.springdoc:springdoc-openapi-ui:1.6.8
|    +--- org.springdoc:springdoc-openapi-webmvc-core:1.6.8
|    +--- org.springframework:spring-webmvc:5.3.29 -> 5.3.27
|    +--- org.springframework.data:spring-data-jpa:2.7.16 -> 2.5.12
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.16 -> 2.5.15
|    +--- org.springdoc:springdoc-openapi-common:1.6.8
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.16 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-orm:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-jdbc:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-tx:5.3.29 -> 5.3.27
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- org.springframework:spring-web:5.3.29 -> 5.3.27
|    +--- org.springframework.data:spring-data-commons:2.7.16 -> 2.5.12
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-aspects:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-jcl:5.3.29 -> 5.3.27
|    +--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.kie:kie-ci:7.73.0.Final
|    +--- org.drools:drools-templates:7.73.0.Final
|    +--- org.drools:drools-serialization-protobuf:7.73.0.Final
|    +--- org.drools:drools-mvel:7.73.0.Final
|    +--- org.drools:drools-compiler:7.73.0.Final
|    +--- org.drools:drools-ecj:7.73.0.Final
|    +--- org.drools:drools-core:7.73.0.Final
|    +--- org.kie.soup:kie-soup-maven-integration:7.73.0.Final
|    +--- org.apache.maven:maven-compat:3.3.9
|    +--- org.apache.maven:maven-core:3.8.7
|    +--- org.flywaydb:flyway-core:8.5.12 -> 7.7.3
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    +--- com.google.inject:guice:4.2.2
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    +--- io.github.openfeign:feign-hystrix:10.12
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    +--- com.netflix.archaius:archaius-core:0.7.7
|    +--- com.google.guava:guava:32.0.1-jre
|    +--- org.apache.maven:maven-settings-builder:3.8.7
|    +--- org.apache.maven:maven-aether-provider:3.3.9
|    +--- org.apache.maven:maven-resolver-provider:3.8.7
|    +--- org.apache.maven:maven-model-builder:3.8.7
|    +--- org.codehaus.plexus:plexus-sec-dispatcher:2.0
|    +--- org.codehaus.plexus:plexus-cipher:2.0
|    +--- javax.inject:javax.inject:1
|    +--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.16 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 -> 9.0.75
|    +--- org.hibernate:hibernate-core:5.6.15.Final -> 5.4.33
|    +--- com.github.ben-manes.caffeine:caffeine:3.1.8 -> 2.9.3
|    +--- org.postgresql:postgresql:42.6.0 -> 42.2.27
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- com.nimbusds:nimbus-jose-jwt:9.25 -> 9.10.1
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.maven.wagon:wagon-http:3.0.0
|    +--- org.apache.maven.wagon:wagon-http-shared:3.0.0
|    +--- commons-io:commons-io:2.11.0
|    +--- org.apache.commons:commons-compress:1.24.0
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.json:json:20230227
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- io.swagger.core.v3:swagger-core:2.2.0
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.5 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.2 -> 2.12.7
|    +--- org.webjars:webjars-locator-core:0.50 -> 0.46
|    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.2 -> 2.12.7
|    +--- com.fasterxml.jackson:jackson-bom:2.14.2
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.5 -> 2.12.7
|    +--- io.github.openfeign:feign-jackson:11.9.1 -> 10.12
|    +--- com.auth0:java-jwt:3.12.0
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- com.thoughtworks.xstream:xstream:1.4.20
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4
|    +--- org.pitest:pitest-junit5-plugin:0.16
|    +--- jakarta.enterprise:jakarta.enterprise.cdi-api:2.0.2
|    +--- jakarta.interceptor:jakarta.interceptor-api:1.2.5
|    +--- jakarta.ejb:jakarta.ejb-api:3.2.6
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- io.micrometer:micrometer-core:1.9.15 -> 1.7.12
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    +--- io.reactivex:rxjava-reactive-streams:1.2.1
|    +--- io.github.openfeign:feign-slf4j:10.12
|    +--- io.github.openfeign:feign-httpclient:11.9.1 -> 10.12
|    +--- io.github.openfeign:feign-core:11.9.1 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.eclipse.aether:aether-transport-http:1.1.0
|    +--- org.apache.httpcomponents:httpclient:4.5.14
|    +--- commons-codec:commons-codec:1.15
|    +--- org.apache.commons:commons-collections4:4.4
|    +--- org.apache.commons:commons-math3:3.6.1
|    +--- com.zaxxer:SparseBitSet:1.3
|    +--- org.apache.poi:poi-ooxml-lite:5.2.4
|    +--- org.apache.xmlbeans:xmlbeans:5.1.1
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.16 -> 2.5.15
|    +--- org.apache.logging.log4j:log4j-to-slf4j:2.19.0 -> 2.17.2
|    +--- org.apache.logging.log4j:log4j-api:2.19.0 -> 2.17.2
|    +--- com.github.virtuald:curvesapi:1.08
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.webjars:swagger-ui:4.10.3 -> 5.2.0
|    +--- io.github.classgraph:classgraph:4.8.143
|    +--- org.drools:drools-core-dynamic:7.73.0.Final
|    +--- org.drools:drools-core-reflective:7.73.0.Final
|    +--- org.kie:kie-internal:7.73.0.Final
|    +--- org.kie:kie-api:7.73.0.Final
|    +--- org.kie.soup:kie-soup-xstream:7.73.0.Final
|    +--- org.kie.soup:kie-soup-maven-support:7.73.0.Final
|    +--- org.slf4j:jcl-over-slf4j:1.7.36
|    +--- org.apache.maven.resolver:maven-resolver-impl:1.6.3
|    +--- com.zaxxer:HikariCP:4.0.3
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.kie.soup:kie-soup-project-datamodel-commons:7.73.0.Final
|    +--- org.kie.soup:kie-soup-project-datamodel-api:7.73.0.Final
|    +--- org.kie.soup:kie-soup-commons:7.73.0.Final
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.kie:kie-memory-compiler:7.73.0.Final
|    +--- org.antlr:antlr-runtime:3.5.2
|    +--- org.apache.maven:maven-plugin-api:3.8.7
|    +--- org.apache.maven:maven-model:3.8.7
|    +--- org.apache.maven:maven-settings:3.8.7
|    +--- org.eclipse.aether:aether-transport-wagon:1.1.0
|    +--- org.apache.maven.wagon:wagon-provider-api:3.0.0
|    +--- org.sonatype.plexus:plexus-sec-dispatcher:1.3
|    +--- org.eclipse.sisu:org.eclipse.sisu.plexus:0.3.5
|    +--- org.codehaus.plexus:plexus-classworlds:2.6.0
|    +--- org.apache.maven:maven-repository-metadata:3.8.7
|    +--- org.apache.maven:maven-artifact:3.8.7
|    +--- org.codehaus.plexus:plexus-utils:3.4.1
|    +--- org.eclipse.aether:aether-impl:1.1.0
|    +--- org.eclipse.aether:aether-connector-basic:1.1.0
|    +--- org.eclipse.aether:aether-transport-file:1.1.0
|    +--- org.eclipse.aether:aether-util:1.1.0
|    +--- org.eclipse.aether:aether-spi:1.1.0
|    +--- org.eclipse.aether:aether-api:1.1.0
|    +--- org.sonatype.plexus:plexus-cipher:1.7
|    +--- org.apache.maven:maven-builder-support:3.8.7
|    +--- org.apache.maven.resolver:maven-resolver-spi:1.6.3
|    +--- org.apache.maven.resolver:maven-resolver-util:1.6.3
|    +--- org.apache.maven.resolver:maven-resolver-api:1.6.3
|    +--- org.apache.maven.shared:maven-shared-utils:3.3.4
|    +--- org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5
|    +--- org.codehaus.plexus:plexus-interpolation:1.26
|    +--- org.codehaus.plexus:plexus-component-annotations:2.1.0
|    +--- org.apache.commons:commons-lang3:3.12.0
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- org.checkerframework:checker-qual:3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.21.1
|    +--- com.google.j2objc:j2objc-annotations:2.8
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.80 -> 9.0.75
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- javax.persistence:javax.persistence-api:2.2
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    +--- javax.activation:javax.activation-api:1.2.0
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- commons-configuration:commons-configuration:1.8
|    +--- commons-logging:commons-logging:1.2
|    +--- commons-collections:commons-collections:3.2.2
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- com.microsoft.azure:applicationinsights-web:2.6.4
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:2.0 -> 1.28
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.80 -> 9.0.75
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- com.nimbusds:content-type:2.2
|    +--- net.minidev:json-smart:2.4.11 -> 2.4.10
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- io.reactivex:rxjava:1.3.8
|    +--- net.minidev:accessors-smart:2.4.11
|    +--- org.ow2.asm:asm:9.3
|    +--- org.reactivestreams:reactive-streams:1.0.4
|    +--- org.mvel:mvel2:2.4.14.Final
|    +--- jakarta.inject:jakarta.inject-api:1.0.3 -> 2.0.1
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- javax.annotation:javax.annotation-api:1.3.2
|    +--- aopalliance:aopalliance:1.0
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- commons-lang:commons-lang:2.6
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- io.swagger.core.v3:swagger-annotations:2.2.0
|    \--- jakarta.validation:jakarta.validation-api:2.0.2
\--- com.github.hmcts.rse-cft-lib:aac-manage-case-assignment:0.19.842
     +--- pl.jalokim.propertiestojson:java-properties-to-json:5.1.3
     +--- com.google.code.gson:gson:2.8.9
     +--- com.vladmihalcea:hibernate-types-52:2.9.13 -> 2.16.3
     +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
     +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
     +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
     +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
     +--- io.github.openfeign.form:feign-form-spring:3.8.0
     +--- commons-fileupload:commons-fileupload:1.5
     +--- uk.gov.service.notify:notifications-java-client:3.15.1-RELEASE
     +--- pl.jalokim.utils:java-utils:1.1.1
     +--- org.springframework.cloud:spring-cloud-starter-netflix-zuul:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-netflix-zuul:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
     +--- com.netflix.hystrix:hystrix-javanica:1.5.18
     +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
     +--- com.netflix.zuul:zuul-core:1.3.1
     +--- commons-io:commons-io:2.8.0 -> 2.11.0
     +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.0.1 -> 3.0.6
     +--- org.springframework.boot:spring-boot-starter-hateoas:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-web:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-validation:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-actuator:2.6.10 -> 2.5.15
     +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
     +--- org.springframework.boot:spring-boot-starter-aop:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-json:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-cache:2.6.10 -> 2.5.15
     +--- org.springframework.retry:spring-retry:1.3.1 -> 1.3.4
     +--- io.springfox:springfox-boot-starter:3.0.0
     +--- org.projectlombok:lombok:1.18.28 -> 1.18.26
     +--- com.github.hmcts.java-logging:logging:5.1.9 -> 6.0.1
     +--- com.github.hmcts.java-logging:logging-appinsights:5.1.9 -> 6.0.1
     +--- io.jsonwebtoken:jjwt:0.9.1
     +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.5.14 -> 2.5.15
     +--- org.springframework.hateoas:spring-hateoas:1.4.4 -> 1.3.7
     +--- com.jayway.jsonpath:json-path:2.6.0 -> 2.5.0
     +--- org.springframework.security:spring-security-oauth2-client:5.6.9 -> 5.5.8
     +--- com.nimbusds:oauth2-oidc-sdk:9.19 -> 9.9.1
     +--- net.minidev:json-smart:2.4.7 -> 2.4.10
     +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
     +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
     +--- org.springframework.security:spring-security-oauth2-resource-server:5.6.9 -> 5.5.8
     +--- org.springframework.security:spring-security-web:5.6.9 -> 5.5.8
     +--- org.springframework.security:spring-security-config:5.6.9 -> 5.5.8
     +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
     +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
     +--- com.warrenstrange:googleauth:1.5.0
     +--- org.springframework.cloud:spring-cloud-starter-netflix-ribbon:2.2.10.RELEASE
     +--- com.netflix.ribbon:ribbon-httpclient:2.3.0
     +--- com.sun.jersey.contribs:jersey-apache-client4:1.19.1
     +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
     +--- com.netflix.ribbon:ribbon:2.3.0
     +--- com.netflix.ribbon:ribbon-transport:2.3.0
     +--- com.netflix.ribbon:ribbon-loadbalancer:2.3.0
     +--- com.netflix.netflix-commons:netflix-commons-util:0.3.0
     +--- javax.inject:javax.inject:1
     +--- org.modelmapper:modelmapper:2.3.7
     +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
     +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
     +--- org.springframework.boot:spring-boot-starter:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-logging:2.6.10 -> 2.5.15
     +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
     +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
     +--- org.springframework.security:spring-security-oauth2-jose:5.6.9 -> 5.5.8
     +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
     +--- org.projectlombok:lombok-mapstruct-binding:0.2.0
     +--- org.reflections:reflections:0.9.11
     +--- com.netflix.ribbon:ribbon-core:2.3.0
     +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
     +--- com.netflix.hystrix:hystrix-serialization:1.5.18
     +--- com.netflix.hystrix:hystrix-core:1.5.18
     +--- com.netflix.archaius:archaius-core:0.7.7
     +--- io.reactivex:rxnetty-servo:0.4.9
     +--- com.netflix.servo:servo-core:0.12.5
     +--- com.google.guava:guava:30.0-jre -> 32.0.1-jre
     +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.6.10 -> 2.5.15
     +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
     +--- net.logstash.logback:logstash-logback-encoder:6.6
     +--- io.github.openfeign:feign-jackson:11.8 -> 10.12
     +--- com.auth0:java-jwt:3.12.0
     +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
     +--- io.springfox:springfox-oas:3.0.0
     +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
     +--- io.springfox:springfox-swagger2:3.0.0
     +--- io.springfox:springfox-swagger-common:3.0.0
     +--- io.swagger:swagger-models:1.5.20
     +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
     +--- org.springframework:spring-webmvc:5.3.20 -> 5.3.27
     +--- org.springframework.security:spring-security-oauth2-core:5.6.9 -> 5.5.8
     +--- org.springframework:spring-web:5.3.20 -> 5.3.27
     +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
     +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
     +--- io.github.openfeign:feign-core:11.8 -> 10.12
     +--- org.springframework.boot:spring-boot-starter-tomcat:2.6.10 -> 2.5.15
     +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
     +--- org.hibernate.validator:hibernate-validator:6.2.3.Final -> 6.2.5.Final
     +--- io.micrometer:micrometer-core:1.8.8 -> 1.7.12
     +--- io.springfox:springfox-data-rest:3.0.0
     +--- io.springfox:springfox-bean-validators:3.0.0
     +--- io.springfox:springfox-swagger-ui:3.0.0
     +--- io.springfox:springfox-spring-webmvc:3.0.0
     +--- io.springfox:springfox-spring-webflux:3.0.0
     +--- io.springfox:springfox-spring-web:3.0.0
     +--- io.springfox:springfox-schema:3.0.0
     +--- io.springfox:springfox-spi:3.0.0
     +--- io.springfox:springfox-core:3.0.0
     +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
     +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
     +--- org.springframework.security:spring-security-core:5.6.9 -> 5.5.8
     +--- org.springframework:spring-context-support:5.3.20 -> 5.3.27
     +--- org.springframework.boot:spring-boot-autoconfigure:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-actuator:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot:2.6.10 -> 2.5.15
     +--- org.springframework:spring-context:5.3.20 -> 5.3.27
     +--- org.springframework:spring-aop:5.3.20 -> 5.3.27
     +--- org.aspectj:aspectjweaver:1.9.7
     +--- com.fasterxml:classmate:1.5.1
     +--- org.slf4j:jul-to-slf4j:1.7.36
     +--- io.github.openfeign.form:feign-form:3.8.0
     +--- org.bitbucket.b_c:jose4j:0.6.5
     +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.1 -> 2.17.2
     +--- io.reactivex:rxnetty-contexts:0.4.9
     +--- io.reactivex:rxnetty:0.4.9
     +--- com.netflix.netflix-commons:netflix-statistics:0.1.1
     +--- org.slf4j:slf4j-api:1.7.36
     +--- javax.servlet:javax.servlet-api:4.0.1
     +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1 -> 2.6.4
     +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
     +--- jakarta.el:jakarta.el-api:4.0.0
     +--- org.springframework:spring-beans:5.3.20 -> 5.3.27
     +--- org.springframework:spring-expression:5.3.20 -> 5.3.27
     +--- org.springframework:spring-core:5.3.20 -> 5.3.27
     +--- org.checkerframework:checker-qual:3.5.0 -> 3.37.0
     +--- com.google.errorprone:error_prone_annotations:2.3.4 -> 2.21.1
     +--- org.apache.httpcomponents:httpcore:4.4.15 -> 4.4.16
     +--- commons-codec:commons-codec:1.15
     +--- commons-cli:commons-cli:1.4
     +--- org.json:json:20180813 -> 20230227
     +--- joda-time:joda-time:2.10.1 -> 2.10.10
     +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
     +--- com.google.guava:failureaccess:1.0.1
     +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     +--- com.google.code.findbugs:jsr305:3.0.2
     +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
     +--- org.codehaus.groovy:groovy-all:2.4.15
     +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
     +--- jakarta.activation:jakarta.activation-api:1.2.2
     +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
     +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
     +--- org.springframework.security:spring-security-crypto:5.6.9 -> 5.5.8
     +--- jakarta.annotation:jakarta.annotation-api:1.3.5
     +--- org.yaml:snakeyaml:1.32 -> 1.28
     +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
     +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
     +--- jakarta.validation:jakarta.validation-api:2.0.2
     +--- org.jboss.logging:jboss-logging:3.4.3.Final
     +--- org.hdrhistogram:HdrHistogram:2.1.12
     +--- org.latencyutils:LatencyUtils:2.0.3
     +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
     +--- org.mapstruct:mapstruct:1.3.1.Final
     +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
     +--- com.microsoft.azure:applicationinsights-core:2.6.1
     +--- com.microsoft.azure:applicationinsights-web:2.6.1 -> 2.6.4
     +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
     +--- io.reactivex:rxjava-reactive-streams:1.2.1
     +--- io.reactivex:rxjava:1.3.8
     +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
     +--- commons-configuration:commons-configuration:1.8
     +--- org.ow2.asm:asm:9.1 -> 9.3
     +--- org.springframework:spring-jcl:5.3.20 -> 5.3.27
     +--- org.javassist:javassist:3.21.0-GA
     +--- org.bouncycastle:bcpkix-jdk15on:1.70
     +--- net.bytebuddy:byte-buddy:1.11.22 -> 1.10.22
     +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
     +--- org.reactivestreams:reactive-streams:1.0.4
     +--- commons-lang:commons-lang:2.6
     +--- commons-collections:commons-collections:3.2.2
     +--- com.sun.jersey:jersey-client:1.19.1
     +--- com.nimbusds:content-type:2.1 -> 2.2
     +--- com.nimbusds:lang-tag:1.5 -> 1.6
     +--- org.bouncycastle:bcutil-jdk15on:1.70
     +--- org.bouncycastle:bcprov-jdk15on:1.70
     +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
     +--- com.sun.jersey:jersey-core:1.19.1
     \--- javax.ws.rs:jsr311-api:1.1.1

cftlibIDECompileOnly - Compile only dependencies for source set 'cftlib ide'. (n)
No dependencies

cftlibIDEImplementation - Implementation only dependencies for source set 'cftlib ide'. (n)
+--- com.github.hmcts.rse-cft-lib:application:0.19.842 (n)
+--- com.github.hmcts.rse-cft-lib:ccd-case-document-am-api:0.19.842 (n)
+--- com.github.hmcts.rse-cft-lib:user-profile-api:0.19.842 (n)
+--- com.github.hmcts.rse-cft-lib:ccd-data-store-api:0.19.842 (n)
+--- com.github.hmcts.rse-cft-lib:am-role-assignment-service:0.19.842 (n)
\--- com.github.hmcts.rse-cft-lib:aac-manage-case-assignment:0.19.842 (n)

cftlibIDERuntimeClasspath - Runtime classpath of source set 'cftlib ide'.
+--- com.github.hmcts.rse-cft-lib:application:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:excel-importer:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:rest-api:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:elastic-search-support:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:domain:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:app-insights:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:repository:0.19.842
|    +--- com.github.hmcts.rse-cft-lib:commons:0.19.842
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.7.11 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
|    +--- io.github.openfeign:feign-httpclient:11.6 -> 10.12
|    +--- com.github.hmcts:befta-fw:8.7.11
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.1 -> 7.12.1
|    +--- org.elasticsearch.client:elasticsearch-rest-client:7.17.1 -> 7.12.1
|    +--- io.rest-assured:rest-assured:4.5.1 -> 4.3.3
|    +--- org.apache.httpcomponents:httpmime:4.5.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.poi:poi-ooxml:5.2.2 -> 5.2.4
|    +--- org.apache.commons:commons-compress:1.21 -> 1.24.0
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.4.1 -> 2.6.1
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.11 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.apache.poi:poi-scratchpad:5.2.2 -> 5.2.3
|    +--- org.apache.poi:poi:5.2.2 -> 5.2.4
|    +--- org.apache.commons:commons-collections4:4.4
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- com.google.code.gson:gson:2.9.0 -> 2.8.9
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.4.1 -> 2.6.4
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- com.zaxxer:HikariCP:4.0.2 -> 4.0.3
|    +--- org.jooq:jool-java-8:0.9.14
|    +--- org.elasticsearch:elasticsearch:7.17.1 -> 7.12.1
|    +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
|    +--- org.flywaydb:flyway-core:6.5.7 -> 7.7.3
|    +--- javax.inject:javax.inject:1
|    +--- com.microsoft.azure:azure-storage:8.0.0
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- io.cucumber:cucumber-junit:5.7.0
|    +--- junit:junit:4.13.1 -> 4.13.2
|    +--- com.github.rholder:guava-retrying:2.0.0
|    +--- com.google.guava:guava:31.1-jre -> 32.0.1-jre
|    +--- org.hibernate.validator:hibernate-validator:6.0.20.Final -> 6.2.5.Final
|    +--- javax.validation:validation-api:2.0.1.Final
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.swagger:swagger-models:1.5.20
|    +--- io.swagger:swagger-annotations:1.6.6
|    +--- com.vladmihalcea:hibernate-types-52:2.16.3
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- commons-validator:commons-validator:1.6
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.postgresql:postgresql:42.5.1 -> 42.2.27
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.74 -> 9.0.75
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-logging:commons-logging:1.2
|    +--- com.auth0:java-jwt:3.12.0
|    +--- commons-codec:commons-codec:1.15
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.springframework.data:spring-data-jpa:2.7.11 -> 2.5.12
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.data:spring-data-commons:2.7.11 -> 2.5.12
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.apache.commons:commons-math3:3.6.1
|    +--- com.zaxxer:SparseBitSet:1.2 -> 1.3
|    +--- org.apache.poi:poi-ooxml-lite:5.2.2 -> 5.2.4
|    +--- org.apache.xmlbeans:xmlbeans:5.0.3 -> 5.1.1
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- com.github.virtuald:curvesapi:1.07 -> 1.08
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- com.microsoft.azure:applicationinsights-core:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.4.1 -> 2.6.4
|    +--- io.github.openfeign:feign-jackson:11.6 -> 10.12
|    +--- org.elasticsearch:elasticsearch-x-content:7.17.1
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.springframework:spring-web:5.3.27
|    +--- org.springframework:spring-orm:5.3.27
|    +--- org.springframework:spring-jdbc:5.3.27
|    +--- org.springframework:spring-tx:5.3.27
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- org.hibernate:hibernate-core:5.6.15.Final -> 5.4.33
|    +--- org.springframework:spring-aspects:5.3.27
|    +--- org.elasticsearch:elasticsearch-lz4:7.17.1
|    +--- org.elasticsearch:elasticsearch-cli:7.17.1
|    +--- org.elasticsearch:elasticsearch-core:7.17.1
|    +--- org.elasticsearch:elasticsearch-secure-sm:7.17.1
|    +--- org.elasticsearch:elasticsearch-geo:7.17.1
|    +--- org.apache.lucene:lucene-core:8.11.1
|    +--- org.apache.lucene:lucene-analyzers-common:8.11.1
|    +--- org.apache.lucene:lucene-backward-codecs:8.11.1
|    +--- org.apache.lucene:lucene-grouping:8.11.1
|    +--- org.apache.lucene:lucene-highlighter:8.11.1
|    +--- org.apache.lucene:lucene-join:8.11.1
|    +--- org.apache.lucene:lucene-memory:8.11.1
|    +--- org.apache.lucene:lucene-misc:8.11.1
|    +--- org.apache.lucene:lucene-queries:8.11.1
|    +--- org.apache.lucene:lucene-queryparser:8.11.1
|    +--- org.apache.lucene:lucene-sandbox:8.11.1
|    +--- org.apache.lucene:lucene-spatial3d:8.11.1
|    +--- org.apache.lucene:lucene-suggest:8.11.1
|    +--- com.carrotsearch:hppc:0.8.1
|    +--- joda-time:joda-time:2.10.10
|    +--- com.tdunning:t-digest:3.2
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- net.java.dev.jna:jna:5.10.0
|    +--- org.elasticsearch:elasticsearch-plugin-classloader:7.17.1
|    +--- org.elasticsearch.plugin:mapper-extras-client:7.17.1
|    +--- org.elasticsearch.plugin:parent-join-client:7.17.1
|    +--- org.elasticsearch.plugin:aggs-matrix-stats-client:7.17.1
|    +--- org.elasticsearch.plugin:rank-eval-client:7.17.1
|    +--- org.elasticsearch.plugin:lang-mustache-client:7.17.1
|    +--- org.checkerframework:checker-qual:3.12.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.11.0 -> 2.21.1
|    +--- com.microsoft.azure:azure-keyvault-core:1.0.0
|    +--- io.rest-assured:xml-path:4.5.1 -> 4.3.3
|    +--- io.rest-assured:json-path:4.5.1 -> 4.3.3
|    +--- io.rest-assured:rest-assured-common:4.5.1
|    +--- org.apache.commons:commons-lang3:3.12.0
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- org.projectlombok:lombok:1.18.26
|    +--- org.json:json:20200518 -> 20230227
|    +--- io.cucumber:cucumber-java:5.7.0
|    +--- org.hamcrest:hamcrest-core:2.2
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.glassfish:javax.el:3.0.0
|    +--- org.mapstruct:mapstruct-processor:1.3.0.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.14.1 -> 2.12.7
|    +--- org.lz4:lz4-java:1.8.0
|    +--- net.sf.jopt-simple:jopt-simple:5.0.2
|    +--- org.apache.httpcomponents:httpasyncclient:4.1.5
|    +--- org.apache.httpcomponents:httpcore-nio:4.4.16
|    +--- com.github.spullara.mustache.java:compiler:0.9.6
|    +--- org.codehaus.groovy:groovy-xml:3.0.17
|    +--- org.codehaus.groovy:groovy-json:3.0.17
|    +--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    +--- io.cucumber:cucumber-core:5.7.0
|    +--- io.cucumber:cucumber-expressions:8.3.1
|    +--- io.cucumber:datatable:3.3.1
|    +--- io.cucumber:cucumber-gherkin-vintage:5.7.0
|    +--- io.cucumber:cucumber-gherkin:5.7.0
|    +--- io.cucumber:cucumber-plugin:5.7.0
|    +--- io.cucumber:docstring:5.7.0
|    +--- org.apiguardian:apiguardian-api:1.1.0
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- commons-digester:commons-digester:1.8.1
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- io.cucumber:tag-expressions:2.0.4
|    \--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
+--- com.github.hmcts.rse-cft-lib:ccd-case-document-am-api:0.19.842
|    +--- com.github.hmcts:service-auth-provider-java-client:3.1.4 -> 4.0.3
|    +--- com.github.hmcts:idam-java-client:1.5.5 -> 2.1.1
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- org.springframework.boot:spring-boot-starter-hateoas:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-validation:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.json:json:20200518 -> 20230227
|    +--- org.projectlombok:lombok:1.18.20 -> 1.18.26
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- org.springframework.retry:spring-retry:1.3.4
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.5.14 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- javax.inject:javax.inject:1
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- org.springframework.hateoas:spring-hateoas:1.5.4 -> 1.3.7
|    +--- com.jayway.jsonpath:json-path:2.7.0 -> 2.5.0
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- io.vavr:vavr:0.10.4
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- org.springframework:spring-web:5.3.27
|    +--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- io.swagger:swagger-models:1.5.20
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- io.github.openfeign:feign-jackson:10.12
|    +--- com.auth0:java-jwt:3.12.0
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- org.checkerframework:checker-qual:2.6.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.3.3 -> 2.21.1
|    +--- commons-logging:commons-logging:1.2
|    +--- commons-collections:commons-collections:3.2.2
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-codec:commons-codec:1.15
|    +--- com.fasterxml:classmate:1.5.1
|    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.1 -> 2.17.2
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- io.vavr:vavr-match:0.10.4
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.74 -> 9.0.75
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- com.microsoft.azure:applicationinsights-web:2.6.4
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- jakarta.validation:jakarta.validation-api:2.0.2
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    \--- org.bouncycastle:bcprov-jdk15on:1.70
+--- com.github.hmcts.rse-cft-lib:user-profile-api:0.19.842
|    +--- org.projectlombok:lombok:1.18.28 -> 1.18.26
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.github.hmcts.java-logging:logging-appinsights:6.0.1
|    +--- org.slf4j:slf4j-simple:2.0.7 -> 1.7.36
|    +--- org.slf4j:jcl-over-slf4j:2.0.7 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-jdbc:3.1.2 -> 2.5.15
|    +--- com.zaxxer:HikariCP:5.0.1 -> 4.0.3
|    +--- org.springframework.data:spring-data-jpa:3.1.2 -> 2.5.12
|    +--- org.springframework.data:spring-data-commons:3.1.2 -> 2.5.12
|    +--- org.springframework.boot:spring-boot-starter-actuator:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-aop:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:3.1.2 -> 2.5.15
|    +--- com.github.hmcts:auth-checker-lib:2.1.5
|    +--- org.springframework.boot:spring-boot-starter-security:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-logging:3.1.2 -> 2.5.15
|    +--- org.slf4j:jul-to-slf4j:2.0.7 -> 1.7.36
|    +--- org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0
|    +--- org.springdoc:springdoc-openapi-starter-webmvc-api:2.2.0
|    +--- org.springdoc:springdoc-openapi-starter-common:2.2.0
|    +--- io.swagger.core.v3:swagger-core-jakarta:2.2.15
|    +--- org.slf4j:slf4j-api:2.0.7 -> 1.7.36
|    +--- org.flywaydb:flyway-core:9.21.1 -> 7.7.3
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.1 -> 2.12.7
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:3.1.2 -> 2.5.15
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models-jakarta:2.2.15
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- org.yaml:snakeyaml:2.1 -> 1.28
|    +--- javax.inject:javax.inject:1
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.commons:commons-lang3:3.13.0 -> 3.12.0
|    +--- com.sun.mail:mailapi:2.0.1
|    +--- org.apache.httpcomponents:httpclient:4.5.14
|    +--- net.jcip:jcip-annotations:1.0
|    +--- org.springframework.security:spring-security-config:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:6.1.2 -> 5.5.8
|    +--- org.springframework.security:spring-security-crypto:6.1.2 -> 5.5.8
|    +--- org.glassfish:jakarta.el:4.0.2 -> 3.0.4
|    +--- org.postgresql:postgresql:42.6.0 -> 42.2.27
|    +--- org.springframework.boot:spring-boot-autoconfigure:3.1.2 -> 2.5.15
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1 -> 2.6.4
|    +--- org.hibernate.orm:hibernate-core:6.2.6.Final
|    +--- org.springframework:spring-aspects:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-orm:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-jdbc:6.0.11 -> 5.3.27
|    +--- io.micrometer:micrometer-core:1.11.2 -> 1.7.12
|    +--- org.springframework:spring-webmvc:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-web:6.0.11 -> 5.3.27
|    +--- io.micrometer:micrometer-observation:1.11.2
|    +--- org.springframework.boot:spring-boot-starter-tomcat:3.1.2 -> 2.5.15
|    +--- org.webjars:swagger-ui:5.2.0
|    +--- com.google.code.gson:gson:2.10.1 -> 2.8.9
|    +--- com.google.guava:guava:28.0-jre -> 32.0.1-jre
|    +--- commons-io:commons-io:2.11.0
|    +--- com.sun.activation:jakarta.activation:2.0.1 -> 1.2.2
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- commons-codec:commons-codec:1.15
|    +--- org.springframework.boot:spring-boot-actuator:3.1.2 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:3.1.2 -> 2.5.15
|    +--- org.springframework:spring-context:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-aop:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-tx:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-beans:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-expression:6.0.11 -> 5.3.27
|    +--- org.springframework:spring-core:6.0.11 -> 5.3.27
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
|    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.6.1 -> 2.6.4
|    +--- org.aspectj:aspectjweaver:1.9.19 -> 1.9.7
|    +--- jakarta.persistence:jakarta.persistence-api:3.1.0 -> 2.2.3
|    +--- jakarta.transaction:jakarta.transaction-api:2.0.1 -> 1.3.3
|    +--- org.jboss.logging:jboss-logging:3.5.3.Final -> 3.4.3.Final
|    +--- org.hibernate.common:hibernate-commons-annotations:6.0.6.Final
|    +--- io.smallrye:jandex:3.0.5
|    +--- com.fasterxml:classmate:1.5.1
|    +--- net.bytebuddy:byte-buddy:1.14.5 -> 1.10.22
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.3 -> 2.3.8
|    +--- org.glassfish.jaxb:jaxb-core:4.0.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 2.3.3
|    +--- jakarta.inject:jakarta.inject-api:2.0.1
|    +--- org.antlr:antlr4-runtime:4.10.1
|    +--- jakarta.annotation:jakarta.annotation-api:2.1.1 -> 1.3.5
|    +--- io.micrometer:micrometer-commons:1.11.2
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.11 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.11 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.11 -> 9.0.75
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.errorprone:error_prone_annotations:2.3.2 -> 2.21.1
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.codehaus.mojo:animal-sniffer-annotations:1.17
|    +--- org.springframework:spring-jcl:6.0.11 -> 5.3.27
|    +--- org.eclipse.angus:angus-activation:2.0.1
|    +--- jakarta.activation:jakarta.activation-api:2.1.2 -> 1.2.2
|    +--- org.glassfish.jaxb:txw2:4.0.3 -> 2.3.8
|    +--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- io.swagger.core.v3:swagger-annotations-jakarta:2.2.15
|    \--- jakarta.validation:jakarta.validation-api:3.0.2 -> 2.0.2
+--- com.github.hmcts.rse-cft-lib:ccd-data-store-api:0.19.842
|    +--- pl.jalokim.propertiestojson:java-properties-to-json:5.1.3
|    +--- io.searchbox:jest:6.3.1
|    +--- io.searchbox:jest-common:6.3.1
|    +--- com.google.code.gson:gson:2.8.9
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.3 -> 3.0.6
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
|    +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
|    +--- com.github.hmcts:ccd-case-document-am-client:1.7.1
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.4.1 -> 2.6.4
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-hateoas:2.7.11 -> 2.5.15
|    +--- io.springfox:springfox-boot-starter:3.0.0
|    +--- org.springframework.hateoas:spring-hateoas:1.5.4 -> 1.3.7
|    +--- io.springfox:springfox-oas:3.0.0
|    +--- io.springfox:springfox-data-rest:3.0.0
|    +--- io.springfox:springfox-bean-validators:3.0.0
|    +--- io.springfox:springfox-swagger2:3.0.0
|    +--- io.springfox:springfox-swagger-ui:3.0.0
|    +--- io.springfox:springfox-swagger-common:3.0.0
|    +--- io.springfox:springfox-spring-webmvc:3.0.0
|    +--- io.springfox:springfox-spring-webflux:3.0.0
|    +--- io.springfox:springfox-spring-web:3.0.0
|    +--- io.springfox:springfox-schema:3.0.0
|    +--- io.springfox:springfox-spi:3.0.0
|    +--- io.springfox:springfox-core:3.0.0
|    +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
|    +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.11 -> 2.5.15
|    +--- org.springframework.retry:spring-retry:1.3.1 -> 1.3.4
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.11 -> 2.5.15
|    +--- com.github.ben-manes.caffeine:caffeine:3.1.6 -> 2.9.3
|    +--- javax.inject:javax.inject:1
|    +--- pl.jalokim.utils:java-utils:1.1.1
|    +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
|    +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
|    +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|    +--- org.flywaydb:flyway-core:8.5.13 -> 7.7.3
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-validation:2.7.11 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.7.11 -> 2.5.15
|    +--- org.elasticsearch:elasticsearch:7.16.2 -> 7.12.1
|    +--- org.elasticsearch:elasticsearch-x-content:7.16.2 -> 7.17.1
|    +--- org.yaml:snakeyaml:1.32 -> 1.28
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.8 -> 5.5.8
|    +--- com.jayway.jsonpath:json-path:2.4.0 -> 2.5.0
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- net.minidev:json-smart:2.4.7 -> 2.4.10
|    +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
|    +--- io.vavr:vavr:0.10.4
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- com.sun.mail:mailapi:1.6.1 -> 2.0.1
|    +--- commons-lang:commons-lang:2.6
|    +--- commons-validator:commons-validator:1.6
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.awaitility:awaitility:3.1.6 -> 4.0.3
|    +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- commons-io:commons-io:2.8.0 -> 2.11.0
|    +--- org.springframework.security:spring-security-config:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.8 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.8 -> 5.5.8
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.8 -> 5.5.8
|    +--- com.vladmihalcea:hibernate-types-52:2.9.13 -> 2.16.3
|    +--- org.hibernate:hibernate-core:5.6.10.Final -> 5.4.33
|    +--- org.apache.commons:commons-jexl3:3.1
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.11 -> 2.5.15
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- org.jooq:jool-java-8:0.9.14
|    +--- org.postgresql:postgresql:42.5.1 -> 42.2.27
|    +--- com.zaxxer:HikariCP:4.0.2 -> 4.0.3
|    +--- org.springframework:spring-webmvc:5.3.27
|    +--- org.springframework:spring-web:5.3.27
|    +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
|    +--- io.github.openfeign:feign-jackson:11.8 -> 10.12
|    +--- io.github.openfeign:feign-core:11.8 -> 10.12
|    +--- com.microsoft.azure:applicationinsights-core:2.4.1 -> 2.6.1
|    +--- com.microsoft.azure:applicationinsights-web:2.4.1 -> 2.6.4
|    +--- org.mapstruct:mapstruct:1.3.1.Final
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11 -> 2.5.15
|    +--- io.micrometer:micrometer-core:1.9.10 -> 1.7.12
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- org.springframework.data:spring-data-jpa:2.7.11 -> 2.5.12
|    +--- org.springframework:spring-aspects:5.3.27
|    +--- org.springframework:spring-orm:5.3.27
|    +--- org.springframework:spring-jdbc:5.3.27
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context-support:5.3.27
|    +--- org.reflections:reflections:0.9.11
|    +--- com.google.guava:guava:30.1-jre -> 32.0.1-jre
|    +--- org.checkerframework:checker-qual:3.33.0 -> 3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.18.0 -> 2.21.1
|    +--- org.apache.httpcomponents:httpcore-nio:4.4.16
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- org.apache.httpcomponents:httpasyncclient:4.1.5
|    +--- commons-logging:commons-logging:1.2
|    +--- com.auth0:java-jwt:3.12.0
|    +--- commons-codec:commons-codec:1.15
|    +--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.springframework.data:spring-data-commons:2.7.11 -> 2.5.12
|    +--- io.swagger:swagger-models:1.5.20
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.11 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.11 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.27
|    +--- org.springframework:spring-aop:5.3.27
|    +--- org.springframework:spring-tx:5.3.27
|    +--- org.springframework:spring-beans:5.3.27
|    +--- org.springframework:spring-expression:5.3.27
|    +--- org.springframework:spring-core:5.3.27
|    +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- io.vavr:vavr-match:0.10.4
|    +--- javax.activation:activation:1.1
|    +--- commons-digester:commons-digester:1.8.1
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.hamcrest:hamcrest-library:2.2
|    +--- org.hamcrest:hamcrest-core:2.2
|    +--- org.objenesis:objenesis:2.6
|    +--- jakarta.el:jakarta.el-api:4.0.0
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- javax.persistence:javax.persistence-api:2.2
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    +--- javax.activation:javax.activation-api:1.2.0
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- org.projectlombok:lombok:1.18.26
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
|    +--- org.elasticsearch:elasticsearch-lz4:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-cli:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-core:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-secure-sm:7.16.2 -> 7.17.1
|    +--- org.elasticsearch:elasticsearch-geo:7.16.2 -> 7.17.1
|    +--- org.apache.lucene:lucene-core:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-analyzers-common:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-backward-codecs:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-grouping:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-highlighter:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-join:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-memory:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-misc:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-queries:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-queryparser:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-sandbox:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-spatial3d:8.10.1 -> 8.11.1
|    +--- org.apache.lucene:lucene-suggest:8.10.1 -> 8.11.1
|    +--- com.carrotsearch:hppc:0.8.1
|    +--- joda-time:joda-time:2.10.10
|    +--- com.tdunning:t-digest:3.2
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- net.java.dev.jna:jna:5.10.0
|    +--- org.elasticsearch:elasticsearch-plugin-classloader:7.16.2 -> 7.17.1
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
|    +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
|    +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
|    +--- org.springframework:spring-jcl:5.3.27
|    +--- com.nimbusds:content-type:2.2
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.ow2.asm:asm:9.1 -> 9.3
|    +--- org.hamcrest:hamcrest:2.2
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
|    +--- org.codehaus.groovy:groovy-all:2.4.15
|    +--- jakarta.validation:jakarta.validation-api:2.0.2
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.14.1 -> 2.12.7
|    +--- org.lz4:lz4-java:1.8.0
|    +--- net.sf.jopt-simple:jopt-simple:5.0.2
|    +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
|    \--- org.javassist:javassist:3.21.0-GA
+--- com.github.hmcts.rse-cft-lib:am-role-assignment-service:0.19.842
|    +--- com.github.hmcts:properties-volume-spring-boot-starter:0.1.1
|    +--- org.springframework.boot:spring-boot-starter-web:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-data-jpa:2.7.16 -> 2.5.15
|    +--- com.github.hmcts:idam-java-client:2.1.1
|    +--- com.github.hmcts:service-auth-provider-java-client:4.0.2 -> 4.0.3
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.7.16 -> 2.5.15
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.10.RELEASE -> 3.0.7
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:2.2.10.RELEASE -> 3.0.7
|    +--- org.springframework.boot:spring-boot-starter-aop:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-json:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-security:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-cache:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.16 -> 2.5.15
|    +--- org.springframework.security:spring-security-oauth2-client:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-web:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-config:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-jose:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.7.10 -> 5.5.8
|    +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-context:3.1.7 -> 3.0.6
|    +--- org.springframework.cloud:spring-cloud-commons:3.1.7 -> 3.0.6
|    +--- org.springframework.security:spring-security-crypto:5.7.10 -> 5.5.8
|    +--- org.springframework.retry:spring-retry:2.0.2 -> 1.3.4
|    +--- org.drools:drools-decisiontables:7.73.0.Final
|    +--- org.apache.poi:poi-ooxml:5.2.4
|    +--- org.apache.poi:poi-scratchpad:5.2.3
|    +--- org.apache.poi:poi:5.2.4
|    +--- org.springframework:spring-context-support:5.3.29 -> 5.3.27
|    +--- org.springdoc:springdoc-openapi-ui:1.6.8
|    +--- org.springdoc:springdoc-openapi-webmvc-core:1.6.8
|    +--- org.springframework:spring-webmvc:5.3.29 -> 5.3.27
|    +--- org.springframework.data:spring-data-jpa:2.7.16 -> 2.5.12
|    +--- org.springframework.boot:spring-boot-starter-jdbc:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.16 -> 2.5.15
|    +--- org.springdoc:springdoc-openapi-common:1.6.8
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot-actuator:2.7.16 -> 2.5.15
|    +--- org.springframework.boot:spring-boot:2.7.16 -> 2.5.15
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-orm:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-jdbc:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-tx:5.3.29 -> 5.3.27
|    +--- io.github.openfeign.form:feign-form-spring:3.8.0
|    +--- org.springframework:spring-web:5.3.29 -> 5.3.27
|    +--- org.springframework.data:spring-data-commons:2.7.16 -> 2.5.12
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-aspects:5.3.29 -> 5.3.27
|    +--- org.springframework:spring-jcl:5.3.29 -> 5.3.27
|    +--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    +--- org.bouncycastle:bcpkix-jdk15on:1.70
|    +--- org.kie:kie-ci:7.73.0.Final
|    +--- org.drools:drools-templates:7.73.0.Final
|    +--- org.drools:drools-serialization-protobuf:7.73.0.Final
|    +--- org.drools:drools-mvel:7.73.0.Final
|    +--- org.drools:drools-compiler:7.73.0.Final
|    +--- org.drools:drools-ecj:7.73.0.Final
|    +--- org.drools:drools-core:7.73.0.Final
|    +--- org.kie.soup:kie-soup-maven-integration:7.73.0.Final
|    +--- org.apache.maven:maven-compat:3.3.9
|    +--- org.apache.maven:maven-core:3.8.7
|    +--- org.flywaydb:flyway-core:8.5.12 -> 7.7.3
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    +--- com.google.inject:guice:4.2.2
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    +--- io.github.openfeign:feign-hystrix:10.12
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    +--- com.netflix.archaius:archaius-core:0.7.7
|    +--- com.google.guava:guava:32.0.1-jre
|    +--- org.apache.maven:maven-settings-builder:3.8.7
|    +--- org.apache.maven:maven-aether-provider:3.3.9
|    +--- org.apache.maven:maven-resolver-provider:3.8.7
|    +--- org.apache.maven:maven-model-builder:3.8.7
|    +--- org.codehaus.plexus:plexus-sec-dispatcher:2.0
|    +--- org.codehaus.plexus:plexus-cipher:2.0
|    +--- javax.inject:javax.inject:1
|    +--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.16 -> 2.5.15
|    +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 -> 9.0.75
|    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 -> 9.0.75
|    +--- org.hibernate:hibernate-core:5.6.15.Final -> 5.4.33
|    +--- com.github.ben-manes.caffeine:caffeine:3.1.8 -> 2.9.3
|    +--- org.postgresql:postgresql:42.6.0 -> 42.2.27
|    +--- com.nimbusds:oauth2-oidc-sdk:9.35 -> 9.9.1
|    +--- com.nimbusds:nimbus-jose-jwt:9.25 -> 9.10.1
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    +--- commons-fileupload:commons-fileupload:1.5
|    +--- org.apache.maven.wagon:wagon-http:3.0.0
|    +--- org.apache.maven.wagon:wagon-http-shared:3.0.0
|    +--- commons-io:commons-io:2.11.0
|    +--- org.apache.commons:commons-compress:1.24.0
|    +--- commons-beanutils:commons-beanutils:1.9.4
|    +--- org.json:json:20230227
|    +--- com.github.hmcts.java-logging:logging:6.0.1
|    +--- io.swagger.core.v3:swagger-core:2.2.0
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.5 -> 2.12.7
|    +--- io.swagger.core.v3:swagger-models:2.2.0
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.2 -> 2.12.7
|    +--- org.webjars:webjars-locator-core:0.50 -> 0.46
|    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.5 -> 2.12.7
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.2 -> 2.12.7
|    +--- com.fasterxml.jackson:jackson-bom:2.14.2
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.5 -> 2.12.7
|    +--- io.github.openfeign:feign-jackson:11.9.1 -> 10.12
|    +--- com.auth0:java-jwt:3.12.0
|    +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
|    +--- com.thoughtworks.xstream:xstream:1.4.20
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4
|    +--- org.pitest:pitest-junit5-plugin:0.16
|    +--- jakarta.enterprise:jakarta.enterprise.cdi-api:2.0.2
|    +--- jakarta.interceptor:jakarta.interceptor-api:1.2.5
|    +--- jakarta.ejb:jakarta.ejb-api:3.2.6
|    +--- jakarta.transaction:jakarta.transaction-api:1.3.3
|    +--- jakarta.persistence:jakarta.persistence-api:2.2.3
|    +--- io.micrometer:micrometer-core:1.9.15 -> 1.7.12
|    +--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    +--- io.reactivex:rxjava-reactive-streams:1.2.1
|    +--- io.github.openfeign:feign-slf4j:10.12
|    +--- io.github.openfeign:feign-httpclient:11.9.1 -> 10.12
|    +--- io.github.openfeign:feign-core:11.9.1 -> 10.12
|    +--- com.warrenstrange:googleauth:1.5.0
|    +--- org.eclipse.aether:aether-transport-http:1.1.0
|    +--- org.apache.httpcomponents:httpclient:4.5.14
|    +--- commons-codec:commons-codec:1.15
|    +--- org.apache.commons:commons-collections4:4.4
|    +--- org.apache.commons:commons-math3:3.6.1
|    +--- com.zaxxer:SparseBitSet:1.3
|    +--- org.apache.poi:poi-ooxml-lite:5.2.4
|    +--- org.apache.xmlbeans:xmlbeans:5.1.1
|    +--- org.springframework.boot:spring-boot-starter-logging:2.7.16 -> 2.5.15
|    +--- org.apache.logging.log4j:log4j-to-slf4j:2.19.0 -> 2.17.2
|    +--- org.apache.logging.log4j:log4j-api:2.19.0 -> 2.17.2
|    +--- com.github.virtuald:curvesapi:1.08
|    +--- org.bouncycastle:bcutil-jdk15on:1.70
|    +--- org.bouncycastle:bcprov-jdk15on:1.70
|    +--- org.webjars:swagger-ui:4.10.3 -> 5.2.0
|    +--- io.github.classgraph:classgraph:4.8.143
|    +--- org.drools:drools-core-dynamic:7.73.0.Final
|    +--- org.drools:drools-core-reflective:7.73.0.Final
|    +--- org.kie:kie-internal:7.73.0.Final
|    +--- org.kie:kie-api:7.73.0.Final
|    +--- org.kie.soup:kie-soup-xstream:7.73.0.Final
|    +--- org.kie.soup:kie-soup-maven-support:7.73.0.Final
|    +--- org.slf4j:jcl-over-slf4j:1.7.36
|    +--- org.apache.maven.resolver:maven-resolver-impl:1.6.3
|    +--- com.zaxxer:HikariCP:4.0.3
|    +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
|    +--- org.slf4j:jul-to-slf4j:1.7.36
|    +--- io.github.openfeign.form:feign-form:3.8.0
|    +--- org.kie.soup:kie-soup-project-datamodel-commons:7.73.0.Final
|    +--- org.kie.soup:kie-soup-project-datamodel-api:7.73.0.Final
|    +--- org.kie.soup:kie-soup-commons:7.73.0.Final
|    +--- org.slf4j:slf4j-api:1.7.36
|    +--- org.kie:kie-memory-compiler:7.73.0.Final
|    +--- org.antlr:antlr-runtime:3.5.2
|    +--- org.apache.maven:maven-plugin-api:3.8.7
|    +--- org.apache.maven:maven-model:3.8.7
|    +--- org.apache.maven:maven-settings:3.8.7
|    +--- org.eclipse.aether:aether-transport-wagon:1.1.0
|    +--- org.apache.maven.wagon:wagon-provider-api:3.0.0
|    +--- org.sonatype.plexus:plexus-sec-dispatcher:1.3
|    +--- org.eclipse.sisu:org.eclipse.sisu.plexus:0.3.5
|    +--- org.codehaus.plexus:plexus-classworlds:2.6.0
|    +--- org.apache.maven:maven-repository-metadata:3.8.7
|    +--- org.apache.maven:maven-artifact:3.8.7
|    +--- org.codehaus.plexus:plexus-utils:3.4.1
|    +--- org.eclipse.aether:aether-impl:1.1.0
|    +--- org.eclipse.aether:aether-connector-basic:1.1.0
|    +--- org.eclipse.aether:aether-transport-file:1.1.0
|    +--- org.eclipse.aether:aether-util:1.1.0
|    +--- org.eclipse.aether:aether-spi:1.1.0
|    +--- org.eclipse.aether:aether-api:1.1.0
|    +--- org.sonatype.plexus:plexus-cipher:1.7
|    +--- org.apache.maven:maven-builder-support:3.8.7
|    +--- org.apache.maven.resolver:maven-resolver-spi:1.6.3
|    +--- org.apache.maven.resolver:maven-resolver-util:1.6.3
|    +--- org.apache.maven.resolver:maven-resolver-api:1.6.3
|    +--- org.apache.maven.shared:maven-shared-utils:3.3.4
|    +--- org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5
|    +--- org.codehaus.plexus:plexus-interpolation:1.26
|    +--- org.codehaus.plexus:plexus-component-annotations:2.1.0
|    +--- org.apache.commons:commons-lang3:3.12.0
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- org.checkerframework:checker-qual:3.37.0
|    +--- com.google.errorprone:error_prone_annotations:2.21.1
|    +--- com.google.j2objc:j2objc-annotations:2.8
|    +--- org.apache.tomcat:tomcat-annotations-api:9.0.80 -> 9.0.75
|    +--- org.hibernate.common:hibernate-commons-annotations:5.1.2.Final -> 6.0.6.Final
|    +--- org.jboss.logging:jboss-logging:3.4.3.Final
|    +--- javax.persistence:javax.persistence-api:2.2
|    +--- net.bytebuddy:byte-buddy:1.12.23 -> 1.10.22
|    +--- antlr:antlr:2.7.7
|    +--- org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final
|    +--- org.jboss:jandex:2.4.2.Final
|    +--- com.fasterxml:classmate:1.5.1
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    +--- javax.activation:javax.activation-api:1.2.0
|    +--- org.glassfish.jaxb:jaxb-runtime:2.3.8
|    +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
|    +--- commons-configuration:commons-configuration:1.8
|    +--- commons-logging:commons-logging:1.2
|    +--- commons-collections:commons-collections:3.2.2
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- com.microsoft.azure:applicationinsights-web:2.6.4
|    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    +--- org.yaml:snakeyaml:2.0 -> 1.28
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.80 -> 9.0.75
|    +--- org.hdrhistogram:HdrHistogram:2.1.12
|    +--- org.latencyutils:LatencyUtils:2.0.3
|    +--- com.nimbusds:content-type:2.2
|    +--- net.minidev:json-smart:2.4.11 -> 2.4.10
|    +--- com.nimbusds:lang-tag:1.6
|    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- io.reactivex:rxjava:1.3.8
|    +--- net.minidev:accessors-smart:2.4.11
|    +--- org.ow2.asm:asm:9.3
|    +--- org.reactivestreams:reactive-streams:1.0.4
|    +--- org.mvel:mvel2:2.4.14.Final
|    +--- jakarta.inject:jakarta.inject-api:1.0.3 -> 2.0.1
|    +--- org.apache.httpcomponents:httpcore:4.4.16
|    +--- javax.annotation:javax.annotation-api:1.3.2
|    +--- aopalliance:aopalliance:1.0
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    +--- org.glassfish.jaxb:txw2:2.3.8
|    +--- com.sun.istack:istack-commons-runtime:3.0.12 -> 4.1.2
|    +--- com.sun.activation:jakarta.activation:1.2.2
|    +--- commons-lang:commons-lang:2.6
|    +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
|    +--- io.swagger.core.v3:swagger-annotations:2.2.0
|    \--- jakarta.validation:jakarta.validation-api:2.0.2
\--- com.github.hmcts.rse-cft-lib:aac-manage-case-assignment:0.19.842
     +--- pl.jalokim.propertiestojson:java-properties-to-json:5.1.3
     +--- com.google.code.gson:gson:2.8.9
     +--- com.vladmihalcea:hibernate-types-52:2.9.13 -> 2.16.3
     +--- com.github.hmcts:service-auth-provider-java-client:4.0.3
     +--- com.github.hmcts:idam-java-client:2.0.1 -> 2.1.1
     +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3 -> 3.0.7
     +--- org.springframework.cloud:spring-cloud-openfeign-core:3.1.3 -> 3.0.7
     +--- io.github.openfeign.form:feign-form-spring:3.8.0
     +--- commons-fileupload:commons-fileupload:1.5
     +--- uk.gov.service.notify:notifications-java-client:3.15.1-RELEASE
     +--- pl.jalokim.utils:java-utils:1.1.1
     +--- org.springframework.cloud:spring-cloud-starter-netflix-zuul:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-netflix-zuul:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
     +--- com.netflix.hystrix:hystrix-javanica:1.5.18
     +--- org.apache.commons:commons-lang3:3.7 -> 3.12.0
     +--- com.netflix.zuul:zuul-core:1.3.1
     +--- commons-io:commons-io:2.8.0 -> 2.11.0
     +--- org.springframework.cloud:spring-cloud-starter-bootstrap:3.0.1 -> 3.0.6
     +--- org.springframework.boot:spring-boot-starter-hateoas:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-web:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-validation:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-actuator:2.6.10 -> 2.5.15
     +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
     +--- org.springframework.boot:spring-boot-starter-aop:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-json:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-cache:2.6.10 -> 2.5.15
     +--- org.springframework.retry:spring-retry:1.3.1 -> 1.3.4
     +--- io.springfox:springfox-boot-starter:3.0.0
     +--- org.projectlombok:lombok:1.18.28 -> 1.18.26
     +--- com.github.hmcts.java-logging:logging:5.1.9 -> 6.0.1
     +--- com.github.hmcts.java-logging:logging-appinsights:5.1.9 -> 6.0.1
     +--- io.jsonwebtoken:jjwt:0.9.1
     +--- org.springframework.boot:spring-boot-starter-oauth2-client:2.5.14 -> 2.5.15
     +--- org.springframework.hateoas:spring-hateoas:1.4.4 -> 1.3.7
     +--- com.jayway.jsonpath:json-path:2.6.0 -> 2.5.0
     +--- org.springframework.security:spring-security-oauth2-client:5.6.9 -> 5.5.8
     +--- com.nimbusds:oauth2-oidc-sdk:9.19 -> 9.9.1
     +--- net.minidev:json-smart:2.4.7 -> 2.4.10
     +--- org.glassfish:jakarta.el:4.0.1 -> 3.0.4
     +--- org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.5.14 -> 2.5.15
     +--- org.springframework.security:spring-security-oauth2-resource-server:5.6.9 -> 5.5.8
     +--- org.springframework.security:spring-security-web:5.6.9 -> 5.5.8
     +--- org.springframework.security:spring-security-config:5.6.9 -> 5.5.8
     +--- io.github.openfeign:feign-httpclient:11.0 -> 10.12
     +--- com.github.ben-manes.caffeine:caffeine:2.7.0 -> 2.9.3
     +--- com.warrenstrange:googleauth:1.5.0
     +--- org.springframework.cloud:spring-cloud-starter-netflix-ribbon:2.2.10.RELEASE
     +--- com.netflix.ribbon:ribbon-httpclient:2.3.0
     +--- com.sun.jersey.contribs:jersey-apache-client4:1.19.1
     +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
     +--- com.netflix.ribbon:ribbon:2.3.0
     +--- com.netflix.ribbon:ribbon-transport:2.3.0
     +--- com.netflix.ribbon:ribbon-loadbalancer:2.3.0
     +--- com.netflix.netflix-commons:netflix-commons-util:0.3.0
     +--- javax.inject:javax.inject:1
     +--- org.modelmapper:modelmapper:2.3.7
     +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
     +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
     +--- org.springframework.cloud:spring-cloud-starter:3.1.3 -> 3.0.6
     +--- org.springframework.boot:spring-boot-starter:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-starter-logging:2.6.10 -> 2.5.15
     +--- ch.qos.logback:logback-classic:1.2.10 -> 1.2.12
     +--- ch.qos.logback:logback-core:1.2.10 -> 1.2.12
     +--- org.springframework.security:spring-security-oauth2-jose:5.6.9 -> 5.5.8
     +--- com.nimbusds:nimbus-jose-jwt:9.21 -> 9.10.1
     +--- org.projectlombok:lombok-mapstruct-binding:0.2.0
     +--- org.reflections:reflections:0.9.11
     +--- com.netflix.ribbon:ribbon-core:2.3.0
     +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
     +--- com.netflix.hystrix:hystrix-serialization:1.5.18
     +--- com.netflix.hystrix:hystrix-core:1.5.18
     +--- com.netflix.archaius:archaius-core:0.7.7
     +--- io.reactivex:rxnetty-servo:0.4.9
     +--- com.netflix.servo:servo-core:0.12.5
     +--- com.google.guava:guava:30.0-jre -> 32.0.1-jre
     +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.6.10 -> 2.5.15
     +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1 -> 2.12.7
     +--- net.logstash.logback:logstash-logback-encoder:6.6
     +--- io.github.openfeign:feign-jackson:11.8 -> 10.12
     +--- com.auth0:java-jwt:3.12.0
     +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.core:jackson-databind:2.14.1 -> 2.12.7.1
     +--- io.springfox:springfox-oas:3.0.0
     +--- io.swagger.core.v3:swagger-models:2.1.2 -> 2.2.0
     +--- io.springfox:springfox-swagger2:3.0.0
     +--- io.springfox:springfox-swagger-common:3.0.0
     +--- io.swagger:swagger-models:1.5.20
     +--- com.fasterxml.jackson.core:jackson-annotations:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.core:jackson-core:2.14.1 -> 2.12.7
     +--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.14.1 -> 2.12.7
     +--- org.springframework:spring-webmvc:5.3.20 -> 5.3.27
     +--- org.springframework.security:spring-security-oauth2-core:5.6.9 -> 5.5.8
     +--- org.springframework:spring-web:5.3.20 -> 5.3.27
     +--- org.springframework.cloud:spring-cloud-commons:3.1.3 -> 3.0.6
     +--- io.github.openfeign:feign-slf4j:11.8 -> 10.12
     +--- io.github.openfeign:feign-core:11.8 -> 10.12
     +--- org.springframework.boot:spring-boot-starter-tomcat:2.6.10 -> 2.5.15
     +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.73 -> 9.0.75
     +--- org.hibernate.validator:hibernate-validator:6.2.3.Final -> 6.2.5.Final
     +--- io.micrometer:micrometer-core:1.8.8 -> 1.7.12
     +--- io.springfox:springfox-data-rest:3.0.0
     +--- io.springfox:springfox-bean-validators:3.0.0
     +--- io.springfox:springfox-swagger-ui:3.0.0
     +--- io.springfox:springfox-spring-webmvc:3.0.0
     +--- io.springfox:springfox-spring-webflux:3.0.0
     +--- io.springfox:springfox-spring-web:3.0.0
     +--- io.springfox:springfox-schema:3.0.0
     +--- io.springfox:springfox-spi:3.0.0
     +--- io.springfox:springfox-core:3.0.0
     +--- org.springframework.plugin:spring-plugin-metadata:2.0.0.RELEASE
     +--- org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE
     +--- org.springframework.security:spring-security-core:5.6.9 -> 5.5.8
     +--- org.springframework:spring-context-support:5.3.20 -> 5.3.27
     +--- org.springframework.boot:spring-boot-autoconfigure:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot-actuator:2.6.10 -> 2.5.15
     +--- org.springframework.boot:spring-boot:2.6.10 -> 2.5.15
     +--- org.springframework:spring-context:5.3.20 -> 5.3.27
     +--- org.springframework:spring-aop:5.3.20 -> 5.3.27
     +--- org.aspectj:aspectjweaver:1.9.7
     +--- com.fasterxml:classmate:1.5.1
     +--- org.slf4j:jul-to-slf4j:1.7.36
     +--- io.github.openfeign.form:feign-form:3.8.0
     +--- org.bitbucket.b_c:jose4j:0.6.5
     +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.1 -> 2.17.2
     +--- io.reactivex:rxnetty-contexts:0.4.9
     +--- io.reactivex:rxnetty:0.4.9
     +--- com.netflix.netflix-commons:netflix-statistics:0.1.1
     +--- org.slf4j:slf4j-api:1.7.36
     +--- javax.servlet:javax.servlet-api:4.0.1
     +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1 -> 2.6.4
     +--- net.minidev:accessors-smart:2.4.7 -> 2.4.11
     +--- jakarta.el:jakarta.el-api:4.0.0
     +--- org.springframework:spring-beans:5.3.20 -> 5.3.27
     +--- org.springframework:spring-expression:5.3.20 -> 5.3.27
     +--- org.springframework:spring-core:5.3.20 -> 5.3.27
     +--- org.checkerframework:checker-qual:3.5.0 -> 3.37.0
     +--- com.google.errorprone:error_prone_annotations:2.3.4 -> 2.21.1
     +--- org.apache.httpcomponents:httpcore:4.4.15 -> 4.4.16
     +--- commons-codec:commons-codec:1.15
     +--- commons-cli:commons-cli:1.4
     +--- org.json:json:20180813 -> 20230227
     +--- joda-time:joda-time:2.10.1 -> 2.10.10
     +--- com.github.stephenc.jcip:jcip-annotations:1.0-1
     +--- com.google.guava:failureaccess:1.0.1
     +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     +--- com.google.code.findbugs:jsr305:3.0.2
     +--- com.google.j2objc:j2objc-annotations:1.3 -> 2.8
     +--- org.codehaus.groovy:groovy-all:2.4.15
     +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
     +--- jakarta.activation:jakarta.activation-api:1.2.2
     +--- org.springframework.cloud:spring-cloud-context:3.1.3 -> 3.0.6
     +--- org.springframework.security:spring-security-rsa:1.0.10.RELEASE -> 1.0.11.RELEASE
     +--- org.springframework.security:spring-security-crypto:5.6.9 -> 5.5.8
     +--- jakarta.annotation:jakarta.annotation-api:1.3.5
     +--- org.yaml:snakeyaml:1.32 -> 1.28
     +--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.73 -> 9.0.75
     +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.73 -> 9.0.75
     +--- jakarta.validation:jakarta.validation-api:2.0.2
     +--- org.jboss.logging:jboss-logging:3.4.3.Final
     +--- org.hdrhistogram:HdrHistogram:2.1.12
     +--- org.latencyutils:LatencyUtils:2.0.3
     +--- io.swagger.core.v3:swagger-annotations:2.1.2 -> 2.2.0
     +--- org.mapstruct:mapstruct:1.3.1.Final
     +--- io.swagger:swagger-annotations:1.5.20 -> 1.6.6
     +--- com.microsoft.azure:applicationinsights-core:2.6.1
     +--- com.microsoft.azure:applicationinsights-web:2.6.1 -> 2.6.4
     +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
     +--- io.reactivex:rxjava-reactive-streams:1.2.1
     +--- io.reactivex:rxjava:1.3.8
     +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
     +--- commons-configuration:commons-configuration:1.8
     +--- org.ow2.asm:asm:9.1 -> 9.3
     +--- org.springframework:spring-jcl:5.3.20 -> 5.3.27
     +--- org.javassist:javassist:3.21.0-GA
     +--- org.bouncycastle:bcpkix-jdk15on:1.70
     +--- net.bytebuddy:byte-buddy:1.11.22 -> 1.10.22
     +--- io.github.classgraph:classgraph:4.8.83 -> 4.8.143
     +--- org.reactivestreams:reactive-streams:1.0.4
     +--- commons-lang:commons-lang:2.6
     +--- commons-collections:commons-collections:3.2.2
     +--- com.sun.jersey:jersey-client:1.19.1
     +--- com.nimbusds:content-type:2.1 -> 2.2
     +--- com.nimbusds:lang-tag:1.5 -> 1.6
     +--- org.bouncycastle:bcutil-jdk15on:1.70
     +--- org.bouncycastle:bcprov-jdk15on:1.70
     +--- org.apache.logging.log4j:log4j-api:2.17.1 -> 2.17.2
     +--- com.sun.jersey:jersey-core:1.19.1
     \--- javax.ws.rs:jsr311-api:1.1.1

cftlibIDERuntimeOnly - Runtime only dependencies for source set 'cftlib ide'. (n)
No dependencies

cftlibImplementation - Implementation only dependencies for source set 'cftlib'. (n)
+--- com.github.hmcts.rse-cft-lib:bootstrapper:0.19.842 (n)
\--- com.github.hmcts.rse-cft-lib:cftlib-agent:0.19.842 (n)

cftlibRuntimeClasspath - Runtime classpath of source set 'cftlib'.
+--- com.github.hmcts.rse-cft-lib:bootstrapper:0.19.842
+--- com.github.hmcts.rse-cft-lib:cftlib-agent:0.19.842
|    \--- com.auth0:java-jwt:3.12.0
|         +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1
|         |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|         |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|         |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|         |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|         |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|         |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|         |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|         \--- commons-codec:commons-codec:1.14 -> 1.15
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre
|    |    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0 (*)
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
\--- net.minidev:json-smart:2.5.0
     \--- net.minidev:accessors-smart:2.5.0
          \--- org.ow2.asm:asm:9.3

cftlibRuntimeOnly - Runtime only dependencies for source set 'cftlib'. (n)
No dependencies

cftlibTestAnnotationProcessor - Annotation processors and their dependencies for source set 'cftlib test'.
No dependencies

cftlibTestCompileClasspath - Compile classpath for source set 'cftlib test'.
+--- com.github.hmcts.rse-cft-lib:bootstrapper:0.19.842
+--- com.github.hmcts.rse-cft-lib:cftlib-agent:0.19.842
+--- com.github.hmcts.rse-cft-lib:test-runner:0.19.842
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    \--- commons-configuration:commons-configuration:1.8
|    |         \--- commons-lang:commons-lang:2.6
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre
|    |         +--- com.google.guava:failureaccess:1.0.1
|    |         +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         +--- com.google.code.findbugs:jsr305:3.0.2
|    |         +--- org.checkerframework:checker-qual:3.37.0
|    |         +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         \--- com.google.j2objc:j2objc-annotations:2.8
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    \--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    \--- org.mockito:mockito-core:3.7.7 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36

cftlibTestCompileOnly - Compile only dependencies for source set 'cftlib test'. (n)
No dependencies

cftlibTestImplementation - Implementation only dependencies for source set 'cftlib test'. (n)
\--- com.github.hmcts.rse-cft-lib:test-runner:0.19.842 (n)

cftlibTestRuntime
No dependencies

cftlibTestRuntimeClasspath - Runtime classpath of source set 'cftlib test'.
+--- com.github.hmcts.rse-cft-lib:bootstrapper:0.19.842
+--- com.github.hmcts.rse-cft-lib:cftlib-agent:0.19.842
|    \--- com.auth0:java-jwt:3.12.0
|         +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1
|         |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|         |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|         |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|         |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|         |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|         |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|         |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|         |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|         \--- commons-codec:commons-codec:1.14 -> 1.15
+--- com.github.hmcts.rse-cft-lib:test-runner:0.19.842
|    +--- org.junit.jupiter:junit-jupiter-api:5.8.2 -> 5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-engine:1.7.2 (c)
|    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    \--- org.junit.platform:junit-platform-console-standalone:1.8.2
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre
|    |    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0 (*)
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-engine:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         +--- org.junit.platform:junit-platform-engine:1.7.2
|    |         |    +--- org.junit:junit-bom:5.7.2 (*)
|    |         |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |         |    +--- org.opentest4j:opentest4j:1.2.0
|    |         |    \--- org.junit.platform:junit-platform-commons:1.7.2 (*)
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    +--- org.mockito:mockito-core:3.7.7 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.0 -> 5.7.2 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
     \--- org.junit.jupiter:junit-jupiter:5.7.0 -> 5.7.2 (*)

cftlibTestRuntimeOnly - Runtime only dependencies for source set 'cftlib test'. (n)
No dependencies

checkstyle - The Checkstyle libraries to be used for this project.
\--- com.puppycrawl.tools:checkstyle:8.25
     +--- info.picocli:picocli:4.0.4
     +--- antlr:antlr:2.7.7
     +--- org.antlr:antlr4-runtime:4.7.2
     +--- commons-beanutils:commons-beanutils:1.9.4
     |    \--- commons-collections:commons-collections:3.2.2
     +--- com.google.guava:guava:28.1-jre
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- com.google.code.findbugs:jsr305:3.0.2
     |    +--- org.checkerframework:checker-qual:2.8.1
     |    +--- com.google.errorprone:error_prone_annotations:2.3.2
     |    +--- com.google.j2objc:j2objc-annotations:1.3
     |    \--- org.codehaus.mojo:animal-sniffer-annotations:1.18
     \--- net.sf.saxon:Saxon-HE:9.9.1-5

compileClasspath - Compile classpath for source set 'main'.
+--- org.projectlombok:lombok:1.18.30
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    \--- commons-configuration:commons-configuration:1.8
|    |         \--- commons-lang:commons-lang:2.6
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre
|    |         +--- com.google.guava:failureaccess:1.0.1
|    |         +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         +--- com.google.code.findbugs:jsr305:3.0.2
|    |         +--- org.checkerframework:checker-qual:3.37.0
|    |         +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         \--- com.google.j2objc:j2objc-annotations:2.8
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.30
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
\--- net.minidev:json-smart:2.5.0
     \--- net.minidev:accessors-smart:2.5.0
          \--- org.ow2.asm:asm:9.3

compileOnly - Compile only dependencies for source set 'main'. (n)
\--- org.projectlombok:lombok:1.18.30 (n)

compileOnlyApi - Compile only API dependencies for source set 'main'. (n)
No dependencies

contractTestAnnotationProcessor - Annotation processors and their dependencies for source set 'contract test'.
No dependencies

contractTestCompileClasspath - Compile classpath for source set 'contract test'.
+--- au.com.dius.pact.consumer:junit5:4.1.7
|    +--- org.junit.jupiter:junit-jupiter-api:5.5.2 -> 5.7.1
|    |    +--- org.junit:junit-bom:5.7.1 -> 5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 -> 5.7.1 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    \--- org.junit.platform:junit-platform-commons:1.7.1 -> 1.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    \--- au.com.dius.pact:consumer:4.1.7
|         +--- com.googlecode.java-diff-utils:diffutils:1.3.0
|         +--- dk.brics.automaton:automaton:1.11-8
|         +--- org.apache.httpcomponents:httpclient:4.5.5 -> 4.5.14
|         |    +--- org.apache.httpcomponents:httpcore:4.4.16
|         |    +--- commons-logging:commons-logging:1.2
|         |    \--- commons-codec:commons-codec:1.11 -> 1.15
|         +--- org.json:json:20160212
|         +--- io.netty:netty-handler:4.1.44.Final -> 4.1.92.Final
|         |    +--- io.netty:netty-common:4.1.92.Final
|         |    +--- io.netty:netty-resolver:4.1.92.Final
|         |    |    \--- io.netty:netty-common:4.1.92.Final
|         |    +--- io.netty:netty-buffer:4.1.92.Final
|         |    |    \--- io.netty:netty-common:4.1.92.Final
|         |    +--- io.netty:netty-transport:4.1.92.Final
|         |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    \--- io.netty:netty-resolver:4.1.92.Final (*)
|         |    +--- io.netty:netty-transport-native-unix-common:4.1.92.Final
|         |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    \--- io.netty:netty-transport:4.1.92.Final (*)
|         |    \--- io.netty:netty-codec:4.1.92.Final
|         |         +--- io.netty:netty-common:4.1.92.Final
|         |         +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |         \--- io.netty:netty-transport:4.1.92.Final (*)
|         +--- org.apache.httpcomponents:httpmime:4.5.5 -> 4.5.14
|         |    \--- org.apache.httpcomponents:httpclient:4.5.14 (*)
|         +--- org.apache.httpcomponents:fluent-hc:4.5.5 -> 4.5.14
|         |    +--- org.apache.httpcomponents:httpclient:4.5.14 (*)
|         |    \--- commons-logging:commons-logging:1.2
|         +--- au.com.dius.pact.core:model:4.1.7
|         |    +--- com.github.zafarkhaja:java-semver:0.9.0
|         |    +--- org.apache.commons:commons-collections4:4.1 -> 4.4
|         |    +--- com.github.mifmif:generex:1.0.2
|         |    |    \--- dk.brics.automaton:automaton:1.11-8
|         |    +--- javax.mail:mail:1.5.0-b01
|         |    |    \--- javax.activation:activation:1.1
|         |    +--- org.jetbrains.kotlin:kotlin-reflect:1.3.72 -> 1.5.32
|         |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.5.32
|         |    |         +--- org.jetbrains:annotations:13.0
|         |    |         \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.32
|         |    +--- au.com.dius.pact.core:support:4.1.7
|         |    |    +--- org.antlr:antlr4:4.7.2
|         |    |    |    +--- org.antlr:antlr4-runtime:4.7.2
|         |    |    |    +--- org.antlr:antlr-runtime:3.5.2
|         |    |    |    +--- org.antlr:ST4:4.1
|         |    |    |    |    \--- org.antlr:antlr-runtime:3.5.2
|         |    |    |    +--- org.abego.treelayout:org.abego.treelayout.core:1.0.3
|         |    |    |    +--- org.glassfish:javax.json:1.0.4
|         |    |    |    \--- com.ibm.icu:icu4j:61.1
|         |    |    +--- io.github.microutils:kotlin-logging:1.6.26
|         |    |    |    +--- io.github.microutils:kotlin-logging-common:1.6.26
|         |    |    |    \--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|         |    |    +--- org.apache.httpcomponents:httpclient:4.5.5 -> 4.5.14 (*)
|         |    |    \--- com.michael-bull.kotlin-result:kotlin-result:1.1.6
|         |    \--- au.com.dius.pact.core:pactbroker:4.1.7
|         |         +--- org.apache.commons:commons-lang3:3.4 -> 3.12.0
|         |         +--- com.google.guava:guava:18.0 -> 30.1.1-jre
|         |         |    +--- com.google.guava:failureaccess:1.0.1
|         |         |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|         |         |    +--- com.google.code.findbugs:jsr305:3.0.2
|         |         |    +--- org.checkerframework:checker-qual:3.8.0
|         |         |    +--- com.google.errorprone:error_prone_annotations:2.5.1
|         |         |    \--- com.google.j2objc:j2objc-annotations:1.3
|         |         +--- io.github.microutils:kotlin-logging:1.6.26 (*)
|         |         +--- au.com.dius.pact.core:support:4.1.7 (*)
|         |         \--- com.michael-bull.kotlin-result:kotlin-result:1.1.6
|         \--- au.com.dius.pact.core:matchers:4.1.7
|              +--- org.apache.commons:commons-lang3:3.4 -> 3.12.0
|              +--- com.googlecode.java-diff-utils:diffutils:1.3.0
|              +--- au.com.dius.pact.core:model:4.1.7 (*)
|              \--- au.com.dius.pact.core:support:4.1.7 (*)
+--- au.com.dius.pact.consumer:java8:4.1.7
|    \--- au.com.dius.pact:consumer:4.1.7 (*)
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2
|    |    |    |    +--- org.slf4j:slf4j-api:1.7.35 -> 1.7.36
|    |    |    |    \--- org.apache.logging.log4j:log4j-api:2.17.2
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.4.10
|    |    |    \--- net.minidev:accessors-smart:2.4.9
|    |    |         \--- org.ow2.asm:asm:9.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 -> 5.7.1 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 -> 5.7.1 (*)
|    +--- org.mockito:mockito-core:3.9.0
|    |    +--- net.bytebuddy:byte-buddy:1.10.20 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.20 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.2
|    +--- org.mockito:mockito-junit-jupiter:3.9.0
|    |    \--- org.mockito:mockito-core:3.9.0 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.junit.jupiter:junit-jupiter-api:5.7.1 (*)
+--- com.fasterxml.jackson.core:jackson-databind:2.12.4 -> 2.11.4
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.11.4 -> 2.12.7
|    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (c)
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (c)
|    |         \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.11.4 (c)
|    \--- com.fasterxml.jackson.core:jackson-core:2.11.4 -> 2.12.7
|         \--- com.fasterxml.jackson:jackson-bom:2.12.7 (*)
+--- com.fasterxml.jackson.core:jackson-databind:2.11.4 (*)
+--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.7.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.7.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.5.8
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.5.8
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:2.2.7.RELEASE -> 3.0.7
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15
|    |    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    |    \--- org.aspectj:aspectjweaver:1.9.7
|    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27
|    |         |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |         +--- commons-fileupload:commons-fileupload:1.4
|    |         |    \--- commons-io:commons-io:2.2
|    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    +--- org.springframework:spring-web:5.2.12.RELEASE -> 5.3.27 (*)
|    +--- org.springframework.cloud:spring-cloud-commons:2.2.7.RELEASE -> 3.0.6 (*)
|    +--- io.github.openfeign:feign-core:10.10.1 -> 10.12
|    +--- io.github.openfeign:feign-slf4j:10.10.1 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12
|    |    \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    \--- io.github.openfeign:feign-hystrix:10.10.1 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12
|         +--- com.netflix.archaius:archaius-core:0.7.6
|         \--- com.netflix.hystrix:hystrix-core:1.5.18
|              +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.6
|              +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|              \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
+--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
+--- com.netflix.ribbon:ribbon-core:2.7.18
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 2.2.7.RELEASE (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    |    \--- io.micrometer:micrometer-core:1.7.12
|    |         \--- org.hdrhistogram:HdrHistogram:2.1.12
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12
|         \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.11.4 (*)
\--- com.google.guava:guava:30.1.1-jre (*)

contractTestCompileOnly - Compile only dependencies for source set 'contract test'. (n)
No dependencies

contractTestImplementation - Implementation only dependencies for source set 'contract test'. (n)
+--- au.com.dius.pact.consumer:junit5:4.1.7 (n)
+--- au.com.dius.pact.consumer:java8:4.1.7 (n)
+--- org.springframework.boot:spring-boot-starter-test (n)
+--- org.junit.jupiter:junit-jupiter-api:5.7.1 (n)
+--- com.fasterxml.jackson.core:jackson-databind:2.12.4 (n)
+--- com.fasterxml.jackson.core:jackson-databind:2.11.4 (n)
+--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.7.RELEASE (n)
+--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (n)
+--- com.netflix.ribbon:ribbon-core:2.7.18 (n)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6 (n)
\--- com.google.guava:guava:30.1.1-jre (n)

contractTestRuntimeClasspath - Runtime classpath of source set 'contract test'.
+--- au.com.dius.pact.consumer:junit5:4.1.7
|    +--- org.slf4j:slf4j-api:1.7.28 -> 1.7.36
|    +--- org.junit.jupiter:junit-jupiter-api:5.5.2 -> 5.7.1
|    |    +--- org.junit:junit-bom:5.7.1 -> 5.8.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.8.2 -> 5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.8.2 -> 5.7.1 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.8.2 -> 5.7.1 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.8.2 -> 5.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-commons:1.8.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-engine:1.8.2 -> 1.7.2 (c)
|    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    \--- org.junit.platform:junit-platform-commons:1.7.1 -> 1.8.2
|    |         \--- org.junit:junit-bom:5.8.2 (*)
|    \--- au.com.dius.pact:consumer:4.1.7
|         +--- org.slf4j:slf4j-api:1.7.28 -> 1.7.36
|         +--- io.ktor:ktor-server-netty:1.3.1
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32
|         |    |    +--- org.jetbrains:annotations:13.0
|         |    |    \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.32
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32
|         |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.5.32 (*)
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61 -> 1.5.32
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.5.32 (*)
|         |    |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.32 (*)
|         |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.60 -> 1.5.32 (*)
|         |    |    \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.60 -> 1.5.32
|         |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3 -> 1.5.2
|         |    |    \--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2
|         |    |         \--- org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2
|         |    |              +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.30 -> 1.5.32 (*)
|         |    |              \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.30 -> 1.5.32
|         |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    +--- io.ktor:ktor-server-host-common:1.3.1
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61 -> 1.5.32 (*)
|         |    |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3 -> 1.5.2 (*)
|         |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |    +--- io.ktor:ktor-server-core:1.3.1
|         |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61 -> 1.5.32 (*)
|         |    |    |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3 -> 1.5.2 (*)
|         |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |    |    +--- io.ktor:ktor-utils-jvm:1.3.1
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |    |    |    |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |    |    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61 -> 1.5.32
|         |    |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3
|         |    |    |    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61 -> 1.5.32
|         |    |    |    |    \--- io.ktor:ktor-io-jvm:1.3.1
|         |    |    |    |         +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |    |    |         +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |    |    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |    |    |         +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |    |    |         +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61 -> 1.5.32
|         |    |    |    |         \--- org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3 (*)
|         |    |    |    +--- io.ktor:ktor-http-jvm:1.3.1
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |    |    |    |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |    |    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61 -> 1.5.32
|         |    |    |    |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3 (*)
|         |    |    |    |    \--- io.ktor:ktor-utils-jvm:1.3.1 (*)
|         |    |    |    +--- com.typesafe:config:1.3.1
|         |    |    |    \--- org.jetbrains.kotlin:kotlin-reflect:1.3.61 -> 1.5.32
|         |    |    |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.5.32 (*)
|         |    |    \--- io.ktor:ktor-http-cio-jvm:1.3.1
|         |    |         +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |         +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |         +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |         +--- io.ktor:ktor-network:1.3.1
|         |    |         |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    |         |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    |         |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    |         |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    |         |    \--- io.ktor:ktor-utils-jvm:1.3.1 (*)
|         |    |         +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.61 -> 1.5.32
|         |    |         +--- org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3 (*)
|         |    |         \--- io.ktor:ktor-http-jvm:1.3.1 (*)
|         |    +--- io.netty:netty-codec-http2:4.1.44.Final -> 4.1.92.Final
|         |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-buffer:4.1.92.Final
|         |    |    |    \--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-transport:4.1.92.Final
|         |    |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    |    \--- io.netty:netty-resolver:4.1.92.Final
|         |    |    |         \--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-codec:4.1.92.Final
|         |    |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    |    \--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |    +--- io.netty:netty-handler:4.1.92.Final
|         |    |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    |    +--- io.netty:netty-resolver:4.1.92.Final (*)
|         |    |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    |    +--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.92.Final
|         |    |    |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    |    |    \--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |    |    \--- io.netty:netty-codec:4.1.92.Final (*)
|         |    |    \--- io.netty:netty-codec-http:4.1.92.Final
|         |    |         +--- io.netty:netty-common:4.1.92.Final
|         |    |         +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |         +--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |         +--- io.netty:netty-codec:4.1.92.Final (*)
|         |    |         \--- io.netty:netty-handler:4.1.92.Final (*)
|         |    +--- org.eclipse.jetty.alpn:alpn-api:1.1.3.v20160715
|         |    +--- io.netty:netty-transport-native-kqueue:4.1.44.Final -> 4.1.92.Final
|         |    |    +--- io.netty:netty-common:4.1.92.Final
|         |    |    +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |    +--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |    +--- io.netty:netty-transport-native-unix-common:4.1.92.Final (*)
|         |    |    \--- io.netty:netty-transport-classes-kqueue:4.1.92.Final
|         |    |         +--- io.netty:netty-common:4.1.92.Final
|         |    |         +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |    |         +--- io.netty:netty-transport:4.1.92.Final (*)
|         |    |         \--- io.netty:netty-transport-native-unix-common:4.1.92.Final (*)
|         |    \--- io.netty:netty-transport-native-epoll:4.1.44.Final -> 4.1.92.Final
|         |         +--- io.netty:netty-common:4.1.92.Final
|         |         +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |         +--- io.netty:netty-transport:4.1.92.Final (*)
|         |         +--- io.netty:netty-transport-native-unix-common:4.1.92.Final (*)
|         |         \--- io.netty:netty-transport-classes-epoll:4.1.92.Final
|         |              +--- io.netty:netty-common:4.1.92.Final
|         |              +--- io.netty:netty-buffer:4.1.92.Final (*)
|         |              +--- io.netty:netty-transport:4.1.92.Final (*)
|         |              \--- io.netty:netty-transport-native-unix-common:4.1.92.Final (*)
|         +--- io.ktor:ktor-network-tls-certificates:1.3.1
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61 -> 1.5.32 (*)
|         |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3 -> 1.5.2 (*)
|         |    +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |    \--- io.ktor:ktor-network-tls:1.3.1
|         |         +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |         +--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61 -> 1.5.32 (*)
|         |         +--- org.jetbrains.kotlinx:atomicfu:0.14.1 (*)
|         |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|         |         +--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3 -> 1.5.2 (*)
|         |         +--- io.ktor:ktor-network:1.3.1 (*)
|         |         \--- io.ktor:ktor-http-cio-jvm:1.3.1 (*)
|         +--- com.googlecode.java-diff-utils:diffutils:1.3.0
|         +--- dk.brics.automaton:automaton:1.11-8
|         +--- org.apache.httpcomponents:httpclient:4.5.5 -> 4.5.14
|         |    +--- org.apache.httpcomponents:httpcore:4.4.16
|         |    +--- commons-logging:commons-logging:1.2
|         |    \--- commons-codec:commons-codec:1.11 -> 1.15
|         +--- org.json:json:20160212
|         +--- io.netty:netty-handler:4.1.44.Final -> 4.1.92.Final (*)
|         +--- org.apache.httpcomponents:httpmime:4.5.5 -> 4.5.14
|         |    \--- org.apache.httpcomponents:httpclient:4.5.14 (*)
|         +--- org.apache.httpcomponents:fluent-hc:4.5.5 -> 4.5.14
|         |    +--- org.apache.httpcomponents:httpclient:4.5.14 (*)
|         |    \--- commons-logging:commons-logging:1.2
|         +--- au.com.dius.pact.core:model:4.1.7
|         |    +--- org.slf4j:slf4j-api:1.7.28 -> 1.7.36
|         |    +--- org.apache.tika:tika-core:1.24.1
|         |    +--- com.github.zafarkhaja:java-semver:0.9.0
|         |    +--- org.apache.commons:commons-collections4:4.1 -> 4.4
|         |    +--- com.github.mifmif:generex:1.0.2
|         |    |    \--- dk.brics.automaton:automaton:1.11-8
|         |    +--- javax.mail:mail:1.5.0-b01
|         |    |    \--- javax.activation:activation:1.1
|         |    +--- org.jetbrains.kotlin:kotlin-reflect:1.3.72 -> 1.5.32 (*)
|         |    +--- au.com.dius.pact.core:support:4.1.7
|         |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72 -> 1.5.32 (*)
|         |    |    +--- org.jetbrains.kotlin:kotlin-reflect:1.3.72 -> 1.5.32 (*)
|         |    |    +--- org.apache.commons:commons-lang3:3.4 -> 3.12.0
|         |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|         |    |    +--- org.antlr:antlr4:4.7.2
|         |    |    |    +--- org.antlr:antlr4-runtime:4.7.2
|         |    |    |    +--- org.antlr:antlr-runtime:3.5.2
|         |    |    |    +--- org.antlr:ST4:4.1
|         |    |    |    |    \--- org.antlr:antlr-runtime:3.5.2
|         |    |    |    +--- org.abego.treelayout:org.abego.treelayout.core:1.0.3
|         |    |    |    +--- org.glassfish:javax.json:1.0.4
|         |    |    |    \--- com.ibm.icu:icu4j:61.1
|         |    |    +--- io.github.microutils:kotlin-logging:1.6.26
|         |    |    |    +--- io.github.microutils:kotlin-logging-common:1.6.26
|         |    |    |    \--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|         |    |    +--- org.apache.httpcomponents:httpclient:4.5.5 -> 4.5.14 (*)
|         |    |    \--- com.michael-bull.kotlin-result:kotlin-result:1.1.6
|         |    |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.3.61 -> 1.5.32 (*)
|         |    \--- au.com.dius.pact.core:pactbroker:4.1.7
|         |         +--- org.slf4j:slf4j-api:1.7.28 -> 1.7.36
|         |         +--- org.apache.commons:commons-lang3:3.4 -> 3.12.0
|         |         +--- com.google.guava:guava:18.0 -> 30.1.1-jre
|         |         |    +--- com.google.guava:failureaccess:1.0.1
|         |         |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|         |         |    +--- com.google.code.findbugs:jsr305:3.0.2
|         |         |    +--- org.checkerframework:checker-qual:3.8.0
|         |         |    +--- com.google.errorprone:error_prone_annotations:2.5.1
|         |         |    \--- com.google.j2objc:j2objc-annotations:1.3
|         |         +--- io.github.microutils:kotlin-logging:1.6.26 (*)
|         |         +--- au.com.dius.pact.core:support:4.1.7 (*)
|         |         \--- com.michael-bull.kotlin-result:kotlin-result:1.1.6 (*)
|         \--- au.com.dius.pact.core:matchers:4.1.7
|              +--- xerces:xercesImpl:2.12.0
|              |    \--- xml-apis:xml-apis:1.4.01
|              +--- org.slf4j:slf4j-api:1.7.28 -> 1.7.36
|              +--- org.atteo:evo-inflector:1.2.2
|              +--- com.github.ajalt:mordant:1.2.1
|              |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.21 -> 1.5.32 (*)
|              |    \--- com.github.ajalt:colormath:1.2.0
|              |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.2.51 -> 1.5.32 (*)
|              +--- org.apache.commons:commons-lang3:3.4 -> 3.12.0
|              +--- com.googlecode.java-diff-utils:diffutils:1.3.0
|              +--- au.com.dius.pact.core:model:4.1.7 (*)
|              \--- au.com.dius.pact.core:support:4.1.7 (*)
+--- au.com.dius.pact.consumer:java8:4.1.7
|    \--- au.com.dius.pact:consumer:4.1.7 (*)
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2
|    |    |    |    +--- org.slf4j:slf4j-api:1.7.35 -> 1.7.36
|    |    |    |    \--- org.apache.logging.log4j:log4j-api:2.17.2
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.4.10
|    |    |    \--- net.minidev:accessors-smart:2.4.9
|    |    |         \--- org.ow2.asm:asm:9.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2 -> 5.8.2 (*)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 -> 5.7.1 (*)
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 -> 5.8.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.2 -> 5.7.1 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-engine:5.7.2 -> 5.7.1
|    |         +--- org.junit:junit-bom:5.7.1 -> 5.8.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         +--- org.junit.platform:junit-platform-engine:1.7.1 -> 1.7.2
|    |         |    +--- org.junit:junit-bom:5.7.2 -> 5.8.2 (*)
|    |         |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |         |    +--- org.opentest4j:opentest4j:1.2.0
|    |         |    \--- org.junit.platform:junit-platform-commons:1.7.2 -> 1.8.2 (*)
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.1 (*)
|    +--- org.mockito:mockito-core:3.9.0
|    |    +--- net.bytebuddy:byte-buddy:1.10.20 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.20 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.2
|    +--- org.mockito:mockito-junit-jupiter:3.9.0
|    |    +--- org.mockito:mockito-core:3.9.0 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.1 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.junit.jupiter:junit-jupiter-api:5.7.1 (*)
+--- com.fasterxml.jackson.core:jackson-databind:2.12.4 -> 2.11.4
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.11.4 -> 2.12.7
|    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (c)
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (c)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.11.4 (c)
|    |         \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (c)
|    \--- com.fasterxml.jackson.core:jackson-core:2.11.4 -> 2.12.7
|         \--- com.fasterxml.jackson:jackson-bom:2.12.7 (*)
+--- com.fasterxml.jackson.core:jackson-databind:2.11.4 (*)
+--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.7.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.7.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.5.8
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.5.8
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-openfeign-core:2.2.7.RELEASE -> 3.0.7
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15
|    |    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    |    \--- org.aspectj:aspectjweaver:1.9.7
|    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27
|    |         |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |         +--- commons-fileupload:commons-fileupload:1.4
|    |         |    \--- commons-io:commons-io:2.2
|    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    +--- org.springframework:spring-web:5.2.12.RELEASE -> 5.3.27 (*)
|    +--- org.springframework.cloud:spring-cloud-commons:2.2.7.RELEASE -> 3.0.6 (*)
|    +--- io.github.openfeign:feign-core:10.10.1 -> 10.12
|    +--- io.github.openfeign:feign-slf4j:10.10.1 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12
|    |    \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    \--- io.github.openfeign:feign-hystrix:10.10.1 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12
|         +--- com.netflix.archaius:archaius-core:0.7.6
|         |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|         |    +--- commons-configuration:commons-configuration:1.8
|         |    |    +--- commons-lang:commons-lang:2.6
|         |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|         |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|         |    +--- com.google.guava:guava:16.0 -> 30.1.1-jre (*)
|         |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|         |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|         |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.11.4 (*)
|         \--- com.netflix.hystrix:hystrix-core:1.5.18
|              +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|              +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.6 (*)
|              +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|              \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
+--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
+--- com.netflix.ribbon:ribbon-core:2.7.18
|    +--- org.slf4j:slf4j-api:1.7.2 -> 1.7.36
|    +--- com.google.code.findbugs:annotations:2.0.0
|    +--- com.google.guava:guava:19.0 -> 30.1.1-jre (*)
|    \--- commons-lang:commons-lang:2.6
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 2.2.7.RELEASE (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 -> 2.11.4 (*)
|    |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.11.4 (*)
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 (*)
|    |    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    |    \--- io.micrometer:micrometer-core:1.7.12
|    |         +--- org.hdrhistogram:HdrHistogram:2.1.12
|    |         \--- org.latencyutils:LatencyUtils:2.0.3
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12
|         \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.11.4 (*)
+--- com.google.guava:guava:30.1.1-jre (*)
+--- org.junit.jupiter:junit-jupiter-engine:5.7.1 (*)
\--- org.junit.platform:junit-platform-commons:1.8.2 (*)

contractTestRuntimeOnly - Runtime only dependencies for source set 'contract test'. (n)
+--- org.junit.jupiter:junit-jupiter-engine:5.7.1 (n)
\--- org.junit.platform:junit-platform-commons:1.8.2 (n)

coverageDataElementsForTest - Binary data file containing results of Jacoco test coverage reporting for the test Test Suite's test target. (n)
No dependencies

default - Configuration for default artifacts. (n)
No dependencies

developmentOnly - Configuration for development-only dependencies such as Spring Boot's DevTools.
No dependencies

functionalTestAnnotationProcessor - Annotation processors and their dependencies for source set 'functional test'.
\--- org.projectlombok:lombok:1.18.30

functionalTestCompileClasspath - Compile classpath for source set 'functional test'.
+--- org.projectlombok:lombok:1.18.30
+--- net.serenity-bdd:serenity-core:2.4.5
|    +--- net.serenity-bdd:serenity-model:2.4.5
|    |    +--- net.serenity-bdd:serenity-report-resources:2.4.5
|    |    +--- org.apache.commons:commons-lang3:3.11 -> 3.12.0
|    |    +--- commons-io:commons-io:2.6 -> 2.13.0
|    |    +--- org.apache.commons:commons-text:1.9 -> 1.10.0
|    |    |    \--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- commons-beanutils:commons-beanutils:1.9.4
|    |    |    +--- commons-logging:commons-logging:1.2
|    |    |    \--- commons-collections:commons-collections:3.2.2
|    |    +--- commons-net:commons-net:3.6 -> 3.8.0
|    |    +--- commons-collections:commons-collections:3.2.2
|    |    +--- commons-codec:commons-codec:1.15
|    |    +--- com.google.guava:guava:30.1-jre -> 32.1.3-jre
|    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    \--- com.google.j2objc:j2objc-annotations:2.8
|    |    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    |    +--- org.hamcrest:hamcrest:2.2
|    |    +--- com.google.inject:guice:5.0.1
|    |    |    +--- javax.inject:javax.inject:1
|    |    |    \--- aopalliance:aopalliance:1.0
|    |    +--- org.jsoup:jsoup:1.13.1
|    |    +--- com.thoughtworks.xstream:xstream:1.4.16
|    |    |    \--- io.github.x-stream:mxparser:1.2.1
|    |    |         \--- xmlpull:xmlpull:1.1.3.1
|    |    +--- joda-time:joda-time:2.8.2 -> 2.10.14
|    |    +--- io.cucumber:cucumber-core:6.10.2
|    |    |    +--- io.cucumber:cucumber-gherkin:6.10.2
|    |    |    |    \--- io.cucumber:cucumber-plugin:6.10.2
|    |    |    +--- io.cucumber:cucumber-gherkin-messages:6.10.2
|    |    |    |    \--- io.cucumber:cucumber-gherkin:6.10.2 (*)
|    |    |    +--- io.cucumber:messages:14.0.1
|    |    |    +--- io.cucumber:tag-expressions:3.0.0
|    |    |    +--- io.cucumber:cucumber-expressions:10.3.0
|    |    |    +--- io.cucumber:datatable:3.5.0
|    |    |    +--- io.cucumber:cucumber-plugin:6.10.2
|    |    |    +--- io.cucumber:docstring:6.10.2
|    |    |    +--- io.cucumber:html-formatter:12.0.0
|    |    |    |    \--- io.cucumber:messages:[14.0.1,15.0.0) -> 14.0.1
|    |    |    \--- io.cucumber:create-meta:3.0.0
|    |    |         \--- io.cucumber:messages:[14.0.1,15.0.0) -> 14.0.1
|    |    +--- io.cucumber:cucumber-java:6.10.2
|    |    |    \--- io.cucumber:cucumber-core:6.10.2 (*)
|    |    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    |    +--- net.sf.opencsv:opencsv:2.0
|    |    +--- com.typesafe:config:1.3.1
|    |    +--- org.imgscalr:imgscalr-lib:4.2
|    |    +--- org.awaitility:awaitility:4.0.3 -> 4.2.0
|    |    |    \--- org.hamcrest:hamcrest:2.1 -> 2.2
|    |    +--- org.freemarker:freemarker:2.3.29 -> 2.3.32
|    |    +--- net.sourceforge.jexcelapi:jxl:2.6.12
|    |    +--- org.asciidoctor:asciidoctorj:1.5.6
|    |    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    |    +--- net.bytebuddy:byte-buddy:1.10.10 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.10 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- net.serenity-bdd:serenity-reports:2.4.5
|    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    +--- net.serenity-bdd:serenity-stats:2.4.5
|    |    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.4.32 -> 1.5.32
|    |    |         +--- org.jetbrains:annotations:13.0
|    |    |         \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.32
|    |    +--- net.serenity-bdd:serenity-reports-configuration:2.4.5
|    |    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.4.32 -> 1.5.32 (*)
|    |    +--- com.vladsch.flexmark:flexmark-all:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30
|    |    |    |    \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-abbreviation:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    +--- org.nibor.autolink:autolink:0.6.0
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30
|    |    |    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |         \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30
|    |    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |         +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |         \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-admonition:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-anchorlink:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-aside:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-attributes:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-definition:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-enumerated-reference:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-attributes:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-escaped-character:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-footnotes:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-issues:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tables:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-users:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gitlab:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-front-matter:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-yaml-front-matter:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-tag:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-media-tags:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-test-util:0.34.30
|    |    |    |         \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-xwiki-macros:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-toc:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-yaml-front-matter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-youtube-embedded:0.34.30
|    |    |    |    \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-html-parser:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-pdf-converter:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-pdfbox:0.0.1-RC13
|    |    |    |    |    +--- org.apache.pdfbox:pdfbox:2.0.8 -> 2.0.29
|    |    |    |    |    |    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    +--- org.apache.pdfbox:xmpbox:2.0.8
|    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    |    \--- de.rototor.pdfbox:graphics2d:0.12
|    |    |    |    |         \--- org.apache.pdfbox:pdfbox:2.0.8 -> 2.0.29 (*)
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-rtl-support:0.0.1-RC13
|    |    |    |    |    +--- com.ibm.icu:icu4j:59.1
|    |    |    |    |    \--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    \--- com.openhtmltopdf:openhtmltopdf-jsoup-dom-converter:0.0.1-RC13
|    |    |    +--- com.vladsch.flexmark:flexmark-profile-pegdown:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-abbreviation:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-anchorlink:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-aside:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-definition:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-escaped-character:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-footnotes:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-front-matter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-toc:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    \--- com.vladsch.flexmark:flexmark-youtrack-converter:0.34.30
|    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |         +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |         +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |         \--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    \--- es.nitaur.markdown:txtmark:0.16
|    +--- net.serenity-bdd:serenity-report-resources:2.4.5
|    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    +--- commons-codec:commons-codec:1.15
|    +--- commons-io:commons-io:2.6 -> 2.13.0
|    +--- org.seleniumhq.selenium:selenium-server:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-java:3.141.59
|    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59
|    |    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9
|    |    |    |    |    |    \--- com.squareup.okio:okio:1.17.2
|    |    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-opera-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    +--- org.seleniumhq.selenium:selenium-safari-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- net.bytebuddy:byte-buddy:1.8.15 -> 1.10.22
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- net.bytebuddy:byte-buddy:1.8.15 -> 1.10.22
|    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-opera-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-safari-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    |    +--- com.beust:jcommander:1.72
|    |    +--- org.apache.commons:commons-exec:1.3
|    |    +--- net.jcip:jcip-annotations:1.0
|    |    +--- org.seleniumhq.selenium:jetty-repacked:9.4.12.v20180830
|    |    |    \--- javax.servlet:javax.servlet-api:3.1.0 -> 4.0.1
|    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    +--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    +--- javax.servlet:javax.servlet-api:3.1.0 -> 4.0.1
|    |    \--- org.yaml:snakeyaml:1.19 -> 2.0
|    +--- org.seleniumhq.selenium:selenium-java:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59 (*)
|    +--- net.sourceforge.htmlunit:htmlunit:2.47.1 -> 2.49.1
|    |    +--- xalan:xalan:2.7.2
|    |    |    \--- xalan:serializer:2.7.2
|    |    +--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- org.apache.commons:commons-text:1.9 -> 1.10.0 (*)
|    |    +--- org.apache.httpcomponents:httpmime:4.5.13 -> 4.5.14
|    |    |    \--- org.apache.httpcomponents:httpclient:4.5.14
|    |    |         +--- org.apache.httpcomponents:httpcore:4.4.16
|    |    |         +--- commons-logging:commons-logging:1.2
|    |    |         \--- commons-codec:commons-codec:1.11 -> 1.15
|    |    +--- net.sourceforge.htmlunit:htmlunit-core-js:2.49.0
|    |    +--- net.sourceforge.htmlunit:neko-htmlunit:2.49.0
|    |    |    \--- xerces:xercesImpl:2.12.0
|    |    |         \--- xml-apis:xml-apis:1.4.01
|    |    +--- net.sourceforge.htmlunit:htmlunit-cssparser:1.7.0
|    |    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    |    +--- commons-logging:commons-logging:1.2
|    |    +--- commons-net:commons-net:3.8.0
|    |    +--- org.brotli:dec:0.1.2
|    |    +--- com.shapesecurity:salvation2:3.0.0
|    |    \--- org.eclipse.jetty.websocket:websocket-client:9.4.39.v20210325 -> 9.4.51.v20230217
|    |         +--- org.eclipse.jetty:jetty-client:9.4.51.v20230217
|    |         |    +--- org.eclipse.jetty:jetty-http:9.4.51.v20230217
|    |         |    |    +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         |    |    \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217
|    |         |    |         \--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         |    \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    |         +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         +--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    |         \--- org.eclipse.jetty.websocket:websocket-common:9.4.51.v20230217
|    |              +--- org.eclipse.jetty.websocket:websocket-api:9.4.51.v20230217
|    |              +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |              \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    +--- org.seleniumhq.selenium:htmlunit-driver:2.47.1 -> 2.49.1
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    |    \--- net.sourceforge.htmlunit:htmlunit:2.49.1 (*)
|    +--- com.codeborne:phantomjsdriver:1.4.4
|    +--- io.appium:java-client:7.3.0
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- commons-validator:commons-validator:1.6
|    |    |    +--- commons-beanutils:commons-beanutils:1.9.2 -> 1.9.4 (*)
|    |    |    +--- commons-digester:commons-digester:1.8.1
|    |    |    \--- commons-collections:commons-collections:3.2.2
|    |    +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
|    |    +--- org.aspectj:aspectjweaver:1.9.4 -> 1.9.7
|    |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    +--- net.sf.opencsv:opencsv:2.0
|    +--- commons-beanutils:commons-beanutils:1.9.4 (*)
|    +--- org.apache.commons:commons-lang3:3.11 -> 3.12.0
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.fluentlenium:fluentlenium-core:0.10.2
|    +--- io.github.bonigarcia:webdrivermanager:4.4.1
|    |    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    |    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    |    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    |    +--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- org.apache.httpcomponents.client5:httpclient5:5.0.3 -> 5.0.4
|    |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.0.4 -> 5.1.5
|    |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.0.4 -> 5.1.5
|    |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.1.5
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    \--- commons-codec:commons-codec:1.15
|    |    +--- org.rauschig:jarchivelib:1.1.0
|    |    |    \--- org.apache.commons:commons-compress:1.20
|    |    \--- org.jsoup:jsoup:1.13.1
|    +--- com.jhlabs:filters:2.0.235
|    +--- com.assertthat:selenium-shutterbug:1.4
|    |    +--- org.seleniumhq.selenium:selenium-java:3.141.59 (*)
|    |    +--- commons-io:commons-io:2.6 -> 2.13.0
|    |    +--- org.projectlombok:lombok:1.18.8 -> 1.18.30
|    |    +--- com.github.zafarkhaja:java-semver:0.9.0
|    |    \--- io.github.bonigarcia:webdrivermanager:4.0.0 -> 4.4.1 (*)
|    +--- ru.yandex.qatools.ashot:ashot:1.5.4
|    |    +--- commons-io:commons-io:2.5 -> 2.13.0
|    |    \--- com.google.code.gson:gson:2.6.2 -> 2.8.9
|    +--- com.paulhammant:ngwebdriver:1.1.6
|    |    \--- org.seleniumhq.selenium:selenium-java:4.0.0-alpha-3 -> 3.141.59 (*)
|    +--- joda-time:joda-time:2.8.2 -> 2.10.14
|    +--- org.hamcrest:hamcrest:2.2
|    +--- com.google.jimfs:jimfs:1.1
|    |    \--- com.google.guava:guava:18.0 -> 32.1.3-jre (*)
|    +--- org.mockito:mockito-core:3.3.3 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.assertj:assertj-core:3.18.0 -> 3.19.0
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    |    \--- javax.activation:javax.activation-api:1.2.0
|    +--- com.sun.xml.bind:jaxb-core:2.3.0.1
|    +--- com.sun.xml.bind:jaxb-impl:2.3.3
|    |    \--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |         \--- jakarta.activation:jakarta.activation-api:1.2.2
|    \--- javax.activation:activation:1.1.1
+--- net.serenity-bdd:serenity-junit:2.4.5
|    \--- net.serenity-bdd:serenity-core:2.4.5 (*)
+--- net.serenity-bdd:serenity-rest-assured:2.4.5
|    +--- net.serenity-bdd:serenity-core:2.4.5 (*)
|    +--- io.rest-assured:rest-assured:4.3.3
|    |    +--- org.hamcrest:hamcrest:2.1 -> 2.2
|    |    +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    |    +--- io.rest-assured:json-path:4.3.3
|    |    |    \--- io.rest-assured:rest-assured-common:4.3.3
|    |    \--- io.rest-assured:xml-path:4.3.3
|    |         +--- io.rest-assured:rest-assured-common:4.3.3
|    |         +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3 (*)
|    |         \--- com.sun.xml.bind:jaxb-impl:2.3.3 (*)
|    +--- org.codehaus.groovy:groovy-json:3.0.8 -> 3.0.17
|    |    \--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.codehaus.groovy:groovy-xml:3.0.8 -> 3.0.17
|    |    \--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    \--- org.opentest4j:opentest4j:1.2.0
+--- net.serenity-bdd:serenity-spring:2.4.5
|    +--- net.serenity-bdd:serenity-core:2.4.5 (*)
|    +--- net.serenity-bdd:serenity-junit:2.4.5 (*)
|    +--- junit:junit:4.13.1 -> 4.13.2
|    +--- org.springframework:spring-test:5.3.3 -> 5.3.27
|    |    \--- org.springframework:spring-core:5.3.27
|    |         \--- org.springframework:spring-jcl:5.3.27
|    +--- org.springframework:spring-context:5.3.3 -> 5.3.27
|    |    +--- org.springframework:spring-aop:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27
|    |    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.27
|    |         \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.springframework:spring-context-support:5.3.3 -> 5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    |    \--- org.springframework:spring-context:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    \--- commons-configuration:commons-configuration:1.8
|    |         \--- commons-lang:commons-lang:2.6
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29 (*)
+--- org.apache.commons:commons-text:1.10.0 (*)
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.30
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1 (*)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3 (*)
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    \--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7 (*)
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    \--- org.mockito:mockito-core:3.7.7 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0 (*)
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36

functionalTestCompileOnly - Compile only dependencies for source set 'functional test'. (n)
\--- org.projectlombok:lombok:1.18.30 (n)

functionalTestImplementation - Implementation only dependencies for source set 'functional test'. (n)
+--- net.serenity-bdd:serenity-core:2.4.5 (n)
+--- net.serenity-bdd:serenity-junit:2.4.5 (n)
+--- net.serenity-bdd:serenity-rest-assured:2.4.5 (n)
+--- net.serenity-bdd:serenity-spring:2.4.5 (n)
+--- unspecified (n)
+--- unspecified (n)
+--- unspecified (n)
\--- unspecified (n)

functionalTestRuntime
No dependencies

functionalTestRuntimeClasspath - Runtime classpath of source set 'functional test'.
+--- net.serenity-bdd:serenity-core:2.4.5
|    +--- net.serenity-bdd:serenity-model:2.4.5
|    |    +--- net.serenity-bdd:serenity-report-resources:2.4.5
|    |    +--- org.apache.commons:commons-lang3:3.11 -> 3.12.0
|    |    +--- commons-io:commons-io:2.6 -> 2.13.0
|    |    +--- org.apache.commons:commons-text:1.9 -> 1.10.0
|    |    |    \--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- commons-beanutils:commons-beanutils:1.9.4
|    |    |    +--- commons-logging:commons-logging:1.2
|    |    |    \--- commons-collections:commons-collections:3.2.2
|    |    +--- commons-net:commons-net:3.6 -> 3.8.0
|    |    +--- commons-collections:commons-collections:3.2.2
|    |    +--- commons-codec:commons-codec:1.15
|    |    +--- com.google.guava:guava:30.1-jre -> 32.1.3-jre
|    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    |    +--- org.hamcrest:hamcrest:2.2
|    |    +--- com.google.inject:guice:5.0.1
|    |    |    +--- javax.inject:javax.inject:1
|    |    |    \--- aopalliance:aopalliance:1.0
|    |    +--- org.jsoup:jsoup:1.13.1
|    |    +--- com.thoughtworks.xstream:xstream:1.4.16
|    |    |    \--- io.github.x-stream:mxparser:1.2.1
|    |    |         \--- xmlpull:xmlpull:1.1.3.1
|    |    +--- joda-time:joda-time:2.8.2 -> 2.10.14
|    |    +--- io.cucumber:cucumber-core:6.10.2
|    |    |    +--- io.cucumber:cucumber-gherkin:6.10.2
|    |    |    |    \--- io.cucumber:cucumber-plugin:6.10.2
|    |    |    +--- io.cucumber:cucumber-gherkin-messages:6.10.2
|    |    |    |    \--- io.cucumber:cucumber-gherkin:6.10.2 (*)
|    |    |    +--- io.cucumber:messages:14.0.1
|    |    |    +--- io.cucumber:tag-expressions:3.0.0
|    |    |    +--- io.cucumber:cucumber-expressions:10.3.0
|    |    |    +--- io.cucumber:datatable:3.5.0
|    |    |    +--- io.cucumber:cucumber-plugin:6.10.2
|    |    |    +--- io.cucumber:docstring:6.10.2
|    |    |    +--- io.cucumber:html-formatter:12.0.0
|    |    |    |    \--- io.cucumber:messages:[14.0.1,15.0.0) -> 14.0.1
|    |    |    \--- io.cucumber:create-meta:3.0.0
|    |    |         \--- io.cucumber:messages:[14.0.1,15.0.0) -> 14.0.1
|    |    +--- io.cucumber:cucumber-java:6.10.2
|    |    |    \--- io.cucumber:cucumber-core:6.10.2 (*)
|    |    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    |    +--- net.sf.opencsv:opencsv:2.0
|    |    +--- com.typesafe:config:1.3.1
|    |    +--- org.imgscalr:imgscalr-lib:4.2
|    |    +--- org.awaitility:awaitility:4.0.3 -> 4.2.0
|    |    |    \--- org.hamcrest:hamcrest:2.1 -> 2.2
|    |    +--- org.freemarker:freemarker:2.3.29 -> 2.3.32
|    |    +--- net.sourceforge.jexcelapi:jxl:2.6.12
|    |    +--- org.asciidoctor:asciidoctorj:1.5.6
|    |    |    \--- org.jruby:jruby-complete:1.7.26
|    |    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    |    +--- net.bytebuddy:byte-buddy:1.10.10 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.10 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- net.serenity-bdd:serenity-reports:2.4.5
|    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    +--- net.serenity-bdd:serenity-stats:2.4.5
|    |    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.4.32 -> 1.5.32
|    |    |         +--- org.jetbrains:annotations:13.0
|    |    |         \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.32
|    |    +--- net.serenity-bdd:serenity-reports-configuration:2.4.5
|    |    |    +--- net.serenity-bdd:serenity-model:2.4.5 (*)
|    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.4.32 -> 1.5.32 (*)
|    |    +--- com.vladsch.flexmark:flexmark-all:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30
|    |    |    |    \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-abbreviation:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    +--- org.nibor.autolink:autolink:0.6.0
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30
|    |    |    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |         \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30
|    |    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |         +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |         \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-admonition:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-anchorlink:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-aside:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-attributes:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-definition:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-enumerated-reference:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-attributes:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-escaped-character:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-footnotes:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-issues:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tables:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-users:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-gitlab:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-front-matter:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-yaml-front-matter:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-tag:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-media-tags:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-test-util:0.34.30
|    |    |    |         \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-xwiki-macros:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-toc:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-yaml-front-matter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-ext-youtube-embedded:0.34.30
|    |    |    |    \--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-formatter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-html-parser:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-jira-converter:0.34.30 (*)
|    |    |    +--- com.vladsch.flexmark:flexmark-pdf-converter:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-pdfbox:0.0.1-RC13
|    |    |    |    |    +--- org.apache.pdfbox:pdfbox:2.0.8 -> 2.0.29
|    |    |    |    |    |    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    +--- org.apache.pdfbox:xmpbox:2.0.8
|    |    |    |    |    |    \--- commons-logging:commons-logging:1.2
|    |    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    |    \--- de.rototor.pdfbox:graphics2d:0.12
|    |    |    |    |         \--- org.apache.pdfbox:pdfbox:2.0.8 -> 2.0.29 (*)
|    |    |    |    +--- com.openhtmltopdf:openhtmltopdf-rtl-support:0.0.1-RC13
|    |    |    |    |    +--- com.ibm.icu:icu4j:59.1
|    |    |    |    |    \--- com.openhtmltopdf:openhtmltopdf-core:0.0.1-RC13
|    |    |    |    \--- com.openhtmltopdf:openhtmltopdf-jsoup-dom-converter:0.0.1-RC13
|    |    |    +--- com.vladsch.flexmark:flexmark-profile-pegdown:0.34.30
|    |    |    |    +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-abbreviation:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-anchorlink:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-aside:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-autolink:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-definition:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-emoji:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-escaped-character:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-footnotes:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-ins:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-jekyll-front-matter:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-superscript:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-toc:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-typographic:0.34.30 (*)
|    |    |    |    +--- com.vladsch.flexmark:flexmark-ext-wikilink:0.34.30 (*)
|    |    |    |    \--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |    \--- com.vladsch.flexmark:flexmark-youtrack-converter:0.34.30
|    |    |         +--- com.vladsch.flexmark:flexmark-util:0.34.30
|    |    |         +--- com.vladsch.flexmark:flexmark:0.34.30 (*)
|    |    |         +--- com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.34.30 (*)
|    |    |         \--- com.vladsch.flexmark:flexmark-ext-tables:0.34.30 (*)
|    |    \--- es.nitaur.markdown:txtmark:0.16
|    +--- net.serenity-bdd:serenity-report-resources:2.4.5
|    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    +--- commons-codec:commons-codec:1.15
|    +--- commons-io:commons-io:2.6 -> 2.13.0
|    +--- org.seleniumhq.selenium:selenium-server:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-java:3.141.59
|    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59
|    |    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9
|    |    |    |    |    |    \--- com.squareup.okio:okio:1.17.2
|    |    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-opera-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    +--- org.seleniumhq.selenium:selenium-safari-driver:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    |    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    |    |    +--- net.bytebuddy:byte-buddy:1.8.15 -> 1.10.22
|    |    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    |    +--- net.bytebuddy:byte-buddy:1.8.15 -> 1.10.22
|    |    |    +--- org.apache.commons:commons-exec:1.3
|    |    |    +--- com.google.guava:guava:25.0-jre -> 32.1.3-jre (*)
|    |    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    |    \--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-opera-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-remote-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-safari-driver:3.141.59 (*)
|    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    |    +--- com.beust:jcommander:1.72
|    |    +--- org.apache.commons:commons-exec:1.3
|    |    +--- net.jcip:jcip-annotations:1.0
|    |    +--- org.seleniumhq.selenium:jetty-repacked:9.4.12.v20180830
|    |    |    \--- javax.servlet:javax.servlet-api:3.1.0 -> 4.0.1
|    |    +--- com.squareup.okhttp3:okhttp:3.11.0 -> 3.14.9 (*)
|    |    +--- com.squareup.okio:okio:1.14.0 -> 1.17.2
|    |    +--- javax.servlet:javax.servlet-api:3.1.0 -> 4.0.1
|    |    \--- org.yaml:snakeyaml:1.19 -> 2.0
|    +--- org.seleniumhq.selenium:selenium-java:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-chrome-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-firefox-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-edge-driver:3.141.59 (*)
|    +--- org.seleniumhq.selenium:selenium-ie-driver:3.141.59 (*)
|    +--- net.sourceforge.htmlunit:htmlunit:2.47.1 -> 2.49.1
|    |    +--- xalan:xalan:2.7.2
|    |    |    \--- xalan:serializer:2.7.2
|    |    +--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- org.apache.commons:commons-text:1.9 -> 1.10.0 (*)
|    |    +--- org.apache.httpcomponents:httpmime:4.5.13 -> 4.5.14
|    |    |    \--- org.apache.httpcomponents:httpclient:4.5.14
|    |    |         +--- org.apache.httpcomponents:httpcore:4.4.16
|    |    |         +--- commons-logging:commons-logging:1.2
|    |    |         \--- commons-codec:commons-codec:1.11 -> 1.15
|    |    +--- net.sourceforge.htmlunit:htmlunit-core-js:2.49.0
|    |    +--- net.sourceforge.htmlunit:neko-htmlunit:2.49.0
|    |    |    \--- xerces:xercesImpl:2.12.0
|    |    |         \--- xml-apis:xml-apis:1.4.01
|    |    +--- net.sourceforge.htmlunit:htmlunit-cssparser:1.7.0
|    |    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    |    +--- commons-logging:commons-logging:1.2
|    |    +--- commons-net:commons-net:3.8.0
|    |    +--- org.brotli:dec:0.1.2
|    |    +--- com.shapesecurity:salvation2:3.0.0
|    |    \--- org.eclipse.jetty.websocket:websocket-client:9.4.39.v20210325 -> 9.4.51.v20230217
|    |         +--- org.eclipse.jetty:jetty-client:9.4.51.v20230217
|    |         |    +--- org.eclipse.jetty:jetty-http:9.4.51.v20230217
|    |         |    |    +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         |    |    \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217
|    |         |    |         \--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         |    \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    |         +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |         +--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    |         \--- org.eclipse.jetty.websocket:websocket-common:9.4.51.v20230217
|    |              +--- org.eclipse.jetty.websocket:websocket-api:9.4.51.v20230217
|    |              +--- org.eclipse.jetty:jetty-util:9.4.51.v20230217
|    |              \--- org.eclipse.jetty:jetty-io:9.4.51.v20230217 (*)
|    +--- org.seleniumhq.selenium:htmlunit-driver:2.47.1 -> 2.49.1
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- org.seleniumhq.selenium:selenium-support:3.141.59 (*)
|    |    \--- net.sourceforge.htmlunit:htmlunit:2.49.1 (*)
|    +--- com.codeborne:phantomjsdriver:1.4.4
|    +--- io.appium:java-client:7.3.0
|    |    +--- org.seleniumhq.selenium:selenium-api:3.141.59
|    |    +--- commons-validator:commons-validator:1.6
|    |    |    +--- commons-beanutils:commons-beanutils:1.9.2 -> 1.9.4 (*)
|    |    |    +--- commons-digester:commons-digester:1.8.1
|    |    |    \--- commons-collections:commons-collections:3.2.2
|    |    +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
|    |    +--- org.aspectj:aspectjweaver:1.9.4 -> 1.9.7
|    |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    +--- net.sf.opencsv:opencsv:2.0
|    +--- commons-beanutils:commons-beanutils:1.9.4 (*)
|    +--- org.apache.commons:commons-lang3:3.11 -> 3.12.0
|    +--- commons-collections:commons-collections:3.2.2
|    +--- org.fluentlenium:fluentlenium-core:0.10.2
|    +--- io.github.bonigarcia:webdrivermanager:4.4.1
|    |    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    |    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    |    +--- com.google.code.gson:gson:2.8.6 -> 2.8.9
|    |    +--- org.apache.commons:commons-lang3:3.12.0
|    |    +--- org.apache.httpcomponents.client5:httpclient5:5.0.3 -> 5.0.4
|    |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.0.4 -> 5.1.5
|    |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.0.4 -> 5.1.5
|    |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.1.5
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    \--- commons-codec:commons-codec:1.15
|    |    +--- org.rauschig:jarchivelib:1.1.0
|    |    |    \--- org.apache.commons:commons-compress:1.20
|    |    \--- org.jsoup:jsoup:1.13.1
|    +--- com.jhlabs:filters:2.0.235
|    +--- com.assertthat:selenium-shutterbug:1.4
|    |    +--- org.seleniumhq.selenium:selenium-java:3.141.59 (*)
|    |    +--- commons-io:commons-io:2.6 -> 2.13.0
|    |    +--- org.projectlombok:lombok:1.18.8 -> 1.18.26
|    |    +--- com.github.zafarkhaja:java-semver:0.9.0
|    |    \--- io.github.bonigarcia:webdrivermanager:4.0.0 -> 4.4.1 (*)
|    +--- ru.yandex.qatools.ashot:ashot:1.5.4
|    |    +--- commons-io:commons-io:2.5 -> 2.13.0
|    |    \--- com.google.code.gson:gson:2.6.2 -> 2.8.9
|    +--- com.paulhammant:ngwebdriver:1.1.6
|    |    \--- org.seleniumhq.selenium:selenium-java:4.0.0-alpha-3 -> 3.141.59 (*)
|    +--- joda-time:joda-time:2.8.2 -> 2.10.14
|    +--- org.hamcrest:hamcrest:2.2
|    +--- com.google.jimfs:jimfs:1.1
|    |    \--- com.google.guava:guava:18.0 -> 32.1.3-jre (*)
|    +--- org.mockito:mockito-core:3.3.3 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.assertj:assertj-core:3.18.0 -> 3.19.0
|    +--- javax.xml.bind:jaxb-api:2.3.1
|    |    \--- javax.activation:javax.activation-api:1.2.0
|    +--- com.sun.xml.bind:jaxb-core:2.3.0.1
|    +--- com.sun.xml.bind:jaxb-impl:2.3.3
|    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    |    \--- com.sun.activation:jakarta.activation:1.2.2
|    \--- javax.activation:activation:1.1.1
+--- net.serenity-bdd:serenity-junit:2.4.5
|    \--- net.serenity-bdd:serenity-core:2.4.5 (*)
+--- net.serenity-bdd:serenity-rest-assured:2.4.5
|    +--- net.serenity-bdd:serenity-core:2.4.5 (*)
|    +--- io.rest-assured:rest-assured:4.3.3
|    |    +--- org.hamcrest:hamcrest:2.1 -> 2.2
|    |    +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    |    +--- io.rest-assured:json-path:4.3.3
|    |    |    \--- io.rest-assured:rest-assured-common:4.3.3
|    |    \--- io.rest-assured:xml-path:4.3.3
|    |         +--- io.rest-assured:rest-assured-common:4.3.3
|    |         +--- org.ccil.cowan.tagsoup:tagsoup:1.2.1
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3 (*)
|    |         \--- com.sun.xml.bind:jaxb-impl:2.3.3 (*)
|    +--- org.codehaus.groovy:groovy-json:3.0.8 -> 3.0.17
|    |    \--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.codehaus.groovy:groovy-xml:3.0.8 -> 3.0.17
|    |    \--- org.codehaus.groovy:groovy:3.0.17
|    +--- org.codehaus.groovy:groovy:3.0.8 -> 3.0.17
|    \--- org.opentest4j:opentest4j:1.2.0
+--- net.serenity-bdd:serenity-spring:2.4.5
|    +--- net.serenity-bdd:serenity-core:2.4.5 (*)
|    +--- net.serenity-bdd:serenity-junit:2.4.5 (*)
|    +--- junit:junit:4.13.1 -> 4.13.2
|    +--- org.springframework:spring-test:5.3.3 -> 5.3.27
|    |    \--- org.springframework:spring-core:5.3.27
|    |         \--- org.springframework:spring-jcl:5.3.27
|    +--- org.springframework:spring-context:5.3.3 -> 5.3.27
|    |    +--- org.springframework:spring-aop:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27
|    |    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.27
|    |         \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.springframework:spring-context-support:5.3.3 -> 5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    |    \--- org.springframework:spring-context:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29 (*)
+--- org.apache.commons:commons-text:1.10.0 (*)
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    |    \--- commons-codec:commons-codec:1.14 -> 1.15
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1 (*)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3 (*)
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-engine:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-engine:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         +--- org.junit.platform:junit-platform-engine:1.7.2
|    |         |    +--- org.junit:junit-bom:5.7.2 (*)
|    |         |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |         |    +--- org.opentest4j:opentest4j:1.2.0
|    |         |    \--- org.junit.platform:junit-platform-commons:1.7.2 (*)
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7 (*)
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    +--- org.mockito:mockito-core:3.7.7 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.0 -> 5.7.2 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0 (*)
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
     \--- org.junit.jupiter:junit-jupiter:5.7.0 -> 5.7.2 (*)

functionalTestRuntimeOnly - Runtime only dependencies for source set 'functional test'. (n)
No dependencies

implementation - Implementation only dependencies for source set 'main'. (n)
+--- org.springframework.boot:spring-boot-starter-web (n)
+--- org.springframework.boot:spring-boot-starter-jdbc (n)
+--- org.springframework.boot:spring-boot-starter-quartz (n)
+--- org.springframework.boot:spring-boot-starter-actuator (n)
+--- org.springframework.retry:spring-retry (n)
+--- org.springframework.boot:spring-boot-starter-validation (n)
+--- org.springframework.boot:spring-boot-starter-mail (n)
+--- org.springframework.security:spring-security-config:5.7.10 (n)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10 (n)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10 (n)
+--- org.springframework.security:spring-security-core:5.7.10 (n)
+--- org.springframework.security:spring-security-crypto:5.7.10 (n)
+--- org.springframework.security:spring-security-web:5.7.10 (n)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (n)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (n)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE (n)
+--- org.apache.logging.log4j:log4j-api:2.20.0 (n)
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (n)
+--- org.apache.pdfbox:pdfbox:2.0.29 (n)
+--- org.apache.commons:commons-text:1.10.0 (n)
+--- org.apache.commons:commons-csv:1.10.0 (n)
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9 (n)
+--- io.github.openfeign:feign-core:11.2 (n)
+--- org.yaml:snakeyaml:2.0 (n)
+--- org.postgresql:postgresql:42.6.0 (n)
+--- org.flywaydb:flyway-core:8.5.13 (n)
+--- io.springfox:springfox-swagger2:2.9.2 (n)
+--- io.springfox:springfox-swagger-ui:2.9.2 (n)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0 (n)
+--- uk.gov.hmcts.reform:logging:5.1.7 (n)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7 (n)
+--- commons-fileupload:commons-fileupload:1.5 (n)
+--- uk.gov.hmcts.reform:idam-client:2.0.0 (n)
+--- uk.gov.hmcts.reform:document-management-client:7.0.0 (n)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6 (n)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0 (n)
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4 (n)
+--- com.github.hmcts:send-letter-client:3.0.16 (n)
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE (n)
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (n)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (n)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3 (n)
+--- com.google.guava:guava:32.1.3-jre (n)
+--- commons-io:commons-io:2.13.0 (n)
+--- javax.xml.bind:jaxb-api:2.3.1 (n)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (n)
\--- net.minidev:json-smart:2.5.0 (n)

integrationTestAnnotationProcessor - Annotation processors and their dependencies for source set 'integration test'.
No dependencies

integrationTestCompileClasspath - Compile classpath for source set 'integration test'.
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre
|    |         |    +--- com.google.guava:failureaccess:1.0.1
|    |         |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |         |    +--- org.checkerframework:checker-qual:3.37.0
|    |         |    +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         |    \--- com.google.j2objc:j2objc-annotations:2.8
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6
|         \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6
|    |    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6 (*)
|    |    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15
|    |    |    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    |    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    |    |    \--- org.aspectj:aspectjweaver:1.9.7
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    \--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    \--- org.mockito:mockito-core:3.7.7 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36

integrationTestCompileOnly - Compile only dependencies for source set 'integration test'. (n)
No dependencies

integrationTestImplementation - Implementation only dependencies for source set 'integration test'. (n)
+--- org.apache.pdfbox:pdfbox:2.0.29 (n)
+--- org.apache.commons:commons-text:1.10.0 (n)
+--- unspecified (n)
\--- unspecified (n)

integrationTestRuntime
No dependencies

integrationTestRuntimeClasspath - Runtime classpath of source set 'integration test'.
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre
|    |         |    +--- com.google.guava:failureaccess:1.0.1
|    |         |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |         |    +--- org.checkerframework:checker-qual:3.37.0
|    |         |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6
|         \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6
|    |    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6 (*)
|    |    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15
|    |    |    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    |    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    |    |    \--- org.aspectj:aspectjweaver:1.9.7
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    |    \--- commons-codec:commons-codec:1.14 -> 1.15
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-engine:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-engine:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         +--- org.junit.platform:junit-platform-engine:1.7.2
|    |         |    +--- org.junit:junit-bom:5.7.2 (*)
|    |         |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |         |    +--- org.opentest4j:opentest4j:1.2.0
|    |         |    \--- org.junit.platform:junit-platform-commons:1.7.2 (*)
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    +--- org.mockito:mockito-core:3.7.7 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.0 -> 5.7.2 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
     \--- org.junit.jupiter:junit-jupiter:5.7.0 -> 5.7.2 (*)

integrationTestRuntimeOnly - Runtime only dependencies for source set 'integration test'. (n)
No dependencies

jacocoAgent - The Jacoco agent to use to get coverage data.
\--- org.jacoco:org.jacoco.agent:0.8.8

jacocoAnt - The Jacoco ant tasks to use to get execute Gradle tasks.
\--- org.jacoco:org.jacoco.ant:0.8.8
     +--- org.jacoco:org.jacoco.core:0.8.8
     |    +--- org.ow2.asm:asm:9.2
     |    +--- org.ow2.asm:asm-commons:9.2
     |    |    +--- org.ow2.asm:asm:9.2
     |    |    +--- org.ow2.asm:asm-tree:9.2
     |    |    |    \--- org.ow2.asm:asm:9.2
     |    |    \--- org.ow2.asm:asm-analysis:9.2
     |    |         \--- org.ow2.asm:asm-tree:9.2 (*)
     |    \--- org.ow2.asm:asm-tree:9.2 (*)
     +--- org.jacoco:org.jacoco.report:0.8.8
     |    \--- org.jacoco:org.jacoco.core:0.8.8 (*)
     \--- org.jacoco:org.jacoco.agent:0.8.8

mainSourceElements - List of source directories contained in the Main SourceSet. (n)
No dependencies

pitest - The PIT libraries to be used for this project.
+--- org.pitest:pitest-command-line:1.15.0
|    \--- org.pitest:pitest-entry:1.15.0
|         +--- org.pitest:pitest:1.15.0
|         \--- org.apache.commons:commons-text:1.10.0
|              \--- org.apache.commons:commons-lang3:3.12.0
\--- org.pitest:pitest-junit5-plugin:1.1.1

productionRuntimeClasspath
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre
|    |    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    |    \--- commons-codec:commons-codec:1.14 -> 1.15
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
\--- net.minidev:json-smart:2.5.0
     \--- net.minidev:accessors-smart:2.5.0
          \--- org.ow2.asm:asm:9.3

runtimeClasspath - Runtime classpath of source set 'main'.
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre
|    |    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    |    \--- commons-codec:commons-codec:1.14 -> 1.15
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
\--- net.minidev:json-smart:2.5.0
     \--- net.minidev:accessors-smart:2.5.0
          \--- org.ow2.asm:asm:9.3

runtimeElements - Elements of runtime for main. (n)
No dependencies

runtimeOnly - Runtime only dependencies for source set 'main'. (n)
No dependencies

smokeTestAnnotationProcessor - Annotation processors and their dependencies for source set 'smoke test'.
No dependencies

smokeTestCompileClasspath - Compile classpath for source set 'smoke test'.
No dependencies

smokeTestCompileOnly - Compile only dependencies for source set 'smoke test'. (n)
No dependencies

smokeTestImplementation - Implementation only dependencies for source set 'smoke test'. (n)
+--- unspecified (n)
\--- unspecified (n)

smokeTestRuntimeClasspath - Runtime classpath of source set 'smoke test'.
No dependencies

smokeTestRuntimeOnly - Runtime only dependencies for source set 'smoke test'. (n)
No dependencies

testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
No dependencies

testCompileClasspath - Compile classpath for source set 'test'.
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         \--- org.hdrhistogram:HdrHistogram:2.1.12
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    \--- commons-configuration:commons-configuration:1.8
|    |         \--- commons-lang:commons-lang:2.6
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre
|    |         +--- com.google.guava:failureaccess:1.0.1
|    |         +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         +--- com.google.code.findbugs:jsr305:3.0.2
|    |         +--- org.checkerframework:checker-qual:3.37.0
|    |         +--- com.google.errorprone:error_prone_annotations:2.21.1
|    |         \--- com.google.j2objc:j2objc-annotations:2.8
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    \--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|         \--- com.microsoft.azure:applicationinsights-web:2.6.1
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.8 -> 2.12.7.1 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    \--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    \--- org.mockito:mockito-core:3.7.7 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
\--- com.github.hmcts:fortify-client:1.2.0
     +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
     +--- commons-io:commons-io:2.6 -> 2.13.0
     \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36

testCompileOnly - Compile only dependencies for source set 'test'. (n)
No dependencies

testImplementation - Implementation only dependencies for source set 'test'. (n)
+--- org.pitest:pitest:1.15.1 (n)
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0 (n)
+--- org.pitest:pitest-junit5-plugin:1.1.1 (n)
+--- org.apache.pdfbox:pdfbox:2.0.29 (n)
+--- org.springframework.boot:spring-boot-starter-test (n)
+--- org.springframework.retry:spring-retry (n)
+--- org.awaitility:awaitility:4.2.0 (n)
+--- org.springframework.security:spring-security-test (n)
+--- org.mockito:mockito-core:3.7.7 (n)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (n)
+--- org.mockito:mockito-inline:3.7.7 (n)
\--- com.github.hmcts:fortify-client:1.2.0 (n)

testResultsElementsForTest - Directory containing binary results of running tests for the test Test Suite's test target. (n)
No dependencies

testRuntime
No dependencies

testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- org.springframework.boot:spring-boot-starter-web -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15
|    |    |    +--- org.springframework:spring-core:5.3.27
|    |    |    |    \--- org.springframework:spring-jcl:5.3.27
|    |    |    \--- org.springframework:spring-context:5.3.27
|    |    |         +--- org.springframework:spring-aop:5.3.27
|    |    |         |    +--- org.springframework:spring-beans:5.3.27
|    |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    |         +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |         +--- org.springframework:spring-core:5.3.27 (*)
|    |    |         \--- org.springframework:spring-expression:5.3.27
|    |    |              \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-starter-logging:2.5.15 -> 2.7.12
|    |    |    +--- ch.qos.logback:logback-classic:1.2.12
|    |    |    |    +--- ch.qos.logback:logback-core:1.2.12
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
|    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2 -> 2.20.0
|    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.20.0
|    |    |    |    \--- org.slf4j:slf4j-api:1.7.36
|    |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
|    |    |         \--- org.slf4j:slf4j-api:1.7.36
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.springframework:spring-core:5.3.27 (*)
|    |    \--- org.yaml:snakeyaml:1.28 -> 2.0
|    +--- org.springframework.boot:spring-boot-starter-json:2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-web:5.3.27
|    |    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    |    \--- org.springframework:spring-core:5.3.27 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3 -> 2.12.7 (c)
|    |    |    |         +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.15.3 -> 2.12.7 (c)
|    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3 -> 2.12.7 (c)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7
|    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    |    \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.7
|    |         +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |         +--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |         \--- com.fasterxml.jackson:jackson-bom:2.12.7 -> 2.15.3 (*)
|    +--- org.springframework.boot:spring-boot-starter-tomcat:2.5.15
|    |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
|    |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.75 -> 9.0.82
|    |    |    \--- org.apache.tomcat:tomcat-annotations-api:9.0.82 -> 9.0.75
|    |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.75 -> 9.0.82
|    |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
|    +--- org.springframework:spring-web:5.3.27 (*)
|    \--- org.springframework:spring-webmvc:5.3.27
|         +--- org.springframework:spring-aop:5.3.27 (*)
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-context:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         +--- org.springframework:spring-expression:5.3.27 (*)
|         \--- org.springframework:spring-web:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-jdbc -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- com.zaxxer:HikariCP:4.0.3
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    \--- org.springframework:spring-jdbc:5.3.27
|         +--- org.springframework:spring-beans:5.3.27 (*)
|         +--- org.springframework:spring-core:5.3.27 (*)
|         \--- org.springframework:spring-tx:5.3.27
|              +--- org.springframework:spring-beans:5.3.27 (*)
|              \--- org.springframework:spring-core:5.3.27 (*)
+--- org.springframework.boot:spring-boot-starter-quartz -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27
|    |    +--- org.springframework:spring-beans:5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.27 (*)
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-tx:5.3.27 (*)
|    \--- org.quartz-scheduler:quartz:2.3.2
|         +--- com.mchange:mchange-commons-java:0.2.15
|         \--- org.slf4j:slf4j-api:1.7.7 -> 1.7.36
+--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-actuator-autoconfigure:2.5.15
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.12.7.1 (*)
|    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7 (*)
|    |    +--- org.springframework.boot:spring-boot-actuator:2.5.15
|    |    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    \--- io.micrometer:micrometer-core:1.7.12
|         +--- org.hdrhistogram:HdrHistogram:2.1.12
|         \--- org.latencyutils:LatencyUtils:2.0.3
+--- org.springframework.retry:spring-retry -> 1.3.4
+--- org.springframework.boot:spring-boot-starter-validation -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.75
|    \--- org.hibernate.validator:hibernate-validator:6.2.5.Final
|         +--- jakarta.validation:jakarta.validation-api:2.0.2
|         +--- org.jboss.logging:jboss-logging:3.4.1.Final -> 3.4.3.Final
|         \--- com.fasterxml:classmate:1.5.1
+--- org.springframework.boot:spring-boot-starter-mail -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework:spring-context-support:5.3.27 (*)
|    \--- com.sun.mail:jakarta.mail:1.6.7
|         \--- com.sun.activation:jakarta.activation:1.2.1 -> 1.2.2
+--- org.springframework.security:spring-security-config:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10
|    |    +--- org.springframework.security:spring-security-crypto:5.7.10
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-resource-server:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    +--- org.springframework.security:spring-security-web:5.7.10
|    |    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    |    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-aop:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-beans:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-context:5.3.29 -> 5.3.27 (*)
|    |    +--- org.springframework:spring-expression:5.3.29 -> 5.3.27 (*)
|    |    \--- org.springframework:spring-web:5.3.29 -> 5.3.27 (*)
|    \--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
+--- org.springframework.security:spring-security-oauth2-jose:5.7.10
|    +--- org.springframework.security:spring-security-core:5.7.10 (*)
|    +--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.29 -> 5.3.27 (*)
|    \--- com.nimbusds:nimbus-jose-jwt:9.22 -> 9.10.1
|         \--- com.github.stephenc.jcip:jcip-annotations:1.0-1
+--- org.springframework.security:spring-security-core:5.7.10 (*)
+--- org.springframework.security:spring-security-crypto:5.7.10
+--- org.springframework.security:spring-security-web:5.7.10 (*)
+--- org.springframework.security:spring-security-oauth2-core:5.7.10 (*)
+--- org.springframework.boot:spring-boot-starter-logging:2.7.12 (*)
+--- org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6
|    |    +--- org.springframework.boot:spring-boot-starter:2.4.13 -> 2.5.15 (*)
|    |    +--- org.springframework.cloud:spring-cloud-context:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6
|    |    |    \--- org.springframework.security:spring-security-crypto:5.4.9 -> 5.7.10
|    |    \--- org.springframework.security:spring-security-rsa:1.0.11.RELEASE
|    |         \--- org.bouncycastle:bcpkix-jdk15on:1.69
|    |              +--- org.bouncycastle:bcprov-jdk15on:1.69
|    |              \--- org.bouncycastle:bcutil-jdk15on:1.69
|    |                   \--- org.bouncycastle:bcprov-jdk15on:1.69
|    +--- org.springframework.cloud:spring-cloud-netflix-hystrix:2.2.10.RELEASE
|    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.12.RELEASE -> 2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-starter-aop:2.3.12.RELEASE -> 2.5.15
|    |         +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |         +--- org.springframework:spring-aop:5.3.27 (*)
|    |         \--- org.aspectj:aspectjweaver:1.9.7
|    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE
|    |    \--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    +--- org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.2.10.RELEASE
|    |    +--- org.springframework.cloud:spring-cloud-starter:2.2.9.RELEASE -> 3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-ribbon:2.2.10.RELEASE (*)
|    |    +--- org.springframework.cloud:spring-cloud-netflix-archaius:2.2.10.RELEASE
|    |    +--- com.netflix.archaius:archaius-core:0.7.7
|    |    |    +--- com.google.code.findbugs:jsr305:3.0.1 -> 3.0.2
|    |    |    +--- commons-configuration:commons-configuration:1.8
|    |    |    |    +--- commons-lang:commons-lang:2.6
|    |    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
|    |    |    +--- org.slf4j:slf4j-api:1.6.4 -> 1.7.36
|    |    |    +--- com.google.guava:guava:16.0 -> 32.1.3-jre
|    |    |    |    +--- com.google.guava:failureaccess:1.0.1
|    |    |    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    |    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    |    |    +--- org.checkerframework:checker-qual:3.37.0
|    |    |    |    \--- com.google.errorprone:error_prone_annotations:2.21.1
|    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.4.3 -> 2.12.7 (*)
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.4.3 -> 2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.4.3 -> 2.12.7.1 (*)
|    |    \--- commons-configuration:commons-configuration:1.8 (*)
|    +--- com.netflix.hystrix:hystrix-core:1.5.18
|    |    +--- org.slf4j:slf4j-api:1.7.0 -> 1.7.36
|    |    +--- com.netflix.archaius:archaius-core:0.4.1 -> 0.7.7 (*)
|    |    +--- io.reactivex:rxjava:1.2.0 -> 1.3.8
|    |    \--- org.hdrhistogram:HdrHistogram:2.1.9 -> 2.1.12
|    +--- com.netflix.hystrix:hystrix-serialization:1.5.18
|    |    +--- com.fasterxml.jackson.module:jackson-module-afterburner:2.7.5 -> 2.12.7
|    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.12.7 (*)
|    |    |    \--- com.fasterxml.jackson.core:jackson-databind:2.12.7 -> 2.12.7.1 (*)
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.7.5 -> 2.12.7 (*)
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.7.5 -> 2.12.7.1 (*)
|    |    \--- com.fasterxml.jackson.core:jackson-annotations:2.7.5 -> 2.12.7 (*)
|    +--- com.netflix.hystrix:hystrix-metrics-event-stream:1.5.18
|    |    +--- com.netflix.hystrix:hystrix-serialization:1.5.18 (*)
|    |    \--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    +--- com.netflix.hystrix:hystrix-javanica:1.5.18
|    |    +--- org.apache.commons:commons-lang3:3.1 -> 3.12.0
|    |    +--- org.ow2.asm:asm:5.0.4 -> 9.3
|    |    +--- com.netflix.hystrix:hystrix-core:1.5.18 (*)
|    |    +--- org.aspectj:aspectjweaver:1.8.6 -> 1.9.7
|    |    \--- com.google.guava:guava:15.0 -> 32.1.3-jre (*)
|    \--- io.reactivex:rxjava-reactive-streams:1.2.1
|         +--- io.reactivex:rxjava:1.2.2 -> 1.3.8
|         \--- org.reactivestreams:reactive-streams:1.0.0 -> 1.0.4
+--- org.apache.logging.log4j:log4j-api:2.20.0
+--- org.apache.logging.log4j:log4j-to-slf4j:2.20.0 (*)
+--- org.apache.pdfbox:pdfbox:2.0.29
|    +--- org.apache.pdfbox:fontbox:2.0.29
|    |    \--- commons-logging:commons-logging:1.2
|    \--- commons-logging:commons-logging:1.2
+--- org.apache.commons:commons-text:1.10.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.commons:commons-csv:1.10.0
+--- com.launchdarkly:launchdarkly-java-server-sdk:5.10.9
|    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
+--- io.github.openfeign:feign-core:11.2
+--- org.yaml:snakeyaml:2.0
+--- org.postgresql:postgresql:42.6.0
|    \--- org.checkerframework:checker-qual:3.31.0 -> 3.37.0
+--- org.flywaydb:flyway-core:8.5.13
+--- io.springfox:springfox-swagger2:2.9.2
|    +--- io.swagger:swagger-annotations:1.5.20
|    +--- io.swagger:swagger-models:1.5.20
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5 -> 2.12.7 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.36
|    |    \--- io.swagger:swagger-annotations:1.5.20
|    +--- io.springfox:springfox-spi:2.9.2
|    |    \--- io.springfox:springfox-core:2.9.2
|    |         +--- net.bytebuddy:byte-buddy:1.8.12 -> 1.10.22
|    |         +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |         +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |         +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |         +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE
|    |         |    +--- org.springframework:spring-beans:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-context:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    +--- org.springframework:spring-aop:4.0.9.RELEASE -> 5.3.27 (*)
|    |         |    \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    |         \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE
|    |              +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |              \--- org.slf4j:slf4j-api:1.7.10 -> 1.7.36
|    +--- io.springfox:springfox-schema:2.9.2
|    |    +--- io.springfox:springfox-core:2.9.2 (*)
|    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    +--- io.springfox:springfox-swagger-common:2.9.2
|    |    +--- io.swagger:swagger-annotations:1.5.20
|    |    +--- io.swagger:swagger-models:1.5.20 (*)
|    |    +--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- io.springfox:springfox-schema:2.9.2 (*)
|    |    +--- io.springfox:springfox-spring-web:2.9.2
|    |    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    |    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    |    |    \--- io.springfox:springfox-spi:2.9.2 (*)
|    |    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    |    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    |    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    |    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    |    \--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    +--- io.springfox:springfox-spring-web:2.9.2 (*)
|    +--- com.google.guava:guava:20.0 -> 32.1.3-jre (*)
|    +--- com.fasterxml:classmate:1.4.0 -> 1.5.1
|    +--- org.slf4j:slf4j-api:1.7.25 -> 1.7.36
|    +--- org.springframework.plugin:spring-plugin-core:1.2.0.RELEASE (*)
|    +--- org.springframework.plugin:spring-plugin-metadata:1.2.0.RELEASE (*)
|    \--- org.mapstruct:mapstruct:1.2.0.Final
+--- io.springfox:springfox-swagger-ui:2.9.2
|    \--- io.springfox:springfox-spring-web:2.9.2 (*)
+--- uk.gov.hmcts.reform:properties-volume-spring-boot-starter:0.1.0
|    +--- org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE -> 2.5.15 (*)
|    \--- org.springframework.cloud:spring-cloud-context:2.2.3.RELEASE -> 3.0.6 (*)
+--- uk.gov.hmcts.reform:logging:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.11.0 -> 2.12.7.1 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- uk.gov.hmcts.reform:logging-appinsights:5.1.7
|    +--- javax.servlet:javax.servlet-api:4.0.1
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.3.2.RELEASE -> 2.5.15 (*)
|    +--- com.microsoft.azure:applicationinsights-logging-logback:2.6.1
|    |    +--- com.microsoft.azure:applicationinsights-core:2.6.1
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- ch.qos.logback:logback-core:1.2.3 -> 1.2.12
|    +--- com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.1
|    |    \--- com.microsoft.azure:applicationinsights-web:2.6.1
|    +--- net.logstash.logback:logstash-logback-encoder:6.4 (*)
|    +--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- org.slf4j:jul-to-slf4j:1.7.30 -> 1.7.36 (*)
|    \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
+--- commons-fileupload:commons-fileupload:1.5
|    \--- commons-io:commons-io:2.11.0 -> 2.13.0
+--- uk.gov.hmcts.reform:idam-client:2.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7
|    |    +--- org.springframework.cloud:spring-cloud-starter:3.0.6 (*)
|    |    +--- org.springframework.cloud:spring-cloud-openfeign-core:3.0.7
|    |    |    +--- org.springframework.boot:spring-boot-autoconfigure:2.4.13 -> 2.5.15 (*)
|    |    |    +--- org.springframework.boot:spring-boot-starter-aop:2.4.13 -> 2.5.15 (*)
|    |    |    \--- io.github.openfeign.form:feign-form-spring:3.8.0
|    |    |         +--- io.github.openfeign.form:feign-form:3.8.0
|    |    |         |    \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    |         +--- org.springframework:spring-web:5.1.5.RELEASE -> 5.3.27 (*)
|    |    |         +--- commons-fileupload:commons-fileupload:1.4 -> 1.5 (*)
|    |    |         \--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.springframework:spring-web:5.3.13 -> 5.3.27 (*)
|    |    +--- org.springframework.cloud:spring-cloud-commons:3.0.6 (*)
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- io.github.openfeign:feign-slf4j:10.12
|    |         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |         \--- org.slf4j:slf4j-api:1.7.13 -> 1.7.36
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- io.github.openfeign:feign-jackson:10.9 -> 10.12
|    |    +--- io.github.openfeign:feign-core:10.12 -> 11.2
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    \--- io.github.openfeign:feign-httpclient:10.9 -> 10.12
|         +--- io.github.openfeign:feign-core:10.12 -> 11.2
|         \--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14
|              +--- org.apache.httpcomponents:httpcore:4.4.16
|              +--- commons-logging:commons-logging:1.2
|              \--- commons-codec:commons-codec:1.11 -> 1.15
+--- uk.gov.hmcts.reform:document-management-client:7.0.0
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.2.3.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:11.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:core-case-data-store-client:4.7.6
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:2.1.0.RELEASE -> 3.0.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.1.3.RELEASE -> 2.5.15 (*)
|    \--- io.github.openfeign:feign-jackson:10.1.0 -> 10.12 (*)
+--- uk.gov.hmcts.reform:service-auth-provider-client:4.0.0
|    +--- org.springframework.boot:spring-boot-starter-actuator:2.4.2 -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.0.1 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:10.12 (*)
|    +--- com.warrenstrange:googleauth:1.5.0
|    |    +--- commons-codec:commons-codec:1.14 -> 1.15
|    |    \--- org.apache.httpcomponents:httpclient:4.5.12 -> 4.5.14 (*)
|    +--- com.auth0:java-jwt:3.12.0
|    |    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5.1 -> 2.12.7.1 (*)
|    |    \--- commons-codec:commons-codec:1.14 -> 1.15
|    \--- javax.servlet:javax.servlet-api:4.0.1
+--- uk.gov.hmcts.reform.auth:auth-checker-lib:2.1.4
|    +--- uk.gov.hmcts.reform:java-logging-spring:5.0.1
|    |    +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |    +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |    +--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    |    \--- uk.gov.hmcts.reform:java-logging:5.0.1
|    |         +--- net.logstash.logback:logstash-logback-encoder:5.3 -> 6.4 (*)
|    |         +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    |         +--- org.slf4j:jul-to-slf4j:1.7.26 -> 1.7.36 (*)
|    |         \--- ch.qos.logback:logback-classic:1.2.3 -> 1.2.12 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9.1 -> 2.12.7.1 (*)
|    +--- org.apache.httpcomponents:httpclient:4.5.9 -> 4.5.14 (*)
|    +--- io.jsonwebtoken:jjwt:0.9.1
|    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.6 -> 2.12.7.1 (*)
|    +--- com.google.guava:guava:28.0-jre -> 32.1.3-jre (*)
|    +--- org.springframework.boot:spring-boot-autoconfigure:2.1.6.RELEASE -> 2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE -> 2.5.15
|    |    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    |    +--- org.springframework:spring-aop:5.3.27 (*)
|    |    +--- org.springframework.security:spring-security-config:5.5.8 -> 5.7.10 (*)
|    |    \--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    \--- org.projectlombok:lombok:1.18.8 -> 1.18.26
+--- com.github.hmcts:send-letter-client:3.0.16
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5 -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-jackson:12.1 -> 10.12 (*)
|    +--- commons-io:commons-io:2.11.0 -> 2.13.0
|    \--- org.springframework.retry:spring-retry:1.3.4
+--- uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE
|    +--- org.bitbucket.b_c:jose4j:0.7.7
|    |    \--- org.slf4j:slf4j-api:1.7.21 -> 1.7.36
|    +--- org.json:json:20210307
|    +--- joda-time:joda-time:2.10.10 -> 2.10.14
|    +--- org.apache.httpcomponents:httpclient:4.5.13 -> 4.5.14 (*)
|    +--- commons-io:commons-io:2.8.0 -> 2.13.0
|    \--- org.apache.commons:commons-lang3:3.12.0
+--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.82 (*)
+--- org.apache.tomcat.embed:tomcat-embed-core:9.0.82 (*)
+--- com.github.hmcts:ccd-case-document-am-client:1.7.3
|    +--- com.fasterxml.jackson.module:jackson-module-parameter-names -> 2.12.7 (*)
|    +--- org.springframework.boot:spring-boot-starter-actuator -> 2.5.15 (*)
|    +--- org.springframework.cloud:spring-cloud-starter-openfeign -> 3.0.7 (*)
|    +--- io.github.openfeign:feign-httpclient:11.1 -> 10.12 (*)
|    +--- io.github.openfeign:feign-jackson:11.1 -> 10.12 (*)
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2 -> 2.12.7 (*)
+--- com.google.guava:guava:32.1.3-jre (*)
+--- commons-io:commons-io:2.13.0
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.15.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-core:2.15.3 -> 2.12.7 (*)
|    +--- com.fasterxml.jackson.core:jackson-databind:2.15.3 -> 2.12.7.1 (*)
|    +--- joda-time:joda-time:2.10.14
|    \--- com.fasterxml.jackson:jackson-bom:2.15.3 (*)
+--- net.minidev:json-smart:2.5.0
|    \--- net.minidev:accessors-smart:2.5.0
|         \--- org.ow2.asm:asm:9.3
+--- org.pitest:pitest:1.15.1
+--- info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0
+--- org.pitest:pitest-junit5-plugin:1.1.1
+--- org.springframework.boot:spring-boot-starter-test -> 2.5.15
|    +--- org.springframework.boot:spring-boot-starter:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test:2.5.15
|    |    \--- org.springframework.boot:spring-boot:2.5.15 (*)
|    +--- org.springframework.boot:spring-boot-test-autoconfigure:2.5.15
|    |    +--- org.springframework.boot:spring-boot:2.5.15 (*)
|    |    +--- org.springframework.boot:spring-boot-test:2.5.15 (*)
|    |    \--- org.springframework.boot:spring-boot-autoconfigure:2.5.15 (*)
|    +--- com.jayway.jsonpath:json-path:2.5.0
|    |    +--- net.minidev:json-smart:2.3 -> 2.5.0 (*)
|    |    \--- org.slf4j:slf4j-api:1.7.30 -> 1.7.36
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
|    |    \--- jakarta.activation:jakarta.activation-api:1.2.2
|    +--- org.assertj:assertj-core:3.19.0
|    +--- org.hamcrest:hamcrest:2.2
|    +--- org.junit.jupiter:junit-jupiter:5.7.2
|    |    +--- org.junit:junit-bom:5.7.2
|    |    |    +--- org.junit.jupiter:junit-jupiter:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.7.2 (c)
|    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-commons:1.7.2 (c)
|    |    |    +--- org.junit.platform:junit-platform-engine:1.7.2 (c)
|    |    |    \--- org.junit.platform:junit-platform-launcher:1.7.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    |    \--- org.junit.platform:junit-platform-commons:1.7.2
|    |    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |    |         \--- org.apiguardian:apiguardian-api:1.1.0
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.7.2
|    |    |    +--- org.junit:junit-bom:5.7.2 (*)
|    |    |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-engine:5.7.2
|    |         +--- org.junit:junit-bom:5.7.2 (*)
|    |         +--- org.apiguardian:apiguardian-api:1.1.0
|    |         +--- org.junit.platform:junit-platform-engine:1.7.2
|    |         |    +--- org.junit:junit-bom:5.7.2 (*)
|    |         |    +--- org.apiguardian:apiguardian-api:1.1.0
|    |         |    +--- org.opentest4j:opentest4j:1.2.0
|    |         |    \--- org.junit.platform:junit-platform-commons:1.7.2 (*)
|    |         \--- org.junit.jupiter:junit-jupiter-api:5.7.2 (*)
|    +--- org.mockito:mockito-core:3.9.0 -> 3.7.7
|    |    +--- net.bytebuddy:byte-buddy:1.10.19 -> 1.10.22
|    |    +--- net.bytebuddy:byte-buddy-agent:1.10.19 -> 1.10.22
|    |    \--- org.objenesis:objenesis:3.1
|    +--- org.mockito:mockito-junit-jupiter:3.9.0 -> 3.7.7
|    |    +--- org.mockito:mockito-core:3.7.7 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.7.0 -> 5.7.2 (*)
|    +--- org.skyscreamer:jsonassert:1.5.1
|    +--- org.springframework:spring-core:5.3.27 (*)
|    +--- org.springframework:spring-test:5.3.27
|    |    \--- org.springframework:spring-core:5.3.27 (*)
|    \--- org.xmlunit:xmlunit-core:2.8.4
+--- org.awaitility:awaitility:4.2.0
|    \--- org.hamcrest:hamcrest:2.1 -> 2.2
+--- org.springframework.security:spring-security-test -> 5.5.8
|    +--- org.springframework.security:spring-security-core:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework.security:spring-security-web:5.5.8 -> 5.7.10 (*)
|    +--- org.springframework:spring-core:5.3.20 -> 5.3.27 (*)
|    \--- org.springframework:spring-test:5.3.20 -> 5.3.27 (*)
+--- org.mockito:mockito-core:3.7.7 (*)
+--- org.mockito:mockito-junit-jupiter:3.7.7 (*)
+--- org.mockito:mockito-inline:3.7.7
|    \--- org.mockito:mockito-core:3.7.7 (*)
+--- com.github.hmcts:fortify-client:1.2.0
|    +--- org.apache.commons:commons-lang3:3.9 -> 3.12.0
|    +--- commons-io:commons-io:2.6 -> 2.13.0
|    +--- org.slf4j:slf4j-api:1.7.26 -> 1.7.36
|    \--- org.junit.jupiter:junit-jupiter:5.7.0 -> 5.7.2 (*)
\--- org.junit.platform:junit-platform-launcher:1.7.2
     +--- org.junit:junit-bom:5.7.2 (*)
     +--- org.apiguardian:apiguardian-api:1.1.0
     \--- org.junit.platform:junit-platform-engine:1.7.2 (*)

testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
No dependencies

(c) - dependency constraint
(*) - dependencies omitted (listed previously)

(n) - Not resolved (configuration is not meant to be resolved)

A web-based, searchable dependency report is available by adding the --scan option.

BUILD SUCCESSFUL in 5s
1 actionable task: 1 executed
