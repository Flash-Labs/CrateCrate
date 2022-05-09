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

import java.util.Optional;

public final class Balance extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate key balance ",
        "Display the user's balance of a single key. To display all keys, use `list`.",
        CommandUtils.argument("user", false, "A username or selector matching a single user (online/offline), defaulting to the player executing this command."),
        CommandUtils.argument("key", true, "A registered key id.")
    );

    @Inject
    private Balance(Command.Builder builder) {
        super(builder
            .aliases("balance")
            .permission("cratecrate.command.key.balance.base")
            .elements(
                GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))),
                GenericArguments.choices(Text.of("key"), Config.KEYS::keySet, Config.KEYS::get, false)
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Key key = args.requireOne("key");
        if (src != user && !src.hasPermission("cratecrate.command.key.balance.other")) {
            throw new CommandException(CrateCrate.get().getMessage("command.key.balance.other.no-permission", src.getLocale(),
                "user", user.getName()
            ));
        }
        CrateCrate.get().sendMessage(src, "command.key.balance.success",
            "key", key.name(Optional.of(key.quantity(user).orElse(0)))
        );
        return CommandResult.success();
    }

}
