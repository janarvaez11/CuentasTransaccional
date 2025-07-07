FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# COPY target/gestion_vehiculos-0.0.1-SNAPSHOT.jar app.jar
COPY target/CuentasTransaccional-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 