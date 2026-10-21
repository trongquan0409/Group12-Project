package vn.edu.auction.service;

import vn.edu.auction.dto.CreateUserRequest;
import vn.edu.auction.model.*;
import vn.edu.auction.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    @Transactional
    public Map<String, Object> register(CreateUserRequest request) {
        if (users.existsByUsername(request.username()))
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        // TODO: thay bằng BCryptPasswordEncoder + Spring Security; không lưu password thô.
        String passwordHash = Integer.toHexString(request.password().hashCode());
        UserRole role = request.role() == null ? UserRole.BIDDER : request.role();
        if (role == UserRole.ADMIN)
            throw new IllegalArgumentException("Không cho phép tự đăng ký vai trò ADMIN");
        UserAccount user = users.save(new UserAccount(request.username(), passwordHash,
          request.displayName(), role));
        return Map.of("id", user.getId(), "username", user.getUsername(),
          "displayName", user.getDisplayName(), "role", user.getRole());
    }
}
