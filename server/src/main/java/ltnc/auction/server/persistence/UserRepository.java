package ltnc.auction.server.persistence;

import ltnc.auction.common.model.UserAccount;
import ltnc.auction.common.model.UserRole;
import ltnc.auction.common.model.UserStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC repository for user accounts stored in the users table.
 */
@SuppressWarnings("unused") // Repository is ready for wiring into the account service.
public final class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /**
     * Inserts a new account and returns a copy containing its database ID.
     */
    public UserAccount save(UserAccount account) {
        Objects.requireNonNull(account, "account");
        String sql = "INSERT INTO users "
                + "(username, password_hash, display_name, role, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPasswordHash());
            statement.setString(3, account.getDisplayName());
            statement.setString(4, account.getRole().name());
            statement.setString(5, account.getStatus().name());
            statement.setTimestamp(6, Timestamp.from(account.getCreatedAt()));
            statement.setTimestamp(7, Timestamp.from(account.getUpdatedAt()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the generated user ID.");
                }
                return copyWithId(account, keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save user account.", exception);
        }
    }

    public Optional<UserAccount> findById(long id) {
        return findOne("SELECT * FROM users WHERE id = ?", statement -> statement.setLong(1, id));
    }

    public Optional<UserAccount> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        return findOne("SELECT * FROM users WHERE username = ?",
                statement -> statement.setString(1, normalized));
    }

    /**
     * Updates editable profile and account state fields; returns false if no row matched.
     */
    public boolean update(UserAccount account) {
        Objects.requireNonNull(account, "account");
        if (account.getId() == null) {
            throw new IllegalArgumentException("Cannot update an account without a database ID.");
        }

        String sql = "UPDATE users SET display_name = ?, role = ?, status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getDisplayName());
            statement.setString(2, account.getRole().name());
            statement.setString(3, account.getStatus().name());
            statement.setTimestamp(4, Timestamp.from(account.getUpdatedAt()));
            statement.setLong(5, account.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update user account.", exception);
        }
    }

    private Optional<UserAccount> findOne(String sql, StatementBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find user account.", exception);
        }
    }

    private UserAccount map(ResultSet row) throws SQLException {
        return new UserAccount(
                row.getLong("id"),
                row.getString("username"),
                row.getString("password_hash"),
                row.getString("display_name"),
                UserRole.valueOf(row.getString("role")),
                UserStatus.valueOf(row.getString("status")),
                toInstant(row.getTimestamp("created_at")),
                toInstant(row.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private UserAccount copyWithId(UserAccount account, long id) {
        return new UserAccount(id, account.getUsername(), account.getPasswordHash(),
                account.getDisplayName(), account.getRole(), account.getStatus(),
                account.getCreatedAt(), account.getUpdatedAt());
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
