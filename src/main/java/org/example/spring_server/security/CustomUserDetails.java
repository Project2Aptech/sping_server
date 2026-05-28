package org.example.spring_server.security;

import lombok.Getter;
import org.example.spring_server.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Integer id;
    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id          = user.getId();
        this.email       = user.getEmail();
        this.password    = user.getPasswordHash();
        this.active      = user.isActive();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    public static Integer extractId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails custom) {
            return custom.getId();
        }
        throw new IllegalStateException("Unexpected UserDetails type");
    }

    public static boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    @Override public String getUsername()                                    { return email; }
    @Override public String getPassword()                                    { return password; }
    @Override public boolean isEnabled()                                     { return active; }
    @Override public boolean isAccountNonExpired()                           { return true; }
    @Override public boolean isAccountNonLocked()                            { return true; }
    @Override public boolean isCredentialsNonExpired()                       { return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
}