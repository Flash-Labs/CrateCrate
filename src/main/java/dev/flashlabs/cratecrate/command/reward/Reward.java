package dev.flashlabs.cratecrate.command.reward;

import com.google.inject.Inject;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public final class Reward extends Command {

    @Inject
    private Reward(Command.Builder builder) {
        super(builder
            .aliases("reward")
            .permission("cratecrate.command.reward.base")
            .children(Give.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getSpec().getUsage(src));
        return CommandResult.success();
    }

}
