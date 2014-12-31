package com.ndpmedia.rocketmq.producer;

import com.ndpmedia.rocketmq.producer.model.Producer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * producer service.
 * the API interface.
 */

@Produces(MediaType.APPLICATION_JSON)
public interface ProducerService
{
    @POST
    @Path("/producersJson")
    List<Producer> list(@FormParam("groupName") String groupName, @FormParam("topic") String topic);
}
