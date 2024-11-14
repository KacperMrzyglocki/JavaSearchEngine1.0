FROM openjdk:23

RUN mkdir /queryOld

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /queryOld/queryOld.jar

WORKDIR /queryOld

CMD ["java","-jar","queryOld.jar"]