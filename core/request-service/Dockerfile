FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar request.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "request.jar"]