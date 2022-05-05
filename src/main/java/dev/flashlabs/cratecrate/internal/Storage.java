package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.flashlibs.database.Connection;
import dev.flashlabs.flashlibs.database.DatabaseService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Storage {

    public static final Map<Location<World>, Optional<Crate>> LOCATIONS = new HashMap<>();

    private static final Path DIRECTORY = Sponge.getConfigManager()
        .getPluginConfig(CrateCrate.get().getContainer())
        .getDirectory()
        .resolve("storage");
    private static DatabaseService DATABASE;

    public static void load() {
        try {
            Files.createDirectories(DIRECTORY);
            DATABASE = DatabaseService.of(Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource("jdbc:h2:" + DIRECTORY.resolve("storage.db") + ";MODE=MySQL"));
            try (Connection connection = DATABASE.getConnection()) {
                connection.update("" +
                    "CREATE TABLE IF NOT EXISTS StandardKeys (\n" +
                    "    user_uuid CHAR (36) NOT NULL,\n" +
                    "    key_id VARCHAR (255) NOT NULL,\n" +
                    "    quantity INT NOT NULL,\n" +
                    "    PRIMARY KEY (user_uuid, key_id)\n" +
                    ")"
                );
                connection.update("" +
                    "CREATE TABLE IF NOT EXISTS Locations (\n" +
                    "    world_key VARCHAR(255) NOT NULL,\n" +
                    "    block_x INT NOT NULL,\n" +
                    "    block_y INT NOT NULL,\n" +
                    "    block_z INT NOT NULL,\n" +
                    "    crate_id VARCHAR(255) NOT NULL,\n" +
                    "    PRIMARY KEY (world_key, block_x, block_y, block_z)\n" +
                    ")"
                );
                ResultSet result = connection.query("" +
                    "SELECT * FROM Locations\n"
                );
                while (result.next()) {
                    World world = Sponge.getServer().getWorld(UUID.fromString(result.getString(1))).orElse(null);
                    if (world != null) {
                        Location<World> location = world.getLocation(result.getInt(2), result.getInt(3), result.getInt(4));
                        Crate crate = Config.CRATES.get(result.getString(5));
                        LOCATIONS.put(location, Optional.ofNullable(crate));
                        if (crate == null) {
                            CrateCrate.get().getContainer().getLogger().error("Location is set to unknown crate: " + result.getString(5) + ".");
                        }
                    } else {
                        CrateCrate.get().getContainer().getLogger().error("Location is set to unknown world: " + result.getString(1) + ".");
                    }
                }
            }
        } catch (IOException | SQLException e) {
            CrateCrate.get().getContainer().getLogger().error("Error loading storage: ", e);
        }
    }

    public static int queryKeyQuantity(User user, Key key) throws SQLException {
        ResultSet result = DATABASE.query("" +
            "SELECT quantity\n" +
            "FROM StandardKeys\n" +
            "WHERE user_uuid = ? AND key_id = ?\n",
            user.getUniqueId().toString(),
            key.id()
        );
        return result.next() ? result.getInt(1) : 0;
    }

    public static void updateKeyQuantity(User user, Key key, int delta) throws SQLException {
        DATABASE.update("" +
            "INSERT INTO StandardKeys (user_uuid, key_id, quantity)\n" +
            "VALUES (?, ?, ?)\n" +
            "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)\n",
            user.getUniqueId().toString(),
            key.id(),
            delta
        );
    }

    public static void setLocation(Location<World> location, Crate crate) throws SQLException {
        DATABASE.update("" +
            "INSERT INTO Locations (world_key, block_x, block_y, block_z, crate_id)\n" +
            "VALUES (?, ?, ?, ?, ?)\n" +
            "ON DUPLICATE KEY UPDATE crate_id = VALUES(crate_id)\n",
            location.getExtent().getUniqueId().toString(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            crate.id()
        );
        LOCATIONS.put(location, Optional.of(crate));
    }

    public static void deleteLocation(Location<World> location) throws SQLException {
        DATABASE.update("" +
            "DELETE FROM Locations\n" +
            "WHERE world_key = ? AND block_x = ? AND block_y = ? AND block_z = ?\n",
            location.getExtent().getUniqueId().toString(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
        LOCATIONS.remove(location);
    }

}
