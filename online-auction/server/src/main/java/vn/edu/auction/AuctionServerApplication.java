package vn.edu.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuctionServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuctionServerApplication.class, args);
    }
}
