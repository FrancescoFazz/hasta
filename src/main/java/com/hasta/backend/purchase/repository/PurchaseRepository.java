package com.hasta.backend.purchase.repository;

import com.hasta.backend.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Purchase> findByProductSellerIdOrderByCreatedAtDesc(Long sellerId);
}