package com.surest.member.service;

import com.surest.member.dto.MemberRequest;
import com.surest.member.dto.MemberResponse;
import com.surest.member.entity.Member;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MemberServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private MemberRequest request;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        member = Member.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = MemberRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .build();
    }

    // Test: Get all members
    @Test
    void test_getAllMembers_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MemberResponse> result = memberService.getAllMembers(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Test: Get member by ID
    @Test
    void test_getMemberById_ShouldReturnMember_WhenFound() throws BusinessServiceException {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        MemberResponse result = memberService.getMemberById(member.getId());

        assertThat(result.getFirstName()).isEqualTo("John");
        verify(memberRepository).findById(member.getId());
    }

    @Test
    void test_getMemberById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberById(id))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Member not found");
    }

    // Test: Create member
    @Test
    void test_createMember_ShouldSaveAndReturnResponse() throws BusinessServiceException {
        Member saved = request.toEntity();
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        MemberResponse response = memberService.createMember(request);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo(request.getFirstName());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void test_createMember_ShouldReturnError() throws BusinessServiceException {
        Member saved = request.toEntity();
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Email already exists");

        verify(memberRepository, never()).save(any(Member.class));
    }

    // Test: Update member
    @Test
    void test_updateMember_ShouldUpdateAndReturnResponse() throws BusinessServiceException {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponse response = memberService.updateMember(member.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Jane");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void test_updateMember_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMember(id, request))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Member not found");
    }

    // Test: Delete member
    @Test
    void test_deleteMember_ShouldDelete_WhenExists() throws BusinessServiceException {
        UUID id = UUID.randomUUID();
        when(memberRepository.existsById(id)).thenReturn(true);

        memberService.deleteMember(id);

        verify(memberRepository).deleteById(id);
    }

    @Test
    void test_deleteMember_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> memberService.deleteMember(id))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Member not found");

        verify(memberRepository, never()).deleteById(any());
    }

    @Test
    void testReturnAllMembers_whenNoFiltersProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        // given

        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<MemberResponse> result = memberService.getAllMembers(null, null, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFilterByFirstName_whenFirstNameProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        // given

        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<MemberResponse> result = memberService.getAllMembers("Ali", null, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFilterByLastName_whenLastNameProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        // given

        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<MemberResponse> result = memberService.getAllMembers(null, "John", pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFilterByFirstNameAndLastName_whenBothProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        // given

        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<MemberResponse> result = memberService.getAllMembers("Car", "Mill", pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testReturnEmptyPage_whenRepositoryReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        // given
        when(memberRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // when
        Page<MemberResponse> result = memberService.getAllMembers("NonExistent", null, pageable);

        // then
        assertTrue(result.isEmpty());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testIgnoreBlankInputs() {
        Pageable pageable = PageRequest.of(0, 10);
        // given

        Page<Member> page = new PageImpl<>(List.of(member));

        when(memberRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<MemberResponse> result = memberService.getAllMembers("   ", "", pageable);

        // then
        assertEquals(1, result.getTotalElements());
        verify(memberRepository).findAll(any(Specification.class), eq(pageable));
    }


}
