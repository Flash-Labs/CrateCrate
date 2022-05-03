package dev.flashlabs.cratecrate.command.location;

import com.google.inject.Inject;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public final class Location extends Command {

    @Inject
    private Location(Command.Builder builder) {
        super(builder
            .aliases("location")
            .permission("cratecrate.command.location.base")
            .children(Set.class, Delete.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getSpec().getUsage(src));
        return CommandResult.success();
    }

}
