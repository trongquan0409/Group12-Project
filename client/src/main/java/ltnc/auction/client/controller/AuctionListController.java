
package ltnc.auction.client.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Collator;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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

    private static final ThreadLocal<Collator> VIETNAMESE_COLLATOR = ThreadLocal.withInitial(() -> {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        collator.setStrength(Collator.SECONDARY);
        return collator;
    });

    static final String FILTER_ALL = "Tất cả";
    static final String SORT_DEFAULT = "Mặc định (Mới nhất)";
    static final String SORT_NAME_ASC = "Tên: A → Z (Có số trước)";
    static final String SORT_NAME_DESC = "Tên: Z → A (Ngược lại)";
    static final String SORT_PRICE_ASC = "Giá: Tăng dần";
    static final String SORT_PRICE_DESC = "Giá: Giảm dần";
    static final long DEFAULT_SLIDER_MAX_PRICE = 100_000_000L;

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
    private ComboBox<String> sellerEndHour;

    @FXML
    private ComboBox<String> sellerEndMinute;

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
    private TextField bidderSearchField;

    @FXML
    private ComboBox<String> bidderTypeFilter;

    @FXML
    private ComboBox<String> bidderStatusFilter;

    @FXML
    private MenuButton bidderPriceMenuButton;

    @FXML
    private TextField bidderMinPriceField;

    @FXML
    private TextField bidderMaxPriceField;

    @FXML
    private Pane bidderPriceRangeHost;

    @FXML
    private ComboBox<String> bidderSortFilter;

    @FXML
    private TableView<Auction> bidderAuctionTable;

    @FXML
    private TableColumn<Auction, String> bidderNameColumn;

    @FXML
    private TableColumn<Auction, String> bidderTypeColumn;

    @FXML
    private TableColumn<Auction, String> bidderPriceColumn;

    @FXML
    private TableColumn<Auction, String> bidderStatusColumn;

    @FXML
    private TableColumn<Auction, String> bidderRemainingColumn;

    // Chi tiết sản phẩm trong khu vực người đấu giá
    @FXML
    private Node bidderDetailPlaceholder;

    @FXML
    private Node bidderDetailContent;

    @FXML
    private Label bidderDetailItemName;

    @FXML
    private Label bidderDetailItemCategory;

    @FXML
    private Label bidderDetailDescription;

    @FXML
    private Label bidderDetailExtraInfo;

    @FXML
    private Label bidderDetailStartingPrice;

    @FXML
    private Label bidderDetailIncrement;

    @FXML
    private Label bidderDetailCurrentPrice;

    @FXML
    private Label bidderDetailLeader;

    @FXML
    private Label bidderDetailStatus;

    @FXML
    private Label bidderDetailCreatedAt;

    @FXML
    private Label bidderDetailEndAt;

    @FXML
    private Label bidderDetailRemainingTime;

    private final List<Auction> allBidderAuctions = new ArrayList<>();
    private Timeline bidderCountdownTimeline;
    private final PriceRangeSlider bidderPriceRangeSlider = new PriceRangeSlider();
    private long bidderSliderMaxBound = DEFAULT_SLIDER_MAX_PRICE;
    private boolean syncingPriceControls;

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

        configureEndTimeSelectors();

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

        configureBidderView();
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
                            startBidderCountdown();
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
        stopBidderCountdown();

        currentUsername = null;
        currentRole = null;
        selectedAuctionId = null;

        updatingAuctionList = true;
        syncingPriceControls = true;
        try {
            allBidderAuctions.clear();
            bidderAuctionTable.getSelectionModel().clearSelection();
            bidderAuctionTable.getItems().clear();
            bidderSearchField.clear();
            bidderTypeFilter.setValue(FILTER_ALL);
            bidderStatusFilter.setValue(FILTER_ALL);
            bidderMinPriceField.clear();
            bidderMaxPriceField.clear();
            updateSliderMaxBound(DEFAULT_SLIDER_MAX_PRICE, true);
            bidderPriceRangeSlider.setLowValue(0);
            bidderPriceRangeSlider.setHighValue(bidderSliderMaxBound);
            bidderPriceMenuButton.setText(FILTER_ALL);
            bidderSortFilter.setValue(SORT_DEFAULT);
            bidderAuctionTable.getSortOrder().clear();
        } finally {
            updatingAuctionList = false;
            syncingPriceControls = false;
        }
        showBidderDetail(null);

        bidHistoryTable.getItems().clear();

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
        LocalDate endDate;
        try {
            endDate = readEndDate();
        } catch (IllegalArgumentException exception) {
            sellerMessage.setText(exception.getMessage());
            return;
        }
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

        Instant endAt;
        try {
            endAt = parseEndAt(
                    endDate,
                    readComboText(sellerEndHour),
                    readComboText(sellerEndMinute),
                    ZoneId.systemDefault()
            );
        } catch (IllegalArgumentException exception) {
            sellerMessage.setText(exception.getMessage());
            return;
        }
        if (!endAt.isAfter(Instant.now())) {
            sellerMessage.setText("Thời điểm kết thúc phải ở tương lai.");
            return;
        }
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
        if (sellerEndDate.getEditor() != null) {
            sellerEndDate.getEditor().clear();
        }
        resetEndTimeSelectors();
        sellerItemType.setValue("Điện tử");
    }

    private void configureEndTimeSelectors() {
        configureTimeComboBox(sellerEndHour, 23, "23");
        configureTimeComboBox(sellerEndMinute, 59, "59");
    }

    private void configureTimeComboBox(
            ComboBox<String> comboBox,
            int max,
            String defaultValue
    ) {
        comboBox.getItems().clear();
        for (int i = 0; i <= max; i++) {
            comboBox.getItems().add(String.format("%02d", i));
        }
        comboBox.setValue(defaultValue);
        if (comboBox.getEditor() != null) {
            comboBox.getEditor().setText(defaultValue);
            comboBox.getEditor().focusedProperty().addListener((_, _, focused) -> {
                if (!focused) {
                    String raw = comboBox.getEditor().getText();
                    if (raw != null && !raw.isBlank()) {
                        try {
                            int parsed = Integer.parseInt(raw.trim());
                            if (parsed >= 0 && parsed <= max) {
                                String formatted = String.format("%02d", parsed);
                                comboBox.setValue(formatted);
                                comboBox.getEditor().setText(formatted);
                            }
                        } catch (NumberFormatException ignored) {
                            // Kiểm tra và báo lỗi cụ thể khi nhấn Tạo phiên
                        }
                    }
                }
            });
        }
    }

    private void resetEndTimeSelectors() {
        sellerEndHour.setValue("23");
        if (sellerEndHour.getEditor() != null) {
            sellerEndHour.getEditor().setText("23");
        }
        sellerEndMinute.setValue("59");
        if (sellerEndMinute.getEditor() != null) {
            sellerEndMinute.getEditor().setText("59");
        }
    }

    private LocalDate readEndDate() {
        if (sellerEndDate.getEditor() == null) {
            return sellerEndDate.getValue();
        }
        String editorText = sellerEndDate.getEditor().getText().trim();
        if (editorText.isEmpty()) {
            sellerEndDate.setValue(null);
            return null;
        }
        try {
            LocalDate parsed = sellerEndDate.getConverter().fromString(editorText);
            if (parsed != null) {
                sellerEndDate.setValue(parsed);
                return parsed;
            }
            return sellerEndDate.getValue();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Ngày kết thúc không hợp lệ.");
        }
    }

    private static String readComboText(ComboBox<String> comboBox) {
        if (comboBox.isEditable() && comboBox.getEditor() != null) {
            return comboBox.getEditor().getText();
        }
        return comboBox.getValue();
    }

    static Instant parseEndAt(
            LocalDate endDate,
            String hourText,
            String minuteText,
            ZoneId zoneId
    ) {
        if (endDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày kết thúc.");
        }
        int hour = parseTimePart(hourText, 23, "Giờ kết thúc");
        int minute = parseTimePart(minuteText, 59, "Phút kết thúc");
        LocalTime endTime = LocalTime.of(hour, minute);
        return LocalDateTime.of(endDate, endTime)
                .atZone(zoneId)
                .toInstant();
    }

    private static int parseTimePart(String text, int max, String fieldLabel) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn " + fieldLabel.toLowerCase(Locale.ROOT) + "."
            );
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0 || parsed > max) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    String.format("%s phải là số từ 00 đến %02d.", fieldLabel, max)
            );
        }
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

    private void configureBidderView() {
        bidderNameColumn.setCellValueFactory(cell ->
                text(cell.getValue().getItem() == null ? "" : cell.getValue().getItem().getName()));
        bidderTypeColumn.setCellValueFactory(cell ->
                text(cell.getValue().getItem() == null
                        ? ""
                        : typeLabel(cell.getValue().getItem().getCategory())));
        bidderPriceColumn.setCellValueFactory(cell ->
                text(money(cell.getValue().getHighestPrice())));
        bidderStatusColumn.setCellValueFactory(cell ->
                text(statusText(cell.getValue().getStatus())));
        bidderRemainingColumn.setCellValueFactory(cell ->
                text(formatRemainingTime(cell.getValue().getEndAt(), cell.getValue().getStatus())));

        bidderNameColumn.setComparator(AuctionListController::compareItemNames);
        bidderPriceColumn.setComparator(AuctionListController::compareFormattedPrices);

        bidderTypeFilter.getItems().setAll(
                FILTER_ALL,
                "Điện tử",
                "Nghệ thuật",
                "Phương tiện"
        );
        bidderTypeFilter.setValue(FILTER_ALL);
        bidderTypeFilter.valueProperty().addListener((_, _, _) -> filterBidderAuctions());

        bidderStatusFilter.getItems().setAll(
                FILTER_ALL,
                "Đang diễn ra",
                "Đang mở",
                "Đã kết thúc",
                "Đã thanh toán",
                "Đã hủy"
        );
        bidderStatusFilter.setValue(FILTER_ALL);
        bidderStatusFilter.valueProperty().addListener((_, _, _) -> filterBidderAuctions());

        bidderSortFilter.getItems().setAll(
                SORT_DEFAULT,
                SORT_NAME_ASC,
                SORT_NAME_DESC,
                SORT_PRICE_ASC,
                SORT_PRICE_DESC
        );
        bidderSortFilter.setValue(SORT_DEFAULT);
        bidderSortFilter.valueProperty().addListener((_, _, _) -> {
            if (!updatingAuctionList) {
                bidderAuctionTable.getSortOrder().clear();
                filterBidderAuctions();
            }
        });

        bidderSearchField.textProperty().addListener((_, _, _) -> filterBidderAuctions());
        configurePriceSliders();

        bidderAuctionTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, selected) -> {
                    if (updatingAuctionList) {
                        return;
                    }

                    if (selected == null) {
                        selectedAuctionId = null;
                        bidHistoryTable.getItems().clear();
                        showBidderDetail(null);
                        return;
                    }

                    selectedAuctionId = selected.getId();
                    showBidderDetail(selected);
                    loadBidHistory(selected.getId(), true);
                });

        showBidderDetail(null);
    }

    @FXML
    private void filterBidderAuctions() {
        applyBidderFilter(true);
    }

    @FXML
    private void resetBidderFilters() {
        updatingAuctionList = true;
        syncingPriceControls = true;
        try {
            bidderSearchField.clear();
            bidderTypeFilter.setValue(FILTER_ALL);
            bidderStatusFilter.setValue(FILTER_ALL);
            bidderMinPriceField.clear();
            bidderMaxPriceField.clear();
            updateSliderMaxBound(computeSliderMaxBound(allBidderAuctions), true);
            bidderPriceRangeSlider.setLowValue(0);
            bidderPriceRangeSlider.setHighValue(bidderSliderMaxBound);
            bidderPriceMenuButton.setText(FILTER_ALL);
            bidderSortFilter.setValue(SORT_DEFAULT);
            bidderAuctionTable.getSortOrder().clear();
        } finally {
            updatingAuctionList = false;
            syncingPriceControls = false;
        }
        applyBidderFilter(true);
    }

    @FXML
    private void clearBidderPriceFilter() {
        updatingAuctionList = true;
        syncingPriceControls = true;
        try {
            bidderMinPriceField.clear();
            bidderMaxPriceField.clear();
            bidderPriceRangeSlider.setLowValue(0);
            bidderPriceRangeSlider.setHighValue(bidderSliderMaxBound);
            bidderPriceMenuButton.setText(FILTER_ALL);
        } finally {
            updatingAuctionList = false;
            syncingPriceControls = false;
        }
        applyBidderFilter(true);
    }

    private void applyBidderFilter(boolean updateStatusMessage) {
        BigDecimal minPrice;
        BigDecimal maxPrice;
        try {
            minPrice = parsePriceFilter(bidderMinPriceField.getText());
            maxPrice = parsePriceFilter(bidderMaxPriceField.getText());
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa.");
            }
        } catch (IllegalArgumentException exception) {
            if ("BIDDER".equals(currentRole)) {
                statusLabel.setText(exception.getMessage());
            }
            return;
        }

        bidderPriceMenuButton.setText(formatPriceButtonLabel(minPrice, maxPrice));
        String keyword = bidderSearchField.getText();
        String type = bidderTypeFilter.getValue();
        String status = bidderStatusFilter.getValue();
        String sortOption = bidderSortFilter.getValue();
        List<Auction> filtered = allBidderAuctions.stream()
                .filter(auction -> matchesBidderFilter(auction, keyword, type, status, minPrice, maxPrice))
                .sorted(bidderAuctionComparator(sortOption))
                .toList();

        String previousSelectedId = selectedAuctionId;
        Auction restoredSelection = null;

        updatingAuctionList = true;
        try {
            bidderAuctionTable.getItems().setAll(filtered);
            if (!bidderAuctionTable.getSortOrder().isEmpty()) {
                bidderAuctionTable.sort();
            }
            if (previousSelectedId != null) {
                for (Auction auction : filtered) {
                    if (previousSelectedId.equals(auction.getId())) {
                        bidderAuctionTable.getSelectionModel().select(auction);
                        restoredSelection = auction;
                        break;
                    }
                }
            }
            if (restoredSelection == null) {
                bidderAuctionTable.getSelectionModel().clearSelection();
                selectedAuctionId = null;
                bidHistoryTable.getItems().clear();
                showBidderDetail(null);
            } else {
                selectedAuctionId = restoredSelection.getId();
                showBidderDetail(restoredSelection);
            }
        } finally {
            updatingAuctionList = false;
        }

        if (updateStatusMessage && "BIDDER".equals(currentRole)) {
            if (allBidderAuctions.isEmpty()) {
                statusLabel.setText("Hiện chưa có phiên đấu giá.");
            } else if (filtered.isEmpty()) {
                statusLabel.setText("Không tìm thấy phiên đấu giá phù hợp.");
            } else {
                statusLabel.setText("Hiển thị " + filtered.size() + "/" + allBidderAuctions.size() + " phiên.");
            }
        }
    }

    static boolean matchesBidderFilter(Auction auction, String keyword, String statusLabelText) {
        return matchesBidderFilter(auction, keyword, FILTER_ALL, statusLabelText, null, null);
    }

    static boolean matchesBidderFilter(
            Auction auction,
            String keyword,
            String typeLabelText,
            String statusLabelText,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        if (auction == null) {
            return false;
        }
        if (typeLabelText != null && !typeLabelText.isBlank() && !FILTER_ALL.equals(typeLabelText)) {
            ItemType itemType = auction.getItem() == null ? null : auction.getItem().getCategory();
            if (!typeLabelText.equals(typeLabel(itemType))) {
                return false;
            }
        }
        if (statusLabelText != null && !statusLabelText.isBlank() && !FILTER_ALL.equals(statusLabelText)) {
            if (!statusLabelText.equals(statusText(auction.getStatus()))) {
                return false;
            }
        }
        BigDecimal currentPrice = auction.getHighestPrice();
        if (minPrice != null && (currentPrice == null || currentPrice.compareTo(minPrice) < 0)) {
            return false;
        }
        if (maxPrice != null && (currentPrice == null || currentPrice.compareTo(maxPrice) > 0)) {
            return false;
        }
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isEmpty()) {
            return true;
        }
        String auctionId = auction.getId() == null ? "" : auction.getId().toLowerCase(Locale.ROOT);
        String itemName = auction.getItem() == null || auction.getItem().getName() == null
                ? ""
                : auction.getItem().getName().toLowerCase(Locale.ROOT);
        return auctionId.contains(normalizedKeyword) || itemName.contains(normalizedKeyword);
    }

    static BigDecimal parsePriceFilter(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String trimmed = rawText.trim().replace(" ", "");
        String normalized;
        if (trimmed.matches("\\d+")) {
            normalized = trimmed;
        } else if (trimmed.matches("\\d{1,3}(\\.\\d{3})+")) {
            normalized = trimmed.replace(".", "");
        } else if (trimmed.matches("\\d{1,3}(,\\d{3})+")) {
            normalized = trimmed.replace(",", "");
        } else {
            normalized = trimmed;
        }
        try {
            BigDecimal value = new BigDecimal(normalized);
            if (value.signum() < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Khoảng giá lọc phải là số không âm hợp lệ.");
        }
    }

    static String formatPriceButtonLabel(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return FILTER_ALL;
        }
        if (minPrice != null && maxPrice == null) {
            return "Từ " + money(minPrice);
        }
        if (minPrice == null) {
            return "Đến " + money(maxPrice);
        }
        return money(minPrice) + " - " + money(maxPrice);
    }

    private void configurePriceSliders() {
        bidderPriceRangeSlider.prefWidthProperty().bind(bidderPriceRangeHost.widthProperty());
        bidderPriceRangeSlider.prefHeightProperty().bind(bidderPriceRangeHost.heightProperty());
        bidderPriceRangeHost.getChildren().setAll(bidderPriceRangeSlider);

        syncingPriceControls = true;
        try {
            updateSliderMaxBound(DEFAULT_SLIDER_MAX_PRICE, true);
            bidderPriceRangeSlider.setLowValue(0);
            bidderPriceRangeSlider.setHighValue(bidderSliderMaxBound);
        } finally {
            syncingPriceControls = false;
        }

        bidderPriceRangeSlider.lowValueProperty().addListener((_, _, newValue) -> {
            if (syncingPriceControls || updatingAuctionList) {
                return;
            }
            long snapped = snapPriceToStep(newValue.doubleValue(), bidderSliderMaxBound);
            long maxSliderVal = Math.round(bidderPriceRangeSlider.getHighValue());
            if (snapped > maxSliderVal) {
                snapped = maxSliderVal;
            }
            syncingPriceControls = true;
            try {
                bidderPriceRangeSlider.setLowValue(snapped);
                if (snapped <= 0) {
                    bidderMinPriceField.clear();
                } else {
                    bidderMinPriceField.setText(CURRENCY_FORMAT.format(snapped));
                }
            } finally {
                syncingPriceControls = false;
            }
            filterBidderAuctions();
        });

        bidderPriceRangeSlider.highValueProperty().addListener((_, _, newValue) -> {
            if (syncingPriceControls || updatingAuctionList) {
                return;
            }
            long snapped = snapPriceToStep(newValue.doubleValue(), bidderSliderMaxBound);
            long minSliderVal = Math.round(bidderPriceRangeSlider.getLowValue());
            if (snapped < minSliderVal) {
                snapped = minSliderVal;
            }
            syncingPriceControls = true;
            try {
                bidderPriceRangeSlider.setHighValue(snapped);
                bidderMaxPriceField.setText(CURRENCY_FORMAT.format(snapped));
            } finally {
                syncingPriceControls = false;
            }
            filterBidderAuctions();
        });

        bidderMinPriceField.textProperty().addListener((_, _, text) -> {
            if (syncingPriceControls || updatingAuctionList) {
                return;
            }
            syncPriceSliderFromMinField(text);
            filterBidderAuctions();
        });

        bidderMaxPriceField.textProperty().addListener((_, _, text) -> {
            if (syncingPriceControls || updatingAuctionList) {
                return;
            }
            syncPriceSliderFromMaxField(text);
            filterBidderAuctions();
        });
    }

    private void syncPriceSliderFromMinField(String text) {
        try {
            BigDecimal parsed = parsePriceFilter(text);
            syncingPriceControls = true;
            try {
                if (parsed == null) {
                    bidderPriceRangeSlider.setLowValue(0);
                } else {
                    long value = parsed.min(BigDecimal.valueOf(100_000_000_000L)).longValue();
                    boolean maxUnlimited = bidderMaxPriceField.getText() == null
                            || bidderMaxPriceField.getText().isBlank();
                    if (maxUnlimited && value > bidderSliderMaxBound) {
                        updateSliderMaxBound(value, true);
                    }
                    bidderPriceRangeSlider.setLowValue(Math.clamp(value, 0L, bidderSliderMaxBound));
                }
            } finally {
                syncingPriceControls = false;
            }
        } catch (IllegalArgumentException ignored) {
            // Thông báo lỗi sẽ được hiển thị ở statusLabel trong applyBidderFilter
        }
    }

    private void syncPriceSliderFromMaxField(String text) {
        try {
            BigDecimal parsed = parsePriceFilter(text);
            syncingPriceControls = true;
            try {
                if (parsed == null) {
                    long loadedMax = computeSliderMaxBound(allBidderAuctions);
                    Long typedMin = parsePriceOrNull(bidderMinPriceField.getText());
                    long targetMax = typedMin != null && typedMin > loadedMax ? typedMin : loadedMax;
                    updateSliderMaxBound(targetMax, true);
                } else {
                    long value = parsed.min(BigDecimal.valueOf(100_000_000_000L)).longValue();
                    long targetMax = Math.max(1L, value);
                    updateSliderMaxBound(targetMax, false);
                    bidderPriceRangeSlider.setHighValue(Math.clamp(value, 0L, bidderSliderMaxBound));
                }
            } finally {
                syncingPriceControls = false;
            }
        } catch (IllegalArgumentException ignored) {
            // Thông báo lỗi sẽ được hiển thị ở statusLabel trong applyBidderFilter
        }
    }

    private void syncSliderRangeWithLoadedAuctions(long loadedMaxBound) {
        Long typedMin = parsePriceOrNull(bidderMinPriceField.getText());
        Long typedMax = parsePriceOrNull(bidderMaxPriceField.getText());
        boolean maxUnlimited = bidderMaxPriceField.getText() == null
                || bidderMaxPriceField.getText().isBlank();
        long targetMax = (!maxUnlimited && typedMax != null)
                ? Math.max(1L, typedMax)
                : (typedMin != null && typedMin > loadedMaxBound ? typedMin : loadedMaxBound);
        syncingPriceControls = true;
        try {
            updateSliderMaxBound(targetMax, maxUnlimited);
            if (typedMin == null) {
                bidderPriceRangeSlider.setLowValue(0);
            } else {
                bidderPriceRangeSlider.setLowValue(Math.clamp(typedMin, 0L, bidderSliderMaxBound));
            }
            if (!maxUnlimited && typedMax != null) {
                bidderPriceRangeSlider.setHighValue(Math.clamp(typedMax, 0L, bidderSliderMaxBound));
            }
        } finally {
            syncingPriceControls = false;
        }
    }

    private static Long parsePriceOrNull(String text) {
        try {
            BigDecimal parsed = parsePriceFilter(text);
            return parsed == null
                    ? null
                    : parsed.min(BigDecimal.valueOf(100_000_000_000L)).longValue();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void updateSliderMaxBound(long maxBound, boolean resetMaxSliderToTop) {
        bidderSliderMaxBound = Math.max(1L, maxBound);
        bidderPriceRangeSlider.setMin(0);
        bidderPriceRangeSlider.setMax(bidderSliderMaxBound);
        if (resetMaxSliderToTop) {
            bidderPriceRangeSlider.setHighValue(bidderSliderMaxBound);
        }
    }

    static long computeSliderMaxBound(List<Auction> auctions) {
        if (auctions == null || auctions.isEmpty()) {
            return DEFAULT_SLIDER_MAX_PRICE;
        }
        BigDecimal maxPrice = BigDecimal.ZERO;
        for (Auction auction : auctions) {
            if (auction != null && auction.getHighestPrice() != null
                    && auction.getHighestPrice().compareTo(maxPrice) > 0) {
                maxPrice = auction.getHighestPrice();
            }
        }
        if (maxPrice.signum() <= 0) {
            return DEFAULT_SLIDER_MAX_PRICE;
        }
        return Math.max(1L, maxPrice.min(BigDecimal.valueOf(100_000_000_000L)).longValue());
    }

    static long computePriceStep(long maxBound) {
        if (maxBound <= 100L) {
            return 1L;
        }
        double target = maxBound / 100.0;
        long magnitude = 1L;
        while (magnitude * 10L <= target) {
            magnitude *= 10L;
        }
        double normalized = target / magnitude;
        long factor;
        if (normalized < 1.5) {
            factor = 1L;
        } else if (normalized < 3.5) {
            factor = 2L;
        } else if (normalized < 7.5) {
            factor = 5L;
        } else {
            factor = 10L;
        }
        return Math.max(1L, factor * magnitude);
    }

    static long snapPriceToStep(double value, long maxBound) {
        if (value <= 0) {
            return 0L;
        }
        if (value >= maxBound) {
            return maxBound;
        }
        long step = computePriceStep(maxBound);
        long snapped = Math.round(value / step) * step;
        return Math.clamp(snapped, 0L, maxBound);
    }

    static Comparator<Auction> bidderAuctionComparator(String sortOption) {
        Comparator<Auction> newestFirst = Comparator.comparing(
                Auction::getStartAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        );
        if (sortOption == null) {
            return newestFirst;
        }
        return switch (sortOption) {
            case SORT_NAME_ASC -> Comparator
                    .comparing(AuctionListController::extractItemName, AuctionListController::compareItemNames)
                    .thenComparing(newestFirst);
            case SORT_NAME_DESC -> Comparator
                    .comparing(
                            AuctionListController::extractItemName,
                            ((Comparator<String>) AuctionListController::compareItemNames).reversed()
                    )
                    .thenComparing(newestFirst);
            case SORT_PRICE_ASC -> Comparator
                    .comparing(Auction::getHighestPrice, Comparator.nullsLast(BigDecimal::compareTo))
                    .thenComparing(newestFirst);
            case SORT_PRICE_DESC -> Comparator
                    .comparing(Auction::getHighestPrice, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(newestFirst);
            default -> newestFirst;
        };
    }

    private static String extractItemName(Auction auction) {
        if (auction == null || auction.getItem() == null || auction.getItem().getName() == null) {
            return "";
        }
        return auction.getItem().getName();
    }

    static int compareItemNames(String left, String right) {
        String a = left == null ? "" : left.trim();
        String b = right == null ? "" : right.trim();
        boolean aHasDigit = containsDigit(a);
        boolean bHasDigit = containsDigit(b);
        if (aHasDigit != bHasDigit) {
            return aHasDigit ? -1 : 1;
        }

        int indexA = 0;
        int indexB = 0;
        Collator collator = VIETNAMESE_COLLATOR.get();
        while (indexA < a.length() && indexB < b.length()) {
            boolean digitA = isAsciiDigit(a.charAt(indexA));
            boolean digitB = isAsciiDigit(b.charAt(indexB));
            int endA = nextChunkEnd(a, indexA, digitA);
            int endB = nextChunkEnd(b, indexB, digitB);
            String chunkA = a.substring(indexA, endA);
            String chunkB = b.substring(indexB, endB);

            int cmp;
            if (digitA && digitB) {
                cmp = new BigInteger(chunkA).compareTo(new BigInteger(chunkB));
                if (cmp == 0) {
                    cmp = Integer.compare(chunkA.length(), chunkB.length());
                }
            } else if (digitA != digitB) {
                cmp = digitA ? -1 : 1;
            } else {
                cmp = collator.compare(chunkA, chunkB);
            }
            if (cmp != 0) {
                return cmp;
            }
            indexA = endA;
            indexB = endB;
        }
        return Integer.compare(a.length(), b.length());
    }

    private static int compareFormattedPrices(String left, String right) {
        BigDecimal a = parseFormattedMoney(left);
        BigDecimal b = parseFormattedMoney(right);
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    private static BigDecimal parseFormattedMoney(String formatted) {
        if (formatted == null || formatted.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(formatted.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (isAsciiDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAsciiDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static int nextChunkEnd(String value, int start, boolean digits) {
        int index = start;
        while (index < value.length() && isAsciiDigit(value.charAt(index)) == digits) {
            index++;
        }
        return index;
    }

    private void showBidderDetail(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            setVisible(bidderDetailPlaceholder, true);
            setVisible(bidderDetailContent, false);
            return;
        }

        setVisible(bidderDetailPlaceholder, false);
        setVisible(bidderDetailContent, true);

        bidderDetailItemName.setText(auction.getItem().getName());
        bidderDetailItemCategory.setText(typeLabel(auction.getItem().getCategory()));
        String desc = auction.getItem().getDescription();
        bidderDetailDescription.setText(desc == null || desc.isBlank() ? "(Không có mô tả)" : desc);
        bidderDetailExtraInfo.setText(auction.getItem().getInfo());

        bidderDetailStartingPrice.setText(money(auction.getStartingPrice()) + " VNĐ");
        bidderDetailIncrement.setText(money(auction.getIncrement()) + " VNĐ");
        bidderDetailCurrentPrice.setText(money(auction.getHighestPrice()) + " VNĐ");
        String leader = auction.getHighestBidderId();
        bidderDetailLeader.setText(leader == null || leader.isBlank() ? "Chưa có lượt đặt" : leader);
        bidderDetailStatus.setText(statusText(auction.getStatus()));
        bidderDetailCreatedAt.setText(time(auction.getStartAt()));
        bidderDetailEndAt.setText(time(auction.getEndAt()));
        bidderDetailRemainingTime.setText(
                formatRemainingTime(auction.getEndAt(), auction.getStatus()));
    }

    @FXML
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

                    List<Auction> auctions = response.auctions().stream()
                            .sorted(Comparator.comparing(Auction::getStartAt,
                                    Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();

                    allBidderAuctions.clear();
                    allBidderAuctions.addAll(auctions);
                    syncSliderRangeWithLoadedAuctions(computeSliderMaxBound(auctions));
                    applyBidderFilter(false);

                    if (allBidderAuctions.isEmpty()) {

                        statusLabel.setText(
                                "Hiện chưa có phiên đấu giá."
                        );

                    } else if (bidderAuctionTable.getItems().isEmpty()) {

                        statusLabel.setText(
                                "Không tìm thấy phiên đấu giá phù hợp bộ lọc."
                        );

                    } else {

                        statusLabel.setText(
                                "Đã tải " + allBidderAuctions.size()
                                        + " phiên đấu giá từ server."
                        );

                        // Nếu phiên đang chọn vẫn tồn tại,
                        // tiếp tục tải lịch sử của phiên đó
                        if (selectedAuctionId != null) {
                            loadBidHistory(selectedAuctionId, false);
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

    private void loadBidHistory(String auctionId, boolean updateStatus) {

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
                        if (updateStatus) {
                            statusLabel.setText(response.getMessage());
                        }

                        return;
                    }

                    List<Bid> bids = response.bids();

                    bidHistoryTable.getItems().setAll(
                            bids == null ? List.of() : bids
                    );

                    if (updateStatus) {
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
                    }
                });

            } catch (Exception exception) {

                Platform.runLater(() -> {

                    if (updateStatus && auctionId.equals(selectedAuctionId)) {

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

                                loadBidHistory(selectedAuctionId, false);
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

    private void startBidderCountdown() {
        stopBidderCountdown();
        bidderCountdownTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        _ -> {
                            bidderAuctionTable.refresh();
                            Auction selected = bidderAuctionTable.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                bidderDetailRemainingTime.setText(
                                        formatRemainingTime(selected.getEndAt(), selected.getStatus()));
                            }
                        }
                )
        );
        bidderCountdownTimeline.setCycleCount(Timeline.INDEFINITE);
        bidderCountdownTimeline.play();
    }

    private void stopBidderCountdown() {
        if (bidderCountdownTimeline != null) {
            bidderCountdownTimeline.stop();
            bidderCountdownTimeline = null;
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

        String auctionId = selectedAuctionId == null ? "" : selectedAuctionId.trim();
        String amountText = amountField.getText().trim();

        if (auctionId.isEmpty()) {

            statusLabel.setText(
                    "Vui lòng chọn một phiên đấu giá từ danh sách."
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
                        loadBidHistory(auctionId, false);
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

    private static final class PriceRangeSlider extends Region {

        private static final double TRACK_HEIGHT = 4.0;
        private static final double THUMB_SIZE = 14.0;
        private static final double CONTROL_HEIGHT = 22.0;
        private static final double DEFAULT_PREF_WIDTH = 210.0;
        private static final double MIN_WIDTH = 100.0;

        private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0.0) {
            @Override
            protected void invalidated() {
                clampBounds();
                requestLayout();
            }
        };

        private final DoubleProperty max = new SimpleDoubleProperty(this, "max", 100_000_000.0) {
            @Override
            protected void invalidated() {
                clampBounds();
                requestLayout();
            }
        };

        private final DoubleProperty lowValue = new SimpleDoubleProperty(this, "lowValue", 0.0) {
            @Override
            protected void invalidated() {
                double clamped = clampLow(get());
                if (Double.compare(clamped, get()) != 0) {
                    set(clamped);
                    return;
                }
                updateThumbZOrder();
                requestLayout();
            }
        };

        private final DoubleProperty highValue = new SimpleDoubleProperty(this, "highValue", 100_000_000.0) {
            @Override
            protected void invalidated() {
                double clamped = clampHigh(get());
                if (Double.compare(clamped, get()) != 0) {
                    set(clamped);
                    return;
                }
                updateThumbZOrder();
                requestLayout();
            }
        };

        private final Region track = new Region();
        private final Region rangeBar = new Region();
        private final Region lowThumb = new Region();
        private final Region highThumb = new Region();

        private boolean adjusting;
        private boolean draggingLowThumb;

        private PriceRangeSlider() {
            getStyleClass().add("price-range-slider");
            track.getStyleClass().add("price-range-track");
            rangeBar.getStyleClass().add("price-range-bar");
            lowThumb.getStyleClass().add("price-range-thumb");
            highThumb.getStyleClass().add("price-range-thumb");

            lowThumb.setCursor(Cursor.HAND);
            highThumb.setCursor(Cursor.HAND);
            setCursor(Cursor.HAND);

            getChildren().addAll(track, rangeBar, lowThumb, highThumb);
            installMouseHandlers();
        }

        private double getMin() {
            return min.get();
        }

        private void setMin(double value) {
            min.set(value);
        }

        private double getMax() {
            return max.get();
        }

        private void setMax(double value) {
            max.set(value);
        }

        private double getLowValue() {
            return lowValue.get();
        }

        private void setLowValue(double value) {
            lowValue.set(value);
        }

        private DoubleProperty lowValueProperty() {
            return lowValue;
        }

        private double getHighValue() {
            return highValue.get();
        }

        private void setHighValue(double value) {
            highValue.set(value);
        }

        private DoubleProperty highValueProperty() {
            return highValue;
        }

        private void installMouseHandlers() {
            lowThumb.setOnMousePressed(event -> {
                draggingLowThumb = true;
                lowThumb.toFront();
                updateValueFromEvent(event, true);
                event.consume();
            });
            lowThumb.setOnMouseDragged(event -> {
                updateValueFromEvent(event, true);
                event.consume();
            });

            highThumb.setOnMousePressed(event -> {
                draggingLowThumb = false;
                highThumb.toFront();
                updateValueFromEvent(event, false);
                event.consume();
            });
            highThumb.setOnMouseDragged(event -> {
                updateValueFromEvent(event, false);
                event.consume();
            });

            setOnMousePressed(event -> {
                double localX = toLocalX(event);
                double lowX = valueToX(getLowValue());
                double highX = valueToX(getHighValue());
                if (Math.abs(localX - lowX) <= Math.abs(localX - highX)) {
                    draggingLowThumb = localX <= highX || getLowValue() >= getMax();
                } else {
                    draggingLowThumb = false;
                }
                if (draggingLowThumb) {
                    lowThumb.toFront();
                } else {
                    highThumb.toFront();
                }
                updateValueFromEvent(event, draggingLowThumb);
                event.consume();
            });

            setOnMouseDragged(event -> {
                updateValueFromEvent(event, draggingLowThumb);
                event.consume();
            });
        }

        private void updateValueFromEvent(MouseEvent event, boolean updateLow) {
            double value = xToValue(toLocalX(event));
            if (updateLow) {
                setLowValue(value);
            } else {
                setHighValue(value);
            }
        }

        private double toLocalX(MouseEvent event) {
            Point2D local = sceneToLocal(event.getSceneX(), event.getSceneY());
            return local == null ? event.getX() : local.getX();
        }

        private void updateThumbZOrder() {
            double span = getMax() - getMin();
            if (span > 0 && Double.compare(getLowValue(), getHighValue()) == 0) {
                if (getHighValue() >= getMax()) {
                    lowThumb.toFront();
                } else if (getLowValue() <= getMin()) {
                    highThumb.toFront();
                }
            }
        }

        private void clampBounds() {
            if (adjusting) {
                return;
            }
            adjusting = true;
            try {
                if (getMax() < getMin()) {
                    setMax(getMin());
                }
                double clampedLow = Math.clamp(getLowValue(), getMin(), getMax());
                double clampedHigh = Math.clamp(getHighValue(), clampedLow, getMax());
                if (Double.compare(clampedLow, getLowValue()) != 0) {
                    setLowValue(clampedLow);
                }
                if (Double.compare(clampedHigh, getHighValue()) != 0) {
                    setHighValue(clampedHigh);
                }
            } finally {
                adjusting = false;
            }
        }

        private double clampLow(double candidate) {
            double upper = Math.max(getMin(), Math.min(getMax(), getHighValue()));
            return Math.clamp(candidate, getMin(), upper);
        }

        private double clampHigh(double candidate) {
            double lower = Math.max(getMin(), Math.min(getMax(), getLowValue()));
            return Math.clamp(candidate, lower, getMax());
        }

        private double valueToX(double value) {
            double radius = THUMB_SIZE / 2.0;
            double trackStart = radius;
            double trackLen = Math.max(1.0, getWidth() - THUMB_SIZE);
            double span = getMax() - getMin();
            if (span <= 0) {
                return trackStart;
            }
            double ratio = Math.clamp((value - getMin()) / span, 0.0, 1.0);
            return trackStart + ratio * trackLen;
        }

        private double xToValue(double x) {
            double radius = THUMB_SIZE / 2.0;
            double trackStart = radius;
            double trackLen = Math.max(1.0, getWidth() - THUMB_SIZE);
            double ratio = Math.clamp((x - trackStart) / trackLen, 0.0, 1.0);
            double span = Math.max(0.0, getMax() - getMin());
            return getMin() + ratio * span;
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            double radius = THUMB_SIZE / 2.0;
            double trackStart = radius;
            double trackLen = Math.max(1.0, width - THUMB_SIZE);
            double centerY = height / 2.0;

            double lowCenterX = valueToX(getLowValue());
            double highCenterX = valueToX(getHighValue());

            track.resizeRelocate(trackStart, centerY - TRACK_HEIGHT / 2.0, trackLen, TRACK_HEIGHT);
            rangeBar.resizeRelocate(
                    lowCenterX,
                    centerY - TRACK_HEIGHT / 2.0,
                    Math.max(0.0, highCenterX - lowCenterX),
                    TRACK_HEIGHT
            );
            lowThumb.resizeRelocate(lowCenterX - radius, centerY - radius, THUMB_SIZE, THUMB_SIZE);
            highThumb.resizeRelocate(highCenterX - radius, centerY - radius, THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        protected double computeMinWidth(double height) {
            return MIN_WIDTH;
        }

        @Override
        protected double computePrefWidth(double height) {
            return DEFAULT_PREF_WIDTH;
        }

        @Override
        protected double computeMinHeight(double width) {
            return CONTROL_HEIGHT;
        }

        @Override
        protected double computePrefHeight(double width) {
            return CONTROL_HEIGHT;
        }

        @Override
        protected double computeMaxHeight(double width) {
            return CONTROL_HEIGHT;
        }
    }
}
