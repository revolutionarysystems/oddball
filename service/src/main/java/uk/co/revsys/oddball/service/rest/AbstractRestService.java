package uk.co.revsys.oddball.service.rest;

import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractRestService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("oddball");
    
	protected Response buildResponse(Object entity) {
		return Response.ok(entity.toString()).build();
	}
    
    protected Response buildErrorResponse(Throwable error){
        LOGGER.error("Internal Server Error", error);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.getMessage()).build();
    }

}
