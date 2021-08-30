package dev.flashlabs.cratecrate.command.location;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Storage;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;

public final class Set {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.location.set.base")
        .addParameter(Parameter.location().key("location").build())
        .addParameter(Parameter.choices(Crate.class, Config.CRATES::get, Config.CRATES::keySet).key("crate").build())
        .executor(Set::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var location = context.requireOne(Parameter.key("location", ServerLocation.class));
        var crate = context.requireOne(Parameter.key("crate", TypeToken.get(Crate.class)));
        location = location.withBlockPosition(location.blockPosition());
        try {
            Storage.setLocation(location, crate);
            Storage.LOCATIONS.put(location, crate);
        } catch (SQLException e) {
            throw new CommandException(Component.text("Error setting location: " + e.getMessage()));
        }
        return CommandResult.success();
    }

}
