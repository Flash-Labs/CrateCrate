package dev.flashlabs.cratecrate.component;

import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
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

    public abstract boolean matches(Node node);

    public abstract T deserializeComponent(Node node) throws SerializationException;

    public abstract void reserializeComponent(Node node, T component) throws SerializationException;

    public abstract Tuple<T, V> deserializeReference(Node node, List<? extends Node> values) throws SerializationException;

    public abstract void reserializeReference(Node node, Tuple<T, V> reference) throws SerializationException;

}
