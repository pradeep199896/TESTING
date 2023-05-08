FROM openjdk:18
RUN mkdir -p /data
RUN mkdir -p /config
COPY target/*.jar .
VOLUME ["/data", "/config"]
ENTRYPOINT ["java","-jar","./pubsubplus-connector-database-1.0.0-SNAPSHOT.jar","--spring.config.additional-location=/config/"]