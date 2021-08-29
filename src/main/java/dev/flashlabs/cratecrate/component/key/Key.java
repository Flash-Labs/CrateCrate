package dev.flashlabs.cratecrate.component.key;

import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Type;
import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Key extends Component<Integer> {

    public static final Map<String, Type<? extends Key, ?>> TYPES = new HashMap<>();

    public Key(String id) {
        super(id);
    }

    public abstract Optional<Integer> quantity(User user);

    public abstract boolean check(User user, Integer value);

    public abstract boolean give(User user, Integer value);

    public abstract boolean take(User user, Integer value);

}
