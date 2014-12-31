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
    @Path("/consumerByGroupName")
    List<Consumer> list(String groupName);

    @POST
    @Path("/consumerProgress")
    List<ConsumerProgress> list(String groupName, String topic, String broker);
}
