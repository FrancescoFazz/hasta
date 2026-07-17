package com.hasta.backend.auction.service;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.repository.AuctionRepository;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.bid.model.Bid;
import com.hasta.backend.bid.repository.BidRepository;
import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.AuctionException;
import com.hasta.backend.exception.enums.UserException;
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
    private final BidRepository bidRepository;

    @Transactional
    public Auction createAuction(CreateAuctionRequest request) {
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
    public Auction findById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AuctionException.NOT_FOUND));
    }

    @Transactional
    public void closeAuction(Long id) { // Rimosso winnerId e finalPrice dai parametri
        Auction auction = auctionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApplicationException(AuctionException.NOT_FOUND));

        if (auction.isSold() || auction.getFinalPrice() != null) {
            throw new ApplicationException(AuctionException.ALREADY_CLOSED);
        }

        if (auction.getEndTime().isAfter(Instant.now())) {
            auction.setEndTime(Instant.now());
        }

        if (auction.getWinner() != null) {
            auction.setSold(true);
            auction.setFinalPrice(auction.getCurrentPrice());
            User seller = auction.getSeller();
            User sellerLocked = userRepository.findByIdForUpdate(seller.getId())
                    .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));

            sellerLocked.setBalance(sellerLocked.getBalance().add(auction.getFinalPrice()));
            userRepository.save(sellerLocked);
        } else {
            auction.setSold(false);
            auction.setFinalPrice(BigDecimal.ZERO);
        }

        auctionRepository.save(auction);
    }

    @Transactional
    public void placeBid(Long auctionId, Long userId, BigDecimal bidAmount) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ApplicationException(AuctionException.NOT_FOUND));

        if (auction.isSold() || (auction.getEndTime() != null && Instant.now().isAfter(auction.getEndTime()))) {
            throw new ApplicationException(AuctionException.ALREADY_CLOSED);
        }

        if (auction.getWinner() != null && auction.getWinner().getId().equals(userId)) {
            throw new ApplicationException(AuctionException.ALREADY_HIGHEST_BIDDER);
        }

        BigDecimal minimumPriceRequired = (auction.getCurrentPrice() != null && auction.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0)
                ? auction.getCurrentPrice()
                : auction.getStartingPrice();

        if (bidAmount.compareTo(minimumPriceRequired) <= 0) {
            throw new ApplicationException(AuctionException.BID_TOO_LOW);
        }

        User newUser = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));

        if (newUser.getBalance().compareTo(bidAmount) < 0) {
            throw new ApplicationException(UserException.INSUFFICIENT_CREDIT);
        }

        User previousWinner = auction.getWinner();
        if (previousWinner != null) {
            User prevUserLocked = userRepository.findByIdForUpdate(previousWinner.getId()).get();
            prevUserLocked.setBalance(prevUserLocked.getBalance().add(auction.getCurrentPrice()));
            userRepository.save(prevUserLocked);
        }

        newUser.setBalance(newUser.getBalance().subtract(bidAmount));
        userRepository.save(newUser);

        auction.setCurrentPrice(bidAmount);
        auction.setWinner(newUser);
        auctionRepository.save(auction);

        Bid bid = new Bid();
        bid.setAmount(bidAmount);
        bid.setUser(newUser);
        bid.setAuction(auction);
        bidRepository.save(bid);
    }
    @Transactional
    public void cancelAuction(Long auctionId, Long sellerId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ApplicationException(AuctionException.NOT_FOUND));

        if (!auction.getSeller().getId().equals(sellerId)) {
            throw new ApplicationException(AuctionException.NOT_OWNER);
        }

        if (auction.isSold() || auction.getFinalPrice() != null
                || (auction.getEndTime() != null && Instant.now().isAfter(auction.getEndTime()))) {
            throw new ApplicationException(AuctionException.ALREADY_CLOSED);
        }

        User currentWinner = auction.getWinner();
        if (currentWinner != null) {
            User winnerLocked = userRepository.findByIdForUpdate(currentWinner.getId())
                    .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
            winnerLocked.setBalance(winnerLocked.getBalance().add(auction.getCurrentPrice()));
            userRepository.save(winnerLocked);

            auction.setWinner(null);
            auction.setCurrentPrice(auction.getStartingPrice());
        }

        auction.setEndTime(Instant.now());
        auction.setSold(false);
        auction.setFinalPrice(BigDecimal.ZERO);

        auctionRepository.save(auction);
    }
}
