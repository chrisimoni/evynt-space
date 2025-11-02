package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import com.chrisimoni.evyntspace.user.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends ActivatableEntity implements UserDetails {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String company;
    private String phoneNumber;
    private String profileImageUrl;
    private String countryCode;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Essential: Returns the user's role(s) to Spring Security for authorization checks.
        // Roles are prefixed with "ROLE_" (e.g., "ROLE_USER") by convention.
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Indicates if the account's validity period has elapsed.
        // Returns true (account never expires) unless logic is added (e.g., for inactivity).
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Indicates if the account has been locked (e.g., due to too many failed login attempts).
        // Returns true (account is never locked) unless logic is added.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Indicates if the user's password or other credentials have expired.
        // Returns true (credentials never expire) unless a password rotation policy is enforced.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Indicates if the user is active and permitted to authenticate.
        // This is typically linked to a status flag in your database.
        // ActivatableEntity provides an 'isActive' field. Links Spring Security's active check to your entity's status.
        return this.isActive();
    }
}
