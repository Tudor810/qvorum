package com.qvorum.server.repository;

import com.qvorum.server.model.Utilizator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * repository: database access through Spring Data JPA. One interface per entity;
 * Spring generates the implementation from the method names.
 */
public interface UtilizatorRepository extends JpaRepository<Utilizator, Long> {

    Optional<Utilizator> findByEmail(String email);
}
