package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static final Path DIRECTORY = Sponge.configManager()
        .pluginConfig(CrateCrate.getContainer())
        .directory();

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("config"));
            var main = load("cratecrate.conf");
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

}
