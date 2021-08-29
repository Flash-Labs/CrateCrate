package dev.flashlabs.cratecrate.command.location;

import dev.flashlabs.cratecrate.internal.Storage;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;

public final class Delete {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.location.delete.base")
        .addParameter(Parameter.location().key("location").build())
        .executor(Delete::execute)
        .build();

    public static CommandResult execute(CommandContext context) throws CommandException {
        var location = context.requireOne(Parameter.key("location", ServerLocation.class));
        location = location.withBlockPosition(location.blockPosition());
        try {
            Storage.deleteLocation(location);
            Storage.LOCATIONS.remove(location);
        } catch (SQLException e) {
            throw new CommandException(Component.text("Error deleting location: " + e.getMessage()));
        }
        return CommandResult.success();
    }

}
