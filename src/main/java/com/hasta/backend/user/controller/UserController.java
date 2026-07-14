package com.hasta.backend.user.controller;

import com.hasta.backend.user.service.UserService;
import com.hasta.backend.user.model.CreateUserRequest;
import com.hasta.backend.user.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.findByUsername(jwt.getClaimAsString("preferred_username"));
    }

    @GetMapping
    public List<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal getBalance(@PathVariable Long id) {
        return userService.getBalance(id);
    }

    @PostMapping("/{id}/charge")
    public User chargeCredit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return userService.addCredit(id, amount);
    }

    @PutMapping("/{id}/role")
    public User changeUserRole(@PathVariable Long id, @RequestParam String role) {
        return userService.updateUserRole(id, role);
    }

}