package account.Entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ACCOUNTANT("ACCOUNTANT"),
    ROLE_ADMINISTRATOR("ADMINISTRATOR"),
    ROLE_USER("USER");

    private String role;

    Role(String role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role;
    }
}