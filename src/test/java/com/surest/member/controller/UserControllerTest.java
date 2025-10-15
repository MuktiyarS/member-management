package com.surest.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.member.dto.UserRequest;
import com.surest.member.dto.UserResponse;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_getAllUsers_ShouldReturnList() throws Exception {

        List<UserResponse> users = List.of(new UserResponse(UUID.randomUUID(), "user1", "user1@example.com", "Password", LocalDate.now(), Set.of("ROLE_USER")));
        Mockito.when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(users.size()))
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void test_getAllUsers_ShouldReturnEmptyList() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void test_getUserById_ShouldReturnUser_WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse user = new UserResponse(id, "user1", "user1@example.com", "Password", LocalDate.now(), Set.of("ROLE_USER"));
        Mockito.when(userService.getUserById(id)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void test_getUserById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userService.getUserById(id)).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_createUser_ShouldReturnUser_WhenValid() throws Exception {
        UserRequest request = new UserRequest("user1", "user1@example.com", "Password", LocalDate.now().minusYears(2), Set.of("ROLE_USER"));
        UserResponse response = new UserResponse(UUID.randomUUID(), "user1", "user1@example.com", "Password", LocalDate.now(), Set.of("ROLE_USER"));

        Mockito.when(userService.createUser(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void test_createUser_ShouldReturn400_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"invalidemail\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.details.username").value("User name is required"))
                .andExpect(jsonPath("$.details.email").value("Invalid email format"));
    }

    @Test
    void test_createUser_ShouldReturnConflict_WhenUserExists() throws Exception {
        UserRequest request = new UserRequest("user1", "user1@example.com", "Password", LocalDate.now().minusYears(2), Set.of("ROLE_USER"));

        Mockito.when(userService.createUser(Mockito.any()))
                .thenThrow(new BusinessServiceException("User already exists", HttpStatus.CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void test_updateUser_ShouldReturnUser_WhenValid() throws Exception {
        UserRequest request = new UserRequest("user1", "user1@example.com", "Password", LocalDate.now().minusYears(2), Set.of("ROLE_USER"));

        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(id, "user1", "user1@example.com", "Password", LocalDate.now(), Set.of("ROLE_USER"));

        Mockito.when(userService.updateUser(Mockito.eq(id), Mockito.any())).thenReturn(response);

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void test_updateUser_ShouldReturn400_WhenValidationFails() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"invalidemail\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void test_updateUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        UserRequest request = new UserRequest("user1", "user1@example.com", "Password", LocalDate.now().minusYears(2), Set.of("ROLE_USER"));

        UUID id = UUID.randomUUID();
        Mockito.when(userService.updateUser(Mockito.eq(id), Mockito.any()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_deleteUser_ShouldReturn204_WhenDeleted() throws Exception {
        UUID id = UUID.randomUUID();

        // Assume void method, no exception thrown
        Mockito.doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_deleteUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new UsernameNotFoundException("User not found")).when(userService).deleteUser(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNotFound());
    }
    @Test
    void createUser_ShouldReturn400_WhenUsernameMissing() throws Exception {
        String invalidRequest = """
            {
                "email": "user@example.com",
                "password": "password123",
                "roles": ["USER"]
            }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").value("User name is required"));
    }

    @Test
    void createUser_ShouldReturn400_WhenEmailInvalid() throws Exception {
        String invalidRequest = """
            {
                "username": "john_doe",
                "email": "not-an-email",
                "password": "password123",
                "roles": ["USER"]
            }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").value("Invalid email format"));
    }

    @Test
    void createUser_ShouldReturn400_WhenRolesMissing() throws Exception {
        String invalidRequest = """
            {
                "username": "john_doe",
                "email": "john@example.com",
                "password": "password123"
            }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.roles").value("At least one role is required"));
    }

}