package com.hasta.backend;

import com.hasta.backend.user.model.Gender;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import com.hasta.backend.user.service.UserService;
import com.hasta.backend.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserCreditIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository; // Iniettato per creare l'utente isolato

    @Test
    @Commit
    void testUserCreditFlow() {
        // Generiamo un utente dinamico per evitare di accumulare credito su utenti reali tra un test e l'altro
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        User testUser = new User();
        testUser.setUsername("user_test_" + uniqueId);
        testUser.setEmail("user_test_" + uniqueId + "@test.com");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setRole("USER");
        testUser.setGender(Gender.MALE);
        testUser.setBalance(BigDecimal.ZERO); // Parte esplicitamente da 0.00

        testUser = userRepository.save(testUser);
        Long userId = testUser.getId();

        // 1. Saldo iniziale (sarà 0.00)
        BigDecimal initialBalance = userService.getBalance(userId);
        System.out.println("--- Saldo iniziale dell'utente di test: " + initialBalance);

        // 2. Test Ricarica: Aggiungiamo 50.00€
        BigDecimal amountToCharge = new BigDecimal("50.00");
        User userAfterCharge = userService.addCredit(userId, amountToCharge);

        assertEquals(initialBalance.add(amountToCharge), userAfterCharge.getBalance());
        System.out.println("--- Saldo dopo ricarica (+50€): " + userAfterCharge.getBalance());

        // 3. Test Prelievo: Sottraiamo 20.00€
        BigDecimal amountToDeduct = new BigDecimal("20.00");
        User userAfterDeduction = userService.deductCredit(userId, amountToDeduct);

        BigDecimal expectedBalance = initialBalance.add(amountToCharge).subtract(amountToDeduct);
        assertEquals(expectedBalance, userAfterDeduction.getBalance());
        System.out.println("--- Saldo dopo prelievo (-20€): " + userAfterDeduction.getBalance());

        // 4. Test Errore: Proviamo a prelevare una cifra enorme per far scattare il blocco (Attualmente ha 30€)
        BigDecimal crazyAmount = new BigDecimal("1000.00");

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            userService.deductCredit(userId, crazyAmount);
        });

        // Verifichiamo che il codice d'errore sia quello corretto della Enum appena creata
        assertEquals("user.insufficient-credit", exception.getCode());
        assertEquals(400, exception.getHttpStatusCode());
        System.out.println("--- Blocco credito insufficiente funzionante! Codice errore: " + exception.getCode());
    }
}