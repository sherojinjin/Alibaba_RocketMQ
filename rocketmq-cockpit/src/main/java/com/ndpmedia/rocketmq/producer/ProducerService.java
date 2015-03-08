package com.ndpmedia.rocketmq.producer;

import com.ndpmedia.rocketmq.producer.model.Producer;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * producer service.
 * the API interface.
 */

@Produces(MediaType.APPLICATION_JSON)
public interface ProducerService {
    @GET
    @POST
    @Path("/pJson")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    List<Producer> list(@FormParam("groupName") String groupName, @FormParam("topic") String topic);
}
