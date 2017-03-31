package controllers;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.indiana.d2i.htrc.portal.*;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jettison.json.JSONException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import play.Logger;
import play.Play;
import play.mvc.Result;
import views.html.*;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//import views.html.login;

public class HTRCPortal extends JavaController {

    private static Logger.ALogger log = play.Logger.of("application");

    public static Result index() throws IOException {
        List<String> announcements = java.nio.file.Files.readAllLines(Paths.get(PlayConfWrapper.announcementDocument()), Charset.defaultCharset());
        String userId = session(PortalConstants.SESSION_USERNAME);
        if(!isAccountActivated(userId)){
            return ok(index.render(null, announcements));
        }
        return ok(index.render(userId, announcements));
    }


    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result login() throws IOException, JAXBException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        String userEmail = session(PortalConstants.SESSION_EMAIL);
        if(userId == null){
            return redirect(routes.HTRCPortal.userIdNotFound());
        } else if (!isAccountActivated(userId)){
            return redirect(routes.HTRCPortal.accountNotActivated(userId, userEmail));
        }
        log.info("Logged in user:"+ userId + ", Email:" + userEmail + ", Remote address:" + request().remoteAddress());
        log.debug("User's access token:" + session(PortalConstants.SESSION_TOKEN));
        return redirect(routes.HTRCPortal.index());
    }

    public static boolean isAccountActivated(String userId){
        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
        if(userId != null){
            log.debug("Role name exists: " + userManager.roleNameExists(userId));
            return userManager.roleNameExists(userId);
        }
        return false;
    }

    public static Result accountNotActivated( String userId, String userEmail){
        return ok(gotopage.render("Looks like you have not activated your account. Your account activation link has sent to " + userEmail + ". Please check your email and activate account. " +
                "If you have not received your activation link, please contact us by email " +
                " ", "mailto:"+PlayConfWrapper.supportEmail()+"?Subject=Issue_with_account_activation_link", PlayConfWrapper.supportEmail(),null));
    }

    public static Result userIdNotFound(){
        return ok(gotopage.render("Sorry. Looks like system can't retrieve your information. Please try again later.",null,null,null));
    }

    public static Result logout() {
        session().clear();
        org.pac4j.play.CallbackController.logoutAndRedirect();
        CommonProfile userProfile = getUserProfile();
        if(userProfile==null){
            log.info("user profile is null");
        }else{
            log.info(userProfile.getId());
        }

        flash("success", "You've been logged out");
        return redirect(controllers.routes.HTRCPortal.index());
    }

    public static Result about() {
        String userId = session(PortalConstants.SESSION_USERNAME);
        if(!isAccountActivated(userId)){
            return ok(about.render(null));
        }
        return ok(about.render(userId));
    }

    public static Result bookWorm() throws IOException {
        String bookWormPage = new String(java.nio.file.Files.readAllBytes(Paths.get(PlayConfWrapper.bookWormPage())));
        String userId = session(PortalConstants.SESSION_USERNAME);
        if(!isAccountActivated(userId)) {
            return ok(bookworm.render(null,bookWormPage));
        }
        return ok(bookworm.render(userId,bookWormPage));
    }

    public static Result datasets() throws IOException {
        String datasetsPage = new String(java.nio.file.Files.readAllBytes(Paths.get(PlayConfWrapper.datasetsPage())));
        String userId = session(PortalConstants.SESSION_USERNAME);
        if(!isAccountActivated(userId)) {
            return ok(bookworm.render(null,datasetsPage));
        }
        return ok(datasets.render(userId,datasetsPage));
    }

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

    public static Result getCustomTheme() throws IOException {
        String cssContent = Files.toString(new File(PlayConfWrapper.customCSSTheme()), Charsets.UTF_8);
        return ok(cssContent).as("text/css");
    }




}
