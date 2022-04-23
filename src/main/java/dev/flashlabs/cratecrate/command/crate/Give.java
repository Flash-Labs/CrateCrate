package dev.flashlabs.cratecrate.command.crate;

import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

public final class Give {

    public static CommandSpec COMMAND = CommandSpec.builder()
        .permission("cratecrate.command.crate.give.base")
        .arguments(
            GenericArguments.player(Text.of("player")),
            GenericArguments.choices(Text.of("crate"), Config.CRATES::keySet, Config.CRATES::get),
            GenericArguments.optional(GenericArguments.location(Text.of("location"))),
            GenericArguments.choices(Text.of("reward"), Config.REWARDS::keySet, Config.REWARDS::get)
        )
        .executor(Give::execute)
        .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = args.requireOne("player");
        Crate crate = args.requireOne("crate");
        Optional<Location<World>> location = args.getOne("location");
        Reward reward = args.requireOne("reward");
        if (crate.give(player, location.orElseGet(player::getLocation), Tuple.of(reward, BigDecimal.ZERO))) {
            src.sendMessage(Text.of("Successfully gave crate."));
        } else {
            throw new CommandException(Text.of("Failed to give crate."));
        }
        return CommandResult.success();
    }

}
