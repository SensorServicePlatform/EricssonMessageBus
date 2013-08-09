package com.ericsson.swot.messaging.peer.restlet;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.ericsson.swot.messaging.peer.MessagingPeer;

public class TestControlResource extends ServerResource {

	@Get("text")
	public Representation control(Representation rep) {
		String callback = getReference().getBaseRef().toString().replace(getReference().getBaseRef().getLastSegment(), "") + "callback";
		
		//MessagingPeer peer = (MessagingPeer) getContext().getAttributes().get(MessagingPeerConstants.MESSAGING_PEER);
		MessagingPeer peer = ((DefaultMessagingPeerApp)getApplication()).getPeer();
		
		if (getReference().getLastSegment().equals("subscribe")) {
			String topic = getQuery().getFirstValue("topic");
			String pubFilterPredicate = getQuery().getFirstValue("predicate");
			
			getLogger().info("callback: " + callback);
			if (topic != null && peer.subscribe(topic, pubFilterPredicate) == true)
				return new StringRepresentation("subscribed successfully");
			else
				return new StringRepresentation("failed to subscribe");
		} else if (getReference().getLastSegment().equals("publish")) {
			String topic = getQuery().getFirstValue("topic");
			Map<String, String> metadata = null;
			getLogger().info("xxx" + getQuery().getFirstValue("metadata"));

			if (getQuery().getFirstValue("metadata") != null)
			{
				metadata = new HashMap<String, String>();

				String metastr = getQuery().getFirstValue("metadata");
				
				String[] pairList = metastr.split(Pattern.quote("|"));
				for (String pair:pairList)
				{
					getLogger().info(pair);
					String key = pair.split(":")[0];
					String value = pair.split(":")[1];
					getLogger().info("xxxx"+ key);
					getLogger().info(value);
					metadata.put(key, value);
				}
				//metadata = new Form(getQuery().getFirstValue("metadata")).getValuesMap();
				getLogger().info("xxx" + getQuery().getFirstValue("metadata"));
				for (Map.Entry<String, String> entry : metadata.entrySet())
				{
					getLogger().info("yyy"+entry.getKey() + ", " + entry.getValue());
				}
				
			}
			String subFilterPredicate = getQuery().getFirstValue("predicate");
			
			if (topic != null && peer.publish(topic, metadata, subFilterPredicate) == true)
				return new StringRepresentation("published successfully");
			else
				return new StringRepresentation("failed to publish");
		}

		
		return null;
	}
}
