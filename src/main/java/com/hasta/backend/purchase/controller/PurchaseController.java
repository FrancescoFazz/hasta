package com.hasta.backend.purchase.controller;

import com.hasta.backend.purchase.model.Purchase;
import com.hasta.backend.purchase.repository.PurchaseRepository;
import com.hasta.backend.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final UserService userService;

    public PurchaseController(PurchaseRepository purchaseRepository, UserService userService) {
        this.purchaseRepository = purchaseRepository;
        this.userService = userService;
    }

    @GetMapping("/me")
    public List<Purchase> getMyPurchases(@AuthenticationPrincipal Jwt jwt) {
        Long userId = userService.findByUsername(jwt.getClaimAsString("preferred_username")).getId();
        return purchaseRepository.findByBuyerIdOrderByCreatedAtDesc(userId);
    }
}