package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.component.effect.Effect;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.converter.TeslaCrateConverter;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.format.ParseException;
import dev.willbanders.storm.serializer.SerializationException;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Config {

    public static final Map<String, Crate> CRATES = new HashMap<>();
    public static final Map<String, Reward> REWARDS = new HashMap<>();
    public static final Map<String, Prize> PRIZES = new HashMap<>();
    public static final Map<String, Key> KEYS = new HashMap<>();
    public static final Map<String, Effect> EFFECTS = new HashMap<>();

    private static final Path DIRECTORY = Sponge.getConfigManager()
        .getPluginConfig(CrateCrate.get().getContainer())
        .getDirectory();

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("config"));
            Node main = load("cratecrate.conf");
            if (main.get("convert.teslacrate", Storm.BOOLEAN.optional(false))) {
                CrateCrate.get().getLogger().info("Converting config from TeslaCrate...");
                TeslaCrateConverter.convert();
                try {
                    main.set("convert.teslacrate", false, Storm.BOOLEAN);
                    Files.write(DIRECTORY.resolve("cratecrate.conf"), Storm.reserialize(main).getBytes());
                    CrateCrate.get().getLogger().info("Successfully converted the config.");
                } catch (IOException e) {
                    CrateCrate.get().getLogger().error("Error saving the config file cratecrate.conf.", e);
                    throw e;
                }
            }
            Node effects = loadComponents("config/effects.conf", Config::resolveEffectType, EFFECTS);
            Node keys = loadComponents("config/keys.conf", Config::resolveKeyType, KEYS);
            Node prizes = loadComponents("config/prizes.conf", Config::resolvePrizeType, PRIZES);
            Node rewards = loadComponents("config/rewards.conf", Config::resolveRewardType, REWARDS);
            Node crates = loadComponents("config/crates.conf", Config::resolveCrateType, CRATES);
            CrateCrate.get().getContainer().getLogger().info("Successfully loaded the config.");
        } catch (IOException | ParseException | SerializationException e) {
            CrateCrate.get().getLogger().error("Configuration loading has halted early, certain features may or may not be operational.");
        }
    }

    private static Node load(String file) throws IOException {
        try {
            Path path = DIRECTORY.resolve(file);
            Sponge.getAssetManager().getAsset(CrateCrate.get().getContainer(), file).get().copyToFile(path);
            return Storm.deserialize(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            CrateCrate.get().getLogger().error("Error loading the config file " + file + ".", e);
            throw e;
        } catch (ParseException e) {
            CrateCrate.get().getLogger().error("Error parsing the config file " + file + ".");
            CrateCrate.get().getLogger().error("\n" + e.getDiagnostic().toString());
            throw e;
        }
    }

    private static <T extends Component<?>> Node loadComponents(
        String file,
        Function<Node, Type<? extends T, ?>> resolver,
        Map<String, T> components
    ) throws IOException {
        Node node = load(file);
        try {
            node.get(Storm.MAP.of(n -> n)).values().forEach(n -> {
                T component = resolver.apply(n).deserializeComponent(n);
                components.put(component.id(), component);
            });
            return node;
        } catch (SerializationException e) {
            String path = e.getNode().getPath().stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
            CrateCrate.get().getLogger().error("Error deserializing the config file " + file + ".");
            CrateCrate.get().getLogger().error("Error @" + path + ": " + e.getMessage());
            throw e;
        }
    }

    public static Type<Crate, Void> resolveCrateType(Node node) throws SerializationException {
        return (Type<Crate, Void>) Config.<Crate>resolveType(node, Crate.TYPES, CRATES);
    }

    public static Type<Reward, BigDecimal> resolveRewardType(Node node) throws SerializationException {
        return (Type<Reward, BigDecimal>) Config.<Reward>resolveType(node, Reward.TYPES, REWARDS);
    }

    public static Type<? extends Prize, ?> resolvePrizeType(Node node) throws SerializationException {
        return Config.<Prize>resolveType(node, Prize.TYPES, PRIZES);
    }

    public static Type<? extends Key, Integer> resolveKeyType(Node node) throws SerializationException {
        return (Type<? extends Key, Integer>) Config.<Key>resolveType(node, Key.TYPES, KEYS);
    }

    public static Type<? extends Effect, ?> resolveEffectType(Node node) throws SerializationException {
        return Config.<Effect>resolveType(node, Effect.TYPES, EFFECTS);
    }

    private static <T extends Component> Type<? extends T, ?> resolveType(
        Node node,
        Map<String, Type<? extends T, ?>> types,
        Map<String, T> registry
    ) throws SerializationException {
        String identifier = node.getType() == Node.Type.STRING ? node.get(Storm.STRING) : "";
        if (registry.containsKey(identifier)) {
            return types.get(registry.get(identifier).getClass().getName());
        }
        if (node.resolve("type").getType() != Node.Type.UNDEFINED) {
            String type = node.resolve("type").get(Storm.STRING);
            if (!types.containsKey(type)) {
                throw new SerializationException(node.resolve("type"), "Unknown type " + type + ".");
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
