FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar event.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "event.jar"]