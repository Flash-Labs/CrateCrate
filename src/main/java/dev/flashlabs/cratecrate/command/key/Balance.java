package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public final class Balance {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.key.balance.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Key.class, Config.KEYS::get, Config.KEYS::keySet).key("key").build())
        .executor(Balance::execute)
        .build();

    public static CommandResult execute(CommandContext context) {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var key = context.requireOne(Parameter.key("key", Key.class));
        context.sendMessage(Identity.nil(), key.name(Optional.of(key.quantity(player.user()).orElse(0))));
        return CommandResult.success();
    }

}
