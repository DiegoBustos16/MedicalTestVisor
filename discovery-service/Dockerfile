FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/discovery-service-0.0.1-SNAPSHOT.jar eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
