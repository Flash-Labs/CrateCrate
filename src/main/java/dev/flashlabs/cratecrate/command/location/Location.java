package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Location extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate location ",
        "The base command for locations.",
        CommandUtils.argument("...", false, "A location subcommand (delete/set).")
    );

    @Inject
    private Location(Command.Builder builder) {
        super(builder
            .aliases("location")
            .permission("cratecrate.command.location.base")
            .children(Delete.class, Set.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandUtils.paginate(src, USAGE, Delete.USAGE, Set.USAGE);
        return CommandResult.success();
    }

}
