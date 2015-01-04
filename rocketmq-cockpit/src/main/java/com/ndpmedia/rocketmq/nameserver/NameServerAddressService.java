package com.ndpmedia.rocketmq.nameserver;

import com.ndpmedia.rocketmq.nameserver.model.NameServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
public interface NameServerAddressService {

    @GET
    @Path("/nsaddr")
    String listNameServer();

    @POST
    @Path("/nsaddr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void add(@FormParam("nameServer") String nameServer);

    @DELETE
    @Path("/nsaddr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void delete(@FormParam("nameServer") String nameServer);

    @GET
    @Path("/nsaddrJson")
    List<NameServer> list();

}
