package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public final class Crate extends Command {

    private Crate(Builder builder) {
        super(builder
            .aliases("crate")
            .permission("cratecrate.command.crate.base")
            .children(Give.class, List.class, Open.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getSpec().getUsage(src));
        return CommandResult.success();
    }

}
