package dev.flashlabs.cratecrate.internal.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

public final class TeslaCrateConverter {

    private static final Path CONFIG = Sponge.getConfigManager().getPluginConfig(CrateCrate.get().getContainer()).getDirectory().getParent();
    private static final Map<String, Node> CRATES = Maps.newHashMap();
    private static final Map<String, Node> REWARDS = Maps.newHashMap();
    private static final Map<String, Node> PRIZES = Maps.newHashMap();
    private static final Map<String, Node> KEYS = Maps.newHashMap();

    public static void convert() throws IOException {
        convertFile("keys.conf", TeslaCrateConverter::convertKeyComponent, KEYS);
        convertFile("prizes.conf", TeslaCrateConverter::convertPrizeComponent, PRIZES);
        convertFile("rewards.conf", TeslaCrateConverter::convertRewardComponent, REWARDS);
        convertFile("crates.conf", TeslaCrateConverter::convertCrateComponent, CRATES);
    }

    private static void convertFile(String file, BiConsumer<ConfigurationNode, Node> converter, Map<String, Node> components) throws IOException {
        try {
            ConfigurationNode from = HoconConfigurationLoader.builder().setPath(CONFIG.resolve("teslacrate/configuration/" + file)).build().load();
            Node to = Node.root();
            from.getChildrenMap().values().forEach(f -> {
                Node t = to.get(String.valueOf(f.getKey()));
                converter.accept(f, t);
                components.put(String.valueOf(f.getKey()), t);
                t.get("quantity").detach();
                t.get("item.quantity").detach();
                t.get("amount").detach();
                t.get("weight").detach();
            });
            Files.write(CONFIG.resolve("cratecrate/config/" + file), Storm.reserialize(to).getBytes());
        } catch (IOException e) {
            CrateCrate.get().getLogger().error("Error converting the config file " + file + ".", e);
            throw e;
        }
    }

    private static void convertComponent(ConfigurationNode from, Node to) {
        to.set("name", from.getNode("name").getString(""), Storm.STRING.optional("").convertDef(true));
        to.resolve("lore", 0).set(from.getNode("description").getString(""), Storm.STRING.optional("").convertDef(true));
        if (!from.getNode("display-item").isVirtual()) {
            convertItem(from.getNode("display-item"), to.get("icon"));
        }
        if (to.get("icon.name").getType() != Node.Type.UNDEFINED && to.get("name").getType() == Node.Type.UNDEFINED) {
            to.set("name", to.get("icon.name").getValue(), Storm.ANY);
            to.get("icon.name").detach();
        }
        if (to.get("icon.lore").getType() != Node.Type.UNDEFINED && to.get("lore").getType() == Node.Type.UNDEFINED) {
            to.set("lore", to.get("icon.lore").getValue(), Storm.ANY);
            to.get("icon.lore").detach();
        }
        if (to.get("icon").getType() == Node.Type.OBJECT && ImmutableSet.of("type").containsAll(to.get("icon").getMap().keySet())) {
            to.get("icon").set(to.get("icon.type").getValue(), Storm.ANY);
        }
    }

    public static void convertCrateComponent(ConfigurationNode from, Node to) {
        convertComponent(from, to);
        to.set("message", from.getNode("message").getString(""), Storm.STRING.optional("").convertDef(true));
        to.set("broadcast", from.getNode("announcement").getString(""), Storm.STRING.optional("").convertDef(true));
        //TODO: opener, effects
        from.getNode("keys").getChildrenMap().values().forEach(n -> {
            int index = to.get("keys").getType() == Node.Type.ARRAY ? to.get("keys").getList().size() : 0;
            convertKeyReference(n, to.resolve("keys", index));
        });
        to.set("rewards", ImmutableList.of(), Storm.LIST);
        from.getNode("rewards").getChildrenMap().values().forEach(n -> {
            convertRewardReference(n, to.resolve("rewards", to.get("rewards").getList().size()));
        });
    }

    public static void convertRewardComponent(ConfigurationNode from, Node to) {
        convertComponent(from, to);
        if (!from.getNode("announce").getBoolean(true)) {
            to.set("broadcast", "", Storm.STRING);
        }
        //TODO: limit
        to.set("prizes", ImmutableList.of(), Storm.LIST);
        from.getNode("prizes").getChildrenMap().values().forEach(n -> {
            convertPrizeReference(n, to.resolve("prizes", to.get("prizes").getList().size()));
        });
        to.set("weight", from.getNode("weight").getDouble(0.0), Storm.DOUBLE);
    }

    public static void convertRewardReference(ConfigurationNode from, Node to) {
        if (from.isMap()) {
            convertRewardComponent(from, to);
        } else {
            String id = String.valueOf(from.getKey()).split("/")[0];
            Node prize = REWARDS.getOrDefault(id, Node.root());
            to.resolve(0).set(id, Storm.STRING);
            to.resolve(1).set(from.getDouble(prize.get("weight", Storm.DOUBLE.optional(0.0))), Storm.DOUBLE);
        }
    }

