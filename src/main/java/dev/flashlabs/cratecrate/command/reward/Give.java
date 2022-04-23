package dev.flashlabs.cratecrate.command.reward;

import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public final class Give {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.reward.give.base")
        .arguments(
            GenericArguments.userOrSource(Text.of("user")),
            GenericArguments.choices(Text.of("reward"), Config.REWARDS::keySet, Config.REWARDS::get)
        )
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Reward reward = args.requireOne("reward");
        if (!reward.give(user)) {
            throw new CommandException(Text.of("Failed to give reward."));
        }
        src.sendMessage(Text.of("Successfully gave reward."));
        return CommandResult.success();
    }

}
