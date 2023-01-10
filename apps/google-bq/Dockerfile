FROM openjdk:17.0.2
EXPOSE 8080
ADD target/secret-poc-v2.jar secret-poc-v2.jar
ENTRYPOINT ["java","-jar","secret-poc-v2.jar"]