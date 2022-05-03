package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.CrateCrate;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class Open extends Command {

    private Open(Command.Builder builder) {
        super(builder
            .aliases("open")
            .permission("cratecrate.command.crate.open.base")
            .elements(
                GenericArguments.player(Text.of("player")),
                GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get),
                GenericArguments.optional(GenericArguments.location(Text.of("location")))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = args.requireOne("player");
        Crate crate = args.requireOne("crate");
        Optional<Location<World>> location = args.getOne("location");
        if (crate.open(player, location.orElseGet(player::getLocation))) {
            CrateCrate.get().sendMessage(src, "command.crate.open.success");
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.crate.open.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
