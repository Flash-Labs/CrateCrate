package dev.flashlabs.cratecrate.command.key;

import org.spongepowered.api.command.spec.CommandSpec;

public final class Key {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.key.base")
        .child(Balance.COMMAND, "balance")
        .child(Give.COMMAND, "give")
        .child(Take.COMMAND, "take")
        .build();

}
