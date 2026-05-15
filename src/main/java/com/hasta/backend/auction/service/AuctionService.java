package com.hasta.backend.auction.service;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.repository.AuctionRepository;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Auction addAuction(CreateAuctionRequest request) {

        User seller = userRepository.getReferenceById(request.getSellerId());
        Product product = productRepository.getReferenceById(request.getProductId());

        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setProduct(product);
        auction.setQuantitySold(request.getQuantitySold());
        auction.setStartingPrice(request.getStartingPrice());
        auctionRepository.save(auction);

        return auction;
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
    public void closeAuction(Long id, Long winnerId, BigDecimal finalPrice) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("auction not found"));

        User winner = userRepository.getReferenceById(winnerId);

        auction.setWinner(winner);
        auction.setFinalPrice(finalPrice);
        auction.setEndTime(Instant.now());
        auction.setSold(true);
        auctionRepository.save(auction);
    }
}
