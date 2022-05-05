package dev.flashlabs.cratecrate.command.reward;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Reward extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate reward ",
        "The base command for rewards.",
        CommandUtils.argument("...", false, "A reward subcommand (give).")
    );

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
        CommandUtils.paginate(src, USAGE, Give.USAGE);
        return CommandResult.success();
    }

}
