# ===== build stage =====
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package


# ===== runtime stage =====
FROM eclipse-temurin:17-jre

# Install openssl (REQUIRED for Kafka TLS)
RUN apt-get update && \
    apt-get install -y openssl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY init-kafka-certs.sh /init-kafka-certs.sh
RUN chmod +x /init-kafka-certs.sh


COPY --from=build /build/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
