FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY settings.gradle.kts .
COPY gradlew .
COPY gradle.properties .
COPY build.gradle.kts .
COPY gradle gradle
RUN chmod +x gradlew
COPY src src
RUN ./gradlew bootJar -x test --no-daemon --refresh-dependencies

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]