package dev.flashlabs.cratecrate.command.location;

import org.spongepowered.api.command.Command;

public final class Location {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.location.base")
        .addChild(Set.COMMAND, "set")
        .addChild(Delete.COMMAND, "delete")
        .build();

}
