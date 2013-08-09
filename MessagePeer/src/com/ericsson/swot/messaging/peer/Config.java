package com.ericsson.swot.messaging.peer;

import com.ericsson.swot.messaging.common.Constants;

public class Config {
	
	/**
	 * toggle this boolean is Oauth needs to be enabled/disabled
	 */
	public static boolean OAUTH_ENABLED = false;//true;	
	
	/**
	 * change this constant to where the Messaging Bus is deployed
	 */
	//public static String MESSAGING_BUS_BASE_URL = "http://localhost:8181/messaging";
	//public static String MESSAGING_BUS_BASE_URL = "http://localhost:8080/MessageBus";
	public static String MESSAGING_BUS_BASE_URL = "http://message-bus-app.herokuapp.com";

	/**
	 * change this constant to where the Oauth server is deployed
	 */
	public static String REMOTE_OAUTH_SERVER_BASE_URI = "http://localhost:6767/oauth";
	
	/**
	 * change these strings if different clientId/clientSecret are used
	 */
	public static String HUB_CLIENT_ID = "1234567890";
	public static String HUB_CLIENT_SECRET = "1234567890";
	public static String PEER_CLIENT_ID = "0987654321";
	public static String PEER_CLIENT_SECRET = "0987654321";		
	
	public static final String MESSAGING_BUS_AUTH_REGISTER_URL = MESSAGING_BUS_BASE_URL + Constants.MESSAGING_BUS_AUTH_REGISTER_PATH;
	public static final String SCHEMA_CATALOG_URL = MESSAGING_BUS_BASE_URL + Constants.SCHEMA_CATALOG_PATH;
	public static final String MESSAGING_HUB_PUBLISH_URL = MESSAGING_BUS_BASE_URL + Constants.MESSAGING_HUB_PUBLISH_PATH;
	public static final String MESSAGING_HUB_SUBSCRIBE_URL = MESSAGING_BUS_BASE_URL + Constants.MESSAGING_HUB_SUBSCRIBE_PATH;
	
	public static final String REMOTE_OAUTH_SERVER_VALIDATION_URI = REMOTE_OAUTH_SERVER_BASE_URI + Constants.VALIDATE_PATH;
	public static final String REMOTE_OAUTH_SERVER_AUTHORIZATION_URI = REMOTE_OAUTH_SERVER_BASE_URI + Constants.AUTHORIZE_PATH;
	public static final String REMOTE_OAUTH_SERVER_TOKEN_URI = REMOTE_OAUTH_SERVER_BASE_URI + Constants.ACCESS_TOKEN_PATH;
}
