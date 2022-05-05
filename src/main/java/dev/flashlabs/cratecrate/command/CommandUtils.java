package dev.flashlabs.cratecrate.command;

import dev.flashlabs.cratecrate.CrateCrate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

public final class CommandUtils {

    private static final Text FOOTER = Text.joinWith(Text.of(TextColors.GRAY, " | "),
        link("Ore", "https://ore.spongepowered.org/FlashLabs/CrateCrate"),
        link("Source", "https://github.com/Flash-Labs/CrateCrate"),
        link("Discord", "https://discord.gg/zWqnAa9KRn"),
        link("FlashLabs", "https://flashlabs.dev")
    );

    public static Text usage(String base, String description, Text... arguments) {
        return Text.builder(base)
            .color(TextColors.GOLD)
            .onHover(TextActions.showText(Text.of(TextColors.GRAY, description)))
            .onClick(arguments.length == 0 ? TextActions.runCommand(base) : TextActions.suggestCommand(base))
            .append(Text.joinWith(Text.of(" "), arguments))
            .build();
    }

    public static Text argument(String name, boolean required, String description) {
        return Text.builder((required ? "<" : "[") + name + (required ? ">" : "]"))
            .color(required ? TextColors.YELLOW : TextColors.GRAY)
            .onHover(TextActions.showText(Text.of(TextColors.GRAY, description)))
            .build();
    }

    public static Text link(String name, String url) {
        try {
            return Text.builder(name)
                .color(TextColors.WHITE)
                .onHover(TextActions.showText(Text.of(TextColors.GRAY, url)))
                .onClick(TextActions.openUrl(new URL(url)))
                .build();
        } catch (MalformedURLException ignored) {
            return Text.builder(name)
                .color(TextColors.WHITE)
                .onHover(TextActions.showText(Text.of(TextColors.RED, url)))
                .build();
        }
    }

    public static void paginate(CommandSource src, Text... contents) {
        PaginationList.builder()
            .title(Text.of(
                TextColors.YELLOW, "Crate",
                TextColors.GOLD, "Crate",
                TextColors.WHITE, CrateCrate.get().getContainer().getVersion().map(v -> " v" + v).orElse("")
            ))
            .padding(Text.of(TextColors.GRAY, "="))
            .contents(contents)
            .footer(Text.of(src instanceof Player ? "                   " : "          ", FOOTER))
            .sendTo(src);
    }

}
