package com.github.maxencelaurent.elbug.rest;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author maxence
 */
@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        register(JacksonFeature.class);

        // register REST controllers from those packages :
        packages("com.github.maxencelaurent.elbug.rest");
    }
}
