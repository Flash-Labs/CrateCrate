package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Inventory;
import dev.flashlabs.cratecrate.internal.Utils;
import dev.flashlabs.flashlibs.command.Command;
import dev.flashlabs.flashlibs.inventory.Element;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.stream.Collectors;

public final class List extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate crate list ",
        "Gives a reward to a player as if given through this crate.",
        CommandUtils.argument("--text", false, "List crates through text rather than GUI (always enabled for console).")
    );

    private List(Builder builder) {
        super(builder
            .aliases("list", "/crates")
            .permission("cratecrate.command.crate.list.base")
            .elements(
                GenericArguments.flags().flag("-text").buildWith(GenericArguments.none())
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player && !args.hasFlag("text")) {
            Inventory.page(
                Text.of(TextColors.YELLOW, "Available ", TextColors.GOLD, "Crates"),
                Config.CRATES.values().stream()
                    .filter(c -> src.hasPermission("cratecrate.crates." + c.id() + ".base"))
                    .map(c -> Element.of(c.icon(Optional.empty()), a -> a.callback(v -> {
                        if (src.hasPermission("cratecrate.crates." + c.id() + ".preview")) {
                            Utils.preview(c, Element.of(Inventory.item(ItemTypes.CHEST, Text.of(TextColors.YELLOW, "Available ", TextColors.GOLD, "Crates")), a2 -> a2.callback(v2 -> {
                                v.open((Player) src);
                            }))).open(a.getPlayer());
                        }
                    })))
                    .collect(Collectors.toList()),
                Inventory.CLOSE
            ).open((Player) src);
        } else {
            PaginationList.builder()
                .title(Text.of(TextColors.YELLOW, "Available ", TextColors.GOLD, "Crates"))
                .padding(Text.of(TextColors.GRAY, "="))
                .contents(Config.CRATES.values().stream()
                    .filter(c -> src.hasPermission("cratecrate.crates." + c.id() + ".base"))
                    .map(c -> c.name(Optional.empty()))
                    .toArray(Text[]::new))
                .sendTo(src);
        }
        return CommandResult.success();
    }

}
