package uk.co.revsys.oddball.service.rest;

public class AllowAllAuthorisationHandler implements AuthorisationHandler{

    @Override
    public boolean isAdministrator() {
        return true;
    }

    @Override
    public boolean isAccountOwner(String accountId) {
        return true;
    }

    @Override
    public boolean isUser() {
        return true;
    }

}
