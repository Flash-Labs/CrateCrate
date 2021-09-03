package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class Balance {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.key.balance.base")
        .addParameter(Parameter.user().key("user").build())
        .addParameter(Parameter.choices(Key.class, Config.KEYS::get, Config.KEYS::keySet).key("key").build())
        .executor(Balance::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var uuid = context.requireOne(Parameter.key("user", UUID.class));
        var key = context.requireOne(Parameter.key("key", Key.class));
        try {
            var user = Sponge.server().userManager().load(uuid).get()
                .orElseThrow(() -> new CommandException(Component.text("Invalid user.")));
            context.sendMessage(Identity.nil(), key.name(Optional.of(key.quantity(user).orElse(0))));
        } catch (InterruptedException | ExecutionException e) {
            throw new CommandException(Component.text("Unable to load user."));
        }
        return CommandResult.success();
    }

}
