package com.spring.logitrack.config;

import com.spring.logitrack.entity.User;
import com.spring.logitrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Loading user: " + email);
        System.out.println("➡️ Attempting login for email: " + email);

        User user = repo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found in DB");
                    return new UsernameNotFoundException("Not found");
                });

        System.out.println("✔ User loaded: " + user.getEmail());
        System.out.println("✔ Hash stored: " + user.getPassword());
        System.out.println("✔ Role: " + user.getRole());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isActive())
                .build();
    }

}
