package com.ericsson.swot.messaging.bus.hub;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PushRecordManager {
	private static PushRecordManager manager = null;
	
	private Map<Date, PushRecord> pushRecords;
	
	public static PushRecordManager getManager() {
		if (manager == null)
			manager = new PushRecordManager();
		
		return manager;
	}
	
	public PushRecordManager() {
		pushRecords = new TreeMap<Date, PushRecord>(); 
	}
	
	public void addNewPushRecord(Date time, PushRecord record) {
		pushRecords.put(time, record);
	}
	
	public Map<Date, PushRecord> getPushRecords() {
		return pushRecords;
	}
	
	
}
