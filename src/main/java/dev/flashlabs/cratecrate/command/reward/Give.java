package dev.flashlabs.cratecrate.command.reward;

import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class Give {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.reward.give.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Reward.class, Config.REWARDS::get, Config.REWARDS::keySet).key("reward").build())
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandContext context) {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var reward = context.requireOne(Parameter.key("reward", Reward.class));
        reward.give(player.user());
        return CommandResult.success();
    }

}
