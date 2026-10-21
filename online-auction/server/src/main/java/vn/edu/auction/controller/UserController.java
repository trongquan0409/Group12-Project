package vn.edu.auction.controller;

import vn.edu.auction.dto.CreateUserRequest;
import vn.edu.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody CreateUserRequest request) {
        return service.register(request);
    }
}
