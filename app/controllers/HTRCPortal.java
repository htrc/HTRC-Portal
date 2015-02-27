package controllers;

import edu.indiana.d2i.htrc.portal.HTRCUserManagerUtility;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.wso2is.extensions.stub.types.UserInfoRequest;
import edu.indiana.d2i.htrc.wso2is.extensions.stub.types.UserInfoResponse;
import htrc.security.oauth2.client.OAuth2Client;
import models.User;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jettison.json.JSONException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.saml.profile.Saml2Profile;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.about;
import views.html.gotopage;
import views.html.index;
//import views.html.login;


import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import static play.data.Form.form;

public class HTRCPortal extends JavaController {

    private static Logger.ALogger log = play.Logger.of("application");




    public static Result index() {
        return ok(index.render(User.findByUserID(session().get(PortalConstants.SESSION_USERNAME))));
    }


    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result login() {
        CommonProfile userProfile = getUserProfile();
        String accessToken = (String) userProfile.getAttribute("access_token");
        String refreshToken = (String) userProfile.getAttribute("refresh_token");
        session().put(PortalConstants.SESSION_TOKEN,accessToken);
        session().put(PortalConstants.SESSION_REFRESH_TOKEN,refreshToken);
        String userId = userProfile.getId();
        log.info("User "+ userId + " is successfully logged in.");
        log.info(session(PortalConstants.SESSION_TOKEN));
        if(userId == null){
            return ok(gotopage.render("Sorry. Looks like system can't retrieve your information. Please tryagain later.",null,null,null));
        }
        session().put(PortalConstants.SESSION_USERNAME, userId);

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        if(User.findByUserID(userId) == null){
            String userEmail = userManager.getEmail(userId);
            User nu = new User(userId, userEmail);
            nu.save();
            log.info("New user " + nu.userId + " is added to User object.");
        }


        return redirect(routes.HTRCPortal.index());
    }

    public static Result logout() {
        session().clear();
        log.info(session(PortalConstants.SESSION_USERNAME));
        org.pac4j.play.CallbackController.logoutAndRedirect();
        CommonProfile userProfile = getUserProfile();
        if(userProfile==null){
            log.info("user profile is null");
        }else{
            log.info(userProfile.getId());
        }

        flash("success", "You've been logged out");
        return redirect(routes.HTRCPortal.index());
    }

//    public static Result authenticate() {
//        Form<Login> loginForm = form(Login.class).bindFromRequest();
//        if (loginForm.hasErrors()) {
//            return badRequest(login.render(loginForm, null));
//        } else {
//            if(User.findByUserID(loginForm.get().userId) == null){
//                try {
//                    String userEmail = getUserEmail(session().get(PortalConstants.SESSION_TOKEN));
//                    User nu = new User(loginForm.get().userId, userEmail);
//                    nu.save();
//                    log.info("New user " + nu.userId + " is added to User object.");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            session().put("userId", loginForm.get().userId);
//
//
//            return redirect(routes.HTRCPortal.index());
//        }
//    }

//    @Security.Authenticated(Secured.class)
    public static Result about() {
        return ok(about.render(User.findByUserID(session().get(PortalConstants.SESSION_USERNAME))));
    }

//    public static class Login {
//        @Constraints.Required
//        public String userId;
//
//        @Constraints.Required
//        public String password;
//
//        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
//
//        public String validate() throws Exception {
//            if (userId.isEmpty() || password.isEmpty()) {
//                return "Please fill username and password.";
//            }
//            if(authenticate(userId,password).isEmpty()){
//                return "Invalid user name or password";
//            }
//            if(!userManager.roleNameExists(userId)){
//                return String.format("Please activate your account. Activation link has been sent to %s",userManager.getEmail(userId));
//            }
//            return null;
//        }
//    }
//
//    public static Http.Session authenticate(String userId, String password){
//        session().clear();
//        try {
//            OAuthClientRequest accessTokenRequest = OAuthClientRequest
//                    .tokenLocation(PlayConfWrapper.tokenEndpoint())
//                    .setGrantType(GrantType.PASSWORD)
//                    .setClientId(PlayConfWrapper.oauthClientID())
//                    .setClientSecret(PlayConfWrapper.oauthClientSecrete())
//                    .setUsername(userId)
//                    .setPassword(password)
//                    .setScope("openid")
//                    .buildBodyMessage();
//
//            OAuth2Client accessTokenClient = new OAuth2Client(new URLConnectionClient());
//            OAuthClientResponse accessTokenResponse = accessTokenClient.accessToken(accessTokenRequest);
//
//
//            session().put(PortalConstants.SESSION_USERNAME, userId);
//            session().put(PortalConstants.SESSION_TOKEN,accessTokenResponse.getParam("access_token"));
//            session().put(PortalConstants.SESSION_REFRESH_TOKEN,accessTokenResponse.getParam("refresh_token"));
//            log.info(accessTokenResponse.getParam("access_token"));
//            return session();
//
//        } catch (Exception e) {
//            log.error("Invalid user ID or Password", e);
//            return session();
//        }
//    }

    public static String getUserEmail(String accessToken) throws IOException {
        String userInfoEndpoint = Play.application().configuration().getString("oauth2.userinfo.endpoint");
        String clientId = Play.application().configuration().getString("oauth2.client.id");
        String clientSecret = Play.application().configuration().getString("oauth2.client.secrete");

        HttpClient userInfoClient = new HttpClient();
        HttpMethod getUserInfo = new GetMethod(userInfoEndpoint);

        getUserInfo.setQueryString(new NameValuePair[]{
                new NameValuePair("schema", "openid"),
                new NameValuePair(OAuth.OAUTH_CLIENT_ID, clientId),
                new NameValuePair(OAuth.OAUTH_CLIENT_SECRET, clientSecret)
        });

        getUserInfo.addRequestHeader(OAuth.HeaderType.CONTENT_TYPE, OAuth.ContentType.JSON);
        getUserInfo.addRequestHeader(OAuth.HeaderType.AUTHORIZATION, OAuth.OAUTH_HEADER_NAME + " " + accessToken);

        userInfoClient.executeMethod(getUserInfo);

        String userInfoResponse = getUserInfo.getResponseBodyAsString();
        Logger.info(userInfoResponse);
        Map<String, Object> userInfo = null;
        try {
            userInfo = JSONUtils.parseJSON(userInfoResponse);
        } catch (JSONException e) {
            throw new IOException("Error when converting JSON response", e);
        }

        return (String)userInfo.get("http://wso2.org/claims/emailaddress");

    }




}