    public static void convertPrizeComponent(ConfigurationNode from, Node to) {
        convertComponent(from, to);
        if (!from.getNode("command").isVirtual()) {
            String command = from.getNode("command", "command").getString(from.getNode("command").getString(""));
            boolean server = from.getNode("command", "source").getString("server").equalsIgnoreCase("server");
            boolean online = from.getNode("command", "online").getBoolean(false);
            command = "/" + command
                .replace("<value>", from.getNode("command", "value").getString("${value}"))
                .replace("<player>", !server || online ? "${player}" : "${user}");
            if (!server || online) {
                to.set("command.command", command, Storm.STRING);
                to.set("command.source", server ? "server" : "player", Storm.STRING.optional("server").convertDef(true));
                to.set("command.online", online, Storm.BOOLEAN.optional(false).convertDef(true));
            } else {
                to.set("command", command, Storm.STRING);
            }
        } else if (!from.getNode("item").isVirtual()) {
            convertItem(from.getNode("item"), to.get("item"));
        } else if (!from.getNode("money").isVirtual()) {
            EconomyService service = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
            String currency = from.getNode("money", "currency").getString(service.getDefaultCurrency().getId());
            int amount = from.getNode("money", "money").getInt(from.getNode("money").getInt(0));
            to.set("money.currency", currency, Storm.STRING.optional(service.getDefaultCurrency().getId()).convertDef(true));
            to.set("amount", BigDecimal.valueOf(amount), Storm.BIG_DECIMAL);
        } else {
            //TODO: Unknown types
        }
    }

    public static void convertPrizeReference(ConfigurationNode from, Node to) {
        if (from.isMap()) {
            convertPrizeComponent(from, to);
        } else {
            String id = String.valueOf(from.getKey()).split("/")[0];
            Node prize = PRIZES.getOrDefault(id, Node.root());
            to.resolve(0).set(id, Storm.STRING);
            if (prize.get("command").getType() != Node.Type.UNDEFINED) {
                boolean simple = prize.get("command").getType() == Node.Type.STRING;
                if (prize.get(simple ? "command" : "command.command", Storm.STRING).contains("${value}")) {
                    to.resolve(1).set(from.getString("").replace("<player>", simple ? "${user}" : "${player}"), Storm.STRING);
                }
            } else if (prize.get("item").getType() != Node.Type.UNDEFINED) {
                to.resolve(1).set(from.getInt(prize.get("item.quantity", Storm.INTEGER.optional(1))), Storm.INTEGER);
            } else if (prize.get("money").getType() != Node.Type.UNDEFINED) {
                to.resolve(1).set(from.getDouble(prize.get("amount", Storm.DOUBLE.optional(0.0))), Storm.DOUBLE);
            } else {
                //TODO: Unknown types
            }
        }
    }

    public static void convertKeyComponent(ConfigurationNode from, Node to) {
        convertComponent(from, to);
        if (!from.getNode("item").isVirtual()) {
            //TODO: Physical keys
            convertItem(from.getNode("item"), to.get("icon").detach());
        } else {
            //TODO: Cooldown/Money keys
        }
        to.set("quantity", from.getNode("quantity").getInt(from.getNode("item", "quantity").getInt(1)), Storm.INTEGER);
    }

    public static void convertKeyReference(ConfigurationNode from, Node to) {
        if (from.isMap()) {
            convertKeyComponent(from, to);
        } else {
            String id = String.valueOf(from.getKey()).split("/")[0];
            Node key = KEYS.getOrDefault(id, Node.root());
            to.resolve(0).set(id, Storm.STRING);
            to.resolve(1).set(from.getInt(key.get("quantity", Storm.INTEGER.optional(1))), Storm.INTEGER);
        }
    }

    private static void convertItem(ConfigurationNode from, Node to) {
        if (from.isMap()) {
            to.set("type", from.getNode("type").getString(""), Storm.STRING);
            if (!from.getNode("data").isVirtual()) {
                to.set("type", to.get("type", Storm.STRING) + "/" + from.getNode("data").getInt(0), Storm.STRING);
            }
            to.set("name", from.getNode("name").getString(""), Storm.STRING.optional("").convertDef(true));
            from.getNode("lore").getChildrenList().forEach(n -> {
                int index = to.get("lore").getType() == Node.Type.ARRAY ? to.get("lore").getList().size() : 0;
                to.resolve("lore", index).set(n.getString(""), Storm.STRING);
            });
            from.getNode("enchantments").getChildrenMap().values().forEach(n -> {
                int index = to.get("enchantments").getType() == Node.Type.ARRAY ? to.get("enchantments").getList().size() : 0;
                to.resolve("enchantments", index, 0).set(String.valueOf(n.getKey()).replace("-", "_"), Storm.STRING);
                to.resolve("enchantments", index, 1).set(n.getInt(1), Storm.INTEGER);
            });
            //TODO: keys
            moveIfDefined(from.getNode("nbt"), to.get("nbt"));
            to.set("quantity", from.getNode("quantity").getInt(1), Storm.INTEGER.optional(1).convertDef(true));
        } else {
            to.set(from.getString(""), Storm.STRING);
        }
    }

    private static void moveIfDefined(ConfigurationNode from, Node to) {
        if (!from.isVirtual()) {
            move(from, to);
        }
    }

    private static void move(ConfigurationNode from, Node to) {
        Object value = from.getValue();
        if (value instanceof Integer) {
            to.set((Integer) value, Storm.INTEGER);
        } else if (value instanceof Double) {
            to.set((Double) value, Storm.DOUBLE);
        } else if (from.isList()) {
            from.getChildrenList().forEach(n -> move(n, to.resolve(n.getKey())));
        } else if (value instanceof Map) {
            from.getChildrenMap().values().forEach(n -> move(n, to.resolve(n.getKey())));
        } else {
            to.set(value, Storm.ANY_NULLABLE);
        }
    }

}
