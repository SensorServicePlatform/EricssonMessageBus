package com.ericsson.swot.messaging.peer.restlet;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

import com.ericsson.swot.messaging.common.Constants;
import com.ericsson.swot.messaging.peer.Config;

public class AuthRegisterResource extends ServerResource {
	
	/**
	 * local resource for exchanging OAuth token with the Messaging Hub
	 * 
	 * @return	a string representation of the obtained OAuth token for the Messaging Hub
	 */
	@Get("text")
	public Representation represent()
	{
		getLogger().info("retrieving access token to the hub");
		
		//get the hub's access token and write it down
		User u = getRequest().getClientInfo().getUser();
		//@SuppressWarnings("deprecation")
		//String hubToken = OAuthUser.getToken(u);
		String hubToken = ((OAuthUser) u).getAccessToken();
		getLogger().info("Fetching hub token  = " + hubToken);
		
		//MessagingPeer peer = (MessagingPeer) getContext().getAttributes().get(MessagingPeerConstants.MESSAGING_PEER);
		//MessagingPeer peer = ((DefaultMessagingPeerApp)getApplication()).getPeer();
		//peer.setHubToken(hubToken);

		//let the hub know how to get an access token for my callback resource
		Reference hubRegisterRef = new Reference(Config.MESSAGING_BUS_AUTH_REGISTER_URL);
		hubRegisterRef.addQueryParameter(OAuthServerResource.OAUTH_TOKEN, hubToken);
		
		OAuthParameters peerOauthParams = (OAuthParameters) getContext().getAttributes().get(Constants.PEER_OAUTH_PARAMS);
		StringBuilder tokenUri = new StringBuilder(peerOauthParams.getBaseRef().toUrl().toString());
		tokenUri.append(peerOauthParams.getAccessTokenPath());

		Form form = new Form();
		form.set(Constants.TOKEN_URI, tokenUri.toString());
		form.set(Constants.CLIENT_ID, peerOauthParams.getClientId());
		form.set(Constants.CLIENT_SECRET, peerOauthParams.getClientSecret());
		//form.set(Constants.CALLBACK_URL, (String) getContext().getAttributes().get(Constants.CALLBACK_URL));
		
		getLogger().info("posting to hub");
		ClientResource clientResource = new ClientResource(hubRegisterRef);
		try {
			clientResource.post(form);
			if (clientResource.getResponse().getStatus().isSuccess()) {
				clientResource.getResponseEntity().getText();
				getLogger().info("registered successfully");
				return new StringRepresentation(hubToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientResource.getResponse().release();
			clientResource.release();
		}
		
		getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "hub did not obtain access token for my callback");
		return new StringRepresentation(hubToken);
	}

}
