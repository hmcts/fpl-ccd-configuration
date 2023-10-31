ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

FROM hmctspublic.azurecr.io/base/java:17-distroless

EXPOSE 4000
USER hmcts
COPY build/libs/service.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/

CMD ["service.jar"]
FROM mcr.microsoft.com/playwright:focal AS BASE
RUN PLAYWRIGHT_BROWSERS_PATH=0
