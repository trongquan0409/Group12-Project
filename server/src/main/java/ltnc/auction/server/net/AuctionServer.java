
package ltnc.auction.server.net;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;
import ltnc.auction.server.persistence.AuctionStore;
import ltnc.auction.server.service.AuctionManager;
import ltnc.auction.server.service.AuthService;

public final class AuctionServer {

    private static final int DEFAULT_PORT = 5050;

    private AuctionServer() {
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(
                System.getenv().getOrDefault(
                        "AUCTION_PORT",
                        String.valueOf(DEFAULT_PORT)
                )
        );

        Path dataFile = Path.of(
                System.getenv().getOrDefault(
                        "AUCTION_DATA_FILE",
                        "server/data/auction-data.ser"
                )
        );

        AuctionManager manager = AuctionManager.getInstance();
        manager.configureStore(new AuctionStore(dataFile));

        initializeSampleAuction(manager);

        AuthService authService = new AuthService();

        ExecutorService pool =
                Executors.newVirtualThreadPerTaskExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.close();
            System.out.println("Auction server đã dừng.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(
                    "Auction server đang lắng nghe tại cổng " + port
            );
            System.out.println(
                    "File dữ liệu: " + dataFile.toAbsolutePath()
            );

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                System.out.println(
                        "Client kết nối: "
                                + clientSocket.getRemoteSocketAddress()
                );

                pool.submit(
                        new ClientHandler(
                                clientSocket,
                                manager,
                                authService
                        )
                );
            }

        } catch (IOException exception) {
            System.err.println(
                    "Không thể khởi động hoặc duy trì server: "
                            + exception.getMessage()
            );
        } finally {
            pool.close();
        }
    }

    private static void initializeSampleAuction(
            AuctionManager manager
    ) {
        if (!manager.listAuctions().isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        Art sampleArt = new Art(
                "item-1",
                "Tranh nghệ thuật mẫu",
                "Sản phẩm mẫu dùng để kiểm thử chức năng đấu giá",
                "seller",
                new BigDecimal("100000")
        );

        Auction sampleAuction = new Auction(
                "auction-1",
                sampleArt,
                new BigDecimal("100000"),
                new BigDecimal("10000"),
                now.minusSeconds(60),
                now.plusSeconds(3600)
        );

        manager.addAuction(sampleAuction);

        System.out.println(
                "Đã khởi tạo phiên đấu giá mẫu: auction-1"
        );
    }
}
