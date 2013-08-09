package com.ericsson.swot.messaging.peer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.OAuthServerResource;

import com.ericsson.oae.pubsub.sub.Subscriber;
import com.ericsson.oae.pubsub.sub.SubscriptionResponse;
import com.ericsson.swot.messaging.common.Constants;

public class SubscriberPlus extends Subscriber {
	
	private Map<String, Object> subProperties;
	private String pubFilterPredicate;
	private String hubToken = null;

	public SubscriptionResponse subscribe(String callback, String topic, Map<String, Object> subProperties, String pubFilterPredicate) {
		this.subProperties = subProperties;
		this.pubFilterPredicate = pubFilterPredicate;
		
		Reference hubRef = new Reference(Config.MESSAGING_HUB_SUBSCRIBE_URL);
		if (hubToken != null)
			hubRef.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, hubToken);
		
		return this.subscribe(hubRef.toString(), callback, topic, Integer.toString(generateNonce()), null, true);
	}
	
	@Override
	protected void addOptionalFields(Form form) {
		if (this.subProperties != null) {
			Form formSubProps = new Form();
			for (String key : this.subProperties.keySet()) 
				formSubProps.add(key, this.subProperties.get(key).toString());				
			form.add(Constants.SUB_PROPERTIES, formSubProps.getQueryString());
		}
		
		if (this.pubFilterPredicate != null)
			form.add(Constants.PUB_FILTER_PREDICATE, this.pubFilterPredicate);
	}
	
	public SubscriptionResponse unsubscribe(String callback, String topic) {
		Reference hubRef = new Reference(Config.MESSAGING_HUB_SUBSCRIBE_URL);
		if (hubToken != null)
			hubRef.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, hubToken);
		
		return this.unsubscribe(hubRef.toString(), callback, topic, Integer.toString(generateNonce()), null, true);
	}
	
	private int generateNonce() {

		SecureRandom rand = null;
		try {
			rand = SecureRandom.getInstance ("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (rand != null)
			return rand.nextInt();
		
		return -1;
	}

	public void setHubToken(String hubToken) {
		this.hubToken = hubToken;
	}
	

}
