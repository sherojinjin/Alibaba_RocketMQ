package com.ndpmedia.rocketmq.topic;

import com.ndpmedia.rocketmq.topic.model.Topic;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

/**
 * topic service.
 * The API interface
 */
@Produces(MediaType.APPLICATION_JSON)
public interface TopicService
{
    @GET
    @POST
    @Path("/topics")
    Set<String> list();

    @GET
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String lookUp(@FormParam("topic") String topic) throws IOException;

    @POST
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    boolean add(Topic topic) throws IOException;


    @DELETE
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    boolean delete(@FormParam("topic") String topic, @FormParam("clusterName") String clusterName) throws IOException;
}
