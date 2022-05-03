package dev.flashlabs.cratecrate.command.key;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.flashlibs.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public final class Give extends Command {

    @Inject
    private Give(Command.Builder builder) {
        super(builder
            .aliases("give")
            .permission("cratecrate.command.key.give.base")
            .elements(
                GenericArguments.userOrSource(Text.of("user")),
                GenericArguments.choices(Text.of("key"), Config.KEYS::keySet, Config.KEYS::get),
                GenericArguments.integer(Text.of("quantity"))
            )
        );
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.requireOne("user");
        Key key = args.requireOne("key");
        int quantity = args.requireOne("quantity");
        //TODO: Other users
        if (key.give(user, quantity)) {
            CrateCrate.get().sendMessage(src, "command.key.give.success");
        } else {
            throw new CommandException(CrateCrate.get().getMessage("command.key.give.failure", src.getLocale()));
        }
        return CommandResult.success();
    }

}
