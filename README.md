**Indexer:**

If we want to run it on folders, in the `pom.xml` file in the executable, write `MainMenu` and run it from Docker.

If we want to try Hazelcast:
On all computers:
```bash
sudo ufw allow 5701:5703/tcp
```

**I) ON THE MAIN COMPUTER:**

1) In the `pom.xml` file in the executable, set `HazelcastIndexerNode`:

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
   <mainClass>com.example.HazelcastIndexerNode</mainClass>
</transformer>
```

2) In `HazelcastIndexerNode`, add the main computer address and a second one (you can also add more):

```java
networkConfig.getJoin().getTcpIpConfig()
   .addMember("192.168.1.44") // Main node address
   .addMember("192.168.1.194") // Second node address, etc...
   .setEnabled(true);
```

3) Then in `HazelcastIndexerNode`, set the current computer address (main one):

```java
networkConfig.setPublicAddress("192.168.1.44:5701"); // Public IP address of the node
```

4) Finally, in the `JavaInvertedIndex` module, run:

```bash
mvn clean package
docker build -t inverted-index:latest .
docker run --network host --name inverted-index-container inverted-index:latest
```

Done, in case you want to try again or remove it, run:

```bash
docker stop inverted-index-container
docker rm inverted-index-container
```

**II) ON THE OTHER COMPUTERS:**

1) In the `pom.xml` file in the executable, set `HazelcastWorkerNode`:

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
   <mainClass>com.example.HazelcastWorkerNode</mainClass>
</transformer>
```

2) In `HazelcastWorkerNode`, add the main node address and you can also add the current one:

```java
networkConfig.getJoin().getTcpIpConfig()
   .addMember("192.168.1.44") // Main node address
   .addMember("192.168.1.194") // This node address
   .setEnabled(true);
```

4) Finally, in the `JavaInvertedIndex` module, run:

```bash
mvn clean package
docker build -t inverted-index:latest .
docker run --network host --name inverted-index-container inverted-index:latest
```

Done, in case you want to try again or remove it, run:

```bash
docker stop inverted-index-container
docker rm inverted-index-container
```

-------------------------------------------------------------------------

**JavaQuery:**

In `HazelcastController`:

```java
clientConfig.getNetworkConfig().addAddress("192.168.1.44", "192.168.1.194");
```

Add the main and current computer addresses. One address is enough for the main one.

Test on two computers in the same network, configuration:
1) On both computers, clear previous images:
```bash
docker stop <id>
docker rm <id>
```
Set the correct addresses in nginx (`ip a`).

2) On computer 1:
```bash
mvn clean package
docker build -t query-api .
docker run --name query1 -p 8081:8081 query-api
```

3) On computer 2:
```bash
mvn clean package
docker build -t query-api .
docker run --name query2 -p 8082:8081 query-api
```

4) Nginx, on computer 1:
```bash
docker build -t nginx-load-balancer .
docker run -d --name load-balancer -p 80:80 nginx-load-balancer
```

5) Check if it works:
```bash
curl http://localhost/hello
```

----------------------------------------------------------------

**UI:**

In the `ui` folder:

```bash
docker build -t my-react-app .
docker run -p 3000:3000 -v $(pwd):/app my-react-app
```

Open `http://localhost:3000/`

For testing the time:
```bash
curl -o /dev/null -s -w "DNS Lookup Time: %{time_namelookup}s\nConnect Time: %{time_connect}s\nStart Transfer Time: %{time_starttransfer}s\nTotal Time: %{time_total}s\n" localhost:8081/<endpoint>
```

-----------------------------------------------------------------------