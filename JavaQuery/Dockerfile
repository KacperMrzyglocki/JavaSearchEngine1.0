FROM openjdk:23

# Utwórz folder dla aplikacji
RUN mkdir /query

# Kopiowanie zasobów (jeśli masz jakieś pliki w folderze assets)
COPY assets /app/assets

# Kopiowanie pliku .jar z folderu target do kontenera
COPY target/JavaInvertedIndex-1.0-SNAPSHOT.jar /query/query.jar

# Ustawienie katalogu roboczego
WORKDIR /query

CMD ["java", "-jar", "query.jar", "com.example.QueryAPI"]