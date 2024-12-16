FROM openjdk:21

RUN useradd --system appuser
USER appuser

WORKDIR /app
COPY server/build/libs/plenr.jar plenr.jar

ENV PLENR_SERVER_PORT=80
ENV PLENR_SERVER_HOST=0.0.0.0

EXPOSE 80

ENTRYPOINT ["java", "-jar", "plenr.jar"]