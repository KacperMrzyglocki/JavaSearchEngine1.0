-----------------------------------------------------------------

Indexer:

Jeśli chcemy uruchomic na folderach to w pom.xml w pliku wykonywalnym wpisac MainMenu i odpalic z dockera.

Jeśli chcemy wypróbować hazlcasta:
Na wszystkich komputerach:
sudo ufw allow 5701:5703/tcp

I) NA GŁÓWNYM KOMPUTERZE:

1) W pom.xml w pliku wykonywalnym dać HazelcastIndexerNode:

<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
   <mainClass>com.example.HazelcastIndexerNode</mainClass>
</transformer>

2) W HazelcastIndexerNode dać tutaj adres głównego komputera i jakiegoś drugiego (mozna tez dac wiecej):

networkConfig.getJoin().getTcpIpConfig()
   .addMember("192.168.1.44") // Adres głównego węzła
   .addMember("192.168.1.194") // Adres drugiego węzła, itd...
   .setEnabled(true);

3) Następnie w HazelcastIndexerNode dać adres obecnego komputera (głównego):

networkConfig.setPublicAddress("192.168.1.44:5701"); // Publiczny adres IP węzła

4) Na koniec w module JavaInvertedIndex odpalamy:
   mvn clean package
   docker build -t inverted-index:latest .
   docker run --network host --name inverted-index-container inverted-index:latest

Gotowe, na wypadek jak chcemy sprobowac jeszcze raz lub usunąć dajemy:
   docker stop inverted-index-container
   docker rm inverted-index-container

II) NA RESZCIE KOMPUTERÓW:

1) W pom.xml w pliku wykonywalnym dać HazelcastWorkerNode:

<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
   <mainClass>com.example.HazelcastWorkerNode</mainClass>
</transformer>

2) W HazelcastWorkerNode dać adres głównego węzła i mozna tez obecnego:

networkConfig.getJoin().getTcpIpConfig()
   .addMember("192.168.1.44") // Adres głównego węzła
   .addMember("192.168.1.194") // Adres tego węzła
   .setEnabled(true);

4) Na koniec w module JavaInvertedIndex odpalamy:
   mvn clean package
   docker build -t inverted-index:latest .
   docker run --network host --name inverted-index-container inverted-index:latest

Gotowe, na wypadek jak chcemy sprobowac jeszcze raz lub usunąć dajemy:
   docker stop inverted-index-container
   docker rm inverted-index-container

-------------------------------------------------------------------------

JavaQuery:

W HazelcastController:
clientConfig.getNetworkConfig().addAddress("192.168.1.44", "192.168.1.194");
Dodac adresy głównego i obecnego komputera. Na głównym wystarczy 1 adres.

Test na dwoch komputerach w tej samej sieci, konfiguracja:
1) Na obu komputerach wyczyścić poprzednie obrazy:
   docker stop <id>
   docker rm <id>
   Ustawić w nginxie odpowiednie adresy (ip a)
2) Na 1 komputerze:
   mvn clean package
   docker build -t query-api .
   docker run --name query1 -p 8081:8081 query-api
3) Na 2 komputerze:
   mvn clean package
   docker build -t query-api .
   docker run --name query2 -p 8082:8081 query-api
4) Nginx, na 1 komputerze:
   docker build -t nginx-load-balancer .
   docker run -d --name load-balancer -p 80:80 nginx-load-balancer
5) Sprawdzic czy dziala:
   np: curl http://localhost/hello

----------------------------------------------------------------

UI:

W folderze ui:
npm install
npm start
wlaczyc http://localhost:3000/

Do testowania czasu:
curl -o /dev/null -s -w "DNS Lookup Time: %{time_namelookup}s\nConnect Time: %{time_connect}s\nStart Transfer Time: %{time_starttransfer}s\nTotal Time: %{time_total}s\n" localhost:8081/<endpoint>


-----------------------------------------------------------------------

