package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class Take {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.key.take.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Key.class, Config.KEYS::get, Config.KEYS::keySet).key("key").build())
        .addParameter(Parameter.rangedInteger(1, Integer.MAX_VALUE).key("quantity").build())
        .executor(Take::execute)
        .build();

    public static CommandResult execute(CommandContext context) {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var key = context.requireOne(Parameter.key("key", Key.class));
        var value = context.requireOne(Parameter.key("quantity", Integer.class));
        key.take(player.user(), value);
        return CommandResult.success();
    }

}
