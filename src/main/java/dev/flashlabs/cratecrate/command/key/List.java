package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Inventory;
import dev.flashlabs.flashlibs.command.Command;
import dev.flashlabs.flashlibs.inventory.Element;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.stream.Collectors;

public final class List extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate key list ",
        "Lists all of a user's keys.",
        CommandUtils.argument("user", false, "A username or selector matching a single user (online/offline), defaulting to the player executing this command."),
        CommandUtils.argument("--text", false, "List keys through text rather than GUI (always enabled for console).")
    );

    private List(Builder builder) {
        super(builder
            .aliases("list", "/keys")
            .permission("cratecrate.command.key.list.base")
            .elements(
                GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))),
                GenericArguments.flags().flag("-text").buildWith(GenericArguments.none())
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        if (src != user && !src.hasPermission("cratecrate.command.key.balance.other")) {
            throw new CommandException(CrateCrate.get().getMessage("command.key.list.other.no-permission", src.getLocale(),
                "user", user.getName()
            ));
        }
        if (src instanceof Player && !args.hasFlag("text")) {
            Inventory.page(
                Text.of(user.getName() + "'s Keys"),
                Config.KEYS.values().stream()
                    .map(c -> Element.of(c.icon(c.quantity(user))))
                    .filter(e -> e.getItem().getQuantity() > 0)
                    .collect(Collectors.toList()),
                Inventory.CLOSE
            ).open((Player) src);
        } else {
            CommandUtils.paginate(src, Config.KEYS.values().stream()
                .map(k -> k.name(k.quantity(user)))
                .toArray(Text[]::new)
            );
        }
        return CommandResult.success();
    }

}
