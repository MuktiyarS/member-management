package com.surest.member.repository;

import com.surest.member.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    private Role createRole(String name) {
        return Role.builder()
                .name(name)
                .build();
    }

    @Test
    @DisplayName("CREATE - should save and retrieve a role successfully")
    void testSaveRole() {
        Role role = createRole("ADMIN");

        Role saved = roleRepository.saveAndFlush(role);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("READ - should find role by name")
    void testFindByName() {
        roleRepository.saveAndFlush(createRole("USER"));

        Optional<Role> found = roleRepository.findByName("USER");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("USER");
    }

    @Test
    @DisplayName("READ - should return empty Optional when role not found by name")
    void testFindByNameNotFound() {
        Optional<Role> found = roleRepository.findByName("MANAGER");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("READ - should find multiple roles by names")
    void testFindByNameIn() {
        roleRepository.saveAllAndFlush(Set.of(
                createRole("USER"),
                createRole("ADMIN"),
                createRole("MANAGER")
        ));

        Set<Role> found = roleRepository.findByNameIn(Set.of("USER", "ADMIN"));

        assertThat(found).hasSize(2);
        assertThat(found)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("READ - should return empty set when none of the names exist")
    void testFindByNameInNotFound() {
        Set<Role> found = roleRepository.findByNameIn(Set.of("GUEST", "TEST"));

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("DELETE - should delete a role")
    void testDeleteRole() {
        Role saved = roleRepository.saveAndFlush(createRole("TEMP"));

        roleRepository.delete(saved);

        Optional<Role> found = roleRepository.findByName("TEMP");
        assertThat(found).isEmpty();
    }

}