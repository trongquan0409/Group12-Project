package ltnc.auction.server.net;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ltnc.auction.common.exception.AuctionClosedException;
import ltnc.auction.common.exception.InvalidBidException;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.RequestType;
import ltnc.auction.common.protocol.Response;
import ltnc.auction.server.service.AuctionManager;
import ltnc.auction.server.service.AuthService;

public final class ClientHandler implements Runnable {

 private final Socket socket;
 private final AuctionManager manager;
 private final AuthService authService;

 public ClientHandler(
         Socket socket,
         AuctionManager manager,
         AuthService authService
 ) {
  this.socket = socket;
  this.manager = manager;
  this.authService = authService;
 }

 @Override
 public void run() {
  try (
          Socket clientSocket = socket;
          ObjectOutputStream out =
                  new ObjectOutputStream(clientSocket.getOutputStream());
          ObjectInputStream in =
                  new ObjectInputStream(clientSocket.getInputStream())
  ) {
   out.flush();

   while (true) {
    Object obj;

    try {
     obj = in.readObject();
    } catch (EOFException exception) {
     break;
    }

    if (!(obj instanceof Request request)) {
     out.writeObject(
             new Response(false, "Yêu cầu không hợp lệ", null)
     );
     out.flush();
     continue;
    }

    Response response = handle(request);

    out.writeObject(response);
    out.flush();
    out.reset();
   }

  } catch (Exception exception) {
   System.err.println(
           "Lỗi xử lý client: " + exception.getMessage()
   );
  }
 }

 private Response handle(Request request) {
  if (request == null || request.getType() == null) {
   return new Response(false, "Loại yêu cầu không hợp lệ", null);
  }

  try {
   return switch (request.getType()) {

    case PING ->
            new Response(true, "PONG", null);

    case LOGIN -> handleLogin(request);

    case LIST_AUCTIONS ->
            new Response(
                    true,
                    "OK",
                    manager.listAuctions()
            );

    case PLACE_BID -> {
     Bid bid = manager.placeBid(
             request.getAuctionId(),
             request.getUsername(),
             request.getAmount()
     );

     yield new Response(
             true,
             "Đặt giá thành công: " + bid.getAmount() + "!",
             null
     );
    }

    case CREATE_AUCTION ->
            new Response(
                    false,
                    "Chức năng tạo phiên đấu giá chưa được triển khai!",
                    null
            );
   };

  } catch (InvalidBidException | AuctionClosedException exception) {
   return new Response(false, exception.getMessage(), null);

  } catch (Exception exception) {
   System.err.println(
           "Lỗi nghiệp vụ: " + exception.getMessage()
   );

   return new Response(
           false,
           "Đã xảy ra lỗi khi xử lý yêu cầu!",
           null
   );
  }
 }

 private Response handleLogin(Request request) {
  String role = authService.authenticate(
          request.getUsername(),
          request.getPassword()
  );

  if (role == null) {
   return new Response(
           false,
           "Tên đăng nhập hoặc mật khẩu không đúng!",
           null
   );
  }

  return new Response(
          true,
          "Đăng nhập thành công!",
          null,
          role
  );
 }
}
