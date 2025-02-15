FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar compilation.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "compilation.jar"]