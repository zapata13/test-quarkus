package com.redhat.app;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.redhat.app.services.GreetingService;

import org.eclipse.microprofile.config.inject.ConfigProperty;


@Path("/hello")
public class GreetingResource {

    @Inject
    GreetingService greetingService;
    @ConfigProperty(name = "msg") 
    String message;
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        //return "hello service "+ message;
        return greetingService.greet(message);
    }
}