package ltnc.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuctionClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ltnc/auction/client/auction-list.fxml"));
        stage.setScene(new Scene(loader.load(), 1120, 760));
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setTitle("BIDWISE | Sàn đấu giá trực tuyến");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
