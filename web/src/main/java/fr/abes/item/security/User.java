package fr.abes.item.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@NoArgsConstructor
public class User implements UserDetails {
    @Getter @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Getter @Setter
    private String userNum;

    @Getter @Setter
    private String userKey;

    @Getter @Setter
    private String userGroup;

    @Setter
    private String role;

    @Getter @Setter
    private String library;

    @Getter @Setter
    private String shortName;

    @Getter @Setter
    private String loginAllowed;

    @Getter @Setter
    private String iln;

    @Getter @Setter
    private String libRcr;

    @Getter @Setter
    private String mail;

    @Getter @Setter
    private String password;

    public User(String userNum, String userKey, String userGroup) {
        this.userNum = userNum;
        this.userKey = userKey;
        this.userGroup = userGroup;
    }

    public String getRole() {
        if (role == null || role.isEmpty()) {
            if (this.userGroup.toLowerCase().trim().equals("coordinateur"))
                role = "USER";
            if (this.userGroup.toLowerCase().trim().equals("abes"))
                role = "ADMIN";
        }
        return this.role;
    }

    @Override
    public String getUsername() {
        return this.userKey;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}

