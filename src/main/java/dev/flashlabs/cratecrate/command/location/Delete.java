package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Registration;
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

public final class Delete extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate location delete ",
        "Deletes a registered crate location.",
        CommandUtils.argument("location", true, "A world (optional for players) and xyz position.")
    );

    @Inject
    private Delete(Command.Builder builder) {
        super(builder
            .aliases("delete")
            .permission("cratecrate.command.location.delete.base")
            .elements(
                //TODO: Fix location parsing
                GenericArguments.location(Text.of("location"))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        if (!Storage.LOCATIONS.containsKey(location)) {
            throw new CommandException(CrateCrate.get().getMessage("command.location.delete.invalid-location", src.getLocale(),
                "location", location.getExtent().getName() + " " + location.getPosition()
            ));
        }
        //TODO: Get registered crate id from database?
        Optional<Crate> crate = Storage.LOCATIONS.get(location).map(Registration::crate);
        try {
            Storage.deleteLocation(location);
            CrateCrate.get().sendMessage(src, "command.location.delete.success",
                "location", location.getExtent().getName() + " " + location.getPosition(),
                "crate", crate.map(Component::id).orElse("unavailable")
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException(CrateCrate.get().getMessage("command.location.delete.failure", src.getLocale(),
                "location", location.getExtent().getName() + " " + location.getPosition(),
                "crate", crate.map(Component::id).orElse("unavailable")
            ));
        }
        return CommandResult.success();
    }

}
