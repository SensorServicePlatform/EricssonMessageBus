package com.ericsson.swot.messaging.bus.hub;

import java.util.Map;

import com.ericsson.oae.pubsub.hub.Sub;

public class SubPlus extends Sub {
	private Map<String, Object> subProperties;
	private String pubFilterPredicate;

	public SubPlus(String callbackURL, String topic) {
		super(callbackURL, topic);
		this.subProperties = null;
		this.pubFilterPredicate = null;
	}
	
	public SubPlus(String callbackURL, String topic, Map<String, Object> subProperties, String pubFilterPredicate) {
		this(callbackURL, topic);
		this.subProperties = subProperties;
		this.pubFilterPredicate = pubFilterPredicate;
	}
	
	public Map<String, Object> getSubProperties() {
		return this.subProperties;
	}

	public String getPubFilterPredicate() {
		return this.pubFilterPredicate;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", subscriber properties: " + this.getSubProperties());
		sb.append(", publisher filter predicate: " + this.getPubFilterPredicate());
		
		return sb.toString();
	}
}
