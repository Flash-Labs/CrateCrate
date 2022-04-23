package dev.flashlabs.cratecrate.command.location;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
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
import java.util.Optional;

public final class Set {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.location.set.base")
        .arguments(
            GenericArguments.location(Text.of("location")),
            GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get)
        )
        .executor(Set::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        Crate crate = args.requireOne("crate");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        try {
            Storage.setLocation(location, crate);
            Storage.LOCATIONS.put(location, Optional.of(crate));
            src.sendMessage(Text.of("Successfully set location."));
        } catch (SQLException e) {
            throw new CommandException(Text.of("Error setting location: " + e.getMessage()));
        }
        return CommandResult.success();
    }

}
