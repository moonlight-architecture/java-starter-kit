package com.servicecops.project.models.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "system_user", schema = "public")
public class SystemUserModel implements UserDetails {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @JsonIgnore
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "username")
    private String username;
    @Column(name = "role_code")
    private String roleCode;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "last_logged_in_at")
    private Timestamp lastLoggedInAt;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @Transient
    @Builder.Default
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

    @Transient
    private String roleName;

    @Transient
    private AppDomains roleDomain;

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(this.getIsActive());
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(this.getIsActive());
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(getIsActive());
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return Boolean.TRUE.equals(getIsActive());
    }
}
