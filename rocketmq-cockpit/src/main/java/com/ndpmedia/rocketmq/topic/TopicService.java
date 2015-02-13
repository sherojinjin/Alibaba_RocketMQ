package com.ndpmedia.rocketmq.topic;

import com.ndpmedia.rocketmq.topic.model.Topic;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * topic service.
 * The API interface
 */
@Produces(MediaType.APPLICATION_JSON)
public interface TopicService
{
    @GET
    @Path("/topics")
    List<Topic> list();

    @GET
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String lookUp(@FormParam("topic") String topic) ;

    @POST
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void add(Topic topic) ;

    @DELETE
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void delete(Map<String, Object> fieldMap) ;

    @PUT
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(Map<String, Object> fieldMap);
}
