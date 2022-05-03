package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.internal.Storage;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;

public final class Delete extends Command {

    @Inject
    private Delete(Command.Builder builder) {
        super(builder
            .aliases("delete")
            .permission("cratecrate.command.location.delete.base")
            .elements(
                GenericArguments.location(Text.of("location"))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        try {
            Storage.deleteLocation(location);
            Storage.LOCATIONS.remove(location);
            CrateCrate.get().sendMessage(src, "command.location.delete.success");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException(CrateCrate.get().getMessage("command.location.delete.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
