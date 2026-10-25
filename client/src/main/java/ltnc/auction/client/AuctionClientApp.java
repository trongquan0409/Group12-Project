package ltnc.auction.client;
import javafx.application.Application; import javafx.fxml.FXMLLoader; import javafx.scene.Scene; import javafx.stage.Stage;
public class AuctionClientApp extends Application {@Override public void start(Stage stage)throws Exception{FXMLLoader loader=new FXMLLoader(getClass().getResource("/ltnc/auction/client/auction-list.fxml"));stage.setScene(new Scene(loader.load(),760,480));stage.setTitle("Online Auction — LTNC 2026");stage.show();}public static void main(String[] args){launch(args);}}
