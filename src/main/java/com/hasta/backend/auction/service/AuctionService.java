package com.hasta.backend.auction.service;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.repository.AuctionRepository;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.AuctionException;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import com.hasta.backend.user.service.UserService;
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
    private final UserService userService;

    @Transactional
    public Auction addAuction(CreateAuctionRequest request) {
        User seller = userRepository.getReferenceById(request.getSellerId());
        Product product = productRepository.getReferenceById(request.getProductId());

        if (request.getEndTime().isBefore(Instant.now())) {
            throw new ApplicationException(AuctionException.PAST_END_TIME);
        }

        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setProduct(product);
        auction.setQuantitySold(request.getQuantitySold());
        auction.setStartingPrice(request.getStartingPrice());
        auction.setCurrentPrice(request.getStartingPrice());
        auction.setEndTime(request.getEndTime());
        auction.setSold(false);

        return auctionRepository.save(auction);
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
                .orElseThrow(() -> new ApplicationException(AuctionException.NOT_FOUND));

        if (auction.isSold()) {
            throw new ApplicationException(AuctionException.ALREADY_CLOSED);
        }

        userService.deductCredit(winnerId, finalPrice);

        User winner = userRepository.getReferenceById(winnerId);
        auction.setWinner(winner);
        auction.setFinalPrice(finalPrice);

        if (auction.getEndTime().isAfter(Instant.now())) {
            auction.setEndTime(Instant.now());
        }

        auction.setSold(true);
        auctionRepository.save(auction);
    }
}
