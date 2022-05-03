package dev.flashlabs.cratecrate.command.reward;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public final class Give extends Command {

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.reward.give.base")
            .elements(
                GenericArguments.userOrSource(Text.of("user")),
                GenericArguments.choices(Text.of("reward"), Config.REWARDS::keySet, Config.REWARDS::get)
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Reward reward = args.requireOne("reward");
        if (reward.give(user)) {
            CrateCrate.get().sendMessage(src, "command.prize.give.success");
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.prize.give.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
