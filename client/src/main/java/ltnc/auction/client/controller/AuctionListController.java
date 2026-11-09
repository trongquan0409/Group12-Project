
package ltnc.auction.client.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import ltnc.auction.client.net.AuctionSocketClient;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.ItemType;
import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.RequestType;
import ltnc.auction.common.protocol.Response;

public class AuctionListController {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5050;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    @FXML
    private StackPane root;

    @FXML
    private VBox authView;

    @FXML
    private VBox registerView;

    @FXML
    private Node adminView;

    @FXML
    private Node sellerView;

    @FXML
    private Node bidderView;

    @FXML
    private TextField loginUsername;

    @FXML
    private TextField registerUsername;

    @FXML
    private TextField auctionIdField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField sellerItemName;

    @FXML
    private TextArea sellerItemDescription;

    @FXML
    private ChoiceBox<String> sellerItemType;

    @FXML
    private TextField sellerBrand;

    @FXML
    private TextField sellerWarranty;

    @FXML
    private TextField sellerArtist;

    @FXML
    private TextField sellerYear;

    @FXML
    private TextField sellerMileage;

    @FXML
    private TextField sellerPlateNo;

    @FXML
    private TextField sellerStartingPrice;

    @FXML
    private TextField sellerIncrement;

    @FXML
    private DatePicker sellerEndDate;

    @FXML
    private Label sellerMessage;


    @FXML
    private PasswordField loginPassword;

    @FXML
    private PasswordField registerPassword;

    @FXML
    private ChoiceBox<String> loginRole;

    @FXML
    private ChoiceBox<String> registerRole;

    @FXML
    private Label authMessage;

    @FXML
    private Label registerMessage;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> auctionList;

    // Bảng lịch sử đặt giá
    @FXML
    private TableView<Bid> bidHistoryTable;

    @FXML
    private TableColumn<Bid, String> bidderColumn;

    @FXML
    private TableColumn<Bid, String> amountColumn;

    @FXML
    private TableColumn<Bid, String> timeColumn;

    private String currentUsername;
    private String currentRole;

    // Mã phiên đấu giá đang được chọn
    private String selectedAuctionId;

    // Tự động cập nhật lịch sử đấu giá
    private Timeline historyRefreshTimeline;

    // Tránh gửi nhiều yêu cầu tải lịch sử cùng lúc
    private final AtomicBoolean historyLoading = new AtomicBoolean(false);

    // Tránh listener xử lý nhầm khi chương trình tự tải lại danh sách
    private boolean updatingAuctionList;

