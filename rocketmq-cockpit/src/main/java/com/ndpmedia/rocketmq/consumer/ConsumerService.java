package com.ndpmedia.rocketmq.consumer;

import com.ndpmedia.rocketmq.consumer.model.Consumer;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * consumer service.
 * the API interface.
 */

@Produces(MediaType.APPLICATION_JSON)
public interface ConsumerService
{
    @POST
    @Path("/cJson")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    List<Consumer> list(@FormParam("groupName") String groupName);

    @POST
    @Path("/cpJson")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    List<ConsumerProgress> list(@FormParam("groupName") String groupName,@FormParam("topic") String topic,
                                @FormParam("broker")String broker);
}
