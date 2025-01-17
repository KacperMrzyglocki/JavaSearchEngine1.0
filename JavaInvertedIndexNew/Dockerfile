FROM openjdk:23

RUN mkdir /index

COPY assets /app/assets

COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /index/index.jar

WORKDIR /index

CMD ["java","-jar","index.jar"]
