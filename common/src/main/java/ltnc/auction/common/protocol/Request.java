package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import ltnc.auction.common.protocol.CreateItemRequest;

public final class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final RequestType type;
    private final String username;
    private final String password;
    private final String auctionId;
    private final BigDecimal amount;
    private final String role;
    private final CreateItemRequest itemRequest;
    private final BigDecimal startingPrice;
    private final BigDecimal increment;
    private final Instant endAt;

    public Request(RequestType type, String username, String password,
                   String auctionId, BigDecimal amount) {
        this(type, username, password, auctionId, amount, null,
                null, null, null, null);
    }

    public Request(RequestType type, String username, String password,
                   String auctionId, BigDecimal amount, String role) {
        this(type, username, password, auctionId, amount, role,
                null, null, null, null);
    }

    public Request(RequestType type, String username,
                   CreateItemRequest itemRequest,
                   BigDecimal startingPrice,
                   BigDecimal increment,
                   Instant endAt) {
        this(type, username, null, null, null, null,
                itemRequest, startingPrice, increment, endAt);
    }

    private Request(RequestType type, String username, String password,
                    String auctionId, BigDecimal amount, String role,
                    CreateItemRequest itemRequest,
                    BigDecimal startingPrice,
                    BigDecimal increment,
                    Instant endAt) {
        this.type = type;
        this.username = username;
        this.password = password;
        this.auctionId = auctionId;
        this.amount = amount;
        this.role = role;
        this.itemRequest = itemRequest;
        this.startingPrice = startingPrice;
        this.increment = increment;
        this.endAt = endAt;
    }

    public RequestType getType() { return type; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getAuctionId() { return auctionId; }
    public BigDecimal getAmount() { return amount; }
    public String getRole() { return role; }
    public CreateItemRequest getItemRequest() { return itemRequest; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getIncrement() { return increment; }
    public Instant getEndAt() { return endAt; }
}
