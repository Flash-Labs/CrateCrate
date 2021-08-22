package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Optional;

public final class CommandPrize extends Prize<String> {

    public static final Type<CommandPrize, String> TYPE = new CommandPrizeType();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final String command;

    public CommandPrize(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        String command
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.command = command;
    }

    /**
     * Returns the name of this prize, defaulting to the command (with slash) if
     * undefined. If a reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public Component getName(Optional<String> value) {
        var base = name.orElseGet(() -> "/" + command);
        if (value.isPresent()) {
            base = base.replaceAll("\\$\\{value}", value.get());
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(base);
    }

    /**
     * Returns the lore of this prize, defaulting to an empty list if undefined.
     * If a reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public List<Component> getLore(Optional<String> value) {
        return lore.map(l -> l.stream().map(s -> {
            if (value.isPresent()) {
                s = s.replaceAll("\\$\\{value}", value.get());
            }
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
        }).toList()).orElseGet(List::of);
    }

    /**
     * Returns the icon of this prize, defaulting to a filled map if undefined.
     * If the icon does not have a defined display name or lore, it is set to
     * {@link #getName(Optional)} / {@link #getLore(Optional)}.
     */
    @Override
    public ItemStack getIcon(Optional<String> value) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.FILLED_MAP, 1));
        if (base.get(Keys.DISPLAY_NAME).isEmpty()) {
            base.offer(Keys.DISPLAY_NAME, getName(value));
        }
        if (lore.isPresent() && base.get(Keys.LORE).isEmpty()) {
            base.offer(Keys.LORE, getLore(value));
        }
        return base;
    }

    @Override
    public boolean give(User user, String value) {
        try {
            Sponge.server().commandManager().process(command.replaceAll("\\$\\{value}", value));
            return true;
        } catch (CommandException e) {
            CrateCrate.getContainer().logger().error("Error processing command: ", e);
            return false;
        }
    }

    private static final class CommandPrizeType extends Type<CommandPrize, String> {

        private CommandPrizeType() {
            super("Command", CrateCrate.getContainer());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public CommandPrize deserializeComponent(ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, CommandPrize component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<CommandPrize, String> deserializeReference(ConfigurationNode node, List<ConfigurationNode> values) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<CommandPrize, String> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
