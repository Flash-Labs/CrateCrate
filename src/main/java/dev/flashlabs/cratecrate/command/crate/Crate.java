package dev.flashlabs.cratecrate.command.crate;

import org.spongepowered.api.command.Command;

public final class Crate {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.crate.base")
        .addChild(Give.COMMAND, "give")
        .addChild(Open.COMMAND, "open")
        .build();

}
