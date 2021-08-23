package dev.flashlabs.cratecrate.command.prize;

import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class Give {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.prize.give.base")
        .addParameter(Parameter.player().key("player").build())
        .addParameter(Parameter.choices(Prize.class, Config.PRIZES::get, Config.PRIZES::keySet).key("prize").build())
        .addParameter(Parameter.remainingJoinedStrings().optional().key("value").build())
        .executor(Give::execute)
        .build();

    public static CommandResult execute(CommandContext context) {
        var player = context.requireOne(Parameter.key("player", ServerPlayer.class));
        var prize = context.requireOne(Parameter.key("prize", TypeToken.get(Prize.class)));
        var value = context.one(Parameter.key("value", String.class));
        if (prize instanceof CommandPrize) {
            prize.give(player.user(), value.orElse(""));
        } else {
            throw new AssertionError(prize.getClass().getName());
        }
        return CommandResult.success();
    }

}
