package com.ericsson.swot.messaging.bus.catalog;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.jexl2.JexlException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * The server resource that exposes the schema catalog to the outside world through HTTP REST API
 * 
 * @author exingbo
 *
 */

public class SchemaCatalogResource extends ServerResource {	
	
	/**
	 * Accepts POST methods with JSON bodies
	 * 
	 * @param rep
	 * @return always null
	 * @throws IOException
	 * @throws JSONException
	 * @throws ClassNotFoundException 
	 * @throws JexlException 
	 */
	
	@Post("json:text")
	public Representation updateSchema(Representation rep) {
		SchemaCatalog catalog = SchemaCatalogManager.getSchemaCatalog();
		if ("topics".equalsIgnoreCase(this.getReference().getBaseRef().getLastSegment())) {			
			
			JsonRepresentation jsonRep = null;
			try {
				jsonRep = new JsonRepresentation(rep);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			JSONObject jsonObj = null;
			try {
				jsonObj = jsonRep.getJsonObject();
			} catch (JSONException e1) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- supposed to be a json object");
				return null;
			}
			
			if (!jsonObj.has("topic")) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'topic' is missing");
				return null;
			}
			String topic = null;
			try {
				topic = jsonObj.getString("topic");
			} catch (JSONException e1) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'topic' is supposed to be a string");
				return null;
			}
						
			boolean schemaUpdated = false;
			if(catalog.addTopic(topic) == true) {
				this.getResponse().setStatus(Status.SUCCESS_CREATED, "topic created.");
				schemaUpdated = true;
			} else
				this.getResponse().setStatus(Status.SUCCESS_OK, "topic already exists.");
			
			
			if (!jsonObj.has("metadata")) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'metadata' is missing");
				return null;
			}
			JSONArray jsonArray = null;
			try {
				jsonArray = jsonObj.getJSONArray("metadata");
			} catch (JSONException e) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'metadata' is supposed to be a json array");
				return null;
			}
			
			if (jsonArray != null) {	//with metadata specified
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject valueTypeObj = null;
					try {
						valueTypeObj = jsonArray.getJSONObject(i);
					} catch (JSONException e1) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- each 'metadata' array entry is supposed to be a json object");
						return null;
					}
					
					if (!valueTypeObj.has("type")) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'type' is missing");
						return null;
					}
					String type = null;
					try {
						type = valueTypeObj.getString("type");
					} catch (JSONException e1) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'type' is supposed to be a string");
						return null;
					}
					
					if (!valueTypeObj.has("field")) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'field' is missing");
						return null;
					}
					String field = null;
					try {
						field = valueTypeObj.getString("field");
					} catch (JSONException e1) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'field' is supposed to be a string");
						return null;
					}
					
					String range = null;
					if (valueTypeObj.has("range")) {
						try {
							range = valueTypeObj.getString("range");
						} catch (JSONException e1) {
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format -- 'range' is supposed to be a string");
							return null;
						}
					}
					
					try{
						if (catalog.addMetadataField(topic, field, new MetadataValueType(type, range)))
							schemaUpdated = true;
					} catch (JexlException e) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect 'range' format: " + range);
						return null;
					} catch (ClassNotFoundException e) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "unsupported 'type': " + type);
						return null;
					}
				}				
			}
			 
			
			if (schemaUpdated) {
				if (catalog instanceof InMemorySchemaCatalog)
					((InMemorySchemaCatalog) catalog).writeTopicSchemaToFileThread();
			}
				
		} else if("properties".equalsIgnoreCase(this.getReference().getBaseRef().getLastSegment())) {
			JsonRepresentation jsonRep = null;
			try {
				jsonRep= new JsonRepresentation(rep);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			JSONArray jsonArray = null;
			boolean added = false;
			try {
				jsonArray = jsonRep.getJsonArray();
				if (jsonArray == null) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format");
					return null;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					boolean success = catalog.addPropertyName(jsonArray.getString(i));
					added = added || success;
				}
			} catch (JSONException e) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "incorrect post body format");
				return null;
			}
			
			if (added) {
				this.getResponse().setStatus(Status.SUCCESS_CREATED, "properties added.");
				if (catalog instanceof InMemorySchemaCatalog)
					((InMemorySchemaCatalog) catalog).writePropertySchemaToFileThread();
			} else
				this.getResponse().setStatus(Status.SUCCESS_OK, "properties all exist.");
		}		
		
		return null;
	}
	
	/**
	 * Accepts GET methods with JSON responses
	 *  
	 * @return the schema in JSON format
	 * @throws JSONException
	 */
	
	@Get("json")
	public Representation getSchema() throws JSONException {

		SchemaCatalog catalog = SchemaCatalogManager.getSchemaCatalog();
		if (this.getReference().getBaseRef().getSegments().contains("topics")) {			
			Map<String, Map<String, MetadataValueType>> topics = catalog.getMsgTopicsAndMetadata();
			String topicName = (String)getRequestAttributes().get("topic_name");
			if (topicName == null) {
				JSONArray jsonArray = new JSONArray();
				for (Entry<String, Map<String,MetadataValueType>> entry : topics.entrySet()) {
					String topic = entry.getKey();
					Map<String, MetadataValueType> metadata = entry.getValue();
					
					JSONObject topicObj = new JSONObject();
					topicObj.put("topic", topic);
					JSONArray metadataArray = new JSONArray();
					for (Entry<String, MetadataValueType> entry1 : metadata.entrySet()) {
						String metadataField = entry1.getKey();
						MetadataValueType type = entry1.getValue();
						
						JSONObject valueTypeObj = new JSONObject();
						valueTypeObj.put("field", metadataField);
						valueTypeObj.put("type", type.getCls().getSimpleName());
						if (type.getRange() != null)
							valueTypeObj.put("range", type.getRange());
						metadataArray.put(valueTypeObj);
					}
					topicObj.put("metadata", metadataArray);
							
					jsonArray.put(topicObj);
				}
				
				Representation rep = new StringRepresentation(jsonArray.toString());
				rep.setMediaType(MediaType.APPLICATION_JSON);
				return rep;
				//return new JsonRepresentation(jsonArray);
			} else {
				if (topics.containsKey(topicName) == false) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "invalid topic: " + topicName);
					return null;
				}
				
				JSONObject topicObj = new JSONObject();
				topicObj.put("topic", topicName);
				JSONArray metadataArray = new JSONArray();
				for (Entry<String, MetadataValueType> entry : topics.get(topicName).entrySet()) {
					String metadataField = entry.getKey();
					MetadataValueType type = entry.getValue();
					JSONObject valueTypeObj = new JSONObject();
					valueTypeObj.put("field", metadataField);
					valueTypeObj.put("type", type.getCls().getSimpleName());
					if (type.getRange() != null)
						valueTypeObj.put("range", type.getRange());
					metadataArray.put(valueTypeObj);
				}
				topicObj.put("metadata", metadataArray);
				
				
				Representation rep = new StringRepresentation(topicObj.toString());
				rep.setMediaType(MediaType.APPLICATION_JSON);
				return rep;
				//return new JsonRepresentation(topicObj);
			}
				
		} else if("properties".equalsIgnoreCase(this.getReference().getBaseRef().getLastSegment())) {
			Set<String> properties = catalog.getPropertyNames();
			JSONArray jsonArray = new JSONArray();
			for (String property : properties)
				jsonArray.put(property);
			
			Representation rep = new StringRepresentation(jsonArray.toString());
			rep.setMediaType(MediaType.APPLICATION_JSON);
			return rep;
			
			//return new JsonRepresentation(jsonArray);	
		}
		
		
		return null;
	}
}
