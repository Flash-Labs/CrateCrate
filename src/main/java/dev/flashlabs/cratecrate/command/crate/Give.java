package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.server.ServerLocation;

import java.math.BigDecimal;

public final class Give {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.crate.give.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Crate.class, Config.CRATES::get, Config.CRATES::keySet).key("crate").build())
        .addParameter(Parameter.location().optional().key("location").build())
        .addParameter(Parameter.choices(Reward.class, Config.REWARDS::get, Config.REWARDS::keySet).key("reward").build())
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var crate = context.requireOne(Parameter.key("crate", Crate.class));
        var location = context.one(Parameter.key("location", ServerLocation.class));
        var reward = context.requireOne(Parameter.key("reward", Reward.class));
        if (crate.give(player, location.orElseGet(player::serverLocation), Tuple.of(reward, BigDecimal.ZERO))) {
            context.sendMessage(Identity.nil(), Component.text("Successfully gave crate."));
        } else {
            throw new CommandException(Component.text("Failed to give crate."));
        }
        return CommandResult.success();
    }

}
