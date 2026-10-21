package vn.edu.auction.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final AuctionWebSocketHandler handler;
    public WebSocketConfig(AuctionWebSocketHandler handler) { this.handler = handler; }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/auctions").setAllowedOrigins("http://localhost", "http://127.0.0.1");
    }
}
