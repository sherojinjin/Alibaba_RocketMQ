package com.ndpmedia.rocketmq.topic;

import com.ndpmedia.rocketmq.topic.model.Topic;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * topic service.
 * The API interface
 */
@Produces(MediaType.APPLICATION_JSON)
public interface TopicService {
    @GET
    @Path("/topics")
    List<Topic> list();

    @GET
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String lookUp(@FormParam("topic") String topic);

    @POST
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void add(Topic topic);

    @DELETE
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void delete(Map<String, Object> fieldMap);

    @PUT
    @Path("/topic")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(Map<String, Object> fieldMap);

    @POST
    @Path("/topicP")
    @Consumes(MediaType.APPLICATION_JSON)
    List<Object> messageDiff(Map<String, Object> fieldMap);
}
