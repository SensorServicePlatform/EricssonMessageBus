package com.ericsson.swot.messaging.bus.catalog;

import com.ericsson.swot.messaging.bus.Config;

public class SchemaCatalogManager {
	private static SchemaCatalog catalog = null;
	
	public static SchemaCatalog getSchemaCatalog() {
		if (catalog == null) {
			catalog = new InMemorySchemaCatalog();
			//loadDefaultSchema();
			
			if (catalog instanceof InMemorySchemaCatalog) {
				try {
					((InMemorySchemaCatalog) catalog).loadTopicSchemaFromFile(Config.getTopicSchemaXmlFilePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					((InMemorySchemaCatalog) catalog).loadPropertySchemaFromFile(Config.getPropertySchemaXmlFilePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		
		return catalog;
	}
	
	private static void loadDefaultSchema() {
		/**
		 * topics and metadata
		 */
		//test_topic
		catalog.addTopic("Test_Topic");
		catalog.addMetadataField("Test_Topic", "testField1", new MetadataValueType(Integer.class, null));
		catalog.addMetadataField("Test_Topic", "testField2", new MetadataValueType(Float.class, "x>=0.0&&x<=1.0"));
		//Dimming
		catalog.addTopic("Dimming_SetLoadLevelTarget");
		catalog.addMetadataField("Dimming_SetLoadLevelTarget", "loadLevelTarget", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
		catalog.addTopic("Dimming_SetLoadLevelTargetWithRate");
		catalog.addMetadataField("Dimming_SetLoadLevelTargetWithRate", "loadLevelTarget", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
		catalog.addMetadataField("Dimming_SetLoadLevelTargetWithRate", "rate", new MetadataValueType(Float.class, null));
		catalog.addTopic("Dimming_On");
		catalog.addTopic("Dimming_Off");
		catalog.addTopic("Dimming_StatusReport");
		catalog.addMetadataField("Dimming_StatusReport", "currentLoadLevel", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
			
		//TemperatureSensor
		catalog.addTopic("TemperatureSensor_StatusReport");
		catalog.addMetadataField("TemperatureSensor_StatusReport", "currentTemperature", new MetadataValueType(Float.class, null));
		
		//LightSensor
		catalog.addTopic("LightSensorSensor_StatusReport");
		catalog.addMetadataField("LightSensorSensor_StatusReport", "light", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
		
		//SwitchPower
		catalog.addTopic("SwitchPower_SetTarget");
		catalog.addMetadataField("SwitchPower_SetTarget", "newTarget", new MetadataValueType(Integer.class, "x>=0&&x<=1"));
		catalog.addTopic("SwitchPower_StatusReport");
		catalog.addMetadataField("SwitchPower_StatusReport", "currentTarget", new MetadataValueType(Integer.class, "x>=0&&x<=1"));
		
		//BinarySensor
		catalog.addTopic("BinarySensor_StatusReport");
		catalog.addMetadataField("BinarySensor_StatusReport", "currentState", new MetadataValueType(Integer.class, "x>=0&&x<=1"));
		
		//PowerSensor
		catalog.addTopic("PowerSensor_StatusReport");
		catalog.addMetadataField("PowerSensor_StatusReport", "currentPower", new MetadataValueType(Float.class, null));
		
		//EnergyMeter
		catalog.addTopic("EnergyMeter_StatusReport");
		catalog.addMetadataField("EnergyMeter_StatusReport", "currentEnergy", new MetadataValueType(Float.class, null));
		
		/**
		 * properties
		 */
		catalog.addPropertyName("URN");
		catalog.addPropertyName("id");
		catalog.addPropertyName("name");
		catalog.addPropertyName("manufacturer");
		catalog.addPropertyName("modelName");
		catalog.addPropertyName("productClass");
		catalog.addPropertyName("protocol");
		catalog.addPropertyName("serialNumber");
		catalog.addPropertyName("location");
		
		//write the schema to XML file
		if (catalog instanceof InMemorySchemaCatalog) {
			((InMemorySchemaCatalog) catalog).writeTopicSchemaToFileThread();
			((InMemorySchemaCatalog) catalog).writePropertySchemaToFileThread();
		}
	}
}
