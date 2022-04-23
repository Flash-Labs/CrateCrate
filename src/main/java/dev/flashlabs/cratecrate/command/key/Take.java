package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public final class Take {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.key.take.base")
        .arguments(
            GenericArguments.userOrSource(Text.of("user")),
            GenericArguments.choices(Text.of("key"), Config.KEYS::keySet, Config.KEYS::get),
            GenericArguments.integer(Text.of("quantity"))
        )
        .executor(Take::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Key key = args.requireOne("key");
        int quantity = args.requireOne("quantity");
        if (!key.take(user, quantity)) {
            throw new CommandException(Text.of("Failed to take key."));
        }
        src.sendMessage(Text.of("Successfully took key."));
        return CommandResult.success();
    }

}
