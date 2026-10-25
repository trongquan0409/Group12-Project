package ltnc.auction.common;
import static org.junit.jupiter.api.Assertions.*; import java.math.BigDecimal; import org.junit.jupiter.api.Test; import ltnc.auction.common.factory.ItemFactory;
class ItemFactoryTest {@Test void createsArtItem(){var item=ItemFactory.create("ART","i1","Painting","desc","s1",new BigDecimal("10"));assertEquals("ART",item.getCategory());}@Test void rejectsUnknownCategory(){assertThrows(IllegalArgumentException.class,()->ItemFactory.create("OTHER","i1","n","d","s",BigDecimal.ONE));}}
