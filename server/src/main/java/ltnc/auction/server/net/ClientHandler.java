package ltnc.auction.server.net;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import ltnc.auction.common.exception.AuctionClosedException;
import ltnc.auction.common.exception.InvalidBidException;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.Response;
import ltnc.auction.server.service.AuctionManager;
import ltnc.auction.server.service.AuthService;

public final class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionManager manager;
    private final AuthService authService;

    public ClientHandler(Socket socket, AuctionManager manager, AuthService authService) {
        this.socket = socket;
        this.manager = manager;
        this.authService = authService;
    }

    @Override
    public void run() {
        try (Socket clientSocket = socket;
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            out.flush();
            while (true) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException exception) {
                    break;
                }
                Response response = obj instanceof Request request
                        ? handle(request)
                        : new Response(false, "Yêu cầu không hợp lệ", null);
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (Exception exception) {
            System.err.println("Lỗi xử lý client: " + exception.getMessage());
        }
    }

    private Response handle(Request request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Loại yêu cầu không hợp lệ", null);
        }
        try {
            return switch (request.getType()) {
                case PING -> new Response(true, "PONG", null);
                case LOGIN -> handleLogin(request);
                case REGISTER -> handleRegister(request);
                case LIST_AUCTIONS -> new Response(true, "OK", manager.listAuctions());
                case LIST_SELLER_AUCTIONS -> handleListSellerAuctions(request);
                case LIST_BIDS -> handleListBids(request);
                case PLACE_BID -> handlePlaceBid(request);
                case CREATE_AUCTION -> handleCreateAuction(request);
            };
        } catch (InvalidBidException | AuctionClosedException | IllegalArgumentException exception) {
            return new Response(false, exception.getMessage(), null);
        } catch (Exception exception) {
            System.err.println("Lỗi nghiệp vụ: " + exception.getMessage());
            return new Response(false, "Đã xảy ra lỗi khi xử lý yêu cầu!", null);
        }
    }

    private Response handleListSellerAuctions(Request request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return new Response(false, "Thiếu thông tin người bán.", null);
        }
        return new Response(true, "OK", manager.listAuctionsBySeller(request.getUsername()));
    }

    private Response handleListBids(Request request) {
        if (manager.getAuction(request.getAuctionId()) == null) {
            return new Response(false, "Không tìm thấy phiên đấu giá.", null, null, null);
        }
        var bids = manager.listBids(request.getAuctionId());
        return new Response(true,
                bids.isEmpty() ? "Phiên đấu giá chưa có lượt đặt giá." : "Đã tải lịch sử đặt giá.",
                null, null, bids);
    }

    private Response handlePlaceBid(Request request)
            throws InvalidBidException, AuctionClosedException {
        Bid bid = manager.placeBid(request.getAuctionId(), request.getUsername(), request.getAmount());
        return new Response(true, "Đặt giá thành công: " + bid.amount() + "!", null);
    }

    private Response handleCreateAuction(Request request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return new Response(false, "Thiếu thông tin người bán.", null);
        }
        Auction auction = manager.createAuction(
                request.getUsername(), request.getItemRequest(),
                request.getStartingPrice(), request.getIncrement(), request.getEndAt());
        return new Response(true,
                "Tạo phiên đấu giá thành công. Mã phiên: " + auction.getId(),
                List.of(auction));
    }

    private Response handleLogin(Request request) {
        String role = authService.authenticate(request.getUsername(), request.getPassword());
        if (role == null) {
            return new Response(false, "Tên đăng nhập hoặc mật khẩu không đúng!", null);
        }
        return new Response(true, "Đăng nhập thành công!", null, role);
    }

    private Response handleRegister(Request request) {
        String role = authService.register(request.getUsername(), request.getPassword(), request.getRole());
        return new Response(true, "Đăng ký tài khoản thành công.", null, role);
    }
}
