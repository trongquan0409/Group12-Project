package ltnc.auction.server.persistence;
import java.io.*; import java.nio.file.*; import java.util.*; import ltnc.auction.common.model.Auction;
/** Serialization/File I/O repository for this course project. */
public final class AuctionStore { private final Path file; public AuctionStore(Path file){this.file=file;}
 public synchronized List<Auction> load(){if(!Files.exists(file))return new ArrayList<>();try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(file))){Object obj=in.readObject();if(obj instanceof List<?> list){List<Auction> out=new ArrayList<>();for(Object x:list)if(x instanceof Auction a)out.add(a);return out;}return new ArrayList<>();}catch(IOException|ClassNotFoundException e){System.err.println("Không đọc được dữ liệu: "+e.getMessage());return new ArrayList<>();}}
 public synchronized void save(Collection<Auction> auctions){try{Path parent=file.getParent();if(parent!=null)Files.createDirectories(parent);try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(file))){out.writeObject(new ArrayList<>(auctions));}}catch(IOException e){throw new IllegalStateException("Không lưu được dữ liệu đấu giá",e);}}
}
