package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuctionClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auction-list.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);
        stage.setTitle("Online Auction — Java 26");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
