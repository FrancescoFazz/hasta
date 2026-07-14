package com.hasta.backend.user.service;

import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.UserException;
import com.hasta.backend.user.model.CreateUserRequest;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new ApplicationException(UserException.ALREADY_EXISTS);
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApplicationException(UserException.ALREADY_EXISTS);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getUsername());
        kcUser.setEmail(request.getEmail());
        kcUser.setFirstName(request.getName());
        kcUser.setLastName(request.getSurname());
        kcUser.setEnabled(true);
        kcUser.setCredentials(List.of(credential));
        kcUser.setEmailVerified(true);
        kcUser.setRequiredActions(List.of());

        Response response = keycloak.realm(realm).users().create(kcUser);

        if (response.getStatus() != 201) {
            throw new ApplicationException(UserException.KEYCLOAK_ERROR);
        }

        User u = new User();
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setName(request.getName());
        u.setSurname(request.getSurname());
        u.setGender(request.getGender());
        u.setBalance(BigDecimal.ZERO);
        u.setRole("USER");
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<User> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
    }

    public BigDecimal getBalance(Long userId) {
        User user = getUserById(userId);
        return user.getBalance();
    }

    @Transactional
    public User addCredit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationException(UserException.INVALID_AMOUNT);
        }

        User user = getUserById(userId);
        user.setBalance(user.getBalance().add(amount));
        return userRepository.save(user);
    }

    @Transactional
    public User deductCredit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationException(UserException.INVALID_AMOUNT);
        }

        User user = getUserById(userId);

        if (user.getBalance().compareTo(amount) < 0) {
            throw new ApplicationException(UserException.INSUFFICIENT_CREDIT);
        }

        user.setBalance(user.getBalance().subtract(amount));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long userId, String newRole) {
        if (!"ADMIN".equals(newRole) && !"USER".equals(newRole)) {
            throw new ApplicationException(UserException.INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));

        user.setRole(newRole);
        return userRepository.save(user);
    }
}