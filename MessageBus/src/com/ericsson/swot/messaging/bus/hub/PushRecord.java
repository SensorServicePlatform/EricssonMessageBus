package com.ericsson.swot.messaging.bus.hub;

import java.util.HashMap;
import java.util.Map;

public class PushRecord {
	private String topic;
	private String message;
	private Map<String, DeliveryResult> deliveries;
	
	public class DeliveryResult {
		public boolean success;
		public int numTries;
		
		public DeliveryResult (boolean s, int n) {
			success = s;
			numTries = n;
		}
	}
	
	public PushRecord(String t, String m) {
		topic = t;
		message = m;
		deliveries = new HashMap<String, DeliveryResult>();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Map<String, DeliveryResult> getDeliveries() {
		return deliveries;
	}
	
	public void addDelivery(String callback, boolean success, int timeOfTries) {
		deliveries.put(callback, new DeliveryResult(success, timeOfTries));
	}
}
