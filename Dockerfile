
FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=build/libs/auth-service-0.1.0.jar
COPY ${JAR_FILE} /app/auth-service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/auth-service.jar"]
