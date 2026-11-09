package ltnc.auction.client.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ltnc.auction.common.protocol.Request;
import ltnc.auction.common.protocol.Response;
import ltnc.auction.common.protocol.ServerEvent;

public final class AuctionSocketClient implements AutoCloseable {

    public interface ServerEventListener {

        void onServerEvent(ServerEvent event);
    }

    private final Socket socket;

    private final ObjectOutputStream output;

    private final ObjectInputStream input;

    private final BlockingQueue<Response> responseQueue =
            new LinkedBlockingQueue<>();

    private volatile ServerEventListener eventListener;

    private final Thread readerThread;

    private volatile boolean running;

    public AuctionSocketClient(
            String host,
            int port
    ) throws IOException {

        socket = new Socket(host, port);

        output = new ObjectOutputStream(
                socket.getOutputStream()
        );

        output.flush();

        input = new ObjectInputStream(
                socket.getInputStream()
        );

        running = true;

        readerThread = Thread.ofVirtual()
                .name("auction-server-reader")
                .start(this::readMessages);
    }

    public void setEventListener(
            ServerEventListener listener
    ) {

        eventListener = listener;
    }

    public boolean isConnected() {

        return running
                && !socket.isClosed()
                && socket.isConnected();
    }

    public Response send(
            Request request
    ) throws IOException, InterruptedException {

        if (!isConnected()) {

            throw new IOException(
                    "Kết nối tới server đang đóng."
            );
        }

        synchronized (output) {

            output.writeObject(request);

            output.flush();

            output.reset();
        }

        return responseQueue.take();
    }

    private void readMessages() {

        try {

            while (running) {

                Object message =
                        input.readObject();

                if (message instanceof Response response) {

                    responseQueue.offer(response);

                } else if (
                        message instanceof ServerEvent event
                ) {

                    ServerEventListener listener =
                            eventListener;

                    if (listener != null) {

                        listener.onServerEvent(event);
                    }
                }
            }

        } catch (EOFException ignored) {

            running = false;

        } catch (IOException exception) {

            if (running) {

                running = false;

                System.err.println(
                        "Mất kết nối với server: "
                                + exception.getMessage()
                );
            }

        } catch (ClassNotFoundException exception) {

            running = false;

            System.err.println(
                    "Không thể đọc dữ liệu từ server: "
                            + exception.getMessage()
            );
        }
    }

    @Override
    public void close() throws IOException {

        if (!running
                && socket.isClosed()) {

            return;
        }

        running = false;

        try {

            input.close();

        } catch (IOException ignored) {

        }

        try {

            output.close();

        } catch (IOException ignored) {

        }

        socket.close();

        readerThread.interrupt();
    }
}