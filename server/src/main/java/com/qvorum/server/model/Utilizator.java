package com.qvorum.server.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * model: JPA entities, one class per database table (see db/migration). Never returned directly
 * to clients; services map them to DTOs.
 */
@Entity
@Table(name = "utilizator")
public class Utilizator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;

    private String prenume;

    private String email;

    /** BCrypt hash of the password; the plain password is never stored. */
    private String parolaHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utilizator_rol", joinColumns = @JoinColumn(name = "utilizator_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Set<Rol> roluri = new HashSet<>();

    /** Required by JPA. */
    protected Utilizator() {
    }

    public Utilizator(String nume, String prenume, String email, String parolaHash, Set<Rol> roluri) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.parolaHash = parolaHash;
        this.roluri = new HashSet<>(roluri);
    }

    public Long getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getEmail() {
        return email;
    }

    public String getParolaHash() {
        return parolaHash;
    }

    public Set<Rol> getRoluri() {
        return roluri;
    }
}
