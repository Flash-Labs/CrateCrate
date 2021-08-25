package dev.flashlabs.cratecrate.command.reward;

import org.spongepowered.api.command.Command;

public final class Reward {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.reward.base")
        .addChild(Give.COMMAND, "give")
        .build();

}
