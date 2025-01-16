# Load Balancing with NGINX in a Distributed System

## Overview
This document discusses the use of NGINX for load balancing in a distributed system. In the project, we implemented a distributed computational system where multiple instances of a JavaQuery module are deployed across computers in the same network. Each instance provides a RESTful API developed using Java Spark. NGINX acts as the load balancer, distributing incoming requests across these instances.

## NGINX Configuration and Default Algorithm

### Configuration
The NGINX load balancer was configured to distribute requests among multiple backend servers. Below is the `nginx.conf` file used in the project:

```nginx
events {}

http {
    upstream javaquery_backend {
        server 192.168.1.44:8081;  # First container
        server 192.168.1.194:8082; # Second container
    }

    server {
        listen 80;
        location / {
            proxy_pass http://javaquery_backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_cache_bypass $http_upgrade;
        }
    }
}
```

Additionally, the `Dockerfile` for deploying the NGINX container is as follows:

```dockerfile
# Use the official NGINX image
FROM nginx:latest

# Copy the configuration file into the container
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80 for incoming requests
EXPOSE 80
```

### Default Load Balancing Algorithm
By default, NGINX uses a round-robin algorithm for load balancing. This method cyclically distributes incoming requests to the available backend servers. This approach ensures an even distribution of requests, preventing a single server from becoming a bottleneck.

#### Advantages of the Round-Robin Algorithm
- **Simplicity**: The round-robin algorithm is easy to implement and requires no additional configuration.
- **Fairness**: It distributes requests evenly across all servers, ensuring balanced resource utilization.
- **Scalability**: New servers can be added easily to the backend pool.

## Data Structures in the Project
The JavaQuery module processes and compares data stored in three different formats:

1. **JSON Files**: Data stored in JSON format, representing hierarchical structures.
2. **Dictionary Files**: Files organized as a dictionary with key-value mappings.
3. **Folder-Based Files**: Nested folder structures containing files for specific subsets of data.

Each module instance has a local copy of these data structures. Queries are processed independently by each module using these local copies.

## Testing with Hazelcast and IMap
To evaluate an alternative storage solution, we tested Hazelcast's distributed data grid. Using Hazelcast's IMap, we set up a cluster where data was centrally stored and indexed. The JavaQuery modules connected to the cluster as clients, fetching data from this single source. This architecture offers several benefits:

- **Centralized Storage**: Eliminates the need for maintaining local copies of data on each server.
- **Scalability**: Hazelcast allows data to be distributed across multiple machines, supporting larger datasets.
- **Flexibility**: Clients can dynamically join or leave the cluster.

Although scaling Hazelcast to distribute data storage across multiple machines is possible, this feature was beyond the scope of our current project. It remains a potential area for future work.

## System Testing
System testing was performed on multiple computers within the same network. In the current setup, two computers were used, each running a single instance of the JavaQuery module. NGINX distributed incoming requests between these instances.

## Conclusion
NGINX's load balancing capabilities, combined with Hazelcast for distributed data storage, provide a robust solution for scalable and efficient distributed systems. This approach ensures even resource utilization while offering flexibility for future expansions.

