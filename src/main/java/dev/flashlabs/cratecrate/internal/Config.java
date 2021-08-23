package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Type;
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

public class Config {

    public static final Map<String, Prize> PRIZES = new HashMap<>();

    private static final Path DIRECTORY = Sponge.configManager()
        .pluginConfig(CrateCrate.getContainer())
        .directory();

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("config"));
            var main = load("cratecrate.conf");
            var prizes = load("config/prizes.conf");
            for (ConfigurationNode node : prizes.childrenMap().values()) {
                Prize prize = ((Type<? extends Prize, ?>) resolveType(node, Prize.class, Prize.TYPES)).deserializeComponent(node);
                PRIZES.put(prize.id, prize);
            }
            CrateCrate.getContainer().logger().info("Successfully loaded the config.");
        } catch (IOException e) {
            CrateCrate.getContainer().logger().error("Error loading the config: ", e);
        }
    }

    private static ConfigurationNode load(String name) throws IOException {
        Path path = DIRECTORY.resolve(name);
        Sponge.assetManager().asset(CrateCrate.getContainer(), name).get().copyToFile(path);
        return HoconConfigurationLoader.builder().path(path).build().load();
    }

    private static <T extends Component<?>> Type<? extends T, ?> resolveType(
        ConfigurationNode node,
        Class<T> component,
        Map<String, Type<? extends T, ?>> types
    ) throws SerializationException {
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
                var names = matches.stream().map(t -> t.name).toList();
                throw new SerializationException(node, component, "Node matched multiple types: " + names + ".");
        }
    }

}
