package com.notic.service;

import com.notic.entity.Role;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role createRole(String roleName) {
        if(roleRepository.existsByName(roleName)) {
            throw new EntityAlreadyExistsException("Role" + roleName + " already exists");
        }

        Role role = new Role(roleName);

        return roleRepository.save(role);
    }

    public Role getDefaultRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("No role found"));
    }
}
