package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
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
     * Returns the name of this prize, defaulting to the command prefixed with
     * {@code '/'}. If a reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public Component name(Optional<String> value) {
        var base = name.orElseGet(() -> "/" + command);
        base = base.replaceAll("\\$\\{value}", value.orElse("${value}"));
        return LegacyComponentSerializer.legacyAmpersand().deserialize(base);
    }

    /**
     * Returns the lore of this prize, defaulting to an empty list. If a
     * reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public List<Component> lore(Optional<String> value) {
        return lore.orElseGet(ImmutableList::of).stream().map(s -> {
            s = s.replaceAll("\\$\\{value}", value.orElse("${value}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
        }).toList();
    }

    /**
     * Returns the icon of this prize, defaulting to a filled map. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<String> value) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.FILLED_MAP, 1));
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, name(value));
        }
        //TODO: Replace with base.get(Keys.LORE).isAbsent(); see SpongePowered/Sponge#3512
        if (lore.isPresent() && !base.toContainer().contains(DataQuery.of("UnsafeData", "display", "Lore"))) {
            base.offer(Keys.LORE, lore(value));
        }
        return base;
    }

    @Override
    public boolean give(User user, String value) {
        try {
            Sponge.server().commandManager().process(command.replaceAll("\\$\\{value}", value));
            return true;
        } catch (CommandException e) {
            CrateCrate.container().logger().error("Error processing command: ", e);
            return false;
        }
    }

    private static final class CommandPrizeType extends Type<CommandPrize, String> {

        private CommandPrizeType() {
            super("Command", CrateCrate.container());
        }

        /**
         * Matches nodes having a {@code command} child or with a string value
         * prefixed with {@code '/'}.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            return node.hasChild("command") || Optional.ofNullable(node.getString())
                .map(s -> s.startsWith("/"))
                .orElse(false);
        }

        /**
         * Deserializes a command prize, defined as:
         *
         * <pre>{@code
         * CommandPrize:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemType>
         *     command: String (prefixed with '/')
         * }</pre>
         */
        @Override
        public CommandPrize deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            var command = Optional.ofNullable(node.node("command").getString())
                .map(s -> s.substring(1))
                .orElse("");
            return new CommandPrize(String.valueOf(node.key()), name, lore, icon, command);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, CommandPrize component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserialize a command prize reference, defined as:
         *
         * <pre>{@code
         * CommandPrizeReference:
         *     node:
         *        CommandPrize |
         *        String (CommandPrize id or prefixed with '/')
         *     values: [
         *        Optional<String> (only allowed with String CommandPrize id)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<CommandPrize, String> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            CommandPrize prize;
            if (node.isMap()) {
                prize = deserializeComponent(node);
                prize = new CommandPrize("CommandPrize@" + node.path(), prize.name, prize.lore, prize.icon, prize.command);
            } else {
                var identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (CommandPrize) Config.PRIZES.get(identifier);
                } else if (identifier.startsWith("/")) {
                    prize = new CommandPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), identifier.substring(1));
                    Config.PRIZES.put(prize.id, prize);
                } else {
                    throw new AssertionError(identifier);
                }
            }
            //TODO: Validate reference value counts
            var value = Optional.ofNullable((!values.isEmpty() ? values.get(0) : node.node("value")).getString()).orElse("");
            return Tuple.of(prize, value);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<CommandPrize, String> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
