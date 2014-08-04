package uk.co.revsys.oddball.service.rest;

public class AllowAllAuthorisationHandler implements AuthorisationHandler{

    @Override
    public boolean isAdministrator() {
        return true;
    }

}
