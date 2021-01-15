FROM openjdk:15
COPY ./build/libs/*.jar /usr/aquila/aquila-server.jar
WORKDIR /usr/aquila
CMD ["java", "-jar", "server.jar"]