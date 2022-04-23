package dev.flashlabs.cratecrate.command.prize;

import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.MoneyPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Optional;

public final class Give {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.prize.give.base")
        .arguments(
            GenericArguments.userOrSource(Text.of("user")),
            GenericArguments.choices(Text.of("prize"), Config.PRIZES::keySet, Config.PRIZES::get),
            GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("value")))
        )
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Prize prize = args.requireOne("prize");
        Optional<String> value = args.getOne("value");
        boolean result;
        if (prize instanceof CommandPrize) {
            result = prize.give(user, value.orElse(""));
        } else if (prize instanceof ItemPrize) {
            result = prize.give(user, value.map(Integer::parseInt).orElse(1));
        } else if (prize instanceof MoneyPrize) {
            result = prize.give(user, value.map(BigDecimal::new).orElse(BigDecimal.ZERO));
        } else {
            throw new AssertionError(prize.getClass().getName());
        }
        if (!result) {
            throw new CommandException(Text.of("Failed to give prize."));
        }
        src.sendMessage(Text.of("Successfully gave prize."));
        return CommandResult.success();
    }

}
