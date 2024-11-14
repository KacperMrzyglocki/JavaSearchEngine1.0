FROM openjdk:23

RUN mkdir /crawler

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /crawler/crawler.jar

WORKDIR /crawler

CMD ["java","-jar","crawler.jar"]