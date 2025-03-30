FROM openjdk:21

RUN useradd --system appuser
USER appuser

WORKDIR /app
COPY server/build/libs/plenr.jar plenr.jar

ENV config.override.server.port=80
ENV config.override.server.host=0.0.0.0

EXPOSE 80

ENTRYPOINT ["java", "-jar", "plenr.jar"]