package com.qvorum.server.dto;

import com.qvorum.server.model.Role;
import com.qvorum.server.model.User;
import java.util.List;

/** Matches User in shared/src/types. Contains no password data. */
public record UserDto(Long id, String firstName, String lastName, String email, List<Role> roles) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                List.copyOf(user.getRoles()));
    }
}
