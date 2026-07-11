package com.hasta.backend;

import com.hasta.backend.product.model.Categories;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.product.service.ProductService;
import com.hasta.backend.purchase.repository.PurchaseRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProductServiceConcurrencyTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @BeforeEach
    void setUp() {
        try {
            List<User> localUsers = userRepository.findAll();
            for(User user : localUsers) {
                var kcUsers = keycloak.realm(realm).users().searchByUsername(user.getUsername(), true);
                for(var kcUser : kcUsers) {
                    keycloak.realm(realm).users().get(kcUser.getId()).remove();
                }
            }
        } catch(Exception e) {
            System.err.println("Impossibile ripulire alcuni utenti su Keycloak");
        }

        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testConcurrentBuyNow() throws InterruptedException {
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
        product.setDescription("Console usata, come nuova");
        product.setCategory(Categories.ELETTRONICA);
        product.setQuantity(1);
        product.setPrice(new BigDecimal("300.00"));
        product.setSeller(seller);
        productRepository.save(product);

        // creazione di 4 acquirenti con budget sufficiente
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

        int numberOfThreads = 4; //configurazione multithreading
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(User buyer : buyers) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    productService.buyNow(product.getId(), buyer.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();
        executorService.shutdown();

        //verifica risultati
        assertEquals(1, successCount.get(), "Solo un acquirente deve riuscire a completare l'acquisto");
        assertEquals(3, failureCount.get(), "Gli altri 3 devono fallire perche' il prodotto risulta gia' esaurito");

        //verifica sul database
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(0, updatedProduct.getQuantity(), "La quantita' deve essere azzerata dopo l'acquisto");
        assertEquals(1, purchaseRepository.findAll().size(), "Deve esistere un solo record di acquisto");

        User updatedSeller = userRepository.findById(seller.getId()).orElseThrow();
        assertEquals(new BigDecimal("300.00"), updatedSeller.getBalance(), "Il seller deve ricevere esattamente il prezzo del prodotto");
    }

}
