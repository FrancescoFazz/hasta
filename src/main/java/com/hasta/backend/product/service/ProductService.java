package com.hasta.backend.product.service;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.repository.AuctionRepository;
import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.ProductException;
import com.hasta.backend.exception.enums.UserException;
import com.hasta.backend.product.model.Categories;
import com.hasta.backend.product.model.CreateProductRequest;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.purchase.model.Purchase;
import com.hasta.backend.purchase.repository.PurchaseRepository;
import com.hasta.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.hasta.backend.user.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final AuctionRepository auctionRepository;

    @Transactional
    public Product createProduct(CreateProductRequest request){

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));

        Product p = new Product();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setQuantity(request.getQuantity());
        p.setPrice(request.getPrice());
        p.setCategory(request.getCategory());
        p.setSeller(seller);
        productRepository.save(p);
        return p;
    }

    @Transactional(readOnly = true)
    public Optional<Product> getById(Long id){
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getAll(){
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> getBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Product> getByCategory(String category) {

        return productRepository.findByCategory(Categories.valueOf(category.toUpperCase()));
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailable() {
        return productRepository.findByQuantityGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Product update(Long id, Product updated) {
        Optional<Product> optional = getById(id);
        if(optional.isEmpty()) {
            throw new ApplicationException(ProductException.ALREADY_EXISTS);
        }
        Product existing = optional.get();
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setQuantity(updated.getQuantity());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Purchase buyNow(Long productId, Long buyerId) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ApplicationException(ProductException.NOT_FOUND));

        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            throw new ApplicationException(ProductException.NOT_AVAILABLE);
        }

        Long sellerId = product.getSeller().getId();
        if (sellerId.equals(buyerId)) {
            throw new ApplicationException(ProductException.CANNOT_BUY_OWN_PRODUCT);
        }

        Auction activeAuction = auctionRepository.findActiveAuctionByProductIdForUpdate(productId)
                .orElse(null);

        if (activeAuction != null && product.getPrice().compareTo(activeAuction.getStartingPrice()) <= 0) {
            throw new ApplicationException(ProductException.PRICE_TOO_LOW_FOR_AUCTION);
        }

        Long previousWinnerId = (activeAuction != null && activeAuction.getWinner() != null)
                ? activeAuction.getWinner().getId()
                : null;

        SortedSet<Long> userIdsToLock = new TreeSet<>(List.of(buyerId, sellerId));
        if (previousWinnerId != null) {
            userIdsToLock.add(previousWinnerId);
        }

        Map<Long, User> lockedUsers = new HashMap<>();
        for (Long id : userIdsToLock) {
            User u = userRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
            lockedUsers.put(id, u);
        }

        User buyer = lockedUsers.get(buyerId);
        User seller = lockedUsers.get(sellerId);

        BigDecimal price = product.getPrice();

        if (buyer.getBalance().compareTo(price) < 0) {
            throw new ApplicationException(UserException.INSUFFICIENT_CREDIT);
        }

        if (activeAuction != null) {
            if (previousWinnerId != null) {
                User previousWinner = lockedUsers.get(previousWinnerId);
                previousWinner.setBalance(previousWinner.getBalance().add(activeAuction.getCurrentPrice()));
                userRepository.save(previousWinner);
            }
            activeAuction.setWinner(null);
            activeAuction.setEndTime(Instant.now());
            auctionRepository.save(activeAuction);
        }

        int purchasedQuantity = product.getQuantity();

        buyer.setBalance(buyer.getBalance().subtract(price));
        userRepository.save(buyer);

        seller.setBalance(seller.getBalance().add(price));
        userRepository.save(seller);

        product.setQuantity(0);
        productRepository.save(product);

        Purchase purchase = new Purchase();
        purchase.setProduct(product);
        purchase.setBuyer(buyer);
        purchase.setPrice(price);
        purchase.setQuantity(purchasedQuantity);

        return purchaseRepository.save(purchase);
    }

}