package com.ericsson.swot.messaging.bus.hub;

public class HubPlusStoreManager {
	private static HubPlusStore store = null;
	
	public static HubPlusStore getStore() {
		if (store == null)
			store = new InMemoryHubPlusStore();
		
		return store;
	}
}
