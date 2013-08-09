package com.ericsson.swot.messaging.bus.hub;

import java.util.Collection;

public interface HubPlusStore {
	
	public Collection<SubPlus> getSubsForTopic(String topic);
	public Collection<SubPlus> getSubsForCallback(String callback);
	public Collection<SubPlus> getSubs();
	
	public SubPlus put(SubPlus sub);
	public SubPlus get(String callbackURL, String topic);
	public SubPlus remove(String callback, String topic);
	
	public void putAccessToken(String url, String token);
	public String getAccessToken(String url);
}
