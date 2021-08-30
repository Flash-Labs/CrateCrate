package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

public final class Open {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.crate.open.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Crate.class, Config.CRATES::get, Config.CRATES::keySet).key("crate").build())
        .addParameter(Parameter.location().optional().key("location").build())
        .executor(Open::execute)
        .build();

    private static CommandResult execute(CommandContext context) {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var crate = context.requireOne(Parameter.key("crate", Crate.class));
        var location = context.one(Parameter.key("location", ServerLocation.class));
        crate.open(player, location.orElseGet(player::serverLocation));
        return CommandResult.success();
    }

}
