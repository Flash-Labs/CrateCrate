package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
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
        public boolean matches(Node node) {
            return node.get("command").getType() != Node.Type.UNDEFINED ||
                node.getType() == Node.Type.STRING && node.get(Storm.STRING).startsWith("/");
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
        public CommandPrize deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            String command = node.get(node.get("command").getType() == Node.Type.STRING ? "command" : "command.command", Storm.STRING.matches("/.+")).substring(1);
            Optional<Source> source = node.get("command.source", Storm.ENUM.of(Source.class).optional());
            Optional<Boolean> online = node.get("command.online", Storm.BOOLEAN.optional());
            return new CommandPrize(String.valueOf(node.getKey()), name, lore, icon, command, source, online);
        }

        @Override
        public void reserializeComponent(Node node, CommandPrize component) throws SerializationException {
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
        public Tuple<CommandPrize, String> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            CommandPrize prize;
            if (node.getType() == Node.Type.OBJECT) {
                prize = deserializeComponent(node);
                prize = new CommandPrize("CommandPrize@" + node.getPath(), prize.name, prize.lore, prize.icon, prize.command, prize.source, prize.online);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (CommandPrize) Config.PRIZES.get(identifier);
                } else if (identifier.startsWith("/")) {
                    prize = new CommandPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), identifier.substring(1), Optional.empty(), Optional.empty());
                    Config.PRIZES.put(prize.id, prize);
                } else {
                    throw new AssertionError(identifier);
                }
            }
            if (prize.command.contains("${value}") && values.isEmpty() && node.get("value").getType() == Node.Type.UNDEFINED) {
                throw new SerializationException(node, "Expected a reference value for the ${value} placeholder.");
            }
            String value = (!values.isEmpty() ? values.get(values.size() - 1) : node.get("value"))
                .get(Storm.STRING.optional(""));
            return Tuple.of(prize, value);
        }

        @Override
        public void reserializeReference(Node node, Tuple<CommandPrize, String> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
