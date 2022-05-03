package dev.flashlabs.cratecrate.command.crate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

public final class Give extends Command {

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.crate.base")
            .elements(
                GenericArguments.player(Text.of("player")),
                GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get),
                GenericArguments.optional(GenericArguments.location(Text.of("location"))),
                GenericArguments.choices(Text.of("reward"), Config.REWARDS::keySet, Config.REWARDS::get)
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = args.requireOne("player");
        Crate crate = args.requireOne("crate");
        Optional<Location<World>> location = args.getOne("location");
        Reward reward = args.requireOne("reward");
        if (crate.give(player, location.orElseGet(player::getLocation), Tuple.of(reward, BigDecimal.ZERO))) {
            CrateCrate.get().sendMessage(src, "command.crate.give.success");
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.crate.give.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
