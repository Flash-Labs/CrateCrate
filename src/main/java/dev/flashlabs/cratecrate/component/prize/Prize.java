package dev.flashlabs.cratecrate.component.prize;

import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Type;
import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.Map;

public abstract class Prize<T> extends Component<T> {

    public static final Map<String, Type<? extends Prize, ?>> TYPES = new HashMap<>();

    protected Prize(String id) {
        super(id);
    }

    public abstract boolean give(User user, T value);

}
