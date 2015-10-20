/*
  Copyright 2012 - 2014 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.JavaWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

/**
 * This controller is the class to finish the authentication process and logout the user.
 * <p />
 * Public methods : {@link #callback()}, {@link #logoutAndOk()} and {@link #logoutAndRedirect()} must be used in the routes file.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackController extends Controller {

    protected static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    /**
     * This method handles the callback call from the provider to finish the authentication process. The credentials and then the profile of
     * the authenticated user is retrieved and the originally requested url (or the specific saved url) is restored.
     * 
     * @return the redirection to the saved request
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Promise<Result> callback() {
        // clear the session
        session().clear();
        logger.debug("Session is cleared.");
        // clients group from config
        final Clients clientsGroup = Config.getClients();

        // web context
        final JavaWebContext context = new JavaWebContext(request(), response(), session(), Play.application().configuration().getString("saml.sso.callback", null));

        // get the client from its type
        final BaseClient client = (BaseClient) clientsGroup.findClient(context);
        logger.debug("client : {}", client);

        // get credentials
        Promise<Result> promise = Promise.promise(new Function0<Result>() {
            public Result apply() {
                Credentials credentials = null;
                try {
                    credentials = client.getCredentials(context);
                    logger.debug("credentials : {}", credentials);

                } catch (final RequiresHttpAction e) {
                    // requires some specific HTTP action
                    final int code = context.getResponseStatus();
                    logger.debug("requires HTTP action : {}", code);
                    if (code == HttpConstants.UNAUTHORIZED) {
                        return unauthorized(Config.getErrorPage401()).as(Constants.HTML_CONTENT_TYPE);
                    } else if (code == HttpConstants.TEMP_REDIRECT) {
                        return Results.status(HttpConstants.TEMP_REDIRECT);
                    } else if (code == HttpConstants.OK) {
                        final String content = context.getResponseContent();
                        logger.debug("render : {}", content);
                        return ok(content).as(Constants.HTML_CONTENT_TYPE);
                    }
                    final String message = "Unsupported HTTP action : " + code;
                    logger.error(message);
                    throw new TechnicalException(message);
                }

                // get user profile
                final CommonProfile profile = client.getUserProfile(credentials, context);
                logger.debug("profile : {}", profile);

                // get or create sessionId
                final String sessionId = StorageHelper.getOrCreationSessionId(session());

                // save user profile only if it's not null
                if (profile != null) {
                    StorageHelper.saveProfile(sessionId, profile);
                    String accessToken = (String) profile.getAttribute("access_token");
                    String refreshToken = (String) profile.getAttribute("refresh_token");
                    session().put("session.token",accessToken);                            // session key is from edu.indiana.d2i.htrc.portal.PortalConstants SESSION_TOKEN = "session.token"
                    session().put("session.refresh.token", refreshToken);                  // session key is from edu.indiana.d2i.htrc.portal.PortalConstants SESSION_REFRESH_TOKEN = "session.refresh.token"
                    session().put("session.username", profile.getId());                    // session key is from edu.indiana.d2i.htrc.portal.PortalConstants SESSION_USERNAME = "session.username"
                }

                // get requested url
                final String requestedUrl = StorageHelper.getRequestedUrl(sessionId, client.getName());

                // retrieve saved request and redirect
                return redirect(defaultUrl(requestedUrl, Config.getDefaultSuccessUrl()));
            }
        });

        return promise;
    }

    /**
     * This method logouts the authenticated user.
     */
    private static void logout() {
        // get the session id
        final String sessionId = session(Constants.SESSION_ID);
        logger.debug("sessionId for logout : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // remove user profile from cache
            StorageHelper.removeProfile(sessionId);
            logger.debug("remove user profile for sessionId : {}", sessionId);
        }
        session().remove(Constants.SESSION_ID);
    }

    /**
     * This method logouts the authenticated user and send him to a blank page.
     * 
     * @return the redirection to the blank page
     */
    public static Result logoutAndOk() {
        logout();
        return ok();
    }

    /**
     * This method logouts the authenticated user and send him to the url defined in the
     * {@link Constants#REDIRECT_URL_LOGOUT_PARAMETER_NAME} parameter name or to the <code>defaultLogoutUrl</code>.
     * This parameter is matched against the {@link Config.getLogoutUrlPattern()}.
     * 
     * @return the redirection to the "logout url"
     */
    public static Result logoutAndRedirect() {
        // get the session id
        final String sessionId = session(Constants.SESSION_ID);
        logger.debug("sessionId for logout : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            CommonProfile cp = StorageHelper.getProfile(sessionId);

            // clients group from config
            final Clients clientsGroup = Config.getClients();

            // web context
            final JavaWebContext context = new JavaWebContext(request(), response(), session(), Play.application().configuration().getString("saml.sso.logoutcallback", null));

            // get the client from its type
            final BaseClient client = (BaseClient) clientsGroup.findClient(context);

            RedirectAction logoutRedirect = client.getLogoutRedirectAction(cp, context);
            return ok(logoutRedirect.getContent());
        }

        // TODO: Show proper error page
        logger.error("Cannot find session id.");
        return ok("Cannot find session id.");
    }

    public static Result logoutCallback(){
        // clients group from config
        final Clients clientsGroup = Config.getClients();

        // web context
        final JavaWebContext context = new JavaWebContext(request(), response(), session(), Play.application().configuration().getString("saml.sso.callback", null));

        // get the client from its type
        final BaseClient client = (BaseClient) clientsGroup.findClient(context);
        logger.debug("client : {}", client);

        try {
            client.logout(context);
        } catch (Exception e){
            return internalServerError("Logout Failed.");
        }

        logout();

        return redirect(Config.getDefaultLogoutUrl());
    }

    /**
     * This method returns the default url from a specified url compared with a default url.
     * 
     * @param url
     * @param defaultUrl
     * @return the default url
     */
    public static String defaultUrl(final String url, final String defaultUrl) {
        String redirectUrl = defaultUrl;
        if (StringUtils.isNotBlank(url)) {
            redirectUrl = url;
        }
        logger.debug("defaultUrl : {}", redirectUrl);
        return redirectUrl;
    }
}
