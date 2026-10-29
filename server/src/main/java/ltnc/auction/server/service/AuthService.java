package ltnc.auction.server.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Handles account storage, password verification, and in-memory sessions. */
public final class AuthService {
    private static final int ITERATIONS = 210_000;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private record Account(String salt, String passwordHash, String role)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private final Path file;
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AuthService() {
        this(Path.of(System.getenv().getOrDefault(
                "AUCTION_ACCOUNT_FILE", "server/data/accounts.ser")));
    }

    public AuthService(Path file) {
        this.file = file;
        load();
        seedDemoAccounts();
    }

    public synchronized String register(
            String username, String password, String requestedRole) {
        String name = normalize(username);
        if (name.length() < 3 || name.length() > 50) {
            throw new IllegalArgumentException("Username must contain 3 to 50 characters.");
        }
        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new IllegalArgumentException("Password must contain 8 to 100 characters.");
        }
        if (accounts.containsKey(name)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        String role = requestedRole == null || requestedRole.isBlank()
                ? "BIDDER" : requestedRole.trim().toUpperCase(Locale.ROOT);
        if (!role.equals("BIDDER") && !role.equals("SELLER")) {
            throw new IllegalArgumentException("Only BIDDER or SELLER can be registered.");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        accounts.put(name, new Account(saltText, hash(password, salt), role));
        save();
        return role;
    }

    public String authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        Account account = accounts.get(normalize(username));
        if (account == null) {
            return null;
        }
        byte[] actual = Base64.getDecoder().decode(hash(password,
                Base64.getDecoder().decode(account.salt())));
        byte[] expected = Base64.getDecoder().decode(account.passwordHash());
        return MessageDigest.isEqual(actual, expected) ? account.role() : null;
    }

    public String createSession(String username) {
        String name = normalize(username);
        if (!accounts.containsKey(name)) {
            throw new IllegalArgumentException("Account does not exist.");
        }
        String token = UUID.randomUUID() + UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, name);
        return token;
    }

    public String usernameForSession(String token) {
        return token == null ? null : sessions.get(token);
    }

    public String roleForSession(String token) {
        String username = usernameForSession(token);
        Account account = username == null ? null : accounts.get(username);
        return account == null ? null : account.role();
    }

    public void invalidateSession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private byte[] hash(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash password.", exception);
        } finally {
            spec.clearPassword();
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            Object data = input.readObject();
            if (data instanceof Map<?, ?> saved) {
                saved.forEach((key, value) -> {
                    if (key instanceof String name && value instanceof Account account) {
                        accounts.put(name, account);
                    }
                });
            }
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException("Could not load account data.", exception);
        }
    }

    private synchronized void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(file))) {
                output.writeObject(new HashMap<>(accounts));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save account data.", exception);
        }
    }

    private void seedDemoAccounts() {
        if (!accounts.isEmpty()) {
            return;
        }
        seedAccount("bidder", "123456", "BIDDER");
        seedAccount("seller", "123456", "SELLER");
        seedAccount("admin", "123456", "ADMIN");
        save();
    }

    private void seedAccount(String username, String password, String role) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        accounts.put(username, new Account(Base64.getEncoder().encodeToString(salt),
                hash(password, salt), role));
    }
}
