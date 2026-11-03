package ltnc.auction.client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.Response;

public final class AuctionSocketClient implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;

    public AuctionSocketClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Response send(Request request)
            throws IOException, ClassNotFoundException {
        output.writeObject(request);
        output.flush();

        Object response = input.readObject();
        if (!(response instanceof Response auctionResponse)) {
            throw new IOException("Phản hồi không hợp lệ");
        }
        return auctionResponse;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
