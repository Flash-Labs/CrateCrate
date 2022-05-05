package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Crate extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate crate ",
        "The base command for crates.",
        CommandUtils.argument("...", false, "A crate subcommand (give/list/open)")
    );

    private Crate(Builder builder) {
        super(builder
            .aliases("crate")
            .permission("cratecrate.command.crate.base")
            .children(Give.class, List.class, Open.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandUtils.paginate(src, USAGE, Give.USAGE, List.USAGE, Open.USAGE);
        return CommandResult.success();
    }

}
