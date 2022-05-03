package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
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
import java.util.Optional;

public final class Set extends Command {

    @Inject
    private Set(Command.Builder builder) {
        super(builder
            .aliases("set")
            .permission("cratecrate.command.location.set.base")
            .elements(
                GenericArguments.location(Text.of("location")),
                GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get)
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        Crate crate = args.requireOne("crate");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        try {
            Storage.setLocation(location, crate);
            Storage.LOCATIONS.put(location, Optional.of(crate));
            CrateCrate.get().sendMessage(src, "command.location.delete.success");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException(CrateCrate.get().getMessage("command.location.delete.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
