package dev.flashlabs.cratecrate.component;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public abstract class Component<T> {

    protected final String id;

    protected Component(String id) {
        this.id = id;
    }

    public final String id() {
        return id;
    }

    public abstract Text name(Optional<T> value);

    public abstract List<Text> lore(Optional<T> value);

    public abstract ItemStack icon(Optional<T> value);

}
