package ltnc.auction.client.controller;

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

import java.math.BigDecimal;

public class AuctionListController {
    @FXML private StackPane root;
    @FXML private VBox authView, registerView;
    @FXML private Node adminView, sellerView, bidderView;
    @FXML private TextField loginUsername, registerUsername, auctionIdField, amountField;
    @FXML private PasswordField loginPassword, registerPassword;
    @FXML private ChoiceBox<String> loginRole, registerRole;
    @FXML private Label authMessage, registerMessage, statusLabel;
    @FXML private ListView<String> auctionList;

    @FXML
    public void initialize() {
        loginRole.getItems().setAll("Người đấu giá", "Người bán", "Admin (demo)");
        loginRole.setValue("Người đấu giá");
        registerRole.getItems().setAll("Người đấu giá", "Người bán");
        registerRole.setValue("Người đấu giá");
        auctionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                auctionIdField.setText(selected.split(" \\| ", 2)[0]);
            }
        });
    }

    @FXML private void showRegister() {
        setVisible(authView, false);
        setVisible(registerView, true);
    }

    @FXML private void showLogin() {
        setVisible(registerView, false);
        setVisible(authView, true);
    }

    @FXML private void signIn() {
        if (loginUsername.getText().isBlank() || loginPassword.getText().isBlank()) {
            authMessage.setText("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        String role = loginRole.getValue();
        setVisible(authView, false);
        if (role.startsWith("Admin")) {
            setVisible(adminView, true);
        } else if (role.equals("Người bán")) {
            setVisible(sellerView, true);
        } else {
            setVisible(bidderView, true);
            loadAuctions();
        }
    }

    @FXML private void register() {
        if (registerUsername.getText().isBlank() || registerPassword.getText().isBlank()) {
            registerMessage.setText("Vui lòng nhập đủ tên đăng nhập và mật khẩu.");
            return;
        }
        registerMessage.setText("Đây là giao diện mẫu. Đăng ký sẽ hoạt động khi backend có API tương ứng.");
    }

    @FXML private void signOut() {
        setVisible(adminView, false);
        setVisible(sellerView, false);
        setVisible(bidderView, false);
        setVisible(authView, true);
        authMessage.setText("Bạn đã đăng xuất.");
    }

    @FXML private void showPrototypeMessage() {
        statusLabel.setText("Thao tác tạo phiên cần API từ backend; đây hiện là bản mẫu giao diện.");
    }

    private void loadAuctions() {
        Thread worker = new Thread(() -> {
            try (AuctionSocketClient client = new AuctionSocketClient("localhost", 5050)) {
                Response response = client.send(new Request(RequestType.LIST_AUCTIONS, null, null, null, null));
                Platform.runLater(() -> {
                    auctionList.getItems().clear();
                    if (response.isSuccess()) {
                        for (Auction auction : response.getAuctions()) {
                            auctionList.getItems().add(auction.getId() + " | "
                                    + auction.getItem().getName() + " | "
                                    + auction.getStatus() + " | " + auction.getHighestPrice());
                        }
                        if (auctionList.getItems().isEmpty()) {
                            statusLabel.setText("Hiện chưa có phiên đấu giá.");
                        } else {
                            statusLabel.setText("Đã tải danh sách phiên.");
                        }
                    } else {
                        statusLabel.setText(response.getMessage());
                    }
                });
            } catch (Exception exception) {
                Platform.runLater(() -> statusLabel.setText(
                        "Chưa kết nối server. Danh sách sẽ tải khi backend sẵn sàng."));
            }
        }, "auction-list-loader");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML private void placeBid() {
        String auctionId = auctionIdField.getText().trim();
        String amountText = amountField.getText().trim();
        if (auctionId.isEmpty()) {
            statusLabel.setText("Vui lòng nhập mã phiên.");
            return;
        }

        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            if (amount.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            statusLabel.setText("Mức giá phải là số lớn hơn 0.");
            return;
        }

        Thread worker = new Thread(() -> {
            try (AuctionSocketClient client = new AuctionSocketClient("localhost", 5050)) {
                Response response = client.send(new Request(RequestType.PLACE_BID,
                        loginUsername.getText().trim(), null, auctionId, amount));
                Platform.runLater(() -> statusLabel.setText(response.getMessage()));
                if (response.isSuccess()) loadAuctions();
            } catch (Exception exception) {
                Platform.runLater(() -> statusLabel.setText("Không thể gửi giá: " + exception.getMessage()));
            }
        }, "place-bid");
        worker.setDaemon(true);
        worker.start();
    }

    private void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
