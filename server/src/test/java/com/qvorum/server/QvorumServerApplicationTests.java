package com.qvorum.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.qvorum.server.model.User;
import com.qvorum.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starting the application already checks a lot: Flyway runs every migration, and Hibernate
 * (ddl-auto=validate) stops with an error if an entity does not match its table.
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
class QvorumServerApplicationTests {

    @Autowired
    UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional // keeps the database session open so the LAZY relationships can be read
    void seedDataLinksResidentToApartment() {
        User resident = userRepository.findByEmail("resident@qvorum.test").orElseThrow();

        assertThat(resident.getApartment().getNumber()).isEqualTo("1");
        assertThat(resident.getAssociation().getFiscalCode()).isEqualTo("RO12345678");
        assertThat(resident.isMustChangePassword()).isFalse();
    }
}
