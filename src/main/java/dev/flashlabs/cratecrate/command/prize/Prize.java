package dev.flashlabs.cratecrate.command.prize;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Prize extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate prize ",
        "The base command for working with prizes.",
        CommandUtils.argument("subcommand ...", false, "A prize subcommand (give) and arguments.")
    );

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
        CommandUtils.paginate(src, USAGE, Give.USAGE);
        return CommandResult.success();
    }

}
