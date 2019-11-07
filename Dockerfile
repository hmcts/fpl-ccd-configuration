FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

EXPOSE 4000

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/service.jar /opt/app/

CMD ["service.jar"]
