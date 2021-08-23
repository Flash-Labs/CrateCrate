package dev.flashlabs.cratecrate.command.prize;

import org.spongepowered.api.command.Command;

public final class Prize {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.prize.base")
        .addChild(Give.COMMAND, "give")
        .build();

}
