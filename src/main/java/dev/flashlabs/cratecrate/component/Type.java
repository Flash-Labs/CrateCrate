package dev.flashlabs.cratecrate.component;

import dev.flashlabs.cratecrate.internal.SerializationException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;

import java.util.List;

public abstract class Type<T extends Component<V>, V> {

    private final String name;
    private final PluginContainer container;

    protected Type(String name, PluginContainer container) {
        this.name = name;
        this.container = container;
    }

    public final String name() {
        return name;
    }

    public final PluginContainer container() {
        return container;
    }

    public abstract boolean matches(ConfigurationNode node);

    public abstract T deserializeComponent(ConfigurationNode node) throws SerializationException;

    public abstract void reserializeComponent(ConfigurationNode node, T component) throws SerializationException;

    public abstract Tuple<T, V> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException;

    public abstract void reserializeReference(ConfigurationNode node, Tuple<T, V> reference) throws SerializationException;

}
