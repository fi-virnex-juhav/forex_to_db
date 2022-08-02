FROM openjdk:17
ARG JAR_FILE=*.jar
EXPOSE 8080
COPY ${JAR_FILE} forex_to_db.jar
ENTRYPOINT ["java", "-jar", "forex_to_db.jar"]
# docker rmi forex_to_db:latest
# docker build -t forex_to_db .
# image: forex_to_db:latest
