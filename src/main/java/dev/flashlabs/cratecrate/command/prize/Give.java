package dev.flashlabs.cratecrate.command.prize;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.MoneyPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public final class Give extends Command {

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.prize.give.base")
            .elements(
                GenericArguments.userOrSource(Text.of("user")),
                GenericArguments.choices(Text.of("prize"), Config.PRIZES::keySet, Config.PRIZES::get),
                GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("value")))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
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
        if (result) {
            CrateCrate.get().sendMessage(src, "command.prize.give.success");
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.prize.give.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
