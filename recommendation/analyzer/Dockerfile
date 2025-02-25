FROM amazoncorretto:21.0.6
WORKDIR /app
COPY target/*.jar analyzer.jar
EXPOSE 8089
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8089/actuator/health || exit 1
ENTRYPOINT ["java", "-D spring.profiles.active=dev", "-jar", "analyzer.jar"]