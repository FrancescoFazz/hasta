package com.hasta.backend;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.auction.service.AuctionService;
import com.hasta.backend.product.model.Categories;
import com.hasta.backend.product.model.CreateProductRequest;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.service.ProductService;
import com.hasta.backend.user.model.Gender;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository; // Iniettato per creare l'acquirente di test

    @Test
    @Commit
    @Rollback(false)
    void testCompleteProductAndAuctionFlow() {
        // ID di Luca Rossi (già presente sul DB) -> Agirà come VENDITORE
        Long sellerId = 1L;
        BigDecimal initialSellerBalance = userService.getBalance(sellerId);
        System.out.println("--- Credito iniziale venditore: " + initialSellerBalance);

        // Creazione dinamica di un ACQUIRENTE per testare il flusso reale dei saldi
        User buyer = new User();
        buyer.setUsername("buyer_integration_test");
        buyer.setEmail("buyer_integration@test.com");
        buyer.setName("Mario");
        buyer.setSurname("Bianchi");
        buyer.setRole("USER");
        buyer.setGender(Gender.MALE);
        buyer = userRepository.save(buyer);
        Long buyerId = buyer.getId();

        // 1. Ricarichiamo il portafoglio dell'ACQUIRENTE per l'acquisto finale
        userService.addCredit(buyerId, new BigDecimal("500.00"));
        BigDecimal initialBuyerBalance = userService.getBalance(buyerId);
        System.out.println("--- Credito iniziale acquirente: " + initialBuyerBalance);

        // 2. Creazione dinamica del Prodotto prima dell'asta (da parte del venditore)
        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("MacBook Pro M3");
        productRequest.setDescription("Nuovo fiammante, 16GB RAM");
        productRequest.setQuantity(1);
        productRequest.setCategory(Categories.ELETTRONICA);
        productRequest.setSellerId(sellerId);

        Product createdProduct = productService.addProduct(productRequest);
        assertNotNull(createdProduct.getId());
        System.out.println("--- Prodotto creato con successo! ID: " + createdProduct.getId());

        // 3. Configurazione e creazione dell'Asta legata al prodotto appena nato
        CreateAuctionRequest auctionRequest = new CreateAuctionRequest();
        auctionRequest.setSellerId(sellerId);
        auctionRequest.setProductId(createdProduct.getId());
        auctionRequest.setQuantitySold(1);
        auctionRequest.setStartingPrice(new BigDecimal("100.00"));
        auctionRequest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        Auction createdAuction = auctionService.addAuction(auctionRequest);
        assertNotNull(createdAuction.getId());
        assertEquals(new BigDecimal("100.00"), createdAuction.getCurrentPrice());
        assertFalse(createdAuction.isSold());
        System.out.println("--- Asta creata per il prodotto! ID Asta: " + createdAuction.getId());

        // 4. L'acquirente piazza un'offerta valida a 300€ (Sposta lo stato interno dell'asta)
        BigDecimal finalPrice = new BigDecimal("300.00");
        auctionService.placeBid(createdAuction.getId(), buyerId, finalPrice);
        System.out.println("--- Offerta di 300.00€ piazzata dall'acquirente.");

        // 4b. Chiusura dell'asta (Senza parametri extra, processa lo stato corrente)
        auctionService.closeAuction(createdAuction.getId());

        // 5. Verifiche finali sul Database dell'Asta chiusa
        Auction closedAuction = auctionService.findById(createdAuction.getId());

        assertTrue(closedAuction.isSold());
        assertEquals(finalPrice, closedAuction.getFinalPrice());
        assertEquals(buyerId, closedAuction.getWinner().getId());
        System.out.println("--- Asta chiusa correttamente.");

        // 6. Verifica dei bilanci: credito scalato all'acquirente e accreditato al venditore
        BigDecimal expectedBuyerBalance = initialBuyerBalance.subtract(finalPrice);
        BigDecimal finalBuyerBalance = userService.getBalance(buyerId);
        assertEquals(expectedBuyerBalance, finalBuyerBalance);

        BigDecimal expectedSellerBalance = initialSellerBalance.add(finalPrice);
        BigDecimal finalSellerBalance = userService.getBalance(sellerId);
        assertEquals(expectedSellerBalance, finalSellerBalance);

        System.out.println("--- Credito finale acquirente: " + finalBuyerBalance);
        System.out.println("--- Credito finale venditore (ricevuto pagamento): " + finalSellerBalance);
    }

    @Test
    void testCreateAuctionWithPastDateShouldFail() {
        CreateAuctionRequest invalidRequest = new CreateAuctionRequest();
        invalidRequest.setSellerId(1L);
        invalidRequest.setProductId(1L);
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