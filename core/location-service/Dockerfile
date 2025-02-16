FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar location.jar
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "location.jar"]