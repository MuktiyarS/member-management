package com.surest.member.repository;

import com.surest.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("should save and retrieve a member successfully")
    void testSaveAndFindMember() {
        // given

        // when
        Member saved = memberRepository.save(createSampleMember());

        // then
        assertThat(saved.getId()).isNotNull();


        Member found = memberRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("should return true if email exists")
    void testExistsByEmail() {
        // given
        Member member = Member.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1992, 5, 12))
                .email("jane.smith@example.com")
                .build();

        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail("jane.smith@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should return false if email does not exist")
    void testExistsByEmailFalse() {
        boolean exists = memberRepository.existsByEmail("not.exists@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should throw error on duplicate email")
    void testDuplicateEmail() {
        Member member1 = Member.builder()
                .firstName("Alice")
                .lastName("Brown")
                .dateOfBirth(LocalDate.of(1991, 7, 15))
                .email("alice@example.com")
                .build();

        Member member2 = Member.builder()
                .firstName("Bob")
                .lastName("Green")
                .dateOfBirth(LocalDate.of(1989, 3, 10))
                .email("alice@example.com")
                .build();

        memberRepository.save(member1);

        // saving second member with same email should throw
        org.junit.jupiter.api.Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> memberRepository.saveAndFlush(member2)
        );
    }

    private Member createSampleMember() {
        return Member.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .build();
    }

    @Test
    @DisplayName("READ - should find a member by ID")
    void testReadMember() {
        // given
        Member saved = memberRepository.save(createSampleMember());

        // when
        Optional<Member> found = memberRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("UPDATE - should update an existing member")
    void testUpdateMember() {
        // given
        Member saved = memberRepository.save(createSampleMember());

        // when
        saved.setFirstName("Jane");
        saved.setLastName("Smith");
        Member updated = memberRepository.saveAndFlush(saved);

        // then
        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("DELETE - should delete a member by ID")
    void testDeleteMember() {
        // given
        Member saved = memberRepository.save(createSampleMember());

        // when
        memberRepository.deleteById(saved.getId());

        // then
        Optional<Member> found = memberRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

}