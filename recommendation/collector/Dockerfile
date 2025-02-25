FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar collector.jar
EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8087/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "collector.jar"]