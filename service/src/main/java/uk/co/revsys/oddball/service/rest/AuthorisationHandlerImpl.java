package uk.co.revsys.oddball.service.rest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import uk.co.revsys.user.manager.model.User;

public class AuthorisationHandlerImpl implements AuthorisationHandler{

    private final String administratorRole;
    private final String accountOwnerRole;
    private final String userRole;

    public AuthorisationHandlerImpl(String administratorRole, String accountOwnerRole, String userRole) {
        this.administratorRole = administratorRole;
        this.accountOwnerRole = accountOwnerRole;
        this.userRole = userRole;
    }
    
    @Override
    public boolean isAdministrator() {
        return SecurityUtils.getSubject().hasRole(administratorRole);
    }

    @Override
    public boolean isAccountOwner(String accountId) {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole(userRole) && subject.hasRole(accountOwnerRole) && subject.getPrincipals().oneByType(User.class).getAccount().equals(accountId);
    }

    @Override
    public boolean isUser() {
        Subject subject = SecurityUtils.getSubject();
        return isAdministrator() || subject.hasRole(userRole);
    }

}
