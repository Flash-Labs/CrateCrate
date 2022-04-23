package dev.flashlabs.cratecrate.command.prize;

import org.spongepowered.api.command.spec.CommandSpec;

public final class Prize {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.prize.base")
        .child(Give.COMMAND, "give")
        .build();

}
