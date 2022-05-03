package dev.flashlabs.cratecrate.command;

import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.crate.Crate;
import dev.flashlabs.cratecrate.command.key.Key;
import dev.flashlabs.cratecrate.command.location.Location;
import dev.flashlabs.cratecrate.command.prize.Prize;
import dev.flashlabs.cratecrate.command.reward.Reward;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;

public final class Base extends Command {

    private Base(Builder builder) {
        super(builder
            .aliases("/cratecrate", "/crate")
            .permission("cratecrate.command.base")
            .children(Crate.class, Reward.class, Prize.class, Key.class, Location.class)
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        try {
            Lists.newArrayList(
                Text.of("CrateCrate v", CrateCrate.get().getContainer().getVersion()),
                Text.of("GitHub: ", Text.builder("https://github.com/flash-labs/CrateCrate")
                    .style(TextStyles.UNDERLINE)
                    .onClick(TextActions.openUrl(new URL("https://github.com/flash-labs/CrateCrate")))
                    .build()),
                Text.of("Discord: ", Text.builder("https://discord.gg/zWqnAa9KRn")
                    .style(TextStyles.UNDERLINE)
                    .onClick(TextActions.openUrl(new URL("https://discord.gg/zWqnAa9KRn")))
                )
            ).forEach(src::sendMessage);
        } catch (MalformedURLException ignored) {}
        return CommandResult.success();
    }

}
