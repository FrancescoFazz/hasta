package com.hasta.hasta.auction;

import com.hasta.hasta.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findBySoldFalse();

    List<Auction> findBySeller(User seller);

    List<Auction> findByWinner(User winner);

}
