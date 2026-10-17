FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle bootJar --no-daemon

FROM openjdk:17.0.1-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8083

ENV PORT=8083

ENTRYPOINT ["java", "-jar", "app.jar"]
