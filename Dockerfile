FROM hmcts/cnp-java-base:openjdk-11-distroless-1.0-beta

EXPOSE 4000

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4000/health

COPY lib/applicationinsights-agent-2.4.0-BETA-SNAPSHOT.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/service.jar /opt/app/

CMD ["service.jar"]
