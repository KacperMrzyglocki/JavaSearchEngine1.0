package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;

public class HazelcastClientConnection {

    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, Map<Integer, Integer>> invertedIndex;

    public HazelcastClientConnection(String clusterMembers) {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(true);


        // Inicjalizacja instancji Hazelcast
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        this.invertedIndex = hazelcastInstance.getMap("invertedIndex");  // Mapa z klastrem
    }

    // Metoda do wyszukiwania słowa w klastrze
    public Map<Integer, Integer> searchWord(String word) {
        return invertedIndex.getOrDefault(word.toLowerCase(), Map.of());
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}