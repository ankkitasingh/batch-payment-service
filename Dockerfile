FROM eclipse-temurin:17-jre
WORKDIR /app
COPY init-kafka-certs.sh /init-kafka-certs.sh
RUN chmod +x /init-kafka-certs.sh
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["/bin/sh", "-c", "/init-kafka-certs.sh && java -jar app.jar"]
