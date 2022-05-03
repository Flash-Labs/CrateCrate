package dev.flashlabs.cratecrate.command.key;

import com.google.inject.Inject;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public final class Key extends Command {

    @Inject
    private Key(Builder builder) {
        super(builder
            .aliases("key")
            .permission("cratecrate.command.key.base")
            .children(Give.class, Take.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getSpec().getUsage(src));
        return CommandResult.success();
    }

}
