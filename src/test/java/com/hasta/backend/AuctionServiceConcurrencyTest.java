package com.hasta.backend;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.repository.AuctionRepository;
import com.hasta.backend.auction.service.AuctionService;
import com.hasta.backend.bid.repository.BidRepository;
import com.hasta.backend.product.model.Categories;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.user.model.Gender;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AuctionServiceConcurrencyTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @BeforeEach
    void setUp() {
        try {
            List<User> localUsers = userRepository.findAll();

            for (User user : localUsers) {
                var kcUsers = keycloak.realm(realm)
                        .users()
                        .searchByUsername(user.getUsername(), true);

                for (var kcUser : kcUsers) {
                    keycloak.realm(realm).users().get(kcUser.getId()).remove();
                }
            }
        } catch (Exception e) {
            System.err.println("Nota: Impossibile ripulire alcuni utenti su Keycloak: " + e.getMessage());
        }

        bidRepository.deleteAll();
        auctionRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testConcurrentBidding() throws InterruptedException {
        // 1. Setup dei dati nel Database di Test
        User seller = new User();
        seller.setUsername("seller");
        seller.setEmail("seller@test.com");
        seller.setBalance(BigDecimal.ZERO);
        seller.setGender(Gender.MALE);
        seller.setName("NomeSeller");
        seller.setSurname("CognomeSeller");
        seller.setRole("USER");
        userRepository.save(seller);

        Product product = new Product();
        product.setName("PlayStation 5");
        product.setCategory(Categories.ELETTRONICA);
        product.setQuantity(1);
        product.setSeller(seller);
        productRepository.save(product);

        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setProduct(product);
        auction.setQuantitySold(1);
        auction.setStartingPrice(new BigDecimal("100.00"));
        auction.setCurrentPrice(new BigDecimal("100.00"));
        auction.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        auction.setSold(false);
        auctionRepository.save(auction);

        // Creazione di 4 acquirenti con budget sufficiente
        List<User> buyers = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            User buyer = new User();
            buyer.setUsername("buyer" + i);
            buyer.setEmail("buyer" + i + "@test.com");
            buyer.setBalance(new BigDecimal("500.00"));
            buyer.setGender(Gender.FEMALE);
            buyer.setName("Nome" + i);
            buyer.setSurname("Cognome" + i);
            buyer.setRole("USER");
            buyers.add(userRepository.save(buyer));
        }

        // 2. Configurazione dell'ambiente multithreading
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1); // Mantiene i thread fermi sulla linea di partenza
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads); // Aspetta che tutti finiscano

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        BigDecimal bidAmount = new BigDecimal("150.00");

        // 3. Esecuzione simultanea delle offerte
        for (User buyer : buyers) {
            executorService.submit(() -> {
                try {
                    latch.await(); // Rimane in attesa del via libera collettivo
                    auctionService.placeBid(auction.getId(), buyer.getId(), bidAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Spara il colpo di inizio: tutti i thread partono insieme
        doneLatch.await(); // Aspetta la fine di tutte le transazioni
        executorService.shutdown();

        // 4. Verifiche dei risultati (Asserzioni)
        assertEquals(1, successCount.get(), "Solo un utente deve essere riuscito a completare l'offerta");
        assertEquals(3, failureCount.get(), "Gli altri 3 utenti devono essere falliti a causa del prezzo già aggiornato o del lock");

        // Verifica finale sul database
        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        assertEquals(bidAmount, updatedAuction.getCurrentPrice(), "Il prezzo finale deve essere esattamente 150.00");
    }
}