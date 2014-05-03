/**
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package models;

import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import htrc.security.oauth2.client.OAuth2Client;
import htrc.security.oauth2.client.OAuthUserInfoRequest;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import play.Logger;
import play.Play;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class User extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String userId;
    public String email;
    public String accessToken;

    private static Logger.ALogger log = play.Logger.of("application");

    public User(String userId, String email, String accessToken) {
        this.userId = userId;
        this.email = email;
        this.accessToken = accessToken;
    }

    public static Finder<Long, User> find = new Finder<Long, User>(
            Long.class, User.class
    );

    public static User findByUserID(String userId) {
        return find.where().eq("userId", userId).findUnique();
    }

    /**
     * find users from user's email.
     *
     * @param email
     * @return list of user Ids with the given email
     */
    public static List<String> findByEmail(String email) {
        List<String> userIdList = new ArrayList<String>();
        for (User user : find.all()) {
            if (user.email.equals(email)) {
                userIdList.add(user.userId);
            }
        }
        return userIdList;
    }

    public static User authenticate(String userId, String password) {
        try {
            OAuthClientRequest accessTokenRequest = OAuthClientRequest
                    .tokenLocation(Play.application().configuration().getString("oauth2.token.endpoint"))
                    .setGrantType(GrantType.PASSWORD)
                    .setClientId(PlayConfWrapper.oauthClientID())
                    .setClientSecret(PlayConfWrapper.oauthClientSecrete())
                    .setUsername(userId)
                    .setPassword(password)
                    .setScope("openid")
                    .buildBodyMessage();

            OAuth2Client accessTokenClient = new OAuth2Client(new URLConnectionClient());
            OAuthClientResponse accessTokenResponse = accessTokenClient.accessToken(accessTokenRequest);

            String accessToken = accessTokenResponse.getParam("access_token");
            User u = find.where().eq("userId", userId).findUnique();
            if (u == null) {
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
                Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse);

                User nu = new User(userId, (String)userInfo.get("http://wso2.org/claims/emailaddress"), accessToken);
                nu.save();
                return nu;

            } else {
                u.accessToken = accessToken;
                u.save();
                return u;
            }
        } catch (Exception e) {
            log.error("Invalid user ID or Password", e);
            return null;
        }
    }


}
