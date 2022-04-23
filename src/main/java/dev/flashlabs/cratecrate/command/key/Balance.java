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

import java.util.Optional;

public final class Balance {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.key.balance.base")
        .arguments(
            GenericArguments.userOrSource(Text.of("user")),
            GenericArguments.choices(Text.of("key"), Config.KEYS::keySet, Config.KEYS::get)
        )
        .executor(Balance::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Key key = args.requireOne("key");
        if (src != user && !src.hasPermission("cratecrate.command.key.balance.other")) {
            throw new CommandException(Text.of("Cannot view other user's keys."));
        }
        src.sendMessage(key.name(Optional.of(key.quantity(user).orElse(0))));
        return CommandResult.success();
    }

}
