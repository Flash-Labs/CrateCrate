package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.prize.Prize;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Config {

    public static final Map<String, Crate> CRATES = new HashMap<>();
    public static final Map<String, Reward> REWARDS = new HashMap<>();
    public static final Map<String, Prize> PRIZES = new HashMap<>();
    public static final Map<String, Key> KEYS = new HashMap<>();

    private static final Path DIRECTORY = Sponge.getConfigManager()
        .getPluginConfig(CrateCrate.getContainer())
        .getDirectory();

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("config"));
            ConfigurationNode main = load("cratecrate.conf");
            ConfigurationNode keys = load("config/keys.conf");
            ConfigurationNode prizes = load("config/prizes.conf");
            ConfigurationNode rewards = load("config/rewards.conf");
            ConfigurationNode crates = load("config/crates.conf");
            keys.getChildrenMap().values().forEach(n -> {
                Key key = resolveKeyType(n).deserializeComponent(n);
                KEYS.put(key.id(), key);
            });
            prizes.getChildrenMap().values().forEach(n -> {
                Prize prize = resolvePrizeType(n).deserializeComponent(n);
                PRIZES.put(prize.id(), prize);
            });
            rewards.getChildrenMap().values().forEach(n -> {
                Reward reward = resolveRewardType(n).deserializeComponent(n);
                REWARDS.put(reward.id(), reward);
            });
            crates.getChildrenMap().values().forEach(n -> {
                Crate crate = resolveCrateType(n).deserializeComponent(n);
                CRATES.put(crate.id(), crate);
            });
            CrateCrate.getContainer().getLogger().info("Successfully loaded the config.");
        } catch (IOException e) {
            CrateCrate.getContainer().getLogger().error("Error loading the config: ", e);
        } catch (SerializationException e) {
            CrateCrate.getContainer().getLogger().error("Error loading the config @" + Arrays.toString(e.getNode().getPath()) + ": " + e.getMessage());
        }
    }

    private static ConfigurationNode load(String name) throws IOException {
        Path path = DIRECTORY.resolve(name);
        Sponge.getAssetManager().getAsset(CrateCrate.getContainer(), name).get().copyToFile(path);
        return HoconConfigurationLoader.builder().setPath(path).build().load();
    }

    public static Type<? extends Crate, Void> resolveCrateType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Crate, Void>) Config.<Crate>resolveType(node, Crate.TYPES, CRATES);
    }

    public static Type<? extends Reward, BigDecimal> resolveRewardType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Reward, BigDecimal>) Config.<Reward>resolveType(node, Reward.TYPES, REWARDS);
    }

    public static Type<? extends Prize, ?> resolvePrizeType(ConfigurationNode node) throws SerializationException {
        return Config.<Prize>resolveType(node, Prize.TYPES, PRIZES);
    }

    public static Type<? extends Key, Integer> resolveKeyType(ConfigurationNode node) throws SerializationException {
        return (Type<? extends Key, Integer>) Config.<Key>resolveType(node, Key.TYPES, KEYS);
    }

    private static <T extends Component> Type<? extends T, ?> resolveType(
        ConfigurationNode node,
        Map<String, Type<? extends T, ?>> types,
        Map<String, T> registry
    ) throws SerializationException {
        String identifier = Optional.ofNullable(node.getString()).orElse("");
        if (registry.containsKey(identifier)) {
            return types.get(registry.get(identifier).getClass().getName());
        }
        if (!node.getNode("type").isVirtual()) {
            String type = node.getNode("type").getString();
            if (!types.containsKey(type)) {
                throw new SerializationException(node.getNode("type"), "Unknown type " + type + ".");
            }
            return types.get(type);
        }
        List<Type<? extends T, ?>> matches = types.values().stream()
            .distinct()
            .filter(t -> t.matches(node))
            .collect(Collectors.toList());
        switch (matches.size()) {
            case 0: throw new SerializationException(node, "Unable to identify type.");
            case 1: return matches.get(0);
            default:
                List<String> names = matches.stream().map(Type::name).collect(Collectors.toList());
                throw new SerializationException(node, "Node matched multiple types: " + names + ".");
        }
    }

}
