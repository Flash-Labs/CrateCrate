package dev.flashlabs.cratecrate.command.crate;

import org.spongepowered.api.command.spec.CommandSpec;

public final class Crate {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.crate.base")
        .child(Give.COMMAND, "give")
        .child(Open.COMMAND, "open")
        .build();

}
