package vn.edu.auction.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
public record CreateAuctionRequest(
    @NotNull Long sellerId,
    @NotBlank @Size(max=150) String itemName,
    String description,
    @NotNull BigDecimal startingPrice,
    @NotNull BigDecimal minIncrement,
    @NotNull Instant startTime,
    @NotNull Instant endTime
) {}
