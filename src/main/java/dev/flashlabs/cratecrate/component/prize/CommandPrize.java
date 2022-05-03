package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CommandPrize extends Prize<String> {

    public static final Type<CommandPrize, String> TYPE = new CommandPrizeType();

    private enum Source {
        SERVER, PLAYER
    }

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final String command;
    private final Optional<Source> source;
    private final Optional<Boolean> online;

    private CommandPrize(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        String command,
        Optional<Source> source,
        Optional<Boolean> online
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.command = command;
        this.source = source;
        this.online = online;
    }

    /**
     * Returns the name of this prize, defaulting to the command prefixed with
     * {@code '/'}. If a reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public Text name(Optional<String> value) {
        String base = name.orElseGet(() -> "/" + command);
        base = base.replaceAll("\\$\\{value}", value.orElse("${value}"));
        return TextSerializers.FORMATTING_CODE.deserialize(base);
    }

    /**
     * Returns the lore of this prize, defaulting to an empty list. If a
     * reference value is given, it replaces {@code ${value}}.
     */
    @Override
    public List<Text> lore(Optional<String> value) {
        return lore.orElse(ImmutableList.of()).stream()
            .map(s -> {
                s = s.replaceAll("\\$\\{value}", value.orElse("${value}"));
                return TextSerializers.FORMATTING_CODE.deserialize(s);
            })
            .collect(Collectors.toList());
    }

    /**
     * Returns the icon of this prize, defaulting to a filled map. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<String> value) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.FILLED_MAP, 1));
        if (!base.get(Keys.DISPLAY_NAME).isPresent()) {
            base.offer(Keys.DISPLAY_NAME, name(value));
        }
        if (lore.isPresent() && !base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(value));
        }
        return base;
    }

    @Override
    public boolean give(User user, String value) {
        CommandSource src;
        String command = this.command.replaceAll("\\$\\{value}", value);
        if (online.orElse(false) || source.orElse(null) == Source.PLAYER) {
            //TODO: Error handling
            Player player = user.getPlayer().get();
            src = source.orElse(null) == Source.PLAYER ? player : Sponge.getServer().getConsole();
            command = command.replaceAll("\\$\\{player}", player.getName());
        } else {
            src = Sponge.getServer().getConsole();
            command = command.replaceAll("\\$\\{user}", user.getName());
        }
        return Sponge.getCommandManager().process(src, command).getSuccessCount().isPresent();
    }

    private static final class CommandPrizeType extends Type<CommandPrize, String> {

        private CommandPrizeType() {
            super("Command", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code command} child or with a string value
         * prefixed with {@code '/'}.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            return !node.getNode("command").isVirtual() || Optional.ofNullable(node.getString())
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
         *     icon: Optional<ItemStack>
         *     command: String (prefixed with '/') | Object
         *         command: String (prefixed with '/')
         *         source: Optional<Source>
         *         online: Optional<Boolean>
         * }</pre>
         */
        @Override
        public CommandPrize deserializeComponent(ConfigurationNode node) throws SerializationException {
            Optional<String> name = Optional.ofNullable(node.getNode("name").getString());
            Optional<ImmutableList<String>> lore = node.getNode("lore").isList()
                ? Optional.of(node.getChildrenList().stream()
                    .map(s -> s.getString(""))
                    .collect(ImmutableList.toImmutableList())
                )
                : Optional.empty();
            Optional<ItemStackSnapshot> icon = !node.getNode("icon").isVirtual()
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.getNode("icon")).createSnapshot())
                : Optional.empty();
            String command = Optional.ofNullable(node.getNode("command", "command").getString())
                .orElseGet(() -> node.getNode("command").getString("/"))
                .substring(1);
            Optional<Source> source = Optional.ofNullable(node.getNode("command", "source").getString())
                .map(s -> Source.valueOf(s.toUpperCase()));
            Optional<Boolean> online = Optional.ofNullable(node.getNode("command", "online").getString())
                .map(Boolean::parseBoolean);
            return new CommandPrize(String.valueOf(node.getKey()), name, lore, icon, command, source, online);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, CommandPrize component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a command prize reference, defined as:
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
                prize = new CommandPrize("CommandPrize@" + Arrays.toString(node.getPath()), prize.name, prize.lore, prize.icon, prize.command, prize.source, prize.online);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (CommandPrize) Config.PRIZES.get(identifier);
                } else if (identifier.startsWith("/")) {
                    prize = new CommandPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), identifier.substring(1), Optional.empty(), Optional.empty());
                    Config.PRIZES.put(prize.id, prize);
                } else {
                    throw new AssertionError(identifier);
                }
            }
            //TODO: Validate reference value counts
            String value = Optional.ofNullable((!values.isEmpty() ? values.get(0) : node.getNode("value")).getString()).orElse("");
            return Tuple.of(prize, value);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<CommandPrize, String> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
