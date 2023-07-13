ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

FROM hmctspublic.azurecr.io/base/java:11-distroless

EXPOSE 4000

COPY build/libs/service.jar /opt/app/

CMD ["service.jar"]
