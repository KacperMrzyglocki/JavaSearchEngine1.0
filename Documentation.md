# Documentation BIG DATA 3

**Authors**: Kacper Mrzygłocki, Andrzej Chwalenia, Bartosz Grabarek, Marek Dobruk, Jakub Jażdżyk, Michał Kowalski

## 1. Crawler

The Crawler module is responsible for collecting data from external sources, specifically utilizing the Gutendex API to gather literary content. This data is parsed and stored in JSON format, facilitating structured storage and further processing. By automating data acquisition and ensuring consistent updates, the Crawler serves as the backbone for feeding fresh content into the indexing pipeline. Implementing the Crawler in Docker enhances portability and scalability, allowing multiple instances to run concurrently if necessary.

## 2. Indexer

Our system indexes data in three distinct ways, each method designed to optimize different stages of data retrieval. These approaches significantly improve fetch times when querying data, allowing for faster and more efficient searches.

### 2.1 Indexing Methods

1. **Inverted Index**  
   The `inverted_index.json` contains a straightforward inverted index. For each word, the index stores the IDs of the books in which that word appears and the frequency of occurrences within those books.

2. **Dictionary**  
   The dictionary approach organizes the index by the first letter of each word. For example, in `a.json`, the inverted index stores words starting with the letter 'a' and the associated book IDs and occurrences. Each letter has its own JSON file, representing an alphabetized catalog.

3. **Folders**  
   This method adds another level of categorization to the dictionary approach. Each letter has its folder, and within that folder, additional files represent the second letter of words. This structure allows for deeper nesting and faster data retrieval.

These three approaches are similar in functionality but differ in complexity and speed. The folders method is the most advanced, significantly speeding up data fetching times in later stages of querying. Below is a comparison of average fetch times for a sample word across these methods:

### Fetching Times (in seconds)

| Method          | Inverted Index | Dictionary | Folders |
|-----------------|----------------|------------|---------|
| **"example"**   | 0.050s          | 0.0032s    | 0.0021s |
| **"literature"**| 0.065s          | 0.0042s    | 0.0029s |

All these data storage methods are kept locally and are used in the `JavaQuery` module to fetch data through the API. Additionally, the **Hazelcast** system, which serves as the core of our application, provides another way to store and manage the inverted index for distributed data sharing across different nodes, which will be discussed further in the next section.

## 3. Query Module

The Query module exposes an API on port 8081 via Java Spark, allowing users to query the indexed data through different storage methods (JSON, Dictionary, Folders). It supports basic word queries as well as logical queries combining terms with **AND** and **OR** operators. This makes it easy to check where both words appear together or where at least one of them exists across documents.

### Available Endpoints:

- **Search Endpoints**:
    - `/search/normal/:word`
    - `/search/normal/or/:word1/:word2`
    - `/search/normal/and/:word1/:word2`
    - `/search/dictionary/:word`
    - `/search/dictionary/or/:word1/:word2`
    - `/search/dictionary/and/:word1/:word2`
    - `/search/folder/:word`
    - `/search/folder/or/:word1/:word2`
    - `/search/folder/and/:word1/:word2`

- **Statistics Endpoints**:
    - `/stats/numberbooks`
    - `/stats/booksByLanguage`
    - `/stats/booksByAuthor`
    - `/stats/topLanguage`
    - `/stats/topAuthor`

- **Metadata Endpoints**:
    - `/search/metadata/:id`
    - `/search/metadata/author/:author`
    - `/search/metadata/language/:language`
    - `/search/metadata/title/:title`

The most important endpoint for testing large data sets, especially when connected to a Hazelcast cluster, is:

- `/documents/:words`  
  (where words are separated by `+`)

This endpoint allows querying for multiple words at once, utilizing the full potential of the distributed system.

## 4. User Interface (UI)

The User Interface (UI) provides a comprehensive web frontend built with React. It allows users to interact with the API, perform searches, and view filtered results. The UI offers a testing ground for users to explore the functionality of the system, enabling search and filtering across various dimensions like language, author, and book metadata.

## 5. Hazelcast

In our system, **Hazelcast** plays a pivotal role in the storage and management of data. It is a distributed in-memory data grid, which allows us to scale our system and share data across multiple nodes efficiently. Hazelcast provides high availability, fault tolerance, and scalability, which are crucial when working with large datasets.

The **JavaInvertedIndex** module integrates Hazelcast in two primary ways:

- **HazelcastIndexerNode**:  
  This node runs on the main computer and is responsible for indexing the incoming books. Once the books are indexed, the data is saved into an `IMap`, a distributed map in Hazelcast. This structure mirrors the indexing methods we described earlier (Inverted Index, Dictionary, and Folders).

- **HazelcastWorkerNode**:  
  Other computers in the network join the existing Hazelcast cluster as worker nodes. They participate in calculations and data storage. The system scales seamlessly across multiple machines, allowing us to handle larger volumes of data.

The distributed nature of Hazelcast allows us to optimize data retrieval, ensuring that the system can handle high concurrency while remaining fault-tolerant. When new machines join the cluster, they only need the address of one existing machine (usually the one running the indexer node), after which they can participate in the shared cluster.

In terms of data retrieval, the **HazelcastQueryNode** is responsible for fetching data from the cluster. The module can query any machine within the Hazelcast network, providing flexibility and fault tolerance.

While the time required to fetch data from Hazelcast is slightly higher than from local file structures, this is expected, as Hazelcast involves communication over the network. However, Hazelcast excels in fault tolerance, scalability, and distributed query processing.

## 6. Nginx

To leverage the power of multiple machines in querying the system, we use **Nginx** as a load balancer. It distributes incoming API requests to various `query` modules running on different machines. The load balancing is done using Nginx's default algorithm, which effectively splits the load across multiple instances.

Each machine in the network runs a Docker container with the query API. The main machine, equipped with Nginx, handles all incoming requests and forwards them to the available worker nodes. This ensures that our system can efficiently handle a large number of requests.

We have tested this solution by modifying a test endpoint’s response on each host in the network. When we queried the endpoint through the load balancer, we observed that different machines returned different responses, confirming that the load balancing was working correctly. While in smaller-scale tests the effect is not as prominent, the solution performs excellently under larger workloads.

### Other Time Comparisons

|                    | Crawler | Indexer |
|--------------------|---------|---------|
| **Old Version**    | 2.357s  | 23.103s |
| **New Version**    | 2.282s  | 10.760s | 

## 7. Conclusion

This project represents a highly scalable and efficient data indexing and querying system. Through the use of various technologies such as Docker, Hazelcast, Nginx, and Java, we have built a solution that can operate independently with each module being containerized. These technologies work well together to create a fast, fault-tolerant, and scalable system capable of processing large datasets.

We have successfully implemented a distributed system that is not only flexible but also capable of handling increasing volumes of data and traffic, thanks to the distributed nature of Hazelcast and the load balancing with Nginx. The modular design of the system allows it to be deployed on various infrastructures, making it adaptable to various use cases.

You can explore the full implementation of this project at the following repository:  
[GitHub Repository](https://github.com/KacperMrzyglocki/JavaSearchEngine1.0)

