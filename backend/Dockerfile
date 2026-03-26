# syntax=docker/dockerfile:1

FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

USER appuser
EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["/app/docker-entrypoint.sh"]
