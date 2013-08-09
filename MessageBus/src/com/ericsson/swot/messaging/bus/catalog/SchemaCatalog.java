package com.ericsson.swot.messaging.bus.catalog;

import java.util.Map;
import java.util.Set;

public interface SchemaCatalog {
	
	/**
	 * Add a message topic to the catalog
	 * 
	 * @param topic
	 * @return true if topic was successfully added, false if topic already exists
	 */
	public boolean addTopic(String topic);
	

	/**
	 * Add a metadata field for a particular topic to the catalog
	 * 
	 * @param topic		the topic to add matadata field for
	 * @param field		the name of the metadata field
	 * @param valueType type of the value of the metadata field
	 * @return true if the topic and metadata field was successfully added, false if everything already exists
	 */
	public boolean addMetadataField(String topic, String field, MetadataValueType valueType);
	
	/**
	 * Add a property (of a messaging peer) to the catalog
	 * 
	 * @param property
	 * @return true if the property was successfully added, false if the property already exists
	 */
	public boolean addPropertyName(String property);
	
	
	/**
	 * List all the allowed message topics and corresponding metadata fields
	 * 
	 * @return the map of topics and their metadata fields/types/value-ranges
	 */
	public Map<String, Map<String, MetadataValueType>> getMsgTopicsAndMetadata();
	
	/**
	 * List the names of all the allowed properties (of a messaging peer)
	 * @return the set of the properties
	 */
	public Set<String> getPropertyNames();
	
	/**
	 * Check whether the topic and the metadata fields are valid
	 * 
	 * @param topic
	 * @param metadata
	 * @return true if valid, false otherwise
	 */
	public boolean checkTopicAndMetadata(String topic, Map<String, String> metadata);
	
	/**
	 * Check whether the property names are valid
	 * 
	 * @param property
	 * @return true if valid, false otherwise
	 */
	public boolean checkProperty(String property);

}
