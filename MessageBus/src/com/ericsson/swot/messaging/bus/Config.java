package com.ericsson.swot.messaging.bus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import com.ericsson.swot.messaging.common.Constants;

public class Config {
	private Logger log = Logger.getLogger(this.getClass().getSimpleName());
	
	/**
	 * toggle this boolean is Oauth needs to be enabled/disabled
	 */
	public static boolean OAUTH_ENABLED = false;	
	
	public static String CONFIG_FILE_PATH = "config";
	
	private String OAUTH_SERVER_VALIDATION_URI = "http://localhost:8080/oauth"  + Constants.VALIDATE_PATH;;
	private String TOPIC_SCHEMA_XML_FILE_PATH = "./swot_messaging_schema/topicSchema.xml";
	private String PROPERTY_SCHEMA_XML_FILE_PATH = "./swot_messaging_schema/propertySchema.xml";
	private boolean PUSH_RECORD_KEEPING = false;
	private Properties configFile;

	private static Config config = null;
	
	private static Config getConfig() {
		if (config == null)
			config = new Config();
		
		return config;
	}
	
	private Config()
	{
		configFile = new java.util.Properties();
		try {
			//InputStream is = new FileInputStream(CONFIG_FILE_PATH);
//			InputStream is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_PATH);
			URL resourceUrl = this.getClass().getClassLoader().getResource(CONFIG_FILE_PATH);
			if (resourceUrl == null)
				throw new Exception();
			log.info("config file found. Loading...");
			File resourceFile = new File(resourceUrl.toURI());
			InputStream is = new FileInputStream(resourceFile);
//			if (is == null);
//				throw new Exception();
			
			
			configFile.load(is);
			
			if (this.configFile.getProperty("oauth_server_validate_uri") != null) {
				OAUTH_SERVER_VALIDATION_URI = this.configFile.getProperty("oauth_server_validate_uri");
				log.info("oauth_server_validate_uri = " + OAUTH_SERVER_VALIDATION_URI);
			}
			
			if (this.configFile.getProperty("topic_schema_file_path") != null) {
				TOPIC_SCHEMA_XML_FILE_PATH = this.configFile.getProperty("topic_schema_file_path");
				log.info("topic_schema_file_path = " + TOPIC_SCHEMA_XML_FILE_PATH);
			}
			
			if (this.configFile.getProperty("property_schema_file_path") != null) {
				PROPERTY_SCHEMA_XML_FILE_PATH = this.configFile.getProperty("property_schema_file_path");
				log.info("property_schema_file_path = " + PROPERTY_SCHEMA_XML_FILE_PATH);
			}
			
			if (this.configFile.getProperty("push_record_keeping") != null) {
				PUSH_RECORD_KEEPING = Boolean.valueOf(this.configFile.getProperty("push_record_keeping"));
				log.info("push_record_keeping = " + PUSH_RECORD_KEEPING);
			}
		} catch(Exception e) {
			log.info("config file not found. Loading default property values.");
			e.printStackTrace();
		}
	}
	
	public static String getOauthServerValidateUri() {
		return Config.getConfig().OAUTH_SERVER_VALIDATION_URI;
	}
	
	public static String getTopicSchemaXmlFilePath() {
		return Config.getConfig().TOPIC_SCHEMA_XML_FILE_PATH;
	}
	
	public static String getPropertySchemaXmlFilePath() {
		return Config.getConfig().PROPERTY_SCHEMA_XML_FILE_PATH;
	}
	
	public static boolean getPushRecordKeeping() {
		return Config.getConfig().PUSH_RECORD_KEEPING;
	}
	
}
