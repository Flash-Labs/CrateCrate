package dev.flashlabs.cratecrate.command.location;

import org.spongepowered.api.command.spec.CommandSpec;

public final class Location {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.location.base")
        .child(Set.COMMAND, "set")
        .child(Delete.COMMAND, "delete")
        .build();

}
