package dev.flashlabs.cratecrate.command;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.crate.Crate;
import dev.flashlabs.cratecrate.command.key.Key;
import dev.flashlabs.cratecrate.command.location.Location;
import dev.flashlabs.cratecrate.command.prize.Prize;
import dev.flashlabs.cratecrate.command.reward.Reward;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.List;

public final class Base {

    public static Command.Parameterized COMMAND = Command.builder()
        .permission("cratecrate.command.base")
        .addChild(Crate.COMMAND, "crate")
        .addChild(Reward.COMMAND, "reward")
        .addChild(Prize.COMMAND, "prize")
        .addChild(Key.COMMAND, "key")
        .addChild(Location.COMMAND, "location")
        .executor(Base::execute)
        .build();

    public static CommandResult execute(CommandContext context) {
        var messages = List.of(
            Component.text("CrateCrate v" + CrateCrate.container().metadata().version()),
            Component.text("GitHub: ").append(Component.text()
                .content("https://github.com/flash-labs/CrateCrate")
                .style(Style.style(ClickEvent.openUrl("https://github.com/flash-labs/CrateCrate")))
                .build()),
            Component.text("Discord: ").append(Component.text()
                .content("https://discord.gg/4wayq37")
                .style(Style.style(ClickEvent.openUrl("https://discord.gg/4wayq37")))
                .build())
        );
        messages.forEach(m -> context.sendMessage(Identity.nil(), m));
        return CommandResult.success();
    }

}
