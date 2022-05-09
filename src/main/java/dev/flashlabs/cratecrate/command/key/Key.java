package dev.flashlabs.cratecrate.command.key;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Key extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate key ",
        "The base command for working with keys.",
        CommandUtils.argument("subcommand ...", false, "A key subcommand (balance/give/list/take) and arguments.")
    );

    @Inject
    private Key(Builder builder) {
        super(builder
            .aliases("key")
            .permission("cratecrate.command.key.base")
            .children(Balance.class, Give.class, List.class, Take.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandUtils.paginate(src, USAGE, Balance.USAGE, Give.USAGE, List.USAGE, Take.USAGE);
        return CommandResult.success();
    }

}
