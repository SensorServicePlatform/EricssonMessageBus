package com.ericsson.swot.messaging.peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.ericsson.oae.pubsub.sub.SubscriptionResponse;
import com.ericsson.swot.messaging.common.Constants;

/**
 * Represents a SWoT messaging peer
 * 
 * @author exingbo
 *
 */

public class MessagingPeer {
	private Logger log = Logger.getLogger(this.getClass().getSimpleName());

	private Map<String, Object> properties;
	private PublisherPlus publisher;
	private SubscriberPlus subscriber;
	private String tokenUrl = null;
	private String callbackUrl = null;
	private String hubToken = null;
	
	public Float temp_sensor1 = null;
	public Float temp_sensor2 = null;
	
	public String avg_temp = "N/A";
	public String body_temp = "N/A";
	public String blood_pressure = "N/A";
	public String heart_rate = "N/A";
	
	public Integer interval = 5;
	
	public String peerType = "";
	public ArrayList<ArrayList<String>> msgHistory;
	
	public MessagingPeer(String tokenUrl, String callbackUrl) {
		this.properties = new HashMap<String, Object>();
		this.publisher = new PublisherPlus();
		this.subscriber = new SubscriberPlus();
		this.tokenUrl = tokenUrl;
		this.callbackUrl = callbackUrl;
	}
	
	public MessagingPeer(Map<String, Object> props, String tokenUrl, String callbackUrl) {
		this(tokenUrl, callbackUrl);
		registerProperties(props);
	}
	
	private void authTokenExchange() {
		if (Config.OAUTH_ENABLED && this.hubToken == null && this.tokenUrl != null) {
			log.info("to register for exchanging oauth takens");
			
/*			ClientResource client = new ClientResource(this.registerUrl);
			client.get();
			if (client.getResponse().getStatus().isSuccess()) {
				try {
					String token = client.getResponseEntity().getText();
					if (token != null)
						this.setHubToken(token);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			client.release();*/
			
			String token = getHubToken(Config.PEER_CLIENT_ID, Config.PEER_CLIENT_SECRET, Config.REMOTE_OAUTH_SERVER_TOKEN_URI);
			if (token != null) {
				String myCallbackToken = this.authRegister(token);
				if (myCallbackToken != null) {
/*					Reference ref = new Reference(this.callbackUrl);
					ref.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, myCallbackToken);
					this.callbackUrl = ref.toString();*/
					this.setHubToken(token);
				}					
			}
		}
	}
	
	private String authRegister(String token) {
		//let the hub know how to get an access token for my callback resource
		Reference hubRegisterRef = new Reference(Config.MESSAGING_BUS_AUTH_REGISTER_URL);
		hubRegisterRef.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, token);

		Form form = new Form();
		form.set(Constants.TOKEN_URI, tokenUrl.toString());
		form.set(Constants.CLIENT_ID, Config.HUB_CLIENT_ID);
		form.set(Constants.CLIENT_SECRET, Config.HUB_CLIENT_SECRET);
		form.set(Constants.CALLBACK_URL, this.callbackUrl);
		
		log.info("posting to hub");
		ClientResource clientResource = new ClientResource(hubRegisterRef);
		try {
			clientResource.post(form);
			if (clientResource.getResponse().getStatus().isSuccess()) {
				log.info("registered successfully");
				return clientResource.getResponseEntity().getText();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientResource.getResponse().release();
			clientResource.release();
		}
		
		return null;
	}
	
