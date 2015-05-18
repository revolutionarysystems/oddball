package uk.co.revsys.oddball.service.rest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

public class SecurityFilter extends AuthorizationFilter{

    private AuthorisationHandler authorisationHandler;

    public SecurityFilter(AuthorisationHandler authorisationHandler) {
        this.authorisationHandler = authorisationHandler;
    }
    
    @Override
    protected boolean isAccessAllowed(ServletRequest sr, ServletResponse sr1, Object o) throws Exception {
        return authorisationHandler.isUser();
    }

}
