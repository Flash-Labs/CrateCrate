package dev.flashlabs.cratecrate.command.prize;

import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
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
        .permission("cratecrate.command.prize.give.base")
        .addParameter(Parameter.user().key("user").build())
        .addParameter(Parameter.choices(Prize.class, Config.PRIZES::get, Config.PRIZES::keySet).key("prize").build())
        .addParameter(Parameter.remainingJoinedStrings().optional().key("value").build())
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandContext context) throws CommandException {
        var uuid = context.requireOne(Parameter.key("user", UUID.class));
        var prize = context.requireOne(Parameter.key("prize", TypeToken.get(Prize.class)));
        var value = context.one(Parameter.key("value", String.class));
        try {
            var user = Sponge.server().userManager().load(uuid).get()
                .orElseThrow(() -> new CommandException(Component.text("Invalid user.")));
            boolean result;
            if (prize instanceof CommandPrize) {
                result = prize.give(user, value.orElse(""));
            } else if (prize instanceof ItemPrize) {
                result = prize.give(user, value.map(Integer::parseInt).orElse(1));
            } else {
                throw new AssertionError(prize.getClass().getName());
            }
            if (result) {
                context.sendMessage(Identity.nil(), Component.text("Successfully gave prize."));
            } else {
                throw new CommandException(Component.text("Failed to give prize."));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new CommandException(Component.text("Unable to load user."));
        }
        return CommandResult.success();
    }

}
