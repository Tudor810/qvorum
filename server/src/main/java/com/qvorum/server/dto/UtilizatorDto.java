package com.qvorum.server.dto;

import com.qvorum.server.model.Rol;
import com.qvorum.server.model.Utilizator;
import java.util.List;

/** Matches Utilizator in shared/src/types. Contains no password data. */
public record UtilizatorDto(Long id, String nume, String prenume, String email, List<Rol> roluri) {

    public static UtilizatorDto from(Utilizator utilizator) {
        return new UtilizatorDto(
                utilizator.getId(),
                utilizator.getNume(),
                utilizator.getPrenume(),
                utilizator.getEmail(),
                List.copyOf(utilizator.getRoluri()));
    }
}
