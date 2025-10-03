ARG JAVA_VERSION=17
# FROM eclipse-temurin:${JAVA_VERSION}-jdk AS build
# ARG JAVA_VERSION
# ENV JAVA_VERSION=${JAVA_VERSION}
# WORKDIR /app
# COPY . .
# RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:${JAVA_VERSION}-jre
ARG JAVA_VERSION
ENV JAVA_VERSION=${JAVA_VERSION}
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
