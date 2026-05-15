package com.hasta.hasta.auction;

import com.hasta.hasta.product.Product;
import com.hasta.hasta.product.ProductRepository;
import com.hasta.hasta.user.User;
import com.hasta.hasta.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AuctionService(AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

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
