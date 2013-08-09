package com.ericsson.swot.messaging.peer.restlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.ericsson.oae.pubsub.PSHBConstants;
import com.ericsson.swot.messaging.common.Constants;
import com.ericsson.swot.messaging.peer.MessagingPeer;

public class DefaultCallbackResource extends ServerResource {
	@Get("text")
	public Representation verify(Representation rep) {
		getLogger().entering(this.getClass().getName(), "verify");
		
		Form form = getQuery();
		getLogger().info("received verify body: " + form.getQueryString());
		
		String hubMode = form.getFirstValue(PSHBConstants.HUB_MODE);
		String hubTopic = form.getFirstValue(PSHBConstants.HUB_TOPIC);
		String hubChallenge = form.getFirstValue(PSHBConstants.HUB_CHALLENGE);
		String hubVerify = form.getFirstValue(PSHBConstants.HUB_TOKEN);
//		String hubLease = query.getFirstValue(PSHBConstants.HUB_LEASE);
		
		String stringToBeVerified = hubMode + "#" + hubTopic + "#" + hubVerify;
//		MessagingPeer peer = (MessagingPeer) getContext().getAttributes().get(MessagingPeerConstants.MESSAGING_PEER);	//needs to be put in the context beforehand
		MessagingPeer peer = ((DefaultMessagingPeerApp)getApplication()).getPeer();
		if (stringToBeVerified.equals(peer.getSubscribeVerifyString())) {
			getLogger().info("verified");
			return new StringRepresentation(hubChallenge);
		}
		
		getLogger().info("Could not verify");
		getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		return null;
	}
	
	@Post("json:")
	@SuppressWarnings("unchecked")
	public Representation receiveMessage(Representation rep) throws JSONException, IOException {
		JsonRepresentation jsonRep = new JsonRepresentation(rep);
		JSONObject jsonResponse = jsonRep.getJsonObject();
		getLogger().info("received notify body: " + jsonResponse.toString());
		
		String topic = jsonResponse.getString("topic");
		JSONObject metadataObj = jsonResponse.getJSONObject("metadata");
		Map<String, String> metadata = null;
		if (metadataObj != null) {
			metadata = new HashMap<String, String>();
			Iterator<String> it = metadataObj.keys();
			while(it.hasNext()) {
				String key = it.next();
				metadata.put(key, metadataObj.getString(key));
			}
		}
		
		handleMessage(topic, metadata);
		
		return null;
	}
	
	/**
	 * post to HANA wrapper
	 */
	
