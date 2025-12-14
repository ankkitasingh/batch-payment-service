# ===== build stage =====
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ===== runtime stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY init-kafka-certs.sh /init-kafka-certs.sh
RUN chmod +x /init-kafka-certs.sh

COPY --from=build /build/target/*.jar app.jar

ENTRYPOINT ["/bin/sh", "-c", "/init-kafka-certs.sh && exec java -jar app.jar"]
