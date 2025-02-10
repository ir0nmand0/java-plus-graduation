FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar stats-server.jar
EXPOSE 9090
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:9090/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "stats-server.jar"]