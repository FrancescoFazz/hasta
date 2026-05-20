package com.hasta.backend.user.service;

import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.UserException;
import com.hasta.backend.user.model.CreateUserRequest;
import com.hasta.backend.user.model.User;
import com.hasta.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new ApplicationException(UserException.ALREADY_EXISTS);
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApplicationException(UserException.ALREADY_EXISTS);

        User u = new User();
        u.setUsername(request.getUsername());
        u.setPassword(request.getPassword());
        u.setEmail(request.getEmail());
        u.setName(request.getName());
        u.setSurname(request.getSurname());
        u.setGender(request.getGender());
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
    public List<User> findAll(@RequestParam int page, @RequestParam int size) {
        return userRepository.getUsersWithPagination(page, size);
    }

    @Transactional(readOnly = true)
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
