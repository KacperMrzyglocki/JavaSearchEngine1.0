FROM openjdk:23

RUN mkdir /query

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /query/query.jar

WORKDIR /query

CMD ["java","-jar","query.jar"]