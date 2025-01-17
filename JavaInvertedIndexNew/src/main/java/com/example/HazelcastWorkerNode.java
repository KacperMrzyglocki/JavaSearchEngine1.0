package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;

public class HazelcastWorkerNode {
    public static void main(String[] args) {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();

        networkConfig.getJoin().getTcpIpConfig()
                .addMember("192.168.1.44") // Address of the main node
                .addMember("192.168.1.194") // Address of this node
                .setEnabled(true);
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        System.setProperty("hazelcast.local.localAddress", "192.168.1.194");

        Hazelcast.newHazelcastInstance(config);

        System.out.println("Worker Node joined the Hazelcast cluster.");
    }
}