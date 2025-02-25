FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar aggregator.jar
EXPOSE 8088
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8088/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "aggregator.jar"]