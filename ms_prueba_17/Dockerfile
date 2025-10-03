ARG JAVA_VERSION=17

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradle.properties settings.gradle build.gradle ./

RUN ./gradlew dependencies --no-daemon || true

COPY src/ src/

RUN ./gradlew bootJar --no-daemon

RUN java -Djarmode=layertools -jar build/libs/*.jar extract

FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]