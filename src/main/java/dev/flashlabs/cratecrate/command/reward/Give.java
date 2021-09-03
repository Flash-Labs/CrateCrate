package dev.flashlabs.cratecrate.command.reward;

import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class Give {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.reward.give.base")
        .addParameter(Parameter.user().key("user").build())
        .addParameter(Parameter.choices(Reward.class, Config.REWARDS::get, Config.REWARDS::keySet).key("reward").build())
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var uuid = context.requireOne(Parameter.key("user", UUID.class));
        var reward = context.requireOne(Parameter.key("reward", Reward.class));
        try {
            var user = Sponge.server().userManager().load(uuid).get()
                .orElseThrow(() -> new CommandException(Component.text("Invalid user.")));
            reward.give(user);
        } catch (InterruptedException | ExecutionException e) {
            throw new CommandException(Component.text("Unable to load user."));
        }
        return CommandResult.success();
    }

}
