package vn.edu.auction.controller;

import vn.edu.auction.dto.*;
import vn.edu.auction.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {
    private final AuctionService service;
    public AuctionController(AuctionService service) { this.service = service; }

    @GetMapping
    public List<AuctionResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public AuctionResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public AuctionResponse create(@Valid @RequestBody CreateAuctionRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/bids")
    public BidResponse bid(@PathVariable Long id, @Valid @RequestBody PlaceBidRequest request) {
        return service.placeBid(id, request);
    }

    @GetMapping("/{id}/bids")
    public List<BidResponse> history(@PathVariable Long id) { return service.history(id); }
}
