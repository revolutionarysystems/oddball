package uk.co.revsys.oddball.service.rest;

public interface AuthorisationHandler {

    public boolean isAdministrator();
    
    public boolean isAccountOwner(String accountId);
    
}
