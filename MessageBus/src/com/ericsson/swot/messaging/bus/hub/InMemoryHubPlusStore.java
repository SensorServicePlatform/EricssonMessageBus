package com.ericsson.swot.messaging.bus.hub;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryHubPlusStore implements HubPlusStore {
	
	private Map<SubPlus, SubPlus> subs;
	private Map<String, Set<SubPlus>> topicIndex;
	private Map<String, Set<SubPlus>> callbackIndex;
	private Map<Map<String, String>, Map<String, String>> peerProperties;
	private Map<String, String> accessTokens;
	
	public InMemoryHubPlusStore() {
		subs = new HashMap<SubPlus, SubPlus>();
		topicIndex = new HashMap<String, Set<SubPlus>>();
		callbackIndex = new HashMap<String, Set<SubPlus>>();
		peerProperties = new HashMap<Map<String, String>, Map<String, String>>();
		accessTokens = new HashMap<String, String>();
	}
	
	/**
	 * put the properties of a peer into the store. Only one copy of identical properties are retained
	 * 
	 * @param properties the peer's properties to be put into the store
	 * @return the only copy of the key-value pairs describing the peer's properties
	 */
	public Map<String, String> putPeerProperties(Map<String, String> properties) {
		if(peerProperties.containsKey(properties))
			return peerProperties.get(properties);
		
		peerProperties.put(properties, properties);
		return properties;
	}

	public Collection<SubPlus> getSubsForTopic(String topic) {
		return topicIndex.get(topic);
	}


	public Collection<SubPlus> getSubsForCallback(String callback) {
		return callbackIndex.get(callback);
	}
	
	public Collection<SubPlus> getSubs() {
		return subs.values();
	}

	public SubPlus put(SubPlus sub) {		
		SubPlus s = subs.put(sub, sub);		//returns the previous value associated with the specified key
		if(s != null) {
			removeFromIndex(callbackIndex, sub.getCallbackURL(), s);
			removeFromIndex(topicIndex, sub.getTopic(), s);
		}
		
		addToIndex(callbackIndex, sub.getCallbackURL(), sub);
		addToIndex(topicIndex, sub.getTopic(), sub);
	    
		return s;
	}

	public SubPlus get(String callbackURL, String topic) {
		return subs.get(new SubPlus(callbackURL, topic));	//a SubPlus is uniquely identified by callbackURL + Topic
	}

	public SubPlus remove(String callback, String topic) {
		SubPlus s = subs.remove(new SubPlus(callback, topic));
		if(s != null) {
	    	removeFromIndex(callbackIndex, s.getCallbackURL(), s);
	    	removeFromIndex(topicIndex, s.getTopic(), s);
	    }
		return s;
	}
	
	private void addToIndex(Map<String, Set<SubPlus>> index, String key, SubPlus s) {
		if(!index.containsKey(key))
			index.put(key, new HashSet<SubPlus>());
		index.get(key).add(s);
	}
	  
	private void removeFromIndex(Map<String, Set<SubPlus>> index, String key, SubPlus s) {
		if(index.containsKey(key)) {
			index.get(key).remove(s);    
			if(index.get(key).isEmpty())
				index.remove(key);
		}
	}
	
	public void putAccessToken(String url, String token) {
		if (url == null || token == null)
			return;
		accessTokens.put(url, token);
	}

	public String getAccessToken(String url) {
		if (url == null)
			return null;
		return accessTokens.get(url);
	}

}
