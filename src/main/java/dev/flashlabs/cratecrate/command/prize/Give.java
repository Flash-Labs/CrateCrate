package dev.flashlabs.cratecrate.command.prize;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
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
import java.util.Optional;

public final class Give extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate prize give ",
        "Gives a prize to a user.",
        CommandUtils.argument("user", false, "A username or selector matching a single user (online/offline), defaulting to the player executing this command."),
        CommandUtils.argument("prize", true, "A registered prize id."),
        CommandUtils.argument("value", false, "A reference value for the prize (varies by type).\n\n - Command: The ${value} placeholder.\n - Item: The integer quantity, greater than 0.\n - Money: The decimal amount, greater than 0.")
    );

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.prize.give.base")
            .elements(
                GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))),
                GenericArguments.choices(Text.of("prize"), Config.PRIZES::keySet, Config.PRIZES::get, false),
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
            //TODO: Check for unused value
            result = prize.give(user, value.orElse(""));
        } else if (prize instanceof ItemPrize) {
            try {
                int quantity = value.map(Integer::parseInt).orElse(1);
                //TODO: Check max quantity
                if (quantity <= 0) {
                    throw new NumberFormatException();
                }
                result = prize.give(user, quantity);
            } catch (NumberFormatException e) {
                throw new CommandException(CrateCrate.get().getMessage("command.prize.give.invalid-quantity", src.getLocale(),
                    "quantity", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\""),
                    "bound", 0
                ));
            }
        } else if (prize instanceof MoneyPrize) {
            try {
                BigDecimal amount = value.map(BigDecimal::new).orElse(BigDecimal.ZERO);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException();
                }
                result = prize.give(user, amount);
            } catch (NumberFormatException e) {
                throw new CommandException(CrateCrate.get().getMessage("command.prize.give.invalid-amount", src.getLocale(),
                    "amount", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\""),
                    "bound", 0
                ));
            }
        } else {
            throw new AssertionError(prize.getClass().getName());
        }
        if (result) {
            CrateCrate.get().sendMessage(src, "command.prize.give.success",
                "user", user.getName(),
                "prize", prize.id(),
                "value", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\"")
            );
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.prize.give.failure", src.getLocale(),
                "user", user.getName(),
                "prize", prize.id(),
                "value", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\"")
            ));
        }
        return CommandResult.success();
    }

}
