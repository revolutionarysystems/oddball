package uk.co.revsys.oddball.service.rest;

import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

public class AbstractRestService {
    
    private static final Logger LOGGER = Logger.getLogger("oddball");
    
	protected Response buildResponse(Object entity) {
		return Response.ok(entity.toString()).build();
	}
    
    protected Response buildErrorResponse(Throwable error){
        LOGGER.error("Internal Server Error", error);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.getMessage()).build();
    }

}
