FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY main_srvc/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
