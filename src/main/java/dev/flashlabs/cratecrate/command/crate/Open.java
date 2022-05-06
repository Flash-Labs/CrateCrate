package dev.flashlabs.cratecrate.command.crate;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public final class Open extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate crate open ",
        "Gives a reward to a player as if given through this crate.",
        CommandUtils.argument("player", false, "A username or selector matching a single player, defaulting to the player executing this command."),
        CommandUtils.argument("crate", true, "A registered crate id."),
        CommandUtils.argument("position", false, "An xyz position or one of the special values #me (source's position) or #target (source's target block), defaulting to the position of the source executing this command.")
    );

    private Open(Command.Builder builder) {
        super(builder
            .aliases("open")
            .permission("cratecrate.command.crate.open.base")
            .elements(
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))),
                GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get, false),
                GenericArguments.withSuggestions(
                    GenericArguments.optional(GenericArguments.vector3d(Text.of("position"))),
                    ImmutableList.of("#me", "#target")
                )
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = args.requireOne("player");
        Crate crate = args.requireOne("crate");
        Vector3d position = args.requireOne("position");
        if (crate.open(player, player.getLocation().setPosition(position))) {
            CrateCrate.get().sendMessage(src, "command.crate.open.success",
                "player", player.getName(),
                "crate", crate.id(),
                "position", position
            );
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.crate.open.failure", src.getLocale(),
                "player", player.getName(),
                "crate", crate.id(),
                "position", position
            ));
        }
        return CommandResult.success();
    }

}
