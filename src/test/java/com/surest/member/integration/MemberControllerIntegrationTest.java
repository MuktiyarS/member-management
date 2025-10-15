package com.surest.member.integration;

import com.surest.member.entity.Member;
import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.repository.MemberRepository;
import com.surest.member.repository.RoleRepository;
import com.surest.member.repository.UserRepository;
import com.surest.member.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtUtil jwtTokenProvider;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Setup roles
        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Role userRole = roleRepository.save(Role.builder().name("ROLE_USER").build());

        // Setup users
        User admin = userRepository.save(User.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("admin@example.com")
                .roles(Set.of(adminRole))
                .build());

        User user = userRepository.save(User.builder()
                .username("user")
                .passwordHash(passwordEncoder.encode("user123"))
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .email("user@example.com")
                .roles(Set.of(userRole))
                .build());

        // Generate JWTs
        adminToken = "Bearer " + jwtTokenProvider.generateToken(admin.getUsername(), admin.getRoles().stream().map(Role::getName).toList());
        userToken = "Bearer " + jwtTokenProvider.generateToken(user.getUsername(), user.getRoles().stream().map(Role::getName).toList());

        // Add sample member
        memberRepository.save(Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build());
    }

    @Test
    void testAllowUserToGetMembers() throws Exception {
        mockMvc.perform(get("/api/v1/members")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void testAllowAdminToCreateMember() throws Exception {
        String json = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "email": "alice@example.com",
                    "dateOfBirth": "05/10/1995"
                }
                """;

        mockMvc.perform(post("/api/v1/members")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testRejectUserCreatingMember() throws Exception {
        String json = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "email": "bob@example.com",
                    "dateOfBirth": "03/14/1992"
                }
                """;

        mockMvc.perform(post("/api/v1/members")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    void testAllowUserToGetMembersFirstName() throws Exception {
        mockMvc.perform(get("/api/v1/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc")
                        .param("firstName", "Jo")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void testAllowUserToGetMembersLastName() throws Exception {
        mockMvc.perform(get("/api/v1/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc")
                        .param("lastName", "D")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"));
    }

}
