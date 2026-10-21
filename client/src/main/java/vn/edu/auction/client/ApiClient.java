package vn.edu.auction.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;

public class ApiClient {
    private static final String BASE = "http://localhost:8080";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public List<AuctionDto> getAuctions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/api/auctions")).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new IllegalStateException(response.body());
        return mapper.readValue(response.body(), new TypeReference<>() {});
    }
}
