package com.ndpmedia.rocketmq.topic;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    @Path("/topicList")
    Set<String> list();
}
