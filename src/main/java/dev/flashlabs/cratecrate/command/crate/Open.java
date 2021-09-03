package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
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

    private static CommandResult execute(CommandContext context) throws CommandException {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var crate = context.requireOne(Parameter.key("crate", Crate.class));
        var location = context.one(Parameter.key("location", ServerLocation.class));
        if (crate.open(player, location.orElseGet(player::serverLocation))) {
            context.sendMessage(Identity.nil(), Component.text("Successfully opened crate."));
        } else {
            throw new CommandException(Component.text("Failed to open crate."));
        }
        return CommandResult.success();
    }

}
