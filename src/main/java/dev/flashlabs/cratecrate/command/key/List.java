package dev.flashlabs.cratecrate.command.key;

import dev.flashlabs.cratecrate.CrateCrate;
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

    private List(Builder builder) {
        super(builder
            .aliases("list", "/keys")
            .permission("cratecrate.command.key.list.base")
            .elements(
                GenericArguments.userOrSource(Text.of("user"))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        if (src instanceof Player) {
            Inventory.page(
                Text.of(user.getName() + "'s Keys"),
                Config.KEYS.values().stream()
                    .map(c -> Element.of(c.icon(c.quantity(user))))
                    .filter(e -> e.getItem().getQuantity() > 0)
                    .collect(Collectors.toList()),
                Inventory.CLOSE
            ).open((Player) src);
        } else {
            //TODO
            throw new CommandException(CrateCrate.get().getMessage("command.crate.list.player-only", src.getLocale()));
        }
        return CommandResult.success();
    }

}
