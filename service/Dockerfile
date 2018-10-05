FROM openjdk:8-jre

COPY build/bootScripts/service /opt/app/bin/

COPY build/libs/service.jar /opt/app/lib/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4000/health

EXPOSE 4000

ENTRYPOINT ["/opt/app/bin/service"]
