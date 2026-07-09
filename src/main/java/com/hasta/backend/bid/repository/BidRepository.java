package com.hasta.backend.bid.repository;


import com.hasta.backend.bid.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);

    List<Bid> findByUserId(Long userId);
}