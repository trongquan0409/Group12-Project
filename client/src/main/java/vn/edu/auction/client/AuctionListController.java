package vn.edu.auction.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class AuctionListController {
    @FXML private TableView<AuctionDto> auctionTable;
    @FXML private TableColumn<AuctionDto, Number> idColumn;
    @FXML private TableColumn<AuctionDto, String> priceColumn;
    @FXML private TableColumn<AuctionDto, String> statusColumn;
    @FXML private Label messageLabel;
    @FXML private TextField auctionIdField;
    @FXML private TextField bidderIdField;
    @FXML private TextField amountField;

    private final ApiClient api = new ApiClient();
    private final ObservableList<AuctionDto> rows = FXCollections.observableArrayList();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private WebSocket webSocket;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().id()));
        priceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
          data.getValue().currentPrice().toPlainString()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().status()));
        auctionTable.setItems(rows);
        refresh();
        connectWebSocket();
    }

    @FXML
    private void refresh() {
        messageLabel.setText("Đang tải dữ liệu...");
        Thread.startVirtualThread(() -> {
            try {
                var auctions = api.getAuctions();
                Platform.runLater(() -> {
                    rows.setAll(auctions);
                    messageLabel.setText("Đã tải " + auctions.size() + " phiên đấu giá.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> messageLabel.setText("Không kết nối được server: " + ex.getMessage()));
            }
        });
    }

    @FXML
    private void placeBid() {
        try {
            long auctionId = Long.parseLong(auctionIdField.getText().trim());
            long bidderId = Long.parseLong(bidderIdField.getText().trim());
            String amount = amountField.getText().trim();
            String json = mapper.writeValueAsString(new BidRequest(bidderId, new java.math.BigDecimal(amount)));
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/auctions/" + auctionId + "/bids"))
              .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            Thread.startVirtualThread(() -> {
                try {
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonNode body = mapper.readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() >= 400) messageLabel.setText("Đặt giá thất bại: " + body.path("error").asText(response.body()));
                        else messageLabel.setText("Đặt giá thành công.");
                        refresh();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> messageLabel.setText("Lỗi gửi bid: " + ex.getMessage()));
                }
            });
        } catch (Exception ex) {
            messageLabel.setText("Dữ liệu không hợp lệ: nhập Auction ID, Bidder ID và số tiền.");
        }
    }

    private void connectWebSocket() {
        http.newWebSocketBuilder().buildAsync(URI.create("ws://localhost:8080/ws/auctions"), new WebSocket.Listener() {
            private final StringBuilder partial = new StringBuilder();
            @Override
            public void onOpen(WebSocket ws) {
                webSocket = ws;
                ws.request(1);
            }
            @Override
            public java.util.concurrent.CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                partial.append(data);
                if (last) {
                    String message = partial.toString();
                    partial.setLength(0);
                    Platform.runLater(() -> {
                        messageLabel.setText("Realtime: " + message);
                        refresh();
                    });
                }
                ws.request(1);
                return null;
            }
            @Override
            public void onError(WebSocket ws, Throwable error) {
                Platform.runLater(() -> messageLabel.setText("WebSocket mất kết nối; dùng nút Làm mới để tải lại."));
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> messageLabel.setText("Không mở được WebSocket: server có đang chạy không?"));
            return null;
        });
    }

    private record BidRequest(Long bidderId, java.math.BigDecimal amount) {}
}
