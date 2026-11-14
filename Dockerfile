FROM gradle:7.6.0-jdk17 AS build
WORKDIR /home/gradle/src

COPY --chown=gradle:gradle .. /home/gradle/src
RUN gradle build -x test

FROM gcr.io/distroless/java17-debian12:latest
EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]