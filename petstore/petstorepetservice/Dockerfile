#
# Build stage
#
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

#
# Package stage
#
FROM openjdk:17-slim
COPY --from=build /build/target/*.jar /app/petstorepetservice.jar
COPY agent/applicationinsights-agent-3.4.15.jar applicationinsights-agent-3.4.15.jar
COPY agent/applicationinsights.json applicationinsights.json
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:applicationinsights-agent-3.4.15.jar","-jar","/app/petstorepetservice.jar"]