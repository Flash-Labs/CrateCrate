package dev.flashlabs.cratecrate.internal.converter;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.util.Color;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    private static final Map<String, Node> EFFECTS = Maps.newHashMap();

    public static void convert() throws IOException {
        convertFile("effects.conf", TeslaCrateConverter::convertEffectComponent, EFFECTS);
        convertFile("keys.conf", TeslaCrateConverter::convertKeyComponent, KEYS);
        convertFile("prizes.conf", TeslaCrateConverter::convertPrizeComponent, PRIZES);
        convertFile("rewards.conf", TeslaCrateConverter::convertRewardComponent, REWARDS);
        convertFile("crates.conf", TeslaCrateConverter::convertCrateComponent, CRATES);
    }

    private static void convertFile(String file, BiConsumer<ConfigurationNode, Node> converter, Map<String, Node> components) throws IOException {
        try {
            ConfigurationNode from = HoconConfigurationLoader.builder().setPath(CONFIG.resolve("teslacrate/configuration/" + file)).build().load();
            Node to = Node.root();
            from.getChildrenMap().values().stream()
                .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
                .forEach(f -> {
                    Node t = to.get(String.valueOf(f.getKey()));
                    converter.accept(f, t);
                    components.put(String.valueOf(f.getKey()), t);
                    ImmutableList.of("target", "offset", "quantity", "item.quantity", "amount", "weight").forEach(k -> t.get(k).detach());
                });
            StringWriter writer = new StringWriter();
            new ComponentConfigGenerator(new PrintWriter(writer)).generate(to);
            Files.write(CONFIG.resolve("cratecrate/config/" + file), writer.toString().getBytes());
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
        switch (from.getNode("opener").getString("standard")) {
            case "instantgui": to.set("opener", "gui", Storm.STRING); break;
            case "roulettegui": to.set("opener", "roulette", Storm.STRING); break;
        }
        from.getNode("keys").getChildrenMap().values().stream()
            .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
            .forEach(n -> {
                int index = to.get("keys").getType() == Node.Type.ARRAY ? to.get("keys").getList().size() : 0;
                convertKeyReference(n, to.resolve("keys", index));
            });
        ImmutableMap.of(
            "passive", "idle",
            "on_open", "open",
            "on_receive", "give",
            "on_reject", "reject",
            "on_preview", "preview"
        ).forEach((f, t) -> from.getNode("effects", f).getChildrenMap().values().stream()
            .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
            .forEach(n -> {
                int index = to.resolve("effects", t).getType() == Node.Type.ARRAY ? to.resolve("effects", t).getList().size() : 0;
                convertEffectReference(n, to.resolve("effects", t, index));
            }));
        to.set("rewards", ImmutableList.of(), Storm.LIST);
        from.getNode("rewards").getChildrenMap().values().stream()
            .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
            .forEach(n -> convertRewardReference(n, to.resolve("rewards", to.get("rewards").getList().size())));
    }

    public static void convertRewardComponent(ConfigurationNode from, Node to) {
        convertComponent(from, to);
        if (!from.getNode("announce").getBoolean(true)) {
            to.set("broadcast", "", Storm.STRING);
        }
        //TODO: limit
        to.set("prizes", ImmutableList.of(), Storm.LIST);
        from.getNode("prizes").getChildrenMap().values().stream()
            .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
            .forEach(n -> convertPrizeReference(n, to.resolve("prizes", to.get("prizes").getList().size())));
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

    public static void convertEffectComponent(ConfigurationNode from, Node to) {
        if (!from.getNode("firework").isVirtual()) {
            to.set("firework.shape", (from.getNode("firework").isMap() ? from.getNode("firework", "type") : from.getNode("firework")).getString(""), Storm.STRING.optional(""));
            moveIfDefined(from.getNode("firework", "color"), to.resolve("firework", "colors", 0), TeslaCrateConverter::convertColor);
            moveIfDefined(from.getNode("firework", "fade"), to.resolve("firework", "fades", 0), TeslaCrateConverter::convertColor);
            moveIfDefined(from.getNode("firework", "flicker"), to.get("firework.flicker"), (f, t) -> t.set(f.getBoolean(false), Storm.BOOLEAN));
            moveIfDefined(from.getNode("firework", "trail"), to.get("firework.trail"), (f, t) -> t.set(f.getBoolean(false), Storm.BOOLEAN));
            moveIfDefined(from.getNode("firework", "strength"), to.get("firework.duration"), (f, t) -> t.set(f.getInt(0), Storm.INTEGER));
        } else if (!from.getNode("particle").isVirtual()) {
            to.set("particle.type", (from.getNode("particle").isMap() ? from.getNode("particle", "type") : from.getNode("particle")).getString("minecraft:redstone_dust"), Storm.STRING);
            moveIfDefined(from.getNode("particle", "color"), to.get("particle.color"), TeslaCrateConverter::convertColor);
            to.set("path.type", (from.getNode("path").isMap() ? from.getNode("path", "type") : from.getNode("path")).getString(""), Storm.STRING);
            moveIfDefined(from.getNode("path", "axis"), to.get("path.axis"));
            to.set("path.interval", from.getNode("path", "interval").getInt(20), Storm.INTEGER.optional(20).convertDef(true));
            to.set("path.precision", from.getNode("path", "precision").getInt(120), Storm.INTEGER.optional(120).convertDef(true));
            to.set("path.segments", from.getNode("path", "segments").getInt(1), Storm.INTEGER.optional(1).convertDef(true));
            to.set("path.shift", from.getNode("path", "shift").getDouble(0.0), Storm.DOUBLE.optional(0.0).convertDef(true));
            to.set("path.speed", from.getNode("path", "speed").getDouble(1.0), Storm.DOUBLE.optional(1.0).convertDef(true));
            moveIfDefined(from.getNode("path", "scale"), to.get("path.scale"));
        } else if (!from.getNode("potion").isVirtual()) {
            to.set("potion.type", (from.getNode("potion").isMap() ? from.getNode("potion", "type") : from.getNode("potion")).getString(""), Storm.STRING);
            if (!from.getNode("potion", "amplifier").isVirtual()) {
                to.set("potion.type", to.get("potion.type", Storm.STRING) + "/" + (from.getNode("potion", "amplifier").getInt(1) - 1), Storm.STRING);
            }
            to.set("potion.ambient", from.getNode("potion", "ambient").getBoolean(false), Storm.BOOLEAN.optional(false).convertDef(true));
            to.set("potion.particles", from.getNode("potion", "particles").getBoolean(true), Storm.BOOLEAN.optional(true).convertDef(true));
            to.set("duration", from.getNode("potion", "duration").getInt(100), Storm.INTEGER);
        } else if (!from.getNode("sound").isVirtual()) {
            to.set("sound.type", (from.getNode("sound").isMap() ? from.getNode("sound", "type") : from.getNode("sound")).getString(""), Storm.STRING);
            to.set("sound.volume", from.getNode("sound", "volume").getDouble(1.0), Storm.DOUBLE.optional(1.0).convertDef(true));
            to.set("sound.pitch", from.getNode("sound", "pitch").getDouble(1.0), Storm.DOUBLE.optional(1.0).convertDef(true));
        } else {
            //TODO: Unknown types
        }
        if (from.getNode("target").getString("location").equalsIgnoreCase("player")) {
            to.set("target", "player", Storm.STRING);
        }
        moveIfDefined(from.getNode("offset"), to.get("offset"));
    }

    public static void convertEffectReference(ConfigurationNode from, Node to) {
        if (from.isMap()) {
            convertEffectComponent(from, to);
        } else {
            String id = String.valueOf(from.getKey()).split("/")[0];
            Node effect = EFFECTS.getOrDefault(id, Node.root());
            to.resolve(0).set(id, Storm.STRING);
            if (effect.get("potion").getType() != Node.Type.UNDEFINED) {
                to.resolve(1).set(from.getInt(effect.get("duration", Storm.INTEGER.optional(1))), Storm.INTEGER);
            } else {
                if (effect.get("target").getType() != Node.Type.UNDEFINED) {
                    to.resolve(1).set(effect.get("target", Storm.STRING), Storm.STRING);
                }
                Vector3d offset = Vector3d.from(
                    from.getNode(0).getDouble(effect.resolve("offset", 0).get(Storm.DOUBLE.optional(0.0))),
                    from.getNode(1).getDouble(effect.resolve("offset", 1).get(Storm.DOUBLE.optional(0.0))),
                    from.getNode(2).getDouble(effect.resolve("offset", 2).get(Storm.DOUBLE.optional(0.0)))
                );
                if (!offset.equals(Vector3d.ZERO)) {
                    to.resolve(to.getList().size()).set(offset.getX(), Storm.DOUBLE);
                    to.resolve(to.getList().size()).set(offset.getY(), Storm.DOUBLE);
                    to.resolve(to.getList().size()).set(offset.getZ(), Storm.DOUBLE);
                }
            }
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
            from.getNode("enchantments").getChildrenMap().values().stream()
                .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
                .forEach(n -> {
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

    private static void convertColor(ConfigurationNode from, Node to) {
        if (from.isList()) {
            to.set(Color.ofRgb(from.getNode(0).getInt(0), from.getNode(1).getInt(0), from.getNode(2).getInt(0)).getRgb(), Storm.INTEGER);
        } else {
            try {
                to.set(Integer.decode(from.getString("")), Storm.INTEGER);
            } catch (NumberFormatException e) {
                to.set(0, Storm.INTEGER);
            }
        }
    }

    private static void moveIfDefined(ConfigurationNode from, Node to) {
        moveIfDefined(from, to, TeslaCrateConverter::move);
    }

    private static void moveIfDefined(ConfigurationNode from, Node to, BiConsumer<ConfigurationNode, Node> converter) {
        if (!from.isVirtual()) {
            converter.accept(from, to);
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
            from.getChildrenMap().values().stream()
                .sorted((o1, o2) -> String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey())))
                .forEach(n -> move(n, to.resolve(n.getKey())));
        } else {
            to.set(value, Storm.ANY_NULLABLE);
        }
    }

}
