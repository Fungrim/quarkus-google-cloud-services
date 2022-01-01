package io.quarkiverse.googlecloudservices.it;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/logging")
public class LoggingResource {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GET
    @Path("/{payload}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryLog(@PathParam("payload") String p) {
        logger.info("Hello {}", p, KeyValueParameter.of("word", p));
        try {
            throwExceptionWithCause();
            ;
        } catch (RuntimeException e) {
            logger.error("Oh no!", e);
        }
        return "Hello " + p;
    }

    private void throwExceptionWithCause() {
        try {
            throwOne();
        } catch (RuntimeException e) {
            throw new RuntimeException("Not again!", e);
        }
    }

    private void throwOne() {
        throw new RuntimeException("Help!");
    }
}