    @FXML
    public void initialize() {

        loginRole.getItems().setAll(
                "Người đấu giá",
                "Người bán",
                "Admin (demo)"
        );

        loginRole.setValue("Người đấu giá");

        registerRole.getItems().setAll(
                "Người đấu giá",
                "Người bán"
        );

        registerRole.setValue("Người đấu giá");

        sellerItemType.getItems().setAll("Điện tử", "Nghệ thuật", "Phương tiện");
        sellerItemType.setValue("Điện tử");

        // Cấu hình các cột trong bảng lịch sử đấu giá
        bidderColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        cell.getValue().bidderId()
                )
        );

        amountColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        CURRENCY_FORMAT.format(
                                cell.getValue().amount()
                        )
                )
        );

        timeColumn.setCellValueFactory(cell -> {
            if (cell.getValue().createdAt() == null) {
                return new ReadOnlyStringWrapper("");
            }

            return new ReadOnlyStringWrapper(
                    TIME_FORMATTER.format(
                            cell.getValue().createdAt()
                    )
            );
        });

        // Xử lý khi người dùng chọn một phiên đấu giá
        auctionList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {

                    if (updatingAuctionList) {
                        return;
                    }

                    if (selected == null) {
                        selectedAuctionId = null;
                        auctionIdField.clear();
                        bidHistoryTable.getItems().clear();
                        return;
                    }

                    String auctionId = selected.split(" \\| ", 2)[0];

                    selectedAuctionId = auctionId;
                    auctionIdField.setText(auctionId);

                    loadBidHistory(auctionId);
                });

        setVisible(registerView, false);
        setVisible(adminView, false);
        setVisible(sellerView, false);
        setVisible(bidderView, false);
        setVisible(authView, true);
    }

    @FXML
    private void showRegister() {
        setVisible(authView, false);
        setVisible(registerView, true);

        registerMessage.setText("");
    }

    @FXML
    private void showLogin() {
        setVisible(registerView, false);
        setVisible(authView, true);

        authMessage.setText("");
    }

    @FXML
    private void signIn() {

        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();
        String selectedRole = loginRole.getValue();

        if (username.isBlank() || password.isBlank()) {
            authMessage.setText(
                    "Vui lòng nhập tên đăng nhập và mật khẩu."
            );
            return;
        }

        if (selectedRole == null) {
            authMessage.setText("Vui lòng chọn vai trò.");
            return;
        }

        String expectedRole;

        if (selectedRole.startsWith("Admin")) {
            expectedRole = "ADMIN";
        } else if (selectedRole.equals("Người bán")) {
            expectedRole = "SELLER";
        } else {
            expectedRole = "BIDDER";
        }

        authMessage.setText("Đang kết nối server...");

        Thread worker = new Thread(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.LOGIN,
                                username,
                                password,
                                null,
                                null
                        )
                );

                Platform.runLater(() -> {

                    if (!response.isSuccess()) {
                        authMessage.setText(response.getMessage());
                        return;
                    }

                    String authenticatedRole = response.role();

                    if (!expectedRole.equals(authenticatedRole)) {
                        authMessage.setText(
                                "Tài khoản không thuộc vai trò đã chọn."
                        );
                        return;
                    }

                    currentUsername = username;
                    currentRole = authenticatedRole;

                    setVisible(authView, false);
                    setVisible(registerView, false);
                    setVisible(adminView, false);
                    setVisible(sellerView, false);
                    setVisible(bidderView, false);

                    switch (authenticatedRole) {

                        case "ADMIN" -> {
                            setVisible(adminView, true);

                            statusLabel.setText(
                                    "Đăng nhập thành công với quyền Admin."
                            );
                        }

                        case "SELLER" -> {
                            setVisible(sellerView, true);

                            statusLabel.setText(
                                    "Đăng nhập thành công với quyền Người bán."
                            );
                        }

                        case "BIDDER" -> {
                            setVisible(bidderView, true);

                            statusLabel.setText(
                                    "Đăng nhập thành công."
                            );

                            loadAuctions();
                            startHistoryAutoRefresh();
                        }

                        default -> {
                            currentUsername = null;
                            currentRole = null;

                            setVisible(authView, true);

                            authMessage.setText(
                                    "Vai trò trả về từ server không hợp lệ."
                            );
                        }
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        authMessage.setText(
                                "Không kết nối được server. "
                                        + "Hãy kiểm tra server đang chạy."
                        )
                );
            }

        }, "login-worker");

        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void register() {

        String username = registerUsername.getText().trim();
        String password = registerPassword.getText();
        String selectedRole = registerRole.getValue();

        if (username.isBlank() || password.isBlank()) {
            registerMessage.setText(
                    "Vui lòng nhập đủ tên đăng nhập và mật khẩu."
            );
            return;
        }

        if (selectedRole == null) {
            registerMessage.setText("Vui lòng chọn vai trò.");
            return;
        }

        String role = selectedRole.equals("Người bán")
                ? "SELLER"
                : "BIDDER";

        registerMessage.setText("Đang tạo tài khoản...");

        Thread worker = new Thread(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.REGISTER,
                                username,
                                password,
                                null,
                                null,
                                role
                        )
                );

                Platform.runLater(() -> {

                    registerMessage.setText(response.getMessage());

                    if (response.isSuccess()) {

                        loginUsername.setText(username);
                        loginPassword.clear();
                        registerPassword.clear();

                        showLogin();

                        authMessage.setText(
                                "Tạo tài khoản thành công. "
                                        + "Vui lòng đăng nhập."
                        );
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        registerMessage.setText(
                                "Không kết nối được server. Hãy thử lại sau."
                        )
                );
            }

        }, "register-worker");

        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void signOut() {

        // Dừng tự động cập nhật lịch sử
        stopHistoryAutoRefresh();

        currentUsername = null;
        currentRole = null;
        selectedAuctionId = null;

        auctionList.getSelectionModel().clearSelection();
        auctionList.getItems().clear();

        bidHistoryTable.getItems().clear();

        auctionIdField.clear();
        amountField.clear();

        setVisible(adminView, false);
        setVisible(sellerView, false);
        setVisible(bidderView, false);
        setVisible(registerView, false);
        setVisible(authView, true);

        loginPassword.clear();

        authMessage.setText("Bạn đã đăng xuất.");
        statusLabel.setText("");
    }

    @FXML
    private void createAuction() {
        if (!"SELLER".equals(currentRole) || currentUsername == null) {
            sellerMessage.setText("Vui lòng đăng nhập bằng tài khoản Người bán.");
            return;
        }

        String name = sellerItemName.getText().trim();
        String description = sellerItemDescription.getText().trim();
        LocalDate endDate = sellerEndDate.getValue();
        if (name.isBlank() || endDate == null) {
            sellerMessage.setText("Vui lòng nhập tên sản phẩm và ngày kết thúc.");
            return;
        }

        BigDecimal startingPrice;
        BigDecimal increment;
        try {
            startingPrice = new BigDecimal(sellerStartingPrice.getText().trim());
            increment = new BigDecimal(sellerIncrement.getText().trim());
            if (startingPrice.signum() < 0 || increment.signum() <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            sellerMessage.setText("Giá khởi điểm và bước giá không hợp lệ.");
            return;
        }

        ItemType type;
        String brand = sellerBrand.getText().trim();
        String artist = sellerArtist.getText().trim();
        int warranty;
        int year;
        int mileage;
        String plateNo;
        try {
            warranty = parseNonNegative(sellerWarranty.getText(), "Bảo hành");
            year = parseNonNegative(sellerYear.getText(), "Năm sáng tác");
            mileage = parseNonNegative(sellerMileage.getText(), "Số km");
            plateNo = String.valueOf(parseNonNegative(sellerPlateNo.getText(), "Biển số"));
            type = switch (sellerItemType.getValue()) {
                case "Nghệ thuật" -> ItemType.ART;
                case "Phương tiện" -> ItemType.VEHICLE;
                default -> ItemType.ELECTRONICS;
            };
            if (type == ItemType.ELECTRONICS && brand.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập thương hiệu.");
            }
            if (type == ItemType.ART && artist.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập tên nghệ sĩ/tác giả.");
            }
        } catch (IllegalArgumentException exception) {
            sellerMessage.setText(exception.getMessage());
            return;
        }

        Instant endAt = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
        CreateItemRequest itemRequest = new CreateItemRequest(
                name, description, type, brand, warranty, artist, year, mileage, plateNo);
        sellerMessage.setText("Đang tạo phiên đấu giá...");

        Thread worker = new Thread(() -> {
            try (AuctionSocketClient client = new AuctionSocketClient(SERVER_HOST, SERVER_PORT)) {
                Response response = client.send(new Request(
                        RequestType.CREATE_AUCTION, currentUsername, itemRequest,
                        startingPrice, increment, endAt));
                Platform.runLater(() -> {
                    sellerMessage.setText(response.getMessage());
                    if (response.isSuccess()) {
                        clearSellerForm();
                    }
                });
            } catch (Exception exception) {
                exception.printStackTrace();

                String message = exception.getMessage();

                if (message == null || message.isBlank()) {
                    message = exception.getClass().getSimpleName();
                }
                final String finalMessage = message;
                Platform.runLater(() ->
                        statusLabel.setText(
                                "Không thể tạo phiên: " + finalMessage
                        )
                );
            }
        }, "create-auction-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private int parseNonNegative(String text, String fieldName) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return 0;
        }
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " phải là số nguyên không âm.");
        }
    }

    private void clearSellerForm() {
        sellerItemName.clear();
        sellerItemDescription.clear();
        sellerBrand.clear();
        sellerWarranty.clear();
        sellerArtist.clear();
        sellerYear.clear();
        sellerMileage.clear();
        sellerPlateNo.clear();
        sellerStartingPrice.clear();
        sellerIncrement.clear();
        sellerEndDate.setValue(null);
        sellerItemType.setValue("Điện tử");
    }

    @FXML
    private void showPrototypeMessage() {

        statusLabel.setText(
                "Chức năng này cần API tương ứng ở backend."
        );
    }

    // =========================================================
    // TẢI DANH SÁCH PHIÊN ĐẤU GIÁ
    // =========================================================

    private void loadAuctions() {

        Thread worker = new Thread(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.LIST_AUCTIONS,
                                null,
                                null,
                                null,
                                null
                        )
                );

                Platform.runLater(() -> {

                    if (!response.isSuccess()) {
                        statusLabel.setText(response.getMessage());
                        return;
                    }

                    // Lưu lại phiên đang chọn trước khi tải lại
                    String previousSelectedId = selectedAuctionId;

                    updatingAuctionList = true;

                    try {

                        auctionList.getItems().clear();

                        for (Auction auction : response.auctions()) {

                            auctionList.getItems().add(
                                    auction.getId()
                                            + " | "
                                            + auction.getItem().getName()
                                            + " | "
                                            + auction.getStatus()
                                            + " | "
                                            + auction.getHighestPrice()
                            );
                        }

                        // Khôi phục lựa chọn cũ nếu phiên vẫn còn
                        if (previousSelectedId != null) {

                            for (int i = 0;
                                 i < auctionList.getItems().size();
                                 i++) {

                                String item = auctionList.getItems().get(i);

                                String id = item.split(" \\| ", 2)[0];

                                if (id.equals(previousSelectedId)) {
                                    auctionList.getSelectionModel()
                                            .select(i);
                                    break;
                                }
                            }
                        }

                    } finally {
                        updatingAuctionList = false;
                    }

                    if (auctionList.getItems().isEmpty()) {

                        selectedAuctionId = null;
                        auctionIdField.clear();
                        bidHistoryTable.getItems().clear();

                        statusLabel.setText(
                                "Hiện chưa có phiên đấu giá."
                        );

                    } else {

                        statusLabel.setText(
                                "Đã tải danh sách phiên đấu giá từ server."
                        );

                        // Nếu phiên đang chọn vẫn tồn tại,
                        // tiếp tục tải lịch sử của phiên đó
                        if (selectedAuctionId != null) {
                            loadBidHistory(selectedAuctionId);
                        }
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        statusLabel.setText(
                                "Không thể tải danh sách phiên đấu giá."
                        )
                );
            }

        }, "auction-list-loader");

        worker.setDaemon(true);
        worker.start();
    }

    // =========================================================
    // TẢI LỊCH SỬ ĐẶT GIÁ CỦA MỘT PHIÊN
    // =========================================================

    private void loadBidHistory(String auctionId) {

        if (auctionId == null || auctionId.isBlank()) {
            return;
        }

        // Không gửi thêm yêu cầu nếu yêu cầu trước chưa hoàn thành
        if (!historyLoading.compareAndSet(false, true)) {
            return;
        }

        Thread worker = new Thread(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.LIST_BIDS,
                                null,
                                null,
                                auctionId,
                                null
                        )
                );

                Platform.runLater(() -> {

                    // Bỏ qua kết quả nếu người dùng đã chọn phiên khác
                    if (!auctionId.equals(selectedAuctionId)) {
                        return;
                    }

                    if (!response.isSuccess()) {

                        bidHistoryTable.getItems().clear();
                        statusLabel.setText(response.getMessage());

                        return;
                    }

                    List<Bid> bids = response.bids();

                    bidHistoryTable.getItems().setAll(
                            bids == null ? List.of() : bids
                    );

                    if (bids == null || bids.isEmpty()) {

                        statusLabel.setText(
                                "Phiên đấu giá chưa có lượt đặt giá."
                        );

                    } else {

                        statusLabel.setText(
                                "Đã tải lịch sử đặt giá: "
                                        + bids.size()
                                        + " lượt."
                        );
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() -> {

                    if (auctionId.equals(selectedAuctionId)) {

                        statusLabel.setText(
                                "Không thể tải lịch sử đặt giá."
                        );
                    }
                });

            } finally {

                historyLoading.set(false);
            }

        }, "bid-history-loader");

        worker.setDaemon(true);
        worker.start();
    }

    // =========================================================
    // TỰ ĐỘNG CẬP NHẬT LỊCH SỬ MỖI 3 GIÂY
    // =========================================================

    private void startHistoryAutoRefresh() {

        stopHistoryAutoRefresh();

        historyRefreshTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(3),
                        event -> {

                            if (selectedAuctionId != null
                                    && "BIDDER".equals(currentRole)) {

                                loadBidHistory(selectedAuctionId);
                            }
                        }
                )
        );

        historyRefreshTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        historyRefreshTimeline.play();
    }

    private void stopHistoryAutoRefresh() {

        if (historyRefreshTimeline != null) {

            historyRefreshTimeline.stop();
            historyRefreshTimeline = null;
        }
    }

    // =========================================================
    // ĐẶT GIÁ
    // =========================================================

    @FXML
    private void placeBid() {

        if (!"BIDDER".equals(currentRole)
                || currentUsername == null) {

            statusLabel.setText(
                    "Vui lòng đăng nhập bằng tài khoản Người đấu giá."
            );

            return;
        }

        String auctionId = auctionIdField.getText().trim();
        String amountText = amountField.getText().trim();

        if (auctionId.isEmpty()) {

            statusLabel.setText(
                    "Vui lòng chọn hoặc nhập mã phiên."
            );

            return;
        }

        final BigDecimal amount;

        try {

            amount = new BigDecimal(amountText);

            if (amount.signum() <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {

            statusLabel.setText(
                    "Mức giá phải là số lớn hơn 0."
            );

            return;
        }

        // Lưu lại thông tin trước khi chạy worker thread
        String bidderUsername = currentUsername;

        statusLabel.setText(
                "Đang gửi mức giá đến server..."
        );

        Thread worker = new Thread(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.PLACE_BID,
                                bidderUsername,
                                null,
                                auctionId,
                                amount
                        )
                );

                Platform.runLater(() -> {

                    statusLabel.setText(response.getMessage());

                    if (response.isSuccess()) {

                        // Cập nhật lại danh sách phiên
                        loadAuctions();

                        // Cập nhật lịch sử đặt giá ngay lập tức
                        loadBidHistory(auctionId);
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        statusLabel.setText(
                                "Không thể gửi giá đến server."
                        )
                );
            }

        }, "place-bid-worker");

        worker.setDaemon(true);
        worker.start();
    }

    // =========================================================
    // HỖ TRỢ HIỂN THỊ GIAO DIỆN
    // =========================================================

    private void setVisible(Node node, boolean visible) {

        if (node != null) {

            node.setVisible(visible);
            node.setManaged(visible);
        }
    }
}
