package com.ericsson.swot.messaging.bus.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import com.ericsson.oae.pubsub.PSHBConstants;
import com.ericsson.oae.pubsub.hub.SubscribeResource;
import com.ericsson.swot.messaging.bus.Utilities;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalog;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalogManager;
import com.ericsson.swot.messaging.common.Constants;

public class SubscribePlusResource extends SubscribeResource {

	@Override
	@Post("form:txt")
	public Representation sub(Representation rep) {
		getLogger().entering(this.getClass().getName(), "sub");
		
		Form f = new Form(rep);
		getLogger().info("received subscribe body: " + f.getQueryString());

		Object o = verifySubscriber(f);
		if(o instanceof Representation)
			return (Representation) o;
		
		String mode = f.getFirstValue(PSHBConstants.HUB_MODE);
		SubPlus sub = (SubPlus)o;
		if(mode.equalsIgnoreCase(PSHBConstants.SUBSCRIBE))	
			HubPlusStoreManager.getStore().put(sub);
		else if(mode.equalsIgnoreCase(PSHBConstants.UNSUBSCRIBE))	//unsubscribe	
			HubPlusStoreManager.getStore().remove(sub.getCallbackURL(), sub.getTopic());
		
		return null;
	}
	
	@Override
	protected Object newSub(String callback, String topic, Integer leaseInt, String secret, String oauthToken, Form postForm) {
		SchemaCatalog catalog = SchemaCatalogManager.getSchemaCatalog();
		
		//check with the schema catalog to see if the topic is valid
		if(catalog.checkTopicAndMetadata(topic, null) == false) {	//topic is not valid
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid message topic: " + topic);
			return new StringRepresentation("Invalid message topic: " + topic);			
		}

		//get subscriber properties
		Map<String, Object> subProperties = new HashMap<String, Object>();
		getLogger().info("sub perperties received: " + postForm.getFirstValue(Constants.SUB_PROPERTIES));
		if (postForm.getFirstValue(Constants.SUB_PROPERTIES) != null) {
			Map<String, String> subPropertiesStrs = new Form(postForm.getFirstValue(Constants.SUB_PROPERTIES)).getValuesMap();
		
			if (subPropertiesStrs != null) {
				//check with the schema catalog to see if the property names are valid
				for (Entry<String, String> entry: subPropertiesStrs.entrySet()) {
					String propertyName = entry.getKey();
					
					if (catalog.checkProperty(propertyName) == false) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid property name: " + propertyName);
						return new StringRepresentation("Invalid message topic: " + propertyName);	
					}

					String value = entry.getValue();
					if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
						subProperties.put(propertyName, Boolean.getBoolean(value));
					else if (Utilities.isNumeric(value) == true)
						subProperties.put(propertyName, Double.parseDouble(value));
					else
						subProperties.put(propertyName, value);
				}
			}
		}
		
		//get publisher filtering predicate
		String pubFilterPredicate = postForm.getFirstValue(Constants.PUB_FILTER_PREDICATE);
		if (pubFilterPredicate != null) {
			List<String> predicateKeys = Utilities.parsePredicateKeys(pubFilterPredicate);
			if (predicateKeys == null) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid predicate syntax: " + pubFilterPredicate);
				return new StringRepresentation("Invalid predicate syntax: " + pubFilterPredicate);	
			}
			for (String key : predicateKeys) {
				if (catalog.checkProperty(key) == false) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid property name in the predicate: " + key);
					return new StringRepresentation("Invalid property name in the predicate: " + key);	
				}
			}
		}
		
		//create the sub structure
		SubPlus sub = new SubPlus(callback, topic, subProperties, pubFilterPredicate);
		sub.setActive(true);
		sub.setStartDate(System.currentTimeMillis());	//Need to verify that this is the correct date
		sub.setLease(leaseInt);
		sub.setSecret(secret);

		return sub;
	}
	
	@Override
	protected boolean verify(String callback, String mode, String topic, Integer lease, String verifyToken) {
		//append OAuth token if any
		String authToken = HubPlusStoreManager.getStore().getAccessToken(callback);

		Reference ref = new Reference(callback);
		if (authToken != null)
			ref.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, authToken);
		
		return super.verify(ref.toString(), mode, topic, lease, verifyToken);
	}
}
