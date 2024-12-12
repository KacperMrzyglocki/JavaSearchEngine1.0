package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {
    private static HazelcastInstance hazelcastInstance;

    static {
        ClientConfig clientConfig = new ClientConfig();
        String hazelcastHost = System.getenv("HAZELCAST_HOST");
        String hazelcastPort = System.getenv("HAZELCAST_PORT");
        clientConfig.getNetworkConfig().addAddress(hazelcastHost + ":" + hazelcastPort);

        hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}