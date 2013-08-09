package com.ericsson.swot.messaging.bus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.routing.Router;

import com.ericsson.oae.pubsub.hub.DefaultHub;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalog;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalogManager;
import com.ericsson.swot.messaging.bus.catalog.SchemaCatalogResource;
import com.ericsson.swot.messaging.bus.hub.PublishPlusResource;
import com.ericsson.swot.messaging.bus.hub.SubscribePlusResource;
import com.ericsson.swot.messaging.common.Constants;

public class MessagingBusApp extends DefaultHub {
	
	public static final String ADMIN_PATH = "/admin";
	
	public static void main(String[] args) throws Exception {
		Component component = new Component();				//Create a new Component  
		
    	component.getServers().add(Protocol.HTTP, 8181);	//Add a new HTTP server listening on port 8181.
    	component.getServers().iterator().next().getContext().getParameters().add("lowThreads", "200");
    	component.getServers().iterator().next().getContext().getParameters().add("maxThreads", "200");
		component.getClients().add(Protocol.HTTP);
/*		component.getClients().add(Protocol.HTTPS);
		component.getClients().add(Protocol.WAR);
		component.getClients().add(Protocol.FILE);
		component.getClients().add(Protocol.CLAP);
		component.getClients().add(Protocol.RIAP);*/
		
		component.getDefaultHost().attach("/messaging", new MessagingBusApp());
		component.start();
	}
	
	@Override
	public void start() throws Exception {
		getLogger().entering(this.getClass().getName(), "start");
		super.start();
		
		SchemaCatalog catalog = SchemaCatalogManager.getSchemaCatalog();

/*		//FOR TESTING PURPOSE
		catalog.addTopic("TURN_ON");
		catalog.addTopic("TURN_OFF");
		catalog.addTopic("TURN_UP");
		catalog.addTopic("TURN_DOWN");
		catalog.addTopic("SET_TEMPERATURE");
		catalog.addMetadataField("SET_TEMPERATURE", "temperature", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
		catalog.addMetadataField("SET_TEMPERATURE", "delay", new MetadataValueType(Integer.class, "x>=0&&x<=100"));
		catalog.addPropertyName("type");
		catalog.addPropertyName("name");
		catalog.addPropertyName("make");
		catalog.addPropertyName("model");
		catalog.addPropertyName("color");
		catalog.addPropertyName("location");*/
	}
		
	@Override
	public Restlet createInboundRoot() {
		getLogger().entering(this.getClass().getName(), "createInboundRoot");
		ExecutorService es = Executors.newScheduledThreadPool(10);
		getContext().getAttributes().put(EXEC_S, es);
		
		Router router = new Router(getContext());
		
		//Messaging Hub API
		if (Config.OAUTH_ENABLED) {
			//OAuthAuthorizer publishAuth = new OAuthAuthorizer(Config.OAUTH_SERVER_VALIDATION_URI, Config.OAUTH_SERVER_AUTHORIZATION_URI);
			OAuthAuthorizer publishAuth = new OAuthAuthorizer(new Reference(Config.getOauthServerValidateUri()));
			publishAuth.setContext(getContext());
			publishAuth.setNext(PublishPlusResource.class);
			//publishAuth.setAuthorizedRoles(Scopes.toRoles("publish"));
			router.attach(Constants.MESSAGING_HUB_PUBLISH_PATH, publishAuth);
			
			//OAuthAuthorizer subscribeAuth = new OAuthAuthorizer(Config.OAUTH_SERVER_VALIDATION_URI, Config.OAUTH_SERVER_AUTHORIZATION_URI);
			OAuthAuthorizer subscribeAuth = new OAuthAuthorizer(new Reference(Config.getOauthServerValidateUri()));
			subscribeAuth.setContext(getContext());
			subscribeAuth.setNext(SubscribePlusResource.class);
			//subscribeAuth.setAuthorizedRoles(Scopes.toRoles("subscribe"));
			router.attach(Constants.MESSAGING_HUB_SUBSCRIBE_PATH, subscribeAuth);
			
			//OAuthAuthorizer registerAuth = new OAuthAuthorizer(Config.OAUTH_SERVER_VALIDATION_URI, Config.OAUTH_SERVER_AUTHORIZATION_URI);
			OAuthAuthorizer registerAuth = new OAuthAuthorizer(new Reference(Config.getOauthServerValidateUri()));
			registerAuth.setContext(getContext());
			registerAuth.setNext(AuthRegistrationResource.class);
			//registerAuth.setAuthorizedRoles(Scopes.toRoles("register"));
			router.attach(Constants.MESSAGING_BUS_AUTH_REGISTER_PATH, registerAuth);
			
		} else {
			router.attach(Constants.MESSAGING_HUB_PUBLISH_PATH, PublishPlusResource.class);
			router.attach(Constants.MESSAGING_HUB_SUBSCRIBE_PATH, SubscribePlusResource.class);
		}
		
		//Messaging Catalog API
		router.attach(Constants.SCHEMA_CATALOG_PATH + "/topics", SchemaCatalogResource.class);
		router.attach(Constants.SCHEMA_CATALOG_PATH + "/topics/{topic_name}", SchemaCatalogResource.class);
		router.attach(Constants.SCHEMA_CATALOG_PATH + "/properties", SchemaCatalogResource.class);
		
		//FOR TESTING PURPOSE
		router.attach(ADMIN_PATH, AdminResource.class);
			
		getLogger().exiting(this.getClass().getName(), "createInboundRoot");
		return router;
	}
}
