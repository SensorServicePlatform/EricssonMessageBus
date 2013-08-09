package com.ericsson.swot.messaging.bus.hub;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.OAuthServerResource;

import com.ericsson.oae.pubsub.hub.SimplePushService;
import com.ericsson.oae.pubsub.hub.Sub;
import com.ericsson.oae.pubsub.hub.ValidationParams;
import com.ericsson.swot.messaging.bus.Utilities;

public class PushServicePlus extends SimplePushService {
	Logger log = Logger.getLogger(this.getClass().getSimpleName());

	private Map<String, Object> pubProperties = null;
	private String subFilterPredicate = null;
	private PushRecord pushRecord = null;
	
	public PushServicePlus(CharSequence toPush, MediaType type, String topic, Context c, ValidationParams vp, 
			Map<String, Object> pubProperties, String subFilterPredicate, PushRecord record) {
		super(toPush, type, topic, c, vp);
		
		this.pubProperties = pubProperties;
		this.subFilterPredicate = subFilterPredicate;
		this.pushRecord = record;
	}
	
	@Override
	protected void generateSendList() {
		Collection<SubPlus> subs = HubPlusStoreManager.getStore().getSubsForTopic(this.topic);
		if(subs == null)
			return;
		log.info("number of subs for topic " + this.topic + ": " + subs.size());
		
		for(SubPlus sub : subs) {
			Map<String, Object> subProperties = sub.getSubProperties();
			String pubFilterPredicate = sub.getPubFilterPredicate();
			
			//log.info(this.subFilterPredicate + " Vs " + subProperties + " = " + Utilities.evaluate(this.subFilterPredicate, subProperties));
			//log.info(pubFilterPredicate + " Vs " + this.pubProperties + " = " + Utilities.evaluate(pubFilterPredicate, this.pubProperties));
			
			if (Utilities.evaluate(this.subFilterPredicate, subProperties) == true
					&& Utilities.evaluate(pubFilterPredicate, this.pubProperties) == true) {
				log.info("Adding subscriber: " + sub.getCallbackURL());
				this.sendList.add(sub);
			}
		}
	}
	
	@Override
	protected String getSubCallback(Sub sub) {
		//append OAuth token if any
		String callbackNoAuth = sub.getCallbackURL();
		String authToken = HubPlusStoreManager.getStore().getAccessToken(callbackNoAuth);

		Reference ref = new Reference(callbackNoAuth);
		if (authToken != null)
			ref.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, authToken);
		
		return ref.toString();
	}
	
	@Override
	protected void deliveryFinished(Sub sub, boolean success, int timesTried) {
		if (this.pushRecord != null)
			this.pushRecord.addDelivery(sub.getCallbackURL(), success, timesTried);
	}
	
	/**
	 * Get the message that is being pushed out (FOR DEBUGGING PURPOSE)
	 * 
	 * @return the message
	 */
	public String getMsg() {
		return this.toPush.toString();
	}

	public List<Sub> getSubList() {
		return this.sendList;
	}
}
