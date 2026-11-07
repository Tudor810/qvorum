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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * model: JPA entities, one class per database table (see db/migration). Never returned directly
 * to clients; services map them to DTOs.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The association a resident or president belongs to. Empty for administrators, who own
     * associations through Association.administrator instead.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id")
    private Association association;

    /** The apartment of a resident (one account per apartment). Empty for administrators. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    /** BCrypt hash of the password; the plain password is never stored. */
    private String passwordHash;

    /** True while the user still has the temporary password sent by email. */
    private boolean mustChangePassword = true;

    private boolean emailVerified;

    /** Hash of the link sent to confirm the email address; null once it is confirmed. */
    private String verificationTokenHash;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    /** Required by JPA. */
    protected User() {
    }

    public User(String firstName, String lastName, String email, String passwordHash, Set<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Long getId() { return id; }
    public Association getAssociation() { return association; }
    public Apartment getApartment() { return apartment; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getVerificationTokenHash() { return verificationTokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Role> getRoles() { return roles; }
}
