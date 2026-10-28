package ltnc.auction.client.controller;

import java.math.BigDecimal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import ltnc.auction.client.net.AuctionSocketClient;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.RequestType;
import ltnc.auction.common.protocol.Response;

public class AuctionListController {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5050;

    @FXML private StackPane root;
    @FXML private VBox authView;
    @FXML private VBox registerView;

    @FXML private Node adminView;
    @FXML private Node sellerView;
    @FXML private Node bidderView;

    @FXML private TextField loginUsername;
    @FXML private TextField registerUsername;
    @FXML private TextField auctionIdField;
    @FXML private TextField amountField;

    @FXML private PasswordField loginPassword;
    @FXML private PasswordField registerPassword;

    @FXML private ChoiceBox<String> loginRole;
    @FXML private ChoiceBox<String> registerRole;

    @FXML private Label authMessage;
    @FXML private Label registerMessage;
    @FXML private Label statusLabel;

    @FXML private ListView<String> auctionList;

    private String currentUsername;
    private String currentRole;

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

        auctionList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {
                    if (selected != null) {
                        auctionIdField.setText(
                                selected.split(" \\| ", 2)[0]
                        );
                    }
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

                    String authenticatedRole = response.getRole();

                    if (authenticatedRole == null
                            || !expectedRole.equals(authenticatedRole)) {
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
        if (registerUsername.getText().isBlank()
                || registerPassword.getText().isBlank()) {
            registerMessage.setText(
                    "Vui lòng nhập đủ tên đăng nhập và mật khẩu."
            );
            return;
        }

        registerMessage.setText(
                "Chức năng đăng ký chưa được triển khai ở backend."
        );
    }

    @FXML
    private void signOut() {
        currentUsername = null;
        currentRole = null;

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
    private void showPrototypeMessage() {
        statusLabel.setText(
                "Chức năng này cần API tương ứng ở backend."
        );
    }

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
                    auctionList.getItems().clear();

                    if (!response.isSuccess()) {
                        statusLabel.setText(response.getMessage());
                        return;
                    }

                    for (Auction auction : response.getAuctions()) {
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

                    if (auctionList.getItems().isEmpty()) {
                        statusLabel.setText(
                                "Hiện chưa có phiên đấu giá."
                        );
                    } else {
                        statusLabel.setText(
                                "Đã tải danh sách phiên đấu giá từ server."
                        );
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
            statusLabel.setText("Vui lòng chọn hoặc nhập mã phiên.");
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

        statusLabel.setText("Đang gửi mức giá đến server...");

        Thread worker = new Thread(() -> {
            try (AuctionSocketClient client =
                         new AuctionSocketClient(
                                 SERVER_HOST,
                                 SERVER_PORT
                         )) {

                Response response = client.send(
                        new Request(
                                RequestType.PLACE_BID,
                                currentUsername,
                                null,
                                auctionId,
                                amount
                        )
                );

                Platform.runLater(() ->
                        statusLabel.setText(response.getMessage())
                );

                if (response.isSuccess()) {
                    loadAuctions();
                }

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

    private void setVisible(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }
}
