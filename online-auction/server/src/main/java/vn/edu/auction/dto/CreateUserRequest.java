package vn.edu.auction.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.edu.auction.model.UserRole;
public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotBlank @Size(max = 100) String displayName,
    UserRole role
) {}
