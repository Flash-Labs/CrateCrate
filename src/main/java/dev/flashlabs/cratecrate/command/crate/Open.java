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
        "Opens a crate for a player, which bypasses keys. Compared to give, open includes any opening effects and rolls a random reward.",
        CommandUtils.argument("player", false, "A username or selector matching a single player, defaulting to the player executing this command."),
        CommandUtils.argument("crate", true, "A registered crate id."),
        CommandUtils.argument("position", false, "An xyz position or either #me/#target (source position / target block), defaulting to the player's position.")
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
        Vector3d position = args.<Vector3d>getOne("position").orElseGet(player::getPosition);
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
