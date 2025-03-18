package com.notic.initializer;


import com.notic.entity.Role;
import com.notic.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if(roleRepository.count() == 0) {
            Role role1 = new Role("ROLE_USER");
            Role role2 = new Role("ROLE_ADMIN");

            roleRepository.save(role1);
            roleRepository.save(role2);

            System.out.println("Default roles created");
        }
    }
}
