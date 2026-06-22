FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.23

ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

COPY --chown=185 --from=build /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /workspace/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /workspace/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
  CMD bash -c '</dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
