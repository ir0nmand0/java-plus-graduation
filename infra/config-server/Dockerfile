FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar config-server.jar
COPY src/main/resources/config /app/config
EXPOSE 9091
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:9091/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "config-server.jar"]