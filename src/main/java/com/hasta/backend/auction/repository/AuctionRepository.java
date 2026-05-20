package com.hasta.backend.auction.repository;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findBySoldFalse();

    List<Auction> findBySeller(User seller);

    List<Auction> findByWinner(User winner);

}
