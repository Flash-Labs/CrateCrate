package dev.flashlabs.cratecrate.command.key;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.component.key.Key;
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

    public static final Text USAGE = CommandUtils.usage(
        "/crate key give ",
        "Gives a quantity of a key to a user.",
        CommandUtils.argument("user", false, "A username or selector matching a single user (online/offline), defaulting to the player executing this command."),
        CommandUtils.argument("key", true, "A registered key id."),
        CommandUtils.argument("quantity", true, "An integer quantity, greater than 0.")
    );

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.key.give.base")
            .elements(
                GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))),
                GenericArguments.choices(Text.of("key"), Config.KEYS::keySet, Config.KEYS::get, false),
                GenericArguments.integer(Text.of("quantity"))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Key key = args.requireOne("key");
        int quantity = args.requireOne("quantity");
        //TODO: Quantity error message
        if (quantity <= 0) {
            throw new CommandException(CrateCrate.get().getMessage("command.key.give.invalid-quantity", src.getLocale(),
                "quantity", quantity,
                "bound", 0
            ));
        }
        if (key.give(user, quantity)) {
            CrateCrate.get().sendMessage(src, "command.key.give.success",
                "user", user.getName(),
                "key", key.id(),
                "quantity", quantity,
                "balance", key.quantity(user).orElse(0)
            );
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.key.give.failure", src.getLocale(),
                "user", user.getName(),
                "key", key.id(),
                "quantity", quantity
            ));
        }
        return CommandResult.success();
    }

}
