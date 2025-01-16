JavaQuery:

Test na dwoch komputerach w tej samej sieci, konfiguracja:
1) Na obu komputerach wyczyścić poprzednie obrazy:
   docker stop <id>
   docker rm <id>
   Ustawić w nginxie odpowiednie adresy (ip a)
2) Na 1 komputerze:
   mvn clean package
   docker build -t query-api .
   docker run -d --name query1 -p 8081:8081 query-api
3) Na 2 komputerze:
   mvn clean package
   docker build -t query-api .
   docker run -d --name query2 -p 8082:8081 query-api
4) Nginx, na 1 komputerze:
   docker build -t nginx-load-balancer .
   docker run -d --name load-balancer -p 80:80 nginx-load-balancer
5) Sprawdzic czy dziala:
   np: curl http://localhost/hello


UI:

W folderze ui:
npm install
npm start
wlaczyc http://localhost:3000/

Do testowania czasu:
curl -o /dev/null -s -w "DNS Lookup Time: %{time_namelookup}s\nConnect Time: %{time_connect}s\nStart Transfer Time: %{time_starttransfer}s\nTotal Time: %{time_total}s\n" localhost:8081/<endpoint>