	public void postToWrapper(Map<String, String> metadata){
		String serverUrl = "http://cmu-sensor-network.herokuapp.com/";
		String path = "sensors_save";
		HttpClient client = new DefaultHttpClient();
		 HttpPost post = new HttpPost(serverUrl + path);
		 try {

		      JSONObject topicObject = new JSONObject();
		      for (Map.Entry<String, String> entry : metadata.entrySet()) {
		    	    String field = entry.getKey();
		    	    String value = entry.getValue();
		    	    if (! "N/A".equals(value))
		    	    {
			    	    Float float_value = null;
			    	    try{
			    	    	float_value = Float.parseFloat(value);
						    topicObject.put(field, float_value);
			    	    } catch (Exception e) {
						    topicObject.put(field, value);
			    	    }		    	    	
		    	    }
		    	}


			  topicObject.put("timestamp", System.currentTimeMillis());
	    	  this.getLogger().info("virtual sensor/device begins to post " + topicObject.toString());

		      StringEntity entity = new StringEntity(topicObject.toString(), HTTP.UTF_8);
		      //System.err.println(topicObject.toString());
		      entity.setContentType("application/json");

		      post.setEntity(entity);


		      HttpResponse response =client.execute(post);
	    	  this.getLogger().info("virtual sensor/device post ends");


		      //Reading Content
		      String output = "";
		      String line = "";
		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		      while ((line = rd.readLine()) != null) {
		    	  output += line;
		      }
	    	  this.getLogger().info("xxxx post to wrapper output" + output);


		 }
		 catch(IOException e){
			 e.printStackTrace(); 
		 } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Handle the received message. To be extended by subclasses.
	 * 
	 * @param topic
	 */
	protected void handleMessage(String topic, Map<String, String> metadata) {
		this.getLogger().info("message received - topic: " + topic + ", metadata: " + metadata);		
		
		MessagingPeer peer = ((DefaultMessagingPeerApp)getApplication()).getPeer();
		
		String peerType = peer.peerType;
		
		if(peerType.equals("listener")) {
			
			ArrayList<String> msg = new ArrayList<String>();
			msg.add(topic);
			msg.add(metadata.toString());
			this.getLogger().info("zzz metadata received in handler:" + msg);

			peer.msgHistory.add(msg);
			postToWrapper(metadata);
			
		} else if(peerType.equals("room-temperature1")
				|| peerType.equals("room-temperature2")
				|| peerType.equals("blood-pressure")
				|| peerType.equals("heart-rate")
				|| peerType.equals("body-temperature")) {
			
			// physical sensors
			if ("WFRun".equals(metadata.get("id")))
			{
				peer.interval = Integer.parseInt(metadata.get("value"));
			}
			
		} else if(peerType.equals("virtual-sensor1")) {
			// virtual sensor

			if ("temp_sensor1".equals(metadata.get("id")) )
			{
				peer.temp_sensor1 = Float.parseFloat(metadata.get("temp"));
				this.getLogger().info("xxxa " + Float.parseFloat(metadata.get("temp")));
			}
			else if ("temp_sensor2".equals(metadata.get("id")))
			{
				peer.temp_sensor2 = Float.parseFloat(metadata.get("temp"));
				this.getLogger().info("xxxb " + Float.parseFloat(metadata.get("temp")));
			}
			if (peer.temp_sensor1 != null && peer.temp_sensor1 != null)
			{
				Map<String, String> publish_metadata = null;
				publish_metadata = new HashMap<String, String>();
				publish_metadata.put("id", "virtual_sensor");
				this.getLogger().info("publish: average temperature" + Float.toString((peer.temp_sensor1 + peer.temp_sensor2)/2));
				publish_metadata.put("temp", Float.toString((peer.temp_sensor1 + peer.temp_sensor2)/2));
				peer.publish("AvgTemperature", publish_metadata, null);
			}
			
		} else if(peerType.equals("virtual-device1")) {
			// virtual device
			
			if ("body_temp_sensor".equals(metadata.get("id")) )
			{
				peer.body_temp = metadata.get("temp");
				this.getLogger().info("body_temp " + metadata.get("temp"));
			}
			else if ("blood_pressure_sensor".equals(metadata.get("id")))
			{
				peer.blood_pressure = metadata.get("pressure");
				this.getLogger().info("blood_pressure " + metadata.get("pressure"));
	
			}else if ("heart_rate_sensor".equals(metadata.get("id")))
			{
				peer.heart_rate = metadata.get("rate");
				this.getLogger().info("heart_rate " + metadata.get("rate"));
	
			}else if ("virtual_sensor".equals(metadata.get("id")))
			{
				peer.avg_temp = metadata.get("temp");
				this.getLogger().info("avg_temp " + metadata.get("temp"));
	
			}
			Map<String, String> publish_metadata = null;
			publish_metadata = new HashMap<String, String>();
			publish_metadata.put("id", "virtual_device");
			publish_metadata.put("avg_temp", peer.avg_temp);
			publish_metadata.put("body_temp", peer.body_temp);
			publish_metadata.put("blood_pressure", peer.blood_pressure);
			publish_metadata.put("heart_rate", peer.heart_rate);
			this.getLogger().info("publish: "  + publish_metadata);
			peer.publish("Environment", publish_metadata, null);	
			
		}
	}
}
