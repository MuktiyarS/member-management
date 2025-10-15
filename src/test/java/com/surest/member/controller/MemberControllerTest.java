package com.surest.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.member.dto.MemberRequest;
import com.surest.member.dto.MemberResponse;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;


    private MemberRequest validRequest;
    private MemberResponse validResponse;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        validRequest = MemberRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("jon@Example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        validResponse = MemberResponse.builder()
                .id(memberId)
                .firstName("John")
                .lastName("Doe")
                .email("jon@Example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------- READ (List) --------------------

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllMembers_ShouldReturnPage_WhenUserRole() throws Exception {
        Page<MemberResponse> page = new PageImpl<>(List.of(validResponse));
        when(memberService.getAllMembers(any(), any(), any()))
                .thenReturn(page);

        ResultActions perform = mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "lastName,asc"));
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllMembers_ShouldReturnPagedMembers_WhenAdminUser() throws Exception {
        // given
        MemberResponse member1 = new MemberResponse(UUID.randomUUID(), "John", "Doe", "john@example.com",
                LocalDate.of(1990, 1, 1), LocalDateTime.now(), LocalDateTime.now());
        MemberResponse member2 = new MemberResponse(UUID.randomUUID(), "Jane", "Smith", "jane@example.com",
                LocalDate.of(1985, 5, 15), LocalDateTime.now(), LocalDateTime.now());
        Page<MemberResponse> pageResponse =
                new PageImpl<>(List.of(member1, member2));

        when(memberService.getAllMembers(any(), any(), any()))
                .thenReturn(pageResponse);

        // when + then
        mockMvc.perform(get("/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[1].firstName").value("Jane"));
    }

    @Test
    void getAllMembers_ShouldReturn403_WhenNoAuth() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isForbidden());
    }

    // -------------------- READ (By ID) --------------------

    @Test
    @WithMockUser(roles = {"USER"})
    void getMemberById_ShouldReturnMember_WhenExists() throws Exception {
        when(memberService.getMemberById(memberId)).thenReturn(validResponse);

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jon@Example.com"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getMemberById_ShouldReturn404_WhenNotFound() throws Exception {
        when(memberService.getMemberById(any(UUID.class)))
                .thenThrow(new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND));

        ResultActions perform = mockMvc.perform(get("/members/{id}", memberId));
        perform.andExpect(status().isNotFound());
    }

    // -------------------- CREATE --------------------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createMember_ShouldReturn201_WhenValidRequest() throws Exception {
        when(memberService.createMember(any(MemberRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createMember_ShouldReturn400_WhenInvalidRequest() throws Exception {
        MemberRequest invalid = MemberRequest.builder()
                .firstName("")
                .lastName("")
                .email("invalid-email")
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createMember_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    // -------------------- UPDATE --------------------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateMember_ShouldReturn200_WhenValidRequest() throws Exception {
        when(memberService.updateMember(eq(memberId), any(MemberRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(put("/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateMember_ShouldReturn404_WhenMemberNotFound() throws Exception {
        when(memberService.updateMember(any(UUID.class), any(MemberRequest.class)))
                .thenThrow(new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateMember_ShouldReturn400_WhenUserRole() throws Exception {
        ResultActions perform = mockMvc.perform(put("/members/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));
        perform.andExpect(status().isBadRequest());
        perform.andExpect(jsonPath("$.message").value("Access Denied"));

    }

    // -------------------- DELETE --------------------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteMember_ShouldReturn204_WhenSuccess() throws Exception {
        doNothing().when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/members/{id}", memberId))
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).deleteMember(memberId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteMember_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND))
                .when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/members/{id}", memberId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteMember_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(delete("/members/{id}", memberId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }
}