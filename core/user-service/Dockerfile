FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar user.jar
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8086/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "user.jar"]