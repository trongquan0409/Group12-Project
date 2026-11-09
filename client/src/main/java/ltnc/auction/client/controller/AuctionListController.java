package ltnc.auction.client.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import ltnc.auction.client.net.AuctionSocketClient;
import ltnc.auction.common.event.AuctionEvent;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.model.ItemType;
import ltnc.auction.common.protocol.CreateItemRequest;
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
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    private AuctionSocketClient socketClient;

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

    private String selectedAuctionId;

    private final AtomicBoolean auctionLoading =
            new AtomicBoolean(false);

    private final AtomicBoolean historyLoading =
            new AtomicBoolean(false);

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

        sellerItemType.getItems().setAll(
                "Điện tử",
                "Nghệ thuật",
                "Phương tiện"
        );

        sellerItemType.setValue("Điện tử");

        configureBidHistoryTable();

        configureAuctionSelection();

        setVisible(registerView, false);
        setVisible(adminView, false);
        setVisible(sellerView, false);
        setVisible(bidderView, false);
        setVisible(authView, true);
    }

    private void configureBidHistoryTable() {

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
    }

    private void configureAuctionSelection() {

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

                    String auctionId =
                            selected.split(" \\| ", 2)[0];

                    selectedAuctionId = auctionId;
                    auctionIdField.setText(auctionId);

                    loadBidHistory(auctionId);
                });
    }

    // =========================================================
    // KẾT NỐI REALTIME
    // =========================================================

    private boolean connectToServer() {

        if (socketClient != null) {
            return true;
        }

        try {

            socketClient = new AuctionSocketClient(
                    SERVER_HOST,
                    SERVER_PORT
            );

            socketClient.setEventListener(event -> {

                AuctionEvent auctionEvent =
                        event.getAuctionEvent();

                if (auctionEvent == null) {
                    return;
                }

                handleRealtimeAuctionEvent(
                        auctionEvent
                );
            });

            return true;

        } catch (IOException exception) {

            socketClient = null;

            Platform.runLater(() -> {

                if (statusLabel != null) {
                    statusLabel.setText(
                            "Không thể kết nối tới server."
                    );
                }
            });

            return false;
        }
    }

    private void handleRealtimeAuctionEvent(
            AuctionEvent auctionEvent
    ) {

        Platform.runLater(() -> {

            if (currentRole == null) {
                return;
            }

            loadAuctions();

            String eventAuctionId =
                    auctionEvent.auctionId();

            if (eventAuctionId != null
                    && eventAuctionId.equals(selectedAuctionId)) {

                loadBidHistory(eventAuctionId);
            }

            if (statusLabel != null
                    && auctionEvent.message() != null) {

                statusLabel.setText(
                        "Cập nhật realtime: "
                                + auctionEvent.message()
                );
            }
        });
    }

    private void disconnectFromServer() {

        if (socketClient == null) {
            return;
        }

        try {

            socketClient.close();

        } catch (IOException ignored) {

        } finally {

            socketClient = null;
        }
    }

    // =========================================================
    // ĐĂNG NHẬP / ĐĂNG KÝ
    // =========================================================

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

        String username =
                loginUsername.getText().trim();

        String password =
                loginPassword.getText();

        String selectedRole =
                loginRole.getValue();

        if (username.isBlank()
                || password.isBlank()) {

            authMessage.setText(
                    "Vui lòng nhập tên đăng nhập và mật khẩu."
            );

            return;
        }

        if (selectedRole == null) {

            authMessage.setText(
                    "Vui lòng chọn vai trò."
            );

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

        authMessage.setText(
                "Đang kết nối server..."
        );

        Thread.ofVirtual().start(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response =
                        client.send(
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

                        authMessage.setText(
                                response.getMessage()
                        );

                        return;
                    }

                    String authenticatedRole =
                            response.role();

                    if (!expectedRole.equals(
                            authenticatedRole
                    )) {

                        authMessage.setText(
                                "Tài khoản không thuộc "
                                        + "vai trò đã chọn."
                        );

                        return;
                    }

                    currentUsername = username;
                    currentRole = authenticatedRole;

                    if (!connectToServer()) {

                        currentUsername = null;
                        currentRole = null;

                        return;
                    }

                    setVisible(authView, false);
                    setVisible(registerView, false);
                    setVisible(adminView, false);
                    setVisible(sellerView, false);
                    setVisible(bidderView, false);

                    switch (authenticatedRole) {

                        case "ADMIN" -> {

                            setVisible(
                                    adminView,
                                    true
                            );

                            statusLabel.setText(
                                    "Đăng nhập thành công "
                                            + "với quyền Admin."
                            );
                        }

                        case "SELLER" -> {

                            setVisible(
                                    sellerView,
                                    true
                            );

                            statusLabel.setText(
                                    "Đăng nhập thành công "
                                            + "với quyền Người bán."
                            );
                        }

                        case "BIDDER" -> {

                            setVisible(
                                    bidderView,
                                    true
                            );

                            statusLabel.setText(
                                    "Đăng nhập thành công."
                            );

                            loadAuctions();
                        }

                        default -> {

                            disconnectFromServer();

                            currentUsername = null;
                            currentRole = null;

                            setVisible(
                                    authView,
                                    true
                            );

                            authMessage.setText(
                                    "Vai trò trả về từ server "
                                            + "không hợp lệ."
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
        });
    }

    @FXML
    private void register() {

        String username =
                registerUsername.getText().trim();

        String password =
                registerPassword.getText();

        String selectedRole =
                registerRole.getValue();

        if (username.isBlank()
                || password.isBlank()) {

            registerMessage.setText(
                    "Vui lòng nhập đủ tên đăng nhập và mật khẩu."
            );

            return;
        }

        if (selectedRole == null) {

            registerMessage.setText(
                    "Vui lòng chọn vai trò."
            );

            return;
        }

        String role =
                selectedRole.equals("Người bán")
                        ? "SELLER"
                        : "BIDDER";

        registerMessage.setText(
                "Đang tạo tài khoản..."
        );

        Thread.ofVirtual().start(() -> {

            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response =
                        client.send(
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

                    registerMessage.setText(
                            response.getMessage()
                    );

                    if (response.isSuccess()) {

                        loginUsername.setText(
                                username
                        );

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
                                "Không kết nối được server. "
                                        + "Hãy thử lại sau."
                        )
                );
            }
        });
    }

    @FXML
    private void signOut() {

        disconnectFromServer();

        currentUsername = null;
        currentRole = null;
        selectedAuctionId = null;

        auctionLoading.set(false);
        historyLoading.set(false);

        auctionList.getSelectionModel()
                .clearSelection();

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

        authMessage.setText(
                "Bạn đã đăng xuất."
        );

        statusLabel.setText("");
    }

    // =========================================================
    // TẠO PHIÊN ĐẤU GIÁ
    // =========================================================

    @FXML
    private void createAuction() {

        if (!"SELLER".equals(currentRole)
                || currentUsername == null) {

            sellerMessage.setText(
                    "Vui lòng đăng nhập bằng "
                            + "tài khoản Người bán."
            );

            return;
        }

        if (socketClient == null
                && !connectToServer()) {

            sellerMessage.setText(
                    "Không thể kết nối server."
            );

            return;
        }

        String name =
                sellerItemName.getText().trim();

        String description =
                sellerItemDescription.getText().trim();

        LocalDate endDate =
                sellerEndDate.getValue();

        if (name.isBlank()
                || endDate == null) {

            sellerMessage.setText(
                    "Vui lòng nhập tên sản phẩm "
                            + "và ngày kết thúc."
            );

            return;
        }

        BigDecimal startingPrice;
        BigDecimal increment;

        try {

            startingPrice =
                    new BigDecimal(
                            sellerStartingPrice.getText()
                                    .trim()
                    );

            increment =
                    new BigDecimal(
                            sellerIncrement.getText()
                                    .trim()
                    );

            if (startingPrice.signum() < 0
                    || increment.signum() <= 0) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {

            sellerMessage.setText(
                    "Giá khởi điểm và bước giá "
                            + "không hợp lệ."
            );

            return;
        }

        ItemType type;

        String brand =
                sellerBrand.getText().trim();

        String artist =
                sellerArtist.getText().trim();

        int warranty;
        int year;
        int mileage;
        String plateNo;

        try {

            warranty =
                    parseNonNegative(
                            sellerWarranty.getText(),
                            "Bảo hành"
                    );

            year =
                    parseNonNegative(
                            sellerYear.getText(),
                            "Năm sáng tác"
                    );

            mileage =
                    parseNonNegative(
                            sellerMileage.getText(),
                            "Số km"
                    );

            plateNo =
                    String.valueOf(
                            parseNonNegative(
                                    sellerPlateNo.getText(),
                                    "Biển số"
                            )
                    );

            type = switch (
                    sellerItemType.getValue()
                    ) {

                case "Nghệ thuật" ->
                        ItemType.ART;

                case "Phương tiện" ->
                        ItemType.VEHICLE;

                default ->
                        ItemType.ELECTRONICS;
            };

            if (type == ItemType.ELECTRONICS
                    && brand.isBlank()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập thương hiệu."
                );
            }

            if (type == ItemType.ART
                    && artist.isBlank()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập tên "
                                + "nghệ sĩ/tác giả."
                );
            }

        } catch (IllegalArgumentException exception) {

            sellerMessage.setText(
                    exception.getMessage()
            );

            return;
        }

        Instant endAt =
                endDate.plusDays(1)
                        .atStartOfDay(
                                ZoneId.systemDefault()
                        )
                        .toInstant()
                        .minusSeconds(1);

        CreateItemRequest itemRequest =
                new CreateItemRequest(
                        name,
                        description,
                        type,
                        brand,
                        warranty,
                        artist,
                        year,
                        mileage,
                        plateNo
                );

        String username = currentUsername;

        sellerMessage.setText(
                "Đang tạo phiên đấu giá..."
        );

        Thread.ofVirtual().start(() -> {

            try {

                Response response =
                        socketClient.send(
                                new Request(
                                        RequestType.CREATE_AUCTION,
                                        username,
                                        itemRequest,
                                        startingPrice,
                                        increment,
                                        endAt
                                )
                        );

                Platform.runLater(() -> {

                    sellerMessage.setText(
                            response.getMessage()
                    );

                    if (response.isSuccess()) {

                        clearSellerForm();
                    }
                });

            } catch (Exception exception) {

                String message =
                        exception.getMessage();

                if (message == null
                        || message.isBlank()) {

                    message =
                            exception.getClass()
                                    .getSimpleName();
                }

                String finalMessage = message;

                Platform.runLater(() ->
                        sellerMessage.setText(
                                "Không thể tạo phiên: "
                                        + finalMessage
                        )
                );
            }
        });
    }

    private int parseNonNegative(
            String text,
            String fieldName
    ) {

        String value =
                text == null
                        ? ""
                        : text.trim();

        if (value.isEmpty()) {
            return 0;
        }

        try {

            int number =
                    Integer.parseInt(value);

            if (number < 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    fieldName
                            + " phải là số nguyên không âm."
            );
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

        sellerItemType.setValue(
                "Điện tử"
        );
    }

    @FXML
    private void showPrototypeMessage() {

        statusLabel.setText(
                "Chức năng này cần API tương ứng "
                        + "ở backend."
        );
    }

    // =========================================================
    // TẢI DANH SÁCH PHIÊN ĐẤU GIÁ
    // =========================================================

    private void loadAuctions() {

        if (socketClient == null) {
            return;
        }

        if (!auctionLoading.compareAndSet(
                false,
                true
        )) {
            return;
        }

        Thread.ofVirtual().start(() -> {

            try {

                Response response =
                        socketClient.send(
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

                        statusLabel.setText(
                                response.getMessage()
                        );

                        return;
                    }

                    updateAuctionList(response);

                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        statusLabel.setText(
                                "Không thể tải danh sách "
                                        + "phiên đấu giá."
                        )
                );

            } finally {

                auctionLoading.set(false);
            }
        });
    }

    private void updateAuctionList(
            Response response
    ) {

        String previousSelectedId =
                selectedAuctionId;

        updatingAuctionList = true;

        try {

            auctionList.getItems().clear();

            List<Auction> auctions =
                    response.auctions();

            if (auctions != null) {

                for (Auction auction : auctions) {

                    auctionList.getItems().add(
                            auction.getId()
                                    + " | "
                                    + auction.getItem()
                                    .getName()
                                    + " | "
                                    + auction.getStatus()
                                    + " | "
                                    + auction.getHighestPrice()
                    );
                }
            }

            if (previousSelectedId != null) {

                for (
                        int i = 0;
                        i < auctionList.getItems().size();
                        i++
                ) {

                    String item =
                            auctionList.getItems().get(i);

                    String id =
                            item.split(
                                    " \\| ",
                                    2
                            )[0];

                    if (id.equals(
                            previousSelectedId
                    )) {

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
                    "Đã tải danh sách phiên "
                            + "đấu giá từ server."
            );
        }
    }

    // =========================================================
    // TẢI LỊCH SỬ ĐẶT GIÁ
    // =========================================================

    private void loadBidHistory(
            String auctionId
    ) {

        if (auctionId == null
                || auctionId.isBlank()
                || socketClient == null) {

            return;
        }

        if (!historyLoading.compareAndSet(
                false,
                true
        )) {

            return;
        }

        Thread.ofVirtual().start(() -> {

            try {

                Response response =
                        socketClient.send(
                                new Request(
                                        RequestType.LIST_BIDS,
                                        null,
                                        null,
                                        auctionId,
                                        null
                                )
                        );

                Platform.runLater(() -> {

                    if (!auctionId.equals(
                            selectedAuctionId
                    )) {

                        return;
                    }

                    if (!response.isSuccess()) {

                        bidHistoryTable.getItems().clear();

                        statusLabel.setText(
                                response.getMessage()
                        );

                        return;
                    }

                    List<Bid> bids =
                            response.bids();

                    bidHistoryTable.getItems().setAll(
                            bids == null
                                    ? List.of()
                                    : bids
                    );
                });

            } catch (Exception exception) {

                Platform.runLater(() -> {

                    if (auctionId.equals(
                            selectedAuctionId
                    )) {

                        statusLabel.setText(
                                "Không thể tải lịch sử "
                                        + "đặt giá."
                        );
                    }
                });

            } finally {

                historyLoading.set(false);
            }
        });
    }

    // =========================================================
    // ĐẶT GIÁ
    // =========================================================

    @FXML
    private void placeBid() {

        if (!"BIDDER".equals(currentRole)
                || currentUsername == null) {

            statusLabel.setText(
                    "Vui lòng đăng nhập bằng "
                            + "tài khoản Người đấu giá."
            );

            return;
        }

        if (socketClient == null
                && !connectToServer()) {

            statusLabel.setText(
                    "Không thể kết nối server."
            );

            return;
        }

        String auctionId =
                auctionIdField.getText().trim();

        String amountText =
                amountField.getText().trim();

        if (auctionId.isEmpty()) {

            statusLabel.setText(
                    "Vui lòng chọn hoặc nhập mã phiên."
            );

            return;
        }

        BigDecimal amount;

        try {

            amount =
                    new BigDecimal(amountText);

            if (amount.signum() <= 0) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {

            statusLabel.setText(
                    "Mức giá phải là số lớn hơn 0."
            );

            return;
        }

        String bidderUsername =
                currentUsername;

        statusLabel.setText(
                "Đang gửi mức giá đến server..."
        );

        Thread.ofVirtual().start(() -> {

            try {

                Response response =
                        socketClient.send(
                                new Request(
                                        RequestType.PLACE_BID,
                                        bidderUsername,
                                        null,
                                        auctionId,
                                        amount
                                )
                        );

                Platform.runLater(() -> {

                    statusLabel.setText(
                            response.getMessage()
                    );

                    if (response.isSuccess()) {

                        amountField.clear();

                        loadAuctions();

                        loadBidHistory(
                                auctionId
                        );
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() ->
                        statusLabel.setText(
                                "Không thể gửi giá đến server."
                        )
                );
            }
        });
    }

    // =========================================================
    // HỖ TRỢ HIỂN THỊ
    // =========================================================

    private void setVisible(
            Node node,
            boolean visible
    ) {

        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }
}