
package ltnc.auction.client.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import ltnc.auction.client.net.AuctionSocketClient;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.AuctionStatus;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.ItemType;
import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.RequestType;
import ltnc.auction.common.protocol.Response;

public class AuctionListController {

    private static final Logger LOGGER = Logger.getLogger(AuctionListController.class.getName());
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5050;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

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

    // Nhóm thuộc tính riêng theo từng loại sản phẩm
    @FXML
    private VBox electronicsFields;

    @FXML
    private VBox artFields;

    @FXML
    private VBox vehicleFields;

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

    // Menu bên trái và các trang của khu vực người bán
    @FXML
    private Button sellerNavProducts;

    @FXML
    private Button sellerNavCreate;

    @FXML
    private Button sellerNavHistory;

    @FXML
    private Node sellerProductsPage;

    @FXML
    private Node sellerCreatePage;

    @FXML
    private Node sellerHistoryPage;

    // Trang "Sản phẩm của tôi"
    @FXML
    private TableView<Auction> sellerProductTable;

    @FXML
    private TableColumn<Auction, String> productNameColumn;

    @FXML
    private TableColumn<Auction, String> productTypeColumn;

    @FXML
    private TableColumn<Auction, String> productStartColumn;

    @FXML
    private TableColumn<Auction, String> productPriceColumn;

    @FXML
    private TableColumn<Auction, String> productStatusColumn;

    @FXML
    private TableColumn<Auction, String> productEndColumn;

    @FXML
    private Label sellerProductsMessage;

    // Chi tiết sản phẩm trong trang "Sản phẩm của tôi"
    @FXML
    private Node sellerDetailPlaceholder;

    @FXML
    private Node sellerDetailContent;

    @FXML
    private Label detailItemName;

    @FXML
    private Label detailItemCategory;

    @FXML
    private Label detailDescription;

    @FXML
    private Label detailExtraInfo;

    @FXML
    private Label detailStartingPrice;

    @FXML
    private Label detailIncrement;

    @FXML
    private Label detailCurrentPrice;

    @FXML
    private Label detailStatus;

    @FXML
    private Label detailCreatedAt;

    @FXML
    private Label detailEndAt;

    @FXML
    private Label detailRemainingTime;

    private Timeline sellerCountdownTimeline;

    // Trang "Kết quả & lịch sử"
    @FXML
    private TableView<Auction> sellerResultTable;

    @FXML
    private TableColumn<Auction, String> resultNameColumn;

    @FXML
    private TableColumn<Auction, String> resultPriceColumn;

    @FXML
    private TableColumn<Auction, String> resultWinnerColumn;

    @FXML
    private TableColumn<Auction, String> resultStatusColumn;

    @FXML
    private TableView<Bid> sellerBidTable;

    @FXML
    private TableColumn<Bid, String> sellerBidderColumn;

    @FXML
    private TableColumn<Bid, String> sellerBidAmountColumn;

    @FXML
    private TableColumn<Bid, String> sellerBidTimeColumn;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private PasswordField registerPassword;

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

        registerRole.getItems().setAll(
                "Người đấu giá",
                "Người bán"
        );

        registerRole.setValue("Người đấu giá");

