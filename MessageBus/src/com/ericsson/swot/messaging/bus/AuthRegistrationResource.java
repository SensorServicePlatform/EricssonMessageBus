package com.ericsson.swot.messaging.bus;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.ericsson.swot.messaging.bus.hub.HubPlusStoreManager;
import com.ericsson.swot.messaging.common.Constants;

public class AuthRegistrationResource extends ServerResource {

	@Post("form:html")
	public Representation register(Representation postBody) {
		Form postForm = new Form(postBody);
		String tokenUri = postForm.getFirstValue(Constants.TOKEN_URI);
		String clientId = postForm.getFirstValue(Constants.CLIENT_ID);
		String clientSecret = postForm.getFirstValue(Constants.CLIENT_SECRET);
		String callback = postForm.getFirstValue(Constants.CALLBACK_URL);
		
		String subCallbackToken = getSubCallbackToken(clientId, clientSecret, tokenUri);
		if(subCallbackToken == null || subCallbackToken.length() == 0) {
			getLogger().warning("Could not retrieve access token for messaging peer callback");
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Could not retrieve access token for messaging peer callback");
		}
		
		//document the sub callback token
		HubPlusStoreManager.getStore().putAccessToken(callback, subCallbackToken);
		
		//send back to the subscriber the sub callback access token
		return new StringRepresentation(subCallbackToken);
	}
		
	private String getSubCallbackToken(String clientId, String clientSecret, String tokenUri) {
		Form form = new Form();
//		form.add(OAuthServerResource.GRANT_TYPE, OAuthServerResource.GrantType.none.name());	//OAuth Autonomous Flow
		form.add(OAuthServerResource.GRANT_TYPE, GrantType.none.name());	//OAuth Autonomous Flow
		form.add(OAuthServerResource.CLIENT_ID, clientId);
		form.add(OAuthServerResource.CLIENT_SECRET, clientSecret);

		Reference tokenRef = new Reference(tokenUri);		
		ClientResource tokenResource = new ClientResource(tokenRef);
		
		getLogger().info("tokenUri = " + tokenUri);
		Representation response = tokenResource.post(form.getWebRepresentation());
		
		boolean ok = tokenResource.getResponse().getStatus().isSuccess();
		tokenResource.release();
		String accessToken = null;
		try {
			if (ok) {
				String responseStr = response.getText();
				getLogger().info("response body = " + responseStr);
				JsonRepresentation returned = new JsonRepresentation(responseStr);
				JSONObject answer = returned.getJsonObject();

				getLogger().info("Got answer on AccessToken = " + answer.toString());
				accessToken = answer.getString(OAuthServerResource.ACCESS_TOKEN);
				getLogger().info("AccessToken = " + accessToken);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			response.release();
		}
		
		return accessToken;
	}
}
