package dev.flashlabs.cratecrate.internal;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.effect.Effect;
import dev.flashlabs.cratecrate.component.effect.ParticleEffect;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public final class Registration {

    private final Location<World> location;
    private final Crate crate;
    private final List<Task> effects = Lists.newArrayList();

    public Registration(Location<World> location, Crate crate) {
        this.location = location;
        this.crate = crate;
    }

    public Location<World> location() {
        return location;
    }

    public Crate crate() {
        return crate;
    }

    public void startEffects() {
        stopEffects();
        crate.effects().getOrDefault(Effect.Action.IDLE, ImmutableList.of()).stream()
            .filter(e -> e.getFirst() instanceof ParticleEffect) //TODO
            .forEach(e -> effects.add(((ParticleEffect) e.getFirst())
                .start(location.add(0.5, 0.5, 0.5).add(((Tuple<Boolean, Vector3d>) e.getSecond()).getSecond()))));
    }

    public void stopEffects() {
        effects.forEach(Task::cancel);
        effects.clear();
    }

}
