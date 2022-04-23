package dev.flashlabs.cratecrate.internal;

import ninja.leaping.configurate.ConfigurationNode;

public class SerializationException extends RuntimeException {

    private final ConfigurationNode node;

    public SerializationException(ConfigurationNode node, String message) {
        super(message);
        this.node = node;
    }

    public ConfigurationNode getNode() {
        return node;
    }

}
