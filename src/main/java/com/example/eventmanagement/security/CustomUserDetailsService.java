package com.example.eventmanagement.security;

import com.example.eventmanagement.model.Permission;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        // Convert to Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    // Convert role and permissions to Spring Security authorities
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role as authority (prefixed with ROLE_)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        // Add each permission as authority
        for (Permission permission : user.getRole().getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermission()));
        }

        return authorities;
    }
}
