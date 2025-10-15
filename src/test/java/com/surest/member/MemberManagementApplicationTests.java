package com.surest.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("test")
@SpringBootTest
class MemberManagementApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodShouldStartApplication() {
        assertDoesNotThrow(() -> MemberManagementApplication.main(new String[]{}));
    }

}
