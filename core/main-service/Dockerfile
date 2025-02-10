FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar main-service.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "main-service.jar"]