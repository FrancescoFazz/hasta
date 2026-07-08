package com.hasta.backend;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.auction.service.AuctionService;
import com.hasta.backend.product.model.CreateProductRequest;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.service.ProductService;
import com.hasta.backend.user.service.UserService;
import com.hasta.backend.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuctionIntegrationTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Test
    @Commit
    @Rollback(false)
    void testCompleteProductAndAuctionFlow() {
        // ID di Luca Rossi (già presente sul DB)
        Long userId = 1L;

        // 1. Ricarichiamo il portafoglio dell'utente per l'acquisto finale
        userService.addCredit(userId, new BigDecimal("500.00"));
        BigDecimal initialBalance = userService.getBalance(userId);
        System.out.println("--- Credito iniziale utente: " + initialBalance);

        // 2. Creazione dinamica del Prodotto prima dell'asta
        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("MacBook Pro M3");
        productRequest.setDescription("Nuovo fiammante, 16GB RAM");
        productRequest.setQuantity(1);
        productRequest.setCategory("ACCESSORI");
        productRequest.setSellerId(userId);

        Product createdProduct = productService.addProduct(productRequest);
        assertNotNull(createdProduct.getId());
        System.out.println("--- Prodotto creato con successo! ID: " + createdProduct.getId());

        // 3. Configurazione e creazione dell'Asta legata al prodotto appena nato
        CreateAuctionRequest auctionRequest = new CreateAuctionRequest();
        auctionRequest.setSellerId(userId);
        auctionRequest.setProductId(createdProduct.getId()); // Usiamo l'ID dinamico
        auctionRequest.setQuantitySold(1);
        auctionRequest.setStartingPrice(new BigDecimal("100.00"));
        auctionRequest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        Auction createdAuction = auctionService.addAuction(auctionRequest);
        assertNotNull(createdAuction.getId());
        assertEquals(new BigDecimal("100.00"), createdAuction.getCurrentPrice());
        assertFalse(createdAuction.isSold());
        System.out.println("--- Asta creata per il prodotto! ID Asta: " + createdAuction.getId());

        // 4. Chiusura dell'asta (Immaginiamo che l'utente stesso vinca la sua asta a 300€ per il test)
        BigDecimal finalPrice = new BigDecimal("300.00");
        auctionService.closeAuction(createdAuction.getId(), userId, finalPrice);

        // 5. Verifiche finali sul Database dell'Asta chiusa
        Auction closedAuction = auctionService.findById(createdAuction.getId())
                .orElseThrow(() -> new AssertionError("Asta non trovata"));

        assertTrue(closedAuction.isSold());
        assertEquals(finalPrice, closedAuction.getFinalPrice());
        assertEquals(userId, closedAuction.getWinner().getId());
        System.out.println("--- Asta chiusa correttamente.");

        // 6. Verifica che il credito sia stato scalato
        BigDecimal expectedBalance = initialBalance.subtract(finalPrice);
        BigDecimal finalBalance = userService.getBalance(userId);

        assertEquals(expectedBalance, finalBalance);
        System.out.println("--- Credito finale dopo transazione d'asta: " + finalBalance);
    }

    @Test
    void testCreateAuctionWithPastDateShouldFail() {
        CreateAuctionRequest invalidRequest = new CreateAuctionRequest();
        invalidRequest.setSellerId(1L);
        invalidRequest.setProductId(1L); // Questo usa 1 generico solo per verificare l'errore sulla data
        invalidRequest.setQuantitySold(1);
        invalidRequest.setStartingPrice(new BigDecimal("10.00"));
        invalidRequest.setEndTime(Instant.now().minus(5, ChronoUnit.MINUTES));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            auctionService.addAuction(invalidRequest);
        });

        assertEquals("auction.past-end-time", exception.getCode());
        System.out.println("--- Blocco data passata riuscito! Codice catturato: " + exception.getCode());
    }
}