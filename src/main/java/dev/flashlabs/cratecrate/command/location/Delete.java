package dev.flashlabs.cratecrate.command.location;

import dev.flashlabs.cratecrate.internal.Storage;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;

public final class Delete {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.location.delete.base")
        .arguments(
            GenericArguments.location(Text.of("location"))
        )
        .executor(Delete::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        try {
            Storage.deleteLocation(location);
            Storage.LOCATIONS.remove(location);
            src.sendMessage(Text.of("Successfully deleted location."));
        } catch (SQLException e) {
            throw new CommandException(Text.of("Error deleting location: " + e.getMessage()));
        }
        return CommandResult.success();
    }

}
