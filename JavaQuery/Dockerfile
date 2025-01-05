FROM openjdk:23

RUN mkdir /query

COPY assets /app/assets
COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /query/query.jar

WORKDIR /query

CMD ["java", "-jar", "query.jar", "com.example.QueryAPI"]