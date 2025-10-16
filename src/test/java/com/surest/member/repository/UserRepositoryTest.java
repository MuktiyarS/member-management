package com.surest.member.repository;


import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User createSampleUser() {
        Role userRole = roleRepository.save(
                Role.builder().name("USER").build()
        );

        return User.builder()
                .username("john_doe")
                .passwordHash("hashed_password")
                .email("john@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    @Test
    @DisplayName("CREATE - should save a user successfully with roles")
    void testCreateUser() {
        User user = createSampleUser();

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("john_doe");
        assertThat(saved.getRoles()).hasSize(1);
        assertThat(saved.getRoles().iterator().next().getName()).isEqualTo("USER");
    }

    @Test
    @DisplayName("READ - should find user by username")
    void testFindByUsername() {
        User saved = userRepository.saveAndFlush(createSampleUser());

        Optional<User> found = userRepository.findByUsername("john_doe");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("READ - should find user by email")
    void testFindByEmail() {
        User saved = userRepository.saveAndFlush(createSampleUser());

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("EXISTS - should return true if username exists")
    void testExistsByUsername() {
        userRepository.saveAndFlush(createSampleUser());

        boolean exists = userRepository.existsByUsername("john_doe");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("EXISTS - should return true if email exists")
    void testExistsByEmail() {
        userRepository.saveAndFlush(createSampleUser());

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("UPDATE - should update user's email")
    void testUpdateUser() {
        User saved = userRepository.saveAndFlush(createSampleUser());

        saved.setEmail("new_email@example.com");
        User updated = userRepository.saveAndFlush(saved);

        assertThat(updated.getEmail()).isEqualTo("new_email@example.com");
    }

    @Test
    @DisplayName("DELETE - should delete user by ID")
    void testDeleteUser() {
        User saved = userRepository.saveAndFlush(createSampleUser());

        userRepository.deleteById(saved.getId());

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}