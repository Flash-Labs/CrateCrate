package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class Give {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.key.give.base")
        .addParameter(Parameter.user().key("user").build())
        .addParameter(Parameter.choices(Key.class, Config.KEYS::get, Config.KEYS::keySet).key("key").build())
        .addParameter(Parameter.rangedInteger(1, Integer.MAX_VALUE).key("quantity").build())
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var uuid = context.requireOne(Parameter.key("user", UUID.class));
        var key = context.requireOne(Parameter.key("key", Key.class));
        var value = context.requireOne(Parameter.key("quantity", Integer.class));
        try {
            var user = Sponge.server().userManager().load(uuid).get()
                .orElseThrow(() -> new CommandException(Component.text("Invalid user.")));
            key.give(user, value);
        } catch (InterruptedException | ExecutionException e) {
            throw new CommandException(Component.text("Unable to load user."));
        }
        return CommandResult.success();
    }

}
