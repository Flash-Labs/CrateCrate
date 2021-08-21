package dev.flashlabs.cratecrate.component;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public abstract class Component<T> {

    public final String id;

    protected Component(String id) {
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public abstract net.kyori.adventure.text.Component getName(Optional<T> value);

    public abstract List<net.kyori.adventure.text.Component> getLore(Optional<T> value);

    public abstract ItemStack getIcon(Optional<T> value);

}
