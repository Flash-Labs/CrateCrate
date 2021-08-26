package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Storage {

    private static final Path DIRECTORY = Sponge.configManager()
        .pluginConfig(CrateCrate.getContainer())
        .directory()
        .resolve("storage");
    private static DataSource source;

    public static void load() {
        try {
            Class.forName("dev.flashlabs.cratecrate.shadow.org.h2.Driver");
            Files.createDirectories(DIRECTORY);
            source = Sponge.sqlManager().dataSource("jdbc:h2:" + DIRECTORY.resolve("storage.db") + ";MODE=MySQL");
        } catch (ClassNotFoundException | IOException | SQLException e) {
            CrateCrate.getContainer().logger().error("Error loading storage: ", e);
        }
    }

}
