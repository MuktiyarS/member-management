package com.surest.member.service;

import com.surest.member.dto.MemberRequest;
import com.surest.member.dto.MemberResponse;
import com.surest.member.exception.BusinessServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MemberService {
    Page<MemberResponse> getAllMembers(String firstName, String lastName, Pageable pageable);

    MemberResponse getMemberById(UUID id) throws BusinessServiceException;

    MemberResponse createMember(MemberRequest memberRequest) throws BusinessServiceException;

    MemberResponse updateMember(UUID id, MemberRequest updatedMember) throws BusinessServiceException;

    void deleteMember(UUID id) throws BusinessServiceException;

}
