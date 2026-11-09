package com.qvorum.server.dto;

import com.qvorum.server.model.Role;
import com.qvorum.server.model.User;
import java.util.List;

/**
 * Matches User in shared/src/types and docs/api-contract.md (section 2). Contains no password data.
 *
 * @param associationId null for administrators
 * @param apartmentId null for administrators
 * @param mustChangePassword true until the temporary password is changed
 */
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        List<Role> roles,
        Long associationId,
        Long apartmentId,
        boolean mustChangePassword) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                List.copyOf(user.getRoles()),
                // getId() on a LAZY relationship reads the foreign key; it does not load the row.
                user.getAssociation() == null ? null : user.getAssociation().getId(),
                user.getApartment() == null ? null : user.getApartment().getId(),
                user.isMustChangePassword());
    }
}
