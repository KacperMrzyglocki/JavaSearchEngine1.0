FROM openjdk:23

RUN mkdir /indexOld

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /indexOld/indexOld.jar

WORKDIR /indexOld

CMD ["java","-jar","indexOld.jar"]