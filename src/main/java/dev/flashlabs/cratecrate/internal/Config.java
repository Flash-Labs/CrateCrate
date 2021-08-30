package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.prize.Prize;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Config {

    public static final Map<String, Crate> CRATES = new HashMap<>();
    public static final Map<String, Reward> REWARDS = new HashMap<>();
    public static final Map<String, Prize> PRIZES = new HashMap<>();
    public static final Map<String, Key> KEYS = new HashMap<>();

    private static final Path DIRECTORY = Sponge.configManager()
        .pluginConfig(CrateCrate.container())
        .directory();

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("config"));
            var main = load("cratecrate.conf");
            var keys = load("config/keys.conf");
            for (ConfigurationNode node : keys.childrenMap().values()) {
                Key key = resolveKeyType(node).deserializeComponent(node);
                KEYS.put(key.id(), key);
            }
            var prizes = load("config/prizes.conf");
            for (ConfigurationNode node : prizes.childrenMap().values()) {
                Prize prize = resolvePrizeType(node).deserializeComponent(node);
                PRIZES.put(prize.id(), prize);
            }
            var rewards = load("config/rewards.conf");
            for (ConfigurationNode node : rewards.childrenMap().values()) {
                Reward reward = resolveRewardType(node).deserializeComponent(node);
                REWARDS.put(reward.id(), reward);
            }
            var crates = load("config/crates.conf");
            for (ConfigurationNode node : crates.childrenMap().values()) {
                Crate crate = resolveCrateType(node).deserializeComponent(node);
                CRATES.put(crate.id(), crate);
            }
            CrateCrate.container().logger().info("Successfully loaded the config.");
        } catch (IOException e) {
            CrateCrate.container().logger().error("Error loading the config: ", e);
        }
    }

    private static ConfigurationNode load(String name) throws IOException {
        Path path = DIRECTORY.resolve(name);
        Sponge.assetManager().asset(CrateCrate.container(), name).get().copyToFile(path);
        return HoconConfigurationLoader.builder().path(path).build().load();
    }

    public static Type<? extends Crate, Void> resolveCrateType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Crate, Void>) Config.<Crate>resolveType(node, Crate.class, Crate.TYPES, CRATES);
    }

    public static Type<? extends Reward, Integer> resolveRewardType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Reward, Integer>) Config.<Reward>resolveType(node, Reward.class, Reward.TYPES, REWARDS);
    }

    public static Type<? extends Prize, ?> resolvePrizeType(ConfigurationNode node) throws SerializationException {
        return Config.<Prize>resolveType(node, Prize.class, Prize.TYPES, PRIZES);
    }

    public static Type<? extends Key, Integer> resolveKeyType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Key, Integer>) Config.<Key>resolveType(node, Key.class, Key.TYPES, KEYS);
    }

    private static <T extends Component> Type<? extends T, ?> resolveType(
        ConfigurationNode node,
        Class<T> component,
        Map<String, Type<? extends T, ?>> types,
        Map<String, T> registry
    ) throws SerializationException {
        var identifier = Optional.ofNullable(node.getString()).orElse("");
        if (registry.containsKey(identifier)) {
            return types.get(registry.get(identifier).getClass().getName());
        }
        if (node.hasChild("type")) {
            var type = node.node("type").getString();
            if (!types.containsKey(type)) {
                throw new SerializationException(node.node("type"), component, "Unknown type " + type + ".");
            }
            return types.get(type);
        }
        var matches = types.values().stream()
            .distinct()
            .filter(t -> t.matches(node))
            .toList();
        switch (matches.size()) {
            case 0: throw new SerializationException(node, component, "Unable to identify type.");
            case 1: return matches.get(0);
            default:
                var names = matches.stream().map(Type::name).toList();
                throw new SerializationException(node, component, "Node matched multiple types: " + names + ".");
        }
    }

}
