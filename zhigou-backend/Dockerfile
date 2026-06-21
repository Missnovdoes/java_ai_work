FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/zhigou-backend-*.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
