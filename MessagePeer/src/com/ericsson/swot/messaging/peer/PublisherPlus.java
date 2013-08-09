package com.ericsson.swot.messaging.peer;

import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.OAuthServerResource;

import com.ericsson.oae.pubsub.pub.Publisher;
import com.ericsson.swot.messaging.common.Constants;

public class PublisherPlus extends Publisher {
	private Map<String, String> metadata;
	private Map<String, Object> pubProperties;
	private String subFilterPredicate;
	private String hubToken = null;
	
	public Status notfiy(String topic, Map<String, String> metadata, Map<String, Object> pubProperties, String subFilterPredicate) 
			throws IllegalArgumentException {
		this.metadata = metadata;
		this.pubProperties = pubProperties;
		this.subFilterPredicate = subFilterPredicate;
		
		Reference hubRef = new Reference(Config.MESSAGING_HUB_PUBLISH_URL);
		if (hubToken != null)
			hubRef.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, hubToken);
				
		return super.notfiy(hubRef.toString(), topic);
	}
	
	
	@Override
	protected void addOptionalFields(Form form) {
		if (this.metadata != null) {
			Form formMetadata = new Form();
			for (String key : this.metadata.keySet()) 
				formMetadata.add(key, this.metadata.get(key));				
			form.add(Constants.MESSAGE_METADATA, formMetadata.getQueryString());	
		}
		
		if (this.pubProperties != null) {
			Form formPubProps = new Form();
			for (String key : this.pubProperties.keySet()) 
				formPubProps.add(key, this.pubProperties.get(key).toString());				
			form.add(Constants.PUB_PROPERTIES, formPubProps.getQueryString());			
		}
		
		if (this.subFilterPredicate != null)
			form.add(Constants.SUB_FILTER_PREDICATE, this.subFilterPredicate);
	}
	
	public void setHubToken(String hubToken) {
		this.hubToken = hubToken;
	}
}
