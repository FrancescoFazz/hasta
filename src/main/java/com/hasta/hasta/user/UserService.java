package com.hasta.hasta.user;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void addUser(String username, String password, String email, String name,
                        String surname, String role, Gender gender) {

        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email required");

        if (userRepository.findByUsername(username).isPresent()) throw new IllegalArgumentException("username already exists");
        if (userRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("email already exists");

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setName(name);
        u.setSurname(surname);
        u.setRole(role);
        u.setGender(gender);
        userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }
}
