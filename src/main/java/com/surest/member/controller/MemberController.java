package com.surest.member.controller;

import com.surest.member.dto.MemberRequest;
import com.surest.member.dto.MemberResponse;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // READ — accessible to both USER and ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort) {
        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));
        return ResponseEntity.ok(memberService.getAllMembers(firstName, lastName, pageable));
    }

    // READ one — accessible to both USER and ADMIN
    @GetMapping("/{id}")
    @Cacheable(value = "member", key = "#id")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable UUID id) throws BusinessServiceException {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    // CREATE — only ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest memberRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(memberRequest));
    }

    // UPDATE — only ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable UUID id, @Valid @RequestBody MemberRequest memberRequest) throws BusinessServiceException {
        return ResponseEntity.ok(memberService.updateMember(id, memberRequest));
    }

    // DELETE — only ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) throws BusinessServiceException {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
