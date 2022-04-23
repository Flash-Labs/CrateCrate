package dev.flashlabs.cratecrate.command.reward;

import org.spongepowered.api.command.spec.CommandSpec;

public final class Reward {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.reward.base")
        .child(Give.COMMAND, "give")
        .build();

}
