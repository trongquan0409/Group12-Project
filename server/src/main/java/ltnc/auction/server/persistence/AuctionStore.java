package ltnc.auction.server.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ltnc.auction.common.model.Auction;

/** Serialization and file I/O repository for auction data. */
public final class AuctionStore {
    private final Path file;

    public AuctionStore(Path file) {
        this.file = file;
    }

    public synchronized List<Auction> load() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            Object data = input.readObject();
            if (data instanceof List<?> savedAuctions) {
                List<Auction> auctions = new ArrayList<>();
                for (Object entry : savedAuctions) {
                    if (entry instanceof Auction auction) {
                        auctions.add(auction);
                    }
                }
                return auctions;
            }
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException exception) {
            System.err.println("Không đọc được dữ liệu: " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized void save(Collection<Auction> auctions) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(file))) {
                output.writeObject(new ArrayList<>(auctions));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Không lưu được dữ liệu đấu giá", exception);
        }
    }
}