        sellerItemType.getItems().setAll("Điện tử", "Nghệ thuật", "Phương tiện");
        // Chỉ hiển thị các thuộc tính của loại sản phẩm đang được chọn
        sellerItemType.valueProperty().addListener(
                (_, _, newValue) -> showFieldsForType(toItemType(newValue)));
        sellerItemType.setValue("Điện tử");
        showFieldsForType(ItemType.ELECTRONICS);

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
                .addListener((_, _, selected) -> {

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

        configureSellerTables();

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

        if (username.isBlank() || password.isBlank()) {
            authMessage.setText(
                    "Vui lòng nhập tên đăng nhập và mật khẩu."
            );
            return;
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
                            showSellerProducts();
                            startSellerCountdown();

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
        stopSellerCountdown();

        currentUsername = null;
        currentRole = null;
        selectedAuctionId = null;

        auctionList.getSelectionModel().clearSelection();
        auctionList.getItems().clear();

        bidHistoryTable.getItems().clear();

        auctionIdField.clear();
        amountField.clear();

        sellerProductTable.getItems().clear();
        sellerResultTable.getItems().clear();
        sellerBidTable.getItems().clear();
        sellerProductsMessage.setText("");
        sellerMessage.setText("");
        showProductDetail(null);

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

        ItemType type = toItemType(sellerItemType.getValue());
        CreateItemRequest itemRequest;
        try {
            // Chỉ đọc và kiểm tra các trường thuộc loại sản phẩm đang chọn,
            // các trường của loại khác được gửi là null.
            itemRequest = switch (type) {
                case ELECTRONICS -> {
                    String brand = requireText(sellerBrand.getText(), "Vui lòng nhập thương hiệu.");
                    int warranty = parseNonNegative(sellerWarranty.getText(), "Bảo hành");
                    yield new CreateItemRequest(name, description, type,
                            brand, warranty, null, null, null, null);
                }
                case ART -> {
                    String artist = requireText(sellerArtist.getText(), "Vui lòng nhập tên nghệ sĩ/tác giả.");
                    requireText(sellerYear.getText(), "Vui lòng nhập năm sáng tác.");
                    int year = parseNonNegative(sellerYear.getText(), "Năm sáng tác");
                    yield new CreateItemRequest(name, description, type,
                            null, null, artist, year, null, null);
                }
                case VEHICLE -> {
                    int mileage = parseNonNegative(sellerMileage.getText(), "Số km");
                    String plateNo = requireText(sellerPlateNo.getText(), "Vui lòng nhập biển số xe.");
                    yield new CreateItemRequest(name, description, type,
                            null, null, null, null, mileage, plateNo);
                }
            };
        } catch (IllegalArgumentException exception) {
            sellerMessage.setText(exception.getMessage());
            return;
        }

        Instant endAt = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
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
                        loadSellerAuctions();
                    }
                });
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Failed to create auction.", exception);

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

    private String requireText(String text, String errorMessage) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private ItemType toItemType(String label) {
        if (label == null) {
            return ItemType.ELECTRONICS;
        }
        return switch (label) {
            case "Nghệ thuật" -> ItemType.ART;
            case "Phương tiện" -> ItemType.VEHICLE;
            default -> ItemType.ELECTRONICS;
        };
    }

    // Hiện nhóm thuộc tính của loại được chọn, ẩn các nhóm còn lại
    private void showFieldsForType(ItemType type) {
        setVisible(electronicsFields, type == ItemType.ELECTRONICS);
        setVisible(artFields, type == ItemType.ART);
        setVisible(vehicleFields, type == ItemType.VEHICLE);
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

    // =========================================================
    // KHU VỰC NGƯỜI BÁN: ĐIỀU HƯỚNG MENU VÀ DỮ LIỆU
    // =========================================================

    @FXML
    private void showSellerProducts() {
        showSellerPage(sellerProductsPage, sellerNavProducts);
        loadSellerAuctions();
    }

    @FXML
    private void showSellerCreate() {
        showSellerPage(sellerCreatePage, sellerNavCreate);
    }

    @FXML
    private void showSellerHistory() {
        showSellerPage(sellerHistoryPage, sellerNavHistory);
        loadSellerAuctions();
    }

    // Hiện một trang, ẩn các trang còn lại và đánh dấu mục menu đang chọn
    private void showSellerPage(Node page, Button navButton) {
        setVisible(sellerProductsPage, page == sellerProductsPage);
        setVisible(sellerCreatePage, page == sellerCreatePage);
        setVisible(sellerHistoryPage, page == sellerHistoryPage);

        for (Button button : List.of(sellerNavProducts, sellerNavCreate, sellerNavHistory)) {
            button.getStyleClass().remove("nav-item-active");
        }
        navButton.getStyleClass().add("nav-item-active");
    }

    private void configureSellerTables() {
        productNameColumn.setCellValueFactory(cell -> text(cell.getValue().getItem().getName()));
        productTypeColumn.setCellValueFactory(cell ->
                text(typeLabel(cell.getValue().getItem().getCategory())));
        productStartColumn.setCellValueFactory(cell -> text(money(cell.getValue().getStartingPrice())));
        productPriceColumn.setCellValueFactory(cell -> text(money(cell.getValue().getHighestPrice())));
        productStatusColumn.setCellValueFactory(cell -> text(statusText(cell.getValue().getStatus())));
        productEndColumn.setCellValueFactory(cell -> text(time(cell.getValue().getEndAt())));

        resultNameColumn.setCellValueFactory(cell -> text(cell.getValue().getItem().getName()));
        resultPriceColumn.setCellValueFactory(cell -> text(money(cell.getValue().getHighestPrice())));
        resultWinnerColumn.setCellValueFactory(cell -> {
            String bidder = cell.getValue().getHighestBidderId();
            return text(bidder == null ? "Chưa có lượt đặt" : bidder);
        });
        resultStatusColumn.setCellValueFactory(cell -> text(statusText(cell.getValue().getStatus())));

        // Chọn sản phẩm ở trang "Sản phẩm của tôi" để xem chi tiết
        sellerProductTable.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selected) -> showProductDetail(selected));

        sellerBidderColumn.setCellValueFactory(cell -> text(cell.getValue().bidderId()));
        sellerBidAmountColumn.setCellValueFactory(cell -> text(money(cell.getValue().amount())));
        sellerBidTimeColumn.setCellValueFactory(cell -> text(time(cell.getValue().createdAt())));

        // Chọn một phiên ở trang kết quả để xem lịch sử đặt giá của phiên đó
        sellerResultTable.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selected) ->
                        sellerBidTable.getItems().setAll(
                                selected == null ? List.of() : selected.getBids()));
    }

    @FXML
    private void loadSellerAuctions() {
        if (!"SELLER".equals(currentRole) || currentUsername == null) {
            return;
        }
        String sellerUsername = currentUsername;
        sellerProductsMessage.setText("Đang tải danh sách sản phẩm...");

        Thread worker = new Thread(() -> {
            try (AuctionSocketClient client = new AuctionSocketClient(SERVER_HOST, SERVER_PORT)) {
                Response response = client.send(new Request(
                        RequestType.LIST_SELLER_AUCTIONS, sellerUsername, null, null, null));
                Platform.runLater(() -> {
                    if (!response.isSuccess()) {
                        sellerProductsMessage.setText(response.getMessage());
                        return;
                    }
                    List<Auction> auctions = response.auctions().stream()
                            .sorted(Comparator.comparing(Auction::getStartAt,
                                    Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();

                    String selectedProductId = sellerProductTable.getSelectionModel().getSelectedItem() == null
                            ? null
                            : sellerProductTable.getSelectionModel().getSelectedItem().getId();
                    String selectedResultId = sellerResultTable.getSelectionModel().getSelectedItem() == null
                            ? null
                            : sellerResultTable.getSelectionModel().getSelectedItem().getId();

                    sellerProductTable.getItems().setAll(auctions);
                    sellerResultTable.getItems().setAll(auctions);

                    if (selectedProductId != null) {
                        auctions.stream()
                                .filter(auction -> auction.getId().equals(selectedProductId))
                                .findFirst()
                                .ifPresentOrElse(
                                        auction -> sellerProductTable.getSelectionModel().select(auction),
                                        () -> showProductDetail(null)
                                );
                    } else {
                        showProductDetail(null);
                    }

                    // Giữ lại phiên đang chọn ở trang kết quả sau khi làm mới
                    if (selectedResultId != null) {
                        auctions.stream()
                                .filter(auction -> auction.getId().equals(selectedResultId))
                                .findFirst()
                                .ifPresent(auction -> sellerResultTable.getSelectionModel().select(auction));
                    }

                    sellerProductsMessage.setText(auctions.isEmpty()
                            ? "Bạn chưa đăng sản phẩm nào."
                            : "Đã tải " + auctions.size() + " sản phẩm.");
                });
            } catch (Exception exception) {
                Platform.runLater(() ->
                        sellerProductsMessage.setText("Không thể tải danh sách sản phẩm từ server."));
            }
        }, "seller-auctions-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private static ReadOnlyStringWrapper text(String value) {
        return new ReadOnlyStringWrapper(value == null ? "" : value);
    }

    private static String money(BigDecimal value) {
        return value == null ? "" : CURRENCY_FORMAT.format(value);
    }

    private static String time(Instant value) {
        return value == null ? "" : TIME_FORMATTER.format(value);
    }

    private static String typeLabel(ItemType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case ELECTRONICS -> "Điện tử";
            case ART -> "Nghệ thuật";
            case VEHICLE -> "Phương tiện";
        };
    }

    private static String statusText(AuctionStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case OPEN -> "Đang mở";
            case RUNNING -> "Đang diễn ra";
            case FINISHED -> "Đã kết thúc";
            case PAID -> "Đã thanh toán";
            case CANCELED -> "Đã hủy";
        };
    }

    private void showProductDetail(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            setVisible(sellerDetailPlaceholder, true);
            setVisible(sellerDetailContent, false);
            return;
        }

        setVisible(sellerDetailPlaceholder, false);
        setVisible(sellerDetailContent, true);

        detailItemName.setText(auction.getItem().getName());
        detailItemCategory.setText(typeLabel(auction.getItem().getCategory()));
        String desc = auction.getItem().getDescription();
        detailDescription.setText(desc == null || desc.isBlank() ? "(Không có mô tả)" : desc);
        detailExtraInfo.setText(auction.getItem().getInfo());

        detailStartingPrice.setText(money(auction.getStartingPrice()) + " VNĐ");
        detailIncrement.setText(money(auction.getIncrement()) + " VNĐ");
        detailCurrentPrice.setText(money(auction.getHighestPrice()) + " VNĐ");
        detailStatus.setText(statusText(auction.getStatus()));
        detailCreatedAt.setText(time(auction.getStartAt()));
        detailEndAt.setText(time(auction.getEndAt()));
        detailRemainingTime.setText(formatRemainingTime(auction.getEndAt(), auction.getStatus()));
    }

    private static String formatRemainingTime(Instant endAt, AuctionStatus status) {
        if (status == AuctionStatus.FINISHED || status == AuctionStatus.PAID) {
            return "Đã kết thúc";
        }
        if (status == AuctionStatus.CANCELED) {
            return "Đã hủy";
        }
        if (endAt == null) {
            return "—";
        }
        Instant now = Instant.now();
        if (now.isAfter(endAt)) {
            return "Hết thời gian (chờ hoàn tất)";
        }
        java.time.Duration duration = java.time.Duration.between(now, endAt);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%d ngày %d giờ %d phút", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d giờ %d phút %d giây", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d phút %d giây", minutes, seconds);
        } else {
            return String.format("%d giây", Math.max(0, seconds));
        }
    }

    private void startSellerCountdown() {
        stopSellerCountdown();
        sellerCountdownTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        _ -> {
                            Auction selected = sellerProductTable.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                detailRemainingTime.setText(
                                        formatRemainingTime(selected.getEndAt(), selected.getStatus()));
                            }
                        }
                )
        );
        sellerCountdownTimeline.setCycleCount(Timeline.INDEFINITE);
        sellerCountdownTimeline.play();
    }

    private void stopSellerCountdown() {
        if (sellerCountdownTimeline != null) {
            sellerCountdownTimeline.stop();
            sellerCountdownTimeline = null;
        }
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
                        _ -> {

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
