package com.ndpmedia.rocketmq.ip;

import com.ndpmedia.rocketmq.ip.model.IPPair;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
public interface IPMappingService {

    @GET
    @Path("/ip")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String lookUp(@FormParam("innerIP") String innerIP) throws IOException;

    @GET
    @Path("/ips")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    List<IPPair> list() throws IOException;

    @POST
    @Path("/ip")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void add(@FormParam("innerIP") String innerIP, @FormParam("publicIP") String publicIP) throws IOException;


    @DELETE
    @Path("/ip")
    @Consumes(MediaType.APPLICATION_JSON)
    void delete(Map<String, Object> params) throws IOException;
}
