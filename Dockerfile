FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

COPY gradle.properties settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./
COPY gradle ./gradle
COPY openapi ./openapi
COPY src ./src

RUN gradle --no-daemon clean shadowJar -x test \
    && JAR=$(ls build/libs/*-all.jar | head -n1) \
    && test -n "$JAR" \
    && cp "$JAR" /app/config-service.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/config-service.jar /app/config-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/config-service.jar"]
