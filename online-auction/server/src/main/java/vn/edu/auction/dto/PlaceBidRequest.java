package vn.edu.auction.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
public record PlaceBidRequest(@NotNull Long bidderId, @NotNull @Positive BigDecimal amount) {}
