package com.ericsson.swot.messaging.common;

public class Constants {
	public static final String MESSAGING_BUS_AUTH_REGISTER_PATH = "/register";
	public static final String SCHEMA_CATALOG_PATH = "/catalog";
	public static final String MESSAGING_HUB_PUBLISH_PATH = "/publish";
	public static final String MESSAGING_HUB_SUBSCRIBE_PATH = "/subscribe";
	public static final String ACCESS_TOKEN_PATH = "/access_token";
	public static final String CALLBACK_PATH = "/callback";
	public static final String VALIDATE_PATH = "/validate";
	public static final String AUTHORIZE_PATH = "/authorize";
	
	public static final String SUB_PROPERTIES = "hub.sub_properties";
	public static final String PUB_PROPERTIES = "hub.pub_properties";
	public static final String PUB_FILTER_PREDICATE = "hub.pub_filter_predicate";
	public static final String SUB_FILTER_PREDICATE = "hub.sub_filter_predicate";
	public static final String MESSAGE_METADATA = "hub.message_metadata";
	
	public static final String MESSAGING_PEER = "MESSAGING_PEER";
	public static final String PEER_OAUTH_PARAMS = "PEER_OAUTH_PARAMS";
	public static final String TOKEN_URI = "TOKEN_URI";
	public static final String CLIENT_ID = "CLIENT_ID";
	public static final String CLIENT_SECRET = "CLIENT_SECRET";
	public static final String CALLBACK_URL = "CALLBACK_URL";
}
