package dev.flashlabs.cratecrate.command;

import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.crate.Crate;
import dev.flashlabs.cratecrate.command.key.Key;
import dev.flashlabs.cratecrate.command.location.Location;
import dev.flashlabs.cratecrate.command.prize.Prize;
import dev.flashlabs.cratecrate.command.reward.Reward;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class Base {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.base")
        .child(Crate.COMMAND, "crate")
        .child(Reward.COMMAND, "reward")
        .child(Prize.COMMAND, "prize")
        .child(Key.COMMAND, "key")
        .child(Location.COMMAND, "location")
        .executor(Base::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) {
        try {
            Lists.newArrayList(
                Text.of("CrateCrate v", CrateCrate.getContainer().getVersion()),
                Text.of("GitHub: ", Text.builder("https://github.com/flash-labs/CrateCrate")
                    .style(TextStyles.UNDERLINE)
                    .onClick(TextActions.openUrl(new URL("https://github.com/flash-labs/CrateCrate")))
                    .build()),
                Text.of("Discord: ", Text.builder("https://discord.gg/zWqnAa9KRn")
                    .style(TextStyles.UNDERLINE)
                    .onClick(TextActions.openUrl(new URL("https://discord.gg/zWqnAa9KRn")))
                )
            ).forEach(m -> src.sendMessage(m));
        } catch (MalformedURLException ignored) {}
        return CommandResult.success();
    }

}
