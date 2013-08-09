package com.ericsson.swot.messaging.bus.hub;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.ericsson.oae.pubsub.PSHBConstants;
import com.ericsson.oae.pubsub.hub.DefaultHub;
import com.ericsson.oae.pubsub.hub.PublishResource;
import com.ericsson.swot.messaging.bus.Config;
import com.ericsson.swot.messaging.bus.Utilities;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalog;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalogManager;
import com.ericsson.swot.messaging.common.Constants;

public class PublishPlusResource extends PublishResource {

	@Override
	@Post("form:")
	public Representation notification(Representation rep) {
		SchemaCatalog catalog = SchemaCatalogManager.getSchemaCatalog();
		
		Form postForm = new Form(rep);
		getLogger().info("received publish body: " + postForm.getQueryString());

		String mode = postForm.getFirstValue(PSHBConstants.HUB_MODE);
		if(!PSHBConstants.PUBLISH.equals(mode)) {		
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid mode: " + mode);
			return null;
	    }
		
		String topic = postForm.getFirstValue(PSHBConstants.HUB_URL);	//hub.url is used to indicate the topic of publication as in the PubSubHubPub spec
		String msgMetadataStr = postForm.getFirstValue(Constants.MESSAGE_METADATA);
		Map<String, String> msgMetadata = null;
		if (msgMetadataStr != null)
			msgMetadata = new Form(msgMetadataStr).getValuesMap();
		if(catalog.checkTopicAndMetadata(topic, msgMetadata) == false) {	//topic and metadata have to be valid
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid topic or metadata");
			return null;
	    }
		
		getLogger().info("got topic: " + topic);
		
		//get publisher properties
		Map<String, Object> pubProperties = new HashMap<String, Object>();
		if (postForm.getFirstValue(Constants.PUB_PROPERTIES) != null) {
			Map<String, String> pubPropertiesStrs = new Form(postForm.getFirstValue(Constants.PUB_PROPERTIES)).getValuesMap();
		
			if (pubPropertiesStrs != null) {
				//check with the schema catalog to see if the property names are valid
				for (Entry<String, String> entry : pubPropertiesStrs.entrySet()) {
					String propertyName = entry.getKey();
					
					if (catalog.checkProperty(propertyName) == false) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid property name: " + propertyName);
						return null;	
					}
					
					String value = entry.getValue();
					if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
						pubProperties.put(propertyName, Boolean.getBoolean(value));
					else if (Utilities.isNumeric(value) == true)
						pubProperties.put(propertyName, Double.parseDouble(value));
					else
						pubProperties.put(propertyName, value);
				}
			}			
		}
		
		getLogger().info("got pubProperties: " + pubProperties);
		
		//get subscriber filtering predicate
		String subFilterPredicate = postForm.getFirstValue(Constants.SUB_FILTER_PREDICATE);
		if (subFilterPredicate != null) {
			List<String> predicateKeys = Utilities.parsePredicateKeys(subFilterPredicate);
			if (predicateKeys == null) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid predicate syntax: " + subFilterPredicate);
				return null;
			}
			for (String key : predicateKeys) {
				if (catalog.checkProperty(key) == false) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid property name in the predicate: " + key);
					return null;	
				}
			}		
		}
		
		getLogger().info("got subFilterPredicate: " + subFilterPredicate);

		//start push service for pushing content to subscribers
		try{
			getLogger().info("Creating new push service");
			CharSequence msg = generateMsg(topic, msgMetadata);			
			
			PushRecord record = null;
			if (Config.getPushRecordKeeping()) {	//keep the push record
				record = new PushRecord(topic, msg.toString());
				PushRecordManager.getManager().addNewPushRecord(new Date(), record);
			}
			
			PushServicePlus pushService = new PushServicePlus(msg, MediaType.APPLICATION_JSON, topic, getContext(), null, pubProperties, subFilterPredicate, record);
			ScheduledThreadPoolExecutor ex = (ScheduledThreadPoolExecutor) getContext().getAttributes().get(DefaultHub.EXEC_S);
			//getLogger().info("Executor: " + ex);
			
			try {
				ex.schedule(pushService, 0L, TimeUnit.SECONDS);
				System.out.println("Active number of threads: " + ex.getActiveCount() + ", number of tasks completed: " + ex.getCompletedTaskCount() + ", number of tasks ever scheduled: " + ex.getTaskCount());
				getLogger().info("Push service submitted");
			} catch (RejectedExecutionException e) {
				getLogger().info("Push service rejected");
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.setStatus(Status.SUCCESS_NO_CONTENT);
		return null;
	}
	
	/**
	 * Generate the actual message to be sent based on message topic and message metadata
	 * 
	 * @param topic
	 * @param msgMetadata
	 * @return the actual message
	 */
	private CharSequence generateMsg(String topic, Map<String, String> msgMetadata) {
		JSONObject jsonObj = new JSONObject();
		
		try {
			jsonObj.put("topic", topic);
			JSONObject metadataObj = new JSONObject();
			if (msgMetadata != null) {
				for (Entry<String, String> entry : msgMetadata.entrySet())
					metadataObj.put(entry.getKey(), entry.getValue());
			}
			
			jsonObj.put("metadata", metadataObj);			 
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		return jsonObj.toString();
	}
}
