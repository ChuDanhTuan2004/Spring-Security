# Build stage
FROM gradle:7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test
# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/DrComputer-0.0.1-SNAPSHOT.war drcomputer.war
EXPOSE 8080
ENTRYPOINT ["java","-jar","drcomputer.war"]