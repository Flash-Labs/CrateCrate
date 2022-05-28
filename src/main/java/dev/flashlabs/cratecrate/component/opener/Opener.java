package dev.flashlabs.cratecrate.component.opener;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.path.*;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class Opener {

    //TODO: Allow registration of custom opener types
    public enum Type {
        GUI,
        ROULETTE,
    }

    public abstract boolean open(Player player, Crate crate, Location<World> location);

    public static Opener deserialize(Node node) {
        Type type = node.getType() == Node.Type.STRING
            ? node.get(Storm.ENUM.of(Type.class))
            : node.get("type", Storm.ENUM.of(Type.class));
        switch (type) {
            case GUI: return GuiOpener.deserialize(node);
            case ROULETTE: return RouletteOpener.deserialize(node);
            default: throw new AssertionError();
        }
    }

}
