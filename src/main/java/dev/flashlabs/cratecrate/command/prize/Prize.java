package dev.flashlabs.cratecrate.command.prize;

import com.google.inject.Inject;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public final class Prize extends Command {

    @Inject
    private Prize(Command.Builder builder) {
        super(builder
            .aliases("prize")
            .permission("cratecrate.command.prize.base")
            .children(Give.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getSpec().getUsage(src));
        return CommandResult.success();
    }

}
