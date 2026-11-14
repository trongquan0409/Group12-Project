package ltnc.auction.server.realtime;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RealtimeClientRegistry {

    private final Set<ObjectOutputStream> clients =
            ConcurrentHashMap.newKeySet();

    public void register(ObjectOutputStream output) {
        if (output != null) {
            clients.add(output);
        }
    }

    public void unregister(ObjectOutputStream output) {
        if (output != null) {
            clients.remove(output);
        }
    }

    public void broadcast(Object message) {
        for (ObjectOutputStream output : clients) {
            try {
                synchronized (output) {
                    output.writeObject(message);
                    output.flush();
                    output.reset();
                }

            } catch (IOException exception) {
                unregister(output);

                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public int getClientCount() {
        return clients.size();
    }
}