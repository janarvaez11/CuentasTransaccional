# -------- Stage 1: Build JAR --------
FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# -------- Stage 2: Runtime image --------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/CuentasTransaccional-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]