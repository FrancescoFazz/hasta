package com.hasta.backend.auction.repository;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.user.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findBySoldFalse();

    List<Auction> findBySeller(User seller);

    List<Auction> findByWinner(User winner);

    Optional<Auction> findFirstByProduct_IdAndSoldFalseAndEndTimeAfter(Long productId, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.product.id = :productId AND a.sold = false AND a.endTime > CURRENT_TIMESTAMP")
    Optional<Auction> findActiveAuctionByProductIdForUpdate(@Param("productId") Long productId);

}