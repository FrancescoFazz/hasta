package com.hasta.hasta.auction;

import com.hasta.hasta.product.Product;
import com.hasta.hasta.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
    private final AuctionRepository auctionRepository;
    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Transactional
    public void addAuction(User seller, Product product, int quantitySold, Double startingPrice) {

        if (startingPrice == null || startingPrice <= 0) throw new IllegalArgumentException("startingPrice must be positive");
        if (seller == null) throw new IllegalArgumentException("seller required");
        if (product == null) throw new IllegalArgumentException("product required");

        Auction auction = new Auction();
        auction.setQuantitySold(quantitySold);
        auction.setStartingPrice(startingPrice);
        auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    public List<Auction> findAll() {
        return auctionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id);
    }

    @Transactional
    public void closeAuction(Long id, User winner, Double finalPrice) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("auction not found"));
        auction.setWinner(winner);
        auction.setFinalPrice(finalPrice);
        auction.setEndTime(Instant.now());
        auction.setSold(true);
        auctionRepository.save(auction);
    }
}
