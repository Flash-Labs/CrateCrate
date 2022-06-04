package dev.flashlabs.cratecrate.command;

import dev.flashlabs.cratecrate.command.crate.Crate;
import dev.flashlabs.cratecrate.command.effect.Effect;
import dev.flashlabs.cratecrate.command.key.Key;
import dev.flashlabs.cratecrate.command.location.Location;
import dev.flashlabs.cratecrate.command.prize.Prize;
import dev.flashlabs.cratecrate.command.reward.Reward;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public final class Base extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate ",
        "The base command for CrateCrate.",
        CommandUtils.argument("subcommand ...", false, "A subcommand (crate/effect/key/location/prize/reward) and arguments.")
    );

    private Base(Builder builder) {
        super(builder
            .aliases("/cratecrate", "/crate")
            .permission("cratecrate.command.base")
            .children(Crate.class, Effect.class, Reward.class, Prize.class, Key.class, Location.class)
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        CommandUtils.paginate(src, USAGE, Crate.USAGE, Effect.USAGE, Key.USAGE, Location.USAGE, Prize.USAGE, Reward.USAGE);
        return CommandResult.success();
    }

}
