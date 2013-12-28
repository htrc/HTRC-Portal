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

package edu.indiana.d2i.htrc.portal;

import edu.illinois.i3.htrc.usermanager.exceptions.UserManagerException;
import edu.illinois.i3.htrc.usermanager.utils.PermissionType;
import edu.illinois.i3.htrc.usermanager.utils.ResourceActionPermission;
import edu.indiana.d2i.htrc.portal.exception.UserAlreadyExistsException;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.extensions.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;
import org.wso2.carbon.utils.NetworkUtils;
import play.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class HTRCUserManagerUtility {
    private static Logger.ALogger log = play.Logger.of("application");
    private Properties configProperties;
    private UserAdminStub userAdmin;
    private UserStoreInfo userStoreInfo;
    private WSRegistryServiceClient registry;
    private ResourceAdminServiceStub resourceAdmin;
    private Pattern userNameRegExp;
    private Pattern roleNameRegExp;

    private static final ResourceActionPermission[] ALL_PERMISSIONS = new ResourceActionPermission[]{
            ResourceActionPermission.GET, ResourceActionPermission.PUT,
            ResourceActionPermission.DELETE, ResourceActionPermission.AUTHORIZE
    };


    public static HTRCUserManagerUtility getInstanceWithDefaultProperties(){
        try {
            Properties userMgtUtilityProps = new Properties();
            userMgtUtilityProps.put(edu.illinois.i3.htrc.usermanager.Constants.CONFIG_HTRC_USER_HOME, "/htrc/%s");                              // %s will be replaced by the appropriate user name
            userMgtUtilityProps.put(edu.illinois.i3.htrc.usermanager.Constants.CONFIG_HTRC_USER_FILES, "/htrc/%s/files");                       // make sure the settings match the configuration used in the Registry Extension
            userMgtUtilityProps.put(edu.illinois.i3.htrc.usermanager.Constants.CONFIG_HTRC_USER_WORKSETS, "/htrc/%s/worksets");
            userMgtUtilityProps.put(edu.illinois.i3.htrc.usermanager.Constants.CONFIG_HTRC_USER_JOBS, "/htrc/%s/files/jobs");

            HTRCUserManagerUtility userManager = new HTRCUserManagerUtility(
                    PlayConfWrapper.oauthBackendUrl() + "/services/",
                    PlayConfWrapper.userRegUrl(),
                    PlayConfWrapper.userRegUser(),
                    PlayConfWrapper.userRegPwd(),
                    userMgtUtilityProps);

            return  userManager;
        } catch (Exception e) {
            String errMessage = "Failed to create User Manager Utility instance.";
            log.error(errMessage, e);
            throw new RuntimeException(errMessage, e);
        }
    }

    public HTRCUserManagerUtility(String isURL, String gregURL, String userName, String password,
                                 Properties configProperties) {
        if (!(configProperties.containsKey(PortalConstants.UR_CONFIG_HTRC_USER_HOME)
                && configProperties.containsKey(PortalConstants.UR_CONFIG_HTRC_USER_FILES)
                && configProperties.containsKey(PortalConstants.UR_CONFIG_HTRC_USER_WORKSETS)
                && configProperties.containsKey(PortalConstants.UR_CONFIG_HTRC_USER_JOBS))) {
            throw new RuntimeException("User registration related configuration is missing or" +
                    " incomplete.");
        }
        this.configProperties = configProperties;

        if (!isURL.endsWith("/")) isURL += "/";
        if (!gregURL.endsWith("/")) gregURL += "/";

        String userAdminEPR = isURL + "UserAdmin";
        String resourceAdminEPR = gregURL + "ResourceAdminService";

        try {
            ConfigurationContext isConfigContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            ConfigurationContext gregConfigContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);

            userAdmin = new UserAdminStub(isConfigContext, userAdminEPR);
            Options option = userAdmin._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
                    userName, password));
            userStoreInfo = userAdmin.getUserStoreInfo();

            String gregAuthCookie = authenticateWithWSO2Server(gregURL, userName, password);
            registry = new WSRegistryServiceClient(gregURL, gregAuthCookie);

            resourceAdmin = new ResourceAdminServiceStub(gregConfigContext, resourceAdminEPR);
            option = resourceAdmin._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(HTTPConstants.COOKIE_STRING, gregAuthCookie);

            userNameRegExp = Pattern.compile(userStoreInfo.getUserNameRegEx().replaceAll("\\\\\\\\", "\\\\"));
            roleNameRegExp = Pattern.compile(userStoreInfo.getRoleNameRegEx().replaceAll("\\\\\\\\", "\\\\"));

        } catch (Exception e) {
            String errMessage = "Error occurred during user registration utility intialization " +
                    "with WSO2IS URL: " + isURL + " and WSO2GREG URL: " + gregURL + ".";
            log.error(errMessage, e);
            throw new RuntimeException(e);
        }
    }

    private String authenticateWithWSO2Server(String wso2ServerURL, String userName,
                                              String password) {
        // TODO: We need to fix all the places where wso2 server url is used to expect
        // TODO: only host name and port from user. We should fill in the context.
        // TODO: For example in authentication admin scenario, we get registry url
        // TODO: https://silvermaple.pti.indiana.edu:9443/ from user config and
        // TODO: we add services/AuthenticationAdmin part.

        try {
            String authAdminEPR = wso2ServerURL + "AuthenticationAdmin";
            String remoteAddress = NetworkUtils.getLocalHostname();

            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            AuthenticationAdminStub adminStub = new AuthenticationAdminStub(configContext, authAdminEPR);
            adminStub._getServiceClient().getOptions().setManageSession(true);
            if (adminStub.login(userName, password, remoteAddress)) {
                return (String) adminStub._getServiceClient().getServiceContext().getProperty
                        (HTTPConstants.COOKIE_STRING);
            } else {
                throw new RuntimeException("Authentication failed against server " +
                        wso2ServerURL + ". This can be due to invalid user name and/or password.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new user for the HTRC platform
     *
     * @param userName The user name
     * @param password The user's password
     * @param claims The <code>ClaimValue[]</code> array of profile claims
     *                  (see <a href="https://htrc3.pti.indiana.edu:9443/carbon/claim-mgt/claim-view.jsp?store=Internal&dialect=http://wso2.org/claims">available claims</a>)
     * @param permissions The array of permission keys to assign to the user, for example: "/permission/admin/login" (can be <code>null</code>)
     * @see #getRequiredUserClaims()
     * @see #getAvailablePermissions()
     * @throws UserManagerException Thrown if an error occurred
     */
    public void createUser(String userName, String password, List<Map.Entry<String, String>> claims,
                           String[] permissions) throws UserAlreadyExistsException {
        if (userName == null) {
            throw new NullPointerException("User name null.");
        }

        if (password == null) {
            throw new NullPointerException("Password null.");
        }

        if(isUserExists(userName)){
            String message = "User with name " + userName + " already exists.";
            log.warn(message);
            throw new UserAlreadyExistsException(message);
        }

        ClaimValue[] claimValues = new ClaimValue[claims.size()];
        for (int i = 0; i < claimValues.length; i++) {
            Map.Entry<String, String> entry = claims.get(i);
            claimValues[i] = new ClaimValue();
            claimValues[i].setClaimURI(entry.getKey());
            claimValues[i].setValue(entry.getValue());
        }

        try {
            if (!(userNameRegExp.matcher(userName).matches() && roleNameRegExp.matcher(userName).matches()))
                throw new RuntimeException("Invalid username; Must conform to both of the following regexps: "
                        + userNameRegExp.pattern() + " and " + roleNameRegExp.pattern());

            // javadoc: addUser(String userName, String password, String[] roles, ClaimValue[] claims, String profileName)
            userAdmin.addUser(userName, password, null, claimValues, "default");
            if (log.isDebugEnabled()) {
                log.debug("Created user: " + userName);
            }

            // javadoc: addRole(String roleName, String[] userList, String[] permissions)
            userAdmin.addRole(userName, new String[]{userName}, permissions);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Created role: %s with permissions: %s", userName,
                        Arrays.toString(permissions)));
            }

            String regUserHome = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_HOME), userName);
            String regUserFiles = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_FILES),
                    userName);
            String regUserWorksets = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_WORKSETS),
                    userName);
            String regUserJobs = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_JOBS), userName);

            Collection filesCollection = registry.newCollection();
            String extra = userName.endsWith("s") ? "'" : "'s";
            filesCollection.setDescription(userName + extra + " file space");
            regUserFiles = registry.put(regUserFiles, filesCollection);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Created user filespace collection: %s", regUserFiles));
            }

            Collection worksetsCollection = registry.newCollection();
            worksetsCollection.setDescription(userName + extra + " worksets");
            regUserWorksets = registry.put(regUserWorksets, worksetsCollection);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Created user worksets collection: %s",
                        regUserWorksets));
            }

            Collection jobsCollection = registry.newCollection();
            jobsCollection.setDescription(userName + extra + " jobs");
            regUserJobs = registry.put(regUserJobs, jobsCollection);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Created user jobs collection: %s", regUserJobs));
            }

            String everyone = userStoreInfo.getEveryOneRole();
            for (ResourceActionPermission permission : ALL_PERMISSIONS) {
                resourceAdmin.addRolePermission(regUserHome, userName, permission.toString(), PermissionType.ALLOW.toString());
                resourceAdmin.addRolePermission(regUserHome, everyone, permission.toString(), PermissionType.DENY.toString());
            }

            resourceAdmin.addRolePermission(regUserWorksets, everyone, ResourceActionPermission.GET.toString(), PermissionType.ALLOW.toString());

            log.info(String.format("User %s created (permissions: %s)", userName,
                    Arrays.toString(permissions)));
        } catch (Exception e) {
            log.error("Error adding new user: " + userName, e);
            throw new RuntimeException("createUser", e);
        }
    }

    /* Check whether the user is already exists
    * @param userName*/
    public boolean isUserExists(String userName){
        try{
            String[] users = userAdmin.listUsers("*");
            return new HashSet<String>(Arrays.asList(users)).contains(userName);
        } catch (Exception e) {
            String errMessage = "Error checking whether given user exists.";
            log.error(errMessage, e);
            throw new RuntimeException(errMessage, e);
        }
    }

    /**
     * Get the list of required user claims (expected to be supplied as part of the createUser request)
     *
     * @return The array of required user claims
     * @throws UserManagerException Thrown if an error occurred
     */
    public String[] getRequiredUserClaims() throws UserManagerException {
        return null;

    }

    /**
     * Get the list of available role permissions
     *
     * @return A Map<String,String> of available role permissions,
     *         where the key represents the permission key, and the value provides a
     *         human readable name for the permission
     * @throws UserManagerException Thrown if an error occurred
     */
    public Map<String, String> getAvailablePermissions() throws UserManagerException{
        return null;

    }


}
