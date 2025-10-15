package com.surest.member.service;

import com.surest.member.dto.UserRequest;
import com.surest.member.dto.UserResponse;
import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.RoleRepository;
import com.surest.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(User::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.toResponse();
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) throws BusinessServiceException {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new BusinessServiceException("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessServiceException("Email already exists", HttpStatus.CONFLICT);
        }


        // Assign roles
        Set<Role> roles = roleRepository.findByNameIn(userRequest.getRoles());

        return userRepository.save(userRequest.toEntity(passwordEncoder.encode(userRequest.getPassword()), roles)).toResponse();
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest updateUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(updateUser.getEmail());
        user.setDateOfBirth(updateUser.getDateOfBirth());
        // Optional: update password or roles
        return userRepository.save(user).toResponse();
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
