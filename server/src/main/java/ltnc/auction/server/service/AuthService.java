package ltnc.auction.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthService {
    private record Account(String password, String role) {}

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public AuthService() {
        accounts.put("bidder", new Account("123456", "BIDDER"));
        accounts.put("seller", new Account("123456", "SELLER"));
        accounts.put("admin", new Account("123456", "ADMIN"));
    }

    public String authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        Account account = accounts.get(username.trim());

        if (account == null || !account.password().equals(password)) {
            return null;
        }

        return account.role();
    }
}
