package dev.flashlabs.cratecrate.command.effect;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.command.CommandUtils;
import dev.flashlabs.cratecrate.component.effect.Effect;
import dev.flashlabs.cratecrate.component.effect.PotionEffect;
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

public final class Give extends Command {

    public static final Text USAGE = CommandUtils.usage(
        "/crate effect give ",
        "Gives an effect to a player/location.",
        CommandUtils.argument("player", false, "A username or selector matching a single player (online/offline), defaulting to the player executing this command (not required for location-based effects)."),
        CommandUtils.argument("location", false, "A world (optional for players) and xyz position, defaulting to the location of the player argument if defined (required for location-based effects)."),
        CommandUtils.argument("effect", true, "A registered effect id."),
        CommandUtils.argument("value", false, "A reference value for the effect (varies by type).\n\n - Potion: The integer duration, greater than 0 (in seconds).\n - Firework, Particle, Sound: The xyz position offset (optional).")
    );

    @Inject
    private Give(Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.reward.give.base")
            .elements(
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player")))),
                GenericArguments.optional(GenericArguments.location(Text.of("location"))),
                GenericArguments.choices(Text.of("effect"), Config.EFFECTS::keySet, Config.EFFECTS::get, false),
                GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("value")))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne(Text.of("player"));
        Optional<Location<World>> location = args.getOne(Text.of("location"));
        Effect effect = args.requireOne("effect");
        Optional<String> value = args.getOne("value");
        boolean result;
        if (effect instanceof PotionEffect) {
            if (!player.isPresent()) {
                throw new CommandException(CrateCrate.get().getMessage("command.effect.give.no-player", src.getLocale(),
                    "type", Effect.TYPES.get(effect.getClass().getName()).name()
                ));
            }
            try {
                int duration = Integer.parseInt(value.orElse(""));
                if (duration <= 0) {
                    throw new NumberFormatException();
                }
                result = effect.give(player.get(), location.orElseGet(player.get()::getLocation), duration);
            } catch (NumberFormatException e) {
                throw new CommandException(CrateCrate.get().getMessage("command.effect.give.invalid-duration", src.getLocale(),
                    "duration", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\""),
                    "bound", 0
                ));
            }
        } else if (effect instanceof Effect.Locatable) {
            if (!location.isPresent()) {
                if (!player.isPresent()) {
                    throw new CommandException(CrateCrate.get().getMessage("command.effect.give.no-location", src.getLocale(),
                        "type", Effect.TYPES.get(effect.getClass().getName()).name()
                    ));
                }
                location = Optional.of(location.orElseGet(player.get()::getLocation));
            }
            try {
                String[] split = value.orElse("0.0 0.0 0.0").split(" ");
                if (split.length != 3) {
                    throw new NumberFormatException();
                }
                Vector3d offset = Vector3d.from(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                location = Optional.of(location.get().add(offset));
                result = ((Effect.Locatable) effect).give(location.get());
            } catch (NumberFormatException e) {
                throw new CommandException(CrateCrate.get().getMessage("command.effect.give.invalid-offset", src.getLocale(),
                    "offset", value.map(v -> v.contains(" ") ? "\"" + v + "\"" : v).orElse("\"\"")
                ));
            }
        } else {
            throw new AssertionError(effect.getClass().getName());
        }
        if (result) {
            CrateCrate.get().sendMessage(src, "command.effect.give.success",
                "target", effect instanceof Effect.Locatable
                    ? location.get().getExtent().getName() + " " + location.get().getPosition()
                    : player.get().getName(),
                "effect", effect.id()
            );
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.effect.give.failure", src.getLocale(),
                "target", effect instanceof Effect.Locatable
                    ? location.get().getExtent().getName() + " " + location.get().getPosition()
                    : player.get().getName(),
                "effect", effect.id()
            ));
        }
        return CommandResult.success();
    }

}
