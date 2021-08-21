package dev.flashlabs.cratecrate.component;

import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;

public abstract class Type<T extends Component<V>, V> {

    public final String name;
    public final PluginContainer container;

    public Type(String name, PluginContainer container) {
        this.name = name;
        this.container = container;
    }

    public abstract boolean matches(ConfigurationNode node);

    public abstract T deserializeComponent(ConfigurationNode node) throws SerializationException;

    public abstract void reserializeComponent(ConfigurationNode node, T component) throws SerializationException;

    public abstract Tuple<T, V> deserializeReference(ConfigurationNode node, List<ConfigurationNode> values) throws SerializationException;

    public abstract void reserializeReference(ConfigurationNode node, Tuple<T, V> reference) throws SerializationException;

}