	private String getHubToken(String clientId, String clientSecret, String tokenUri) {
		Form form = new Form();
		form.add(OAuthServerResource.GRANT_TYPE, GrantType.none.name());	//OAuth Autonomous Flow
		//form.add(OAuthServerResource.GRANT_TYPE, OAuthServerResource.GrantType.none.name());	//OAuth Autonomous Flow
		form.add(OAuthServerResource.CLIENT_ID, clientId);
		form.add(OAuthServerResource.CLIENT_SECRET, clientSecret);

		Reference tokenRef = new Reference(tokenUri);		
		ClientResource tokenResource = new ClientResource(tokenRef);
		
		log.info("tokenUri = " + tokenUri);
		Representation response = tokenResource.post(form.getWebRepresentation());
		
		boolean ok = tokenResource.getResponse().getStatus().isSuccess();
		tokenResource.release();
		String accessToken = null;
		try {
			if (ok) {
				String responseStr = response.getText();
				log.info("response body = " + responseStr);
				JsonRepresentation returned = new JsonRepresentation(responseStr);
				JSONObject answer = returned.getJsonObject();

				log.info("Got answer on AccessToken = " + answer.toString());
				accessToken = answer.getString(OAuthServerResource.ACCESS_TOKEN);
				log.info("AccessToken = " + accessToken);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			response.release();
		}
		
		return accessToken;
	}
	
	/**
	 * Register the properties of the messaging peer
	 * 
	 * @param props the properties to be registered in key-value pairs
	 * @return the properties that have been successfully registered to-date for this messaging peer
	 */
	public Map<String, Object> registerProperties(Map<String, Object> props) {
		Set<String> validPropsSet = new HashSet<String>();
		Reference ref = new Reference(Config.SCHEMA_CATALOG_URL);
		ref.addSegment("properties");
		ClientResource client = new ClientResource(ref);
		client.get();
		if (client.getResponse().getStatus().isSuccess()) {
			try {
				JsonRepresentation response = new JsonRepresentation(client.getResponseEntity());
				JSONArray validPropsArray = response.getJsonArray();
				for (int i = 0; i < validPropsArray.length(); i++)
					validPropsSet.add(validPropsArray.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (String prop : props.keySet()) {
			if (validPropsSet.contains(prop)) {
				this.properties.put(prop, props.get(prop));
				log.info("property name \"" + prop + "\" verified");
			}			
		}
		
		return this.properties;
	}
	
	/**
	 * Get the registered properties as key-value pairs
	 * @return the registered properties as key-value pairs
	 */
	public Map<String, Object> getProperties() {
		return this.properties;
	}
	
	/**
	 * Publish a message
	 * 
	 * @param topic					the topic of the message
	 * @param metadata				the metadata of the message
	 * @param subFilterPredicate	the predicate specifies the expected properties of the receivers of the message
	 * @return 						true if it was published successfully, false if it failed
	 */
	public boolean publish(String topic, Map<String, String> metadata, String subFilterPredicate) {
		authTokenExchange();
		
		Status status = this.publisher.notfiy(topic, metadata, this.properties, subFilterPredicate);
		
		if (status.isSuccess())
			return true;
		
		log.info("ERROR -- " + status.getDescription() + ": " + status.getReasonPhrase());
		return false;
	}
	
	
	/**
	 * Subscribe to a message topic
	 * 
	 * @param topic
	 * @param callbackURL
	 * @param pubFilterPredicate
	 * @return true if it was subscribed successfully, false if it failed
	 */
	public boolean subscribe(String topic, String pubFilterPredicate) {
		authTokenExchange();
		
		SubscriptionResponse response = this.subscriber.subscribe(this.callbackUrl, topic, this.properties, pubFilterPredicate);
		
		if (response.getStatus().isSuccess())
			return true;
		
		log.info("ERROR -- " + response.getStatus().getDescription() + ": " + response.getStatus().getReasonPhrase());
		return false;
	}
	
	/**
	 * Unsubscribe to a message topic
	 * 
	 * @param topic
	 * @param callbackURL
	 * @return true if it was unsubscribed successfully, false it if failed
	 */
	public boolean unsubscribe(String topic) {
		authTokenExchange();
		
		SubscriptionResponse response = this.subscriber.unsubscribe(this.callbackUrl, topic);
		
		if (response.getStatus().isSuccess())
			return true;
		
		log.info("ERROR -- " + response.getStatus().getDescription() + ": " + response.getStatus().getReasonPhrase());
		return false;
	}
	
	public String getSubscribeVerifyString() {
		return this.subscriber.getVerifyString();
	}
	
	public String getCallbackUrl() {
		return this.callbackUrl;
	}

	private void setHubToken(String hubToken) {
		log.info("retrieved access token for the hub: " + hubToken);
		this.hubToken = hubToken;
		this.publisher.setHubToken(hubToken);
		this.subscriber.setHubToken(hubToken);
	}

}
