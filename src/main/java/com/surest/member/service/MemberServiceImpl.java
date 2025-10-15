package com.surest.member.service;

import com.surest.member.dto.MemberRequest;
import com.surest.member.dto.MemberResponse;
import com.surest.member.entity.Member;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {


    private MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(String firstName, String lastName, Pageable pageable) {
        Specification<Member> spec = Specification.allOf();

        if (firstName != null && !firstName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null && !lastName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }

        return memberRepository.findAll(spec, pageable)
                .map(Member::toResponse);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(UUID id) throws BusinessServiceException {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND));
        return member.toResponse();
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request) {

        return memberRepository.save(request.toEntity()).toResponse();
    }

    @Transactional
    public MemberResponse updateMember(UUID id, MemberRequest request) throws BusinessServiceException {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setUpdatedAt(LocalDateTime.now());

        return memberRepository.save(member).toResponse();
    }

    @Transactional
    public void deleteMember(UUID id) throws BusinessServiceException {
        if (!memberRepository.existsById(id)) {
            throw new BusinessServiceException("Member not found", HttpStatus.NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

}
