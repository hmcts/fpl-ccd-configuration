ARG APP_INSIGHTS_AGENT_VERSION=2.5.0-BETA.3

# Build image

FROM busybox as downloader

RUN wget -P /tmp https://github.com/microsoft/ApplicationInsights-Java/releases/download/2.5.0-BETA.3/applicationinsights-agent-2.5.0-BETA.3.jar

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.0

EXPOSE 4000

COPY --from=downloader /tmp/applicationinsights-agent-2.5.0-BETA.3.jar /opt/app/
COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/service.jar /opt/app/

CMD ["service.jar"]
