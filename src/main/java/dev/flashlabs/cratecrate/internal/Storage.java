package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Storage {

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

}
