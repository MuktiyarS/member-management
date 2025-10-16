package com.surest.member.service;

import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // given
        String username = "john";
        Role roleUser = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .passwordHash("hashed_pw")
                .email("john@example.com")
                .roles(Set.of(roleUser))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.getPassword()).isEqualTo("hashed_pw");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void test_loadUserByUsername_ShouldThrow_WhenUserNotFound() {
        // given
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: " + username);

        verify(userRepository).findByUsername(username);
    }

    @Test
    void test_loadUserByUsername_ShouldMapMultipleRoles() {
        // given
        String username = "admin";
        Role roleUser = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .build();
        Role roleAdmin = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_ADMIN")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .passwordHash("securepw")
                .email("admin@example.com")
                .roles(Set.of(roleUser, roleAdmin))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}