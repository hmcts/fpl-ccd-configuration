ARG APP_INSIGHTS_AGENT_VERSION=3.5.2

FROM hmctspublic.azurecr.io/base/java:21-distroless

EXPOSE 4000
USER hmcts
COPY build/libs/service.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/

CMD ["service.jar"]
