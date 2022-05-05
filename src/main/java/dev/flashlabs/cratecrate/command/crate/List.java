package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Inventory;
import dev.flashlabs.cratecrate.internal.Utils;
import dev.flashlabs.flashlibs.command.Command;
import dev.flashlabs.flashlibs.inventory.Element;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.stream.Collectors;

public final class List extends Command {

    private List(Builder builder) {
        super(builder
            .aliases("list", "/crates")
            .permission("cratecrate.command.crate.list.base")
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            //TODO: Filter with permission
            Inventory.page(
                Text.of("Available Crates"),
                Config.CRATES.values().stream()
                    .map(c -> Element.of(c.icon(Optional.empty()), a -> a.callback(v -> {
                        Utils.preview(c, Element.of(Inventory.item(ItemTypes.CHEST, Text.of("Available Crates")), a2 -> a2.callback(v2 -> {
                            v.open((Player) src);
                        }))).open(a.getPlayer());
                    })))
                    .collect(Collectors.toList()),
                Inventory.CLOSE
            ).open((Player) src);
        } else {
            //TODO: Text display (pagination)
            throw new CommandException(CrateCrate.get().getMessage("command.crate.list.player-only", src.getLocale()));
        }
        return CommandResult.success();
    }

}
