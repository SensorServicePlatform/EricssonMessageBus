package com.ericsson.swot.messaging.peer.restlet;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.AccessTokenServerResource;
import org.restlet.ext.oauth.ClientStoreFactory;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.ext.oauth.ValidationServerResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.routing.Router;

import com.ericsson.swot.messaging.common.Constants;
import com.ericsson.swot.messaging.peer.Config;
import com.ericsson.swot.messaging.peer.MessagingPeer;


public class DefaultMessagingPeerApp extends Application {
	public static final String REGISTER_PATH = "/register";	
	
	//variables that come with default values but can be overridden 
	protected String protocol = "http";
	protected String host = "localhost";
	protected int port = 8182;
	protected String basePath = "/peer";
	protected Map<String, Object> peerProperties = new HashMap<String, Object>();
	protected Class<? extends DefaultCallbackResource> callbackResourceCls = DefaultCallbackResource.class; 

	private MessagingPeer peer;
	
	public static void main(String[] args) throws Exception {
		final int port1 = 8182;
		final String path1 = "/peer";		
		Component component = new Component();
    	component.getServers().add(Protocol.HTTP, port1);
		component.getClients().add(Protocol.HTTP);
		component.getClients().add(Protocol.HTTPS);
		component.getDefaultHost().attach(path1, 
										new DefaultMessagingPeerApp() {
											@Override 
											protected void init(){												
												this.port = port1;
												this.basePath = path1;	
											//	this.peerProperties.put("type", "car");
											//	this.peerProperties.put("make", "Nissan");
											//	this.peerProperties.put("model", "Leaf");
												this.peerProperties.put("color", "green");
												this.peerProperties.put("location", "garage");
											}
										});
		component.start();
		
		
		final int port2 = 8183;
		final String path2 = "/peer";
		component = new Component();
    	component.getServers().add(Protocol.HTTP, port2);
		component.getClients().add(Protocol.HTTP);
		component.getClients().add(Protocol.HTTPS);
		component.getDefaultHost().attach(path2, 
										new DefaultMessagingPeerApp() {
											@Override 
											protected void init(){												
												this.port = port2;
												this.basePath = path2;	
												this.peerProperties.put("type", "thermo");
												this.peerProperties.put("make", "Honeywell");
											}
										});
		//component.start();
	}
	
	@Override
	public void start() throws Exception {
		getLogger().entering(this.getClass().getName(), "start");
		init();		
		super.start();
		String peerType = "listener";
		this.peer = new MessagingPeer(this.peerProperties, 
						this.protocol + "://localhost:" + this.port + this.basePath + Constants.ACCESS_TOKEN_PATH,			//token URL
						//this.protocol + "://" + this.host + ":" + this.port + this.basePath + Constants.CALLBACK_PATH);		//callback URL
						this.protocol + "://message-peer-" + peerType + ".herokuapp.com" + Constants.CALLBACK_PATH);		//callback URL
		this.peer.peerType = peerType;
	}
	
	/**
	 * to be overridden
	 */
	protected void init() {
		getLogger().entering(this.getClass().getName(), "init");

	}
	
	public MessagingPeer getPeer() {
		return this.peer;
	}
	
	@Override
	public Restlet createInboundRoot() {
		getLogger().entering(this.getClass().getName(), "createInboundRoot");
		
		Router router = new Router(getContext());
		
		//getContext().getAttributes().put(MessagingPeerConstants.MESSAGING_PEER, this.peer);		//IMPORTANT: pass over the messaging peer instance, so that the callback URL can be verified
		
		if (Config.OAUTH_ENABLED) {
			//set up a private OAuth DB store
			ClientStoreFactory.getInstance().createClient(Config.HUB_CLIENT_ID, Config.HUB_CLIENT_SECRET, null);			//add the hub as an accepted OAuth client
			
			//mini OAuth server endpoints
			router.attach(Constants.ACCESS_TOKEN_PATH, AccessTokenServerResource.class);	//mini OAuth server endpoint for obtaining access tokens
			router.attach(Constants.VALIDATE_PATH, ValidationServerResource.class);			//mini OAuth server endpoint for validating access tokens
			
			//the protected resource: subscriber callback
			//OAuthAuthorizer remoteAuth = new OAuthAuthorizer(Constants.VALIDATE_PATH, Constants.AUTHORIZE_PATH, true);	//local authorizer
			String validatePath = this.protocol + "://localhost:" + this.port + this.basePath + Constants.VALIDATE_PATH;
			OAuthAuthorizer remoteAuth = new OAuthAuthorizer(new Reference(validatePath));
			remoteAuth.setNext(this.callbackResourceCls);
			router.attach(Constants.CALLBACK_PATH, remoteAuth);
			
/*			OAuthParameters peerOauthParams = new OAuthParameters(Constants.HUB_CLIENT_ID, Constants.HUB_CLIENT_SECRET, localBaseUrl + "/");
			getContext().getAttributes().put(Constants.PEER_OAUTH_PARAMS, peerOauthParams);
			getContext().getAttributes().put(Constants.CALLBACK_URL, this.protocol + "://" + this.host + ":" + this.port + this.basePath + Constants.CALLBACK_PATH);
			OAuthParameters hubOauthParams = new OAuthParameters(Constants.PEER_CLIENT_ID, Constants.PEER_CLIENT_SECRET, 
																Constants.REMOTE_OAUTH_SERVER_BASE_URI, Scopes.toRoles("subscribe publish register"));
			OAuthProxy oauthProxy = new OAuthProxy(hubOauthParams, getContext());
			oauthProxy.setContext(getContext());
			oauthProxy.setNext(AuthRegisterResource.class);		
			router.attach(REGISTER_PATH, oauthProxy);
*/			
		} else
			router.attach(Constants.CALLBACK_PATH, this.callbackResourceCls);
		
		//FOR TESTING PURPOSE
		router.attach("/subscribe", TestControlResource.class);
		router.attach("/publish", TestControlResource.class);
		
		//FOR DEMO PURPOSE
		router.attach("/demo", WorkflowDemoResource.class);
		
		return router;
	}
	
}
