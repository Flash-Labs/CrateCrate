package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.key.Key;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.server.ServerLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public final class Storage {

    public static final Map<ServerLocation, Crate> LOCATIONS = new HashMap<>();

    private static final Path DIRECTORY = Sponge.configManager()
        .pluginConfig(CrateCrate.container())
        .directory()
        .resolve("storage");
    private static DataSource source;

    public static void load() {
        try {
            Class.forName("dev.flashlabs.cratecrate.shadow.org.h2.Driver");
            Files.createDirectories(DIRECTORY);
            source = Sponge.sqlManager().dataSource("jdbc:h2:" + DIRECTORY.resolve("storage.db") + ";MODE=MySQL");
            try (var connection = source.getConnection()) {
                connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS StandardKeys (
                        uuid CHAR (36) NOT NULL,
                        key_id VARCHAR (255) NOT NULL,
                        quantity INT NOT NULL,
                        PRIMARY KEY (uuid, key_id)
                    )
                    """).executeUpdate();
                connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS Locations (
                        world_key VARCHAR(255) NOT NULL,
                        block_x INT NOT NULL,
                        block_y INT NOT NULL,
                        block_z INT NOT NULL,
                        crate_id VARCHAR(255) NOT NULL,
                        PRIMARY KEY (world_key, block_x, block_y, block_z)
                    )
                    """).executeUpdate();
                var result = connection.prepareStatement("""
                    SELECT * FROM Locations
                    """).executeQuery();
                while (result.next()) {
                    var world = Sponge.server().worldManager().world(ResourceKey.resolve(result.getString(1)));
                    if (world.isPresent()) {
                        var location = world.get().location(result.getInt(2), result.getInt(3), result.getInt(4));
                        var crate = Config.CRATES.get(result.getString(5));
                        LOCATIONS.put(location, crate);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException | SQLException e) {
            CrateCrate.container().logger().error("Error loading storage: ", e);
        }
    }

    public static int queryKeyQuantity(User user, Key key) throws SQLException {
        try (var connection = source.getConnection()) {
            var statement = connection.prepareStatement("""
                SELECT quantity
                FROM StandardKeys
                WHERE uuid = ? AND key_id = ?
                """);
            statement.setString(1, user.uniqueId().toString());
            statement.setString(2, key.id());
            var result = statement.executeQuery();
            return result.next() ? result.getInt(1) : 0;
        }
    }

    public static void updateKeyQuantity(User user, Key key, int delta) throws SQLException {
        try (var connection = source.getConnection()) {
            var statement = connection.prepareStatement("""
                INSERT INTO StandardKeys (uuid, key_id, quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
                """);
            statement.setString(1, user.uniqueId().toString());
            statement.setString(2, key.id());
            statement.setInt(3, delta);
            statement.executeUpdate();
        }
    }

    public static void setLocation(ServerLocation location, Crate crate) throws SQLException {
        try (var connection = source.getConnection()) {
            var statement = connection.prepareStatement("""
                INSERT INTO Locations (world_key, block_x, block_y, block_z, crate_id)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE crate_id = VALUES(crate_id)
                """);
            statement.setString(1, location.world().key().formatted());
            statement.setInt(2, location.blockX());
            statement.setInt(3, location.blockY());
            statement.setInt(4, location.blockZ());
            statement.setString(5, crate.id());
            statement.executeUpdate();
        }
    }

    public static void deleteLocation(ServerLocation location) throws SQLException {
        try (var connection = source.getConnection()) {
            var statement = connection.prepareStatement("""
                DELETE FROM Locations
                WHERE world_key = ? AND block_x = ? AND block_y = ? AND block_z = ?
                """);
            statement.setString(1, location.world().key().formatted());
            statement.setInt(2, location.blockX());
            statement.setInt(3, location.blockY());
            statement.setInt(4, location.blockZ());
            statement.executeUpdate();
        }
    }

}
