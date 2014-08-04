package uk.co.revsys.oddball.service.rest;

import org.apache.shiro.SecurityUtils;

public class AuthorisationHandlerImpl implements AuthorisationHandler{

    private final String administratorRole;

    public AuthorisationHandlerImpl(String administratorRole) {
        this.administratorRole = administratorRole;
    }
    
    @Override
    public boolean isAdministrator() {
        return SecurityUtils.getSubject().hasRole(administratorRole);
    }

}
