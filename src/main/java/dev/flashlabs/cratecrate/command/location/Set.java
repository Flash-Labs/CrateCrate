package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
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

public final class Set extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate location set ",
        "Sets a registered crate location. This command does not change the block at the given location; whatever block is present will work with the crate.",
        CommandUtils.argument("location", true, "A world (optional for players) and xyz position."),
        CommandUtils.argument("crate", true, "A registered crate id.")
    );

    @Inject
    private Set(Command.Builder builder) {
        super(builder
            .aliases("set")
            .permission("cratecrate.command.location.set.base")
            .elements(
                //TODO: Fix location parsing
                GenericArguments.location(Text.of("location")),
                GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get, false)
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> location = args.requireOne("location");
        Crate crate = args.requireOne("crate");
        location = new Location<>(location.getExtent(), location.getBlockPosition());
        if (Storage.LOCATIONS.containsKey(location)) {
            Optional<Crate> registered = Storage.LOCATIONS.get(location).map(Registration::crate);
            throw new CommandException(CrateCrate.get().getMessage("command.location.set.invalid-location", src.getLocale(),
                "location", location.getExtent().getName() + " " + location.getPosition(),
                "crate", registered.map(Component::id).orElse("unavailable")
            ));
        }
        try {
            Storage.setLocation(location, crate);
            CrateCrate.get().sendMessage(src, "command.location.set.success",
                "location", location.getExtent().getName() + " " + location.getPosition(),
                "crate", crate.id()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException(CrateCrate.get().getMessage("command.location.set.failure", src.getLocale(),
                "location", location.getExtent().getName() + " " + location.getPosition(),
                "crate", crate.id()
            ));
        }
        return CommandResult.success();
    }

}
