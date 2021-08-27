package dev.flashlabs.cratecrate.command.key;

import org.spongepowered.api.command.Command;

public final class Key {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.key.base")
        .addChild(Balance.COMMAND, "balance")
        .addChild(Give.COMMAND, "give")
        .addChild(Take.COMMAND, "take")
        .build();

}
