FROM openjdk:23

RUN mkdir /index

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /index/index.jar

WORKDIR /index

CMD ["java","-jar","index.jar"]