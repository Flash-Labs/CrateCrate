package dev.flashlabs.cratecrate.command.effect;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Effect extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate effect ",
        "The base command for working with effects.",
        CommandUtils.argument("subcommand ...", false, "An effect subcommand (give) and arguments.")
    );

    @Inject
    private Effect(Builder builder) {
        super(builder
            .aliases("effect")
            .permission("cratecrate.command.effect.base")
            .children(Give.class)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandUtils.paginate(src, USAGE, Give.USAGE);
        return CommandResult.success();
    }

}
