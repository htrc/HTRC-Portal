/**
 * Copyright 2013 The Trustees of Indiana University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.indiana.d2i.htrc.portal;


import edu.indiana.d2i.htrc.portal.exception.ChangePasswordUserAdminExceptionException;
import edu.indiana.d2i.htrc.portal.exception.EmailAlreadyExistsException;
import edu.indiana.d2i.htrc.portal.exception.RoleNameAlreadyExistsException;
import edu.indiana.d2i.htrc.portal.exception.UserAlreadyExistsException;
import edu.indiana.d2i.htrc.wso2is.extensions.stub.types.UserInfoRequest;
import edu.indiana.d2i.htrc.wso2is.extensions.stub.types.UserInfoResponse;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.extensions.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import edu.indiana.d2i.htrc.wso2is.extensions.stub.ExtendedUserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.carbon.utils.NetworkUtils;
import play.Logger;

import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Pattern;


public class HTRCUserManagerUtility {
  private static Logger.ALogger log = play.Logger.of("application");

  private static final String CONFIG_HTRC_USER_HOME = "user.home";
  private static final String CONFIG_HTRC_USER_FILES = "user.files";
  private static final String CONFIG_HTRC_USER_WORKSETS = "user.worksets";
  private static final String CONFIG_HTRC_USER_JOBS = "user.jobs";

  private Properties configProperties;
  private UserAdminStub userAdmin;
  private OAuthAdminServiceStub oauthAdminServiceStub;
  private ExtendedUserAdminStub extendedUserAdminStub;
  private UserRealmInfo userRealmInfo;

  private ResourceAdminServiceStub resourceAdmin;
  private Pattern userNameRegExp;
  private Pattern roleNameRegExp;

  private String isURL;
  private String userAdminEPR;
  private String oauthAdminEPR;
  private String extendedUserAdminEPR;
  private String resourceAdminEPR;
  private String authAdminEPR;
  private String isUserName;
  private String isPassword;

  private static HTRCUserManagerUtility instance = null;
  private static Object mutex = new Object();


  private static final ResourceActionPermission[] ALL_PERMISSIONS = new ResourceActionPermission[]{
      ResourceActionPermission.GET, ResourceActionPermission.PUT,
      ResourceActionPermission.DELETE, ResourceActionPermission.AUTHORIZE
  };


  public static HTRCUserManagerUtility getInstanceWithDefaultProperties() {
    if (instance == null) {
      try {
        Properties userMgtUtilityProps = new Properties();
        userMgtUtilityProps.put(CONFIG_HTRC_USER_HOME, "/htrc/%s");                              // %s will be replaced by the appropriate user name
        userMgtUtilityProps.put(CONFIG_HTRC_USER_FILES, "/htrc/%s/files");                       // make sure the settings match the configuration used in the Registry Extension
        userMgtUtilityProps.put(CONFIG_HTRC_USER_WORKSETS, "/htrc/%s/worksets");
        userMgtUtilityProps.put(CONFIG_HTRC_USER_JOBS, "/htrc/%s/files/jobs");
        synchronized (mutex) {
          if (instance == null) {
            instance = new HTRCUserManagerUtility(
                PlayConfWrapper.oauthBackendUrl() + "/services/",
                PlayConfWrapper.userRegUser(),
                PlayConfWrapper.userRegPwd(),
                userMgtUtilityProps);
          }
        }
      } catch (Exception e) {
        String errMessage = "Failed to create User Manager Utility instance.";
        log.error(errMessage, e);
        throw new RuntimeException(errMessage, e);
      }
    }
    return instance;
  }

  private HTRCUserManagerUtility(String isURL, String isUserName, String isPassword,
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

    this.isURL = isURL;
    this.isUserName = isUserName;
    this.isPassword = isPassword;

    this.userAdminEPR = isURL + "UserAdmin";
    this.oauthAdminEPR = isURL + "OAuthAdminService";
    this.extendedUserAdminEPR = isURL + "ExtendedUserAdmin";
    this.resourceAdminEPR = isURL + "ResourceAdminService";
    this.authAdminEPR = isURL + "AuthenticationAdmin";

    try {
      ConfigurationContext configurationContext = ConfigurationContextFactory
          .createConfigurationContextFromFileSystem(null, null);

      MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();

      HttpConnectionManagerParams params = new HttpConnectionManagerParams();
      params.setDefaultMaxConnectionsPerHost(20);
      multiThreadedHttpConnectionManager.setParams(params);
      HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
      configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

      userAdmin = new UserAdminStub(configurationContext, userAdminEPR);
      Options option = userAdmin._getServiceClient().getOptions();
      option.setManageSession(true);
      option.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
          isUserName, isPassword));
      userRealmInfo = userAdmin.getUserRealmInfo();

      resourceAdmin = new ResourceAdminServiceStub(configurationContext, resourceAdminEPR);
      option = resourceAdmin._getServiceClient().getOptions();
      option.setManageSession(true);
      option.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
          isUserName, isPassword));

      userNameRegExp = Pattern.compile(userRealmInfo.getPrimaryUserStoreInfo().getUserNameRegEx().replaceAll("\\\\\\\\", "\\\\"));
      roleNameRegExp = Pattern.compile(userRealmInfo.getPrimaryUserStoreInfo().getRoleNameRegEx().replaceAll("\\\\\\\\", "\\\\"));

      oauthAdminServiceStub = new OAuthAdminServiceStub(configurationContext, oauthAdminEPR);
      Options option_1 = oauthAdminServiceStub._getServiceClient().getOptions();
      option_1.setManageSession(true);
      option_1.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
          isUserName, isPassword));

      extendedUserAdminStub = new ExtendedUserAdminStub(configurationContext, extendedUserAdminEPR);
      Options option_2 = extendedUserAdminStub._getServiceClient().getOptions();
      option_2.setManageSession(true);
      option_2.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
          isUserName, isPassword));


    } catch (Exception e) {
      String errMessage = "Error occurred during user registration utility intialization " +
          "with WSO2IS URL: " + isURL + ".";
      log.error(errMessage, e);
      throw new RuntimeException(e);
    }
  }

  private String authenticateWithWSO2Server(String wso2ServerURL, String userName,
                                            String password) {

    try {
      AuthenticationAdminStub adminStub = new AuthenticationAdminStub(
          ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null),
          authAdminEPR);
      adminStub._getServiceClient().getOptions().setManageSession(true);
      adminStub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);

      String remoteAddress = NetworkUtils.getLocalHostname();

      if (adminStub.login(userName, password, remoteAddress)) {
        String authToken = (String) adminStub._getServiceClient().getServiceContext().getProperty
            (HTTPConstants.COOKIE_STRING);
        if (log.isDebugEnabled()) {
          log.debug("Authentication successful with token: " + authToken);
        }
        return authToken;
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
   * @param password The user's isPassword
   * @param claims   The <code>ClaimValue[]</code> array of profile claims
   *                 (see <a href="https://htrc3.pti.indiana.edu:9443/carbon/claim-mgt/claim-view.jsp?store=Internal&dialect=http://wso2.org/claims">available claims</a>)
   *                 //     * @param permissions The array of permission keys to assign to the user, for example: "/permission/admin/login" (can be <code>null</code>)
   * @throws UserAlreadyExistsException Thrown if an error occurred
   * @see #getRequiredUserClaims()
   * @see #getAvailablePermissions()
   */
  public void createUser(String userName, String password, String email, List<Map.Entry<String, String>> claims) throws Exception { // TODO: Review this
    if (userName == null) {
      throw new NullPointerException("User name null.");
    }

    if (password == null) {
      throw new NullPointerException("Password null.");
    }

    if (isUserExists(userName)) {
      String message = "User with name " + userName + " already exists.";
      log.warn(message);
      throw new UserAlreadyExistsException(message);
    }

    if (roleNameExists(userName)) {
      String message = userName + " is a already exist role name.";
      log.warn(message);
      throw new RoleNameAlreadyExistsException(message);
    }

    List<String> usersWithEmail = getUserIdsFromEmail(email);

    if (usersWithEmail != null && usersWithEmail.size() > 0) {
      String message = email + " is already used for user accounts: " + usersWithEmail;
      log.warn(message);
      throw new EmailAlreadyExistsException(message);
    }

    ClaimValue[] claimValues = new ClaimValue[claims.size()];
    for (int i = 0; i < claimValues.length; i++) {
      Map.Entry<String, String> entry = claims.get(i);
      claimValues[i] = new ClaimValue();
      claimValues[i].setClaimURI(entry.getKey());
      claimValues[i].setValue(entry.getValue());
    }

    if (!(userNameRegExp.matcher(userName).matches() && roleNameRegExp.matcher(userName).matches()))
      throw new RuntimeException("Invalid username; Must conform to both of the following regexps: "
          + userNameRegExp.pattern() + " and " + roleNameRegExp.pattern());

    try {
      addUser(userName, password, claimValues);

      WSRegistryServiceClient registry = new WSRegistryServiceClient(isURL, authenticateWithWSO2Server(isURL,
          isUserName, isPassword));
      String regUserFiles = createFilesCollection(userName, registry);

      if (log.isDebugEnabled()) {
        log.debug(String.format("Created user filespace collection: %s", regUserFiles));
      }

      String regUserWorksets = createWorksetsCollection(userName, registry);
      if (log.isDebugEnabled()) {
        log.debug(String.format("Created user worksets collection: %s",
            regUserWorksets));
      }

      String regUserJobs = createJobsColleciton(userName, registry);
      if (log.isDebugEnabled()) {
        log.debug(String.format("Created user jobs collection: %s", regUserJobs));
      }

      String regUserHome = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_HOME), userName);
      setupRegistryPermissions(regUserHome, regUserWorksets, userName);

      if (log.isDebugEnabled()) {
        log.debug("Created user: " + userName);
      }
    } catch (Exception e) {
      log.error("Error occurred while creating a user: " + userName, e);
      throw new RuntimeException("createUser", e);
    }
  }

  private void setupRegistryPermissions(String userHome, String worksetsCollection, String userName) {
    try {
      String everyone = userRealmInfo.getEveryOneRole();
      for (ResourceActionPermission permission : ALL_PERMISSIONS) {
        resourceAdmin.addRolePermission(userHome, userName, permission.toString(), PermissionType.ALLOW.toString());
        resourceAdmin.addRolePermission(userHome, everyone, permission.toString(), PermissionType.DENY.toString());
      }

      resourceAdmin.addRolePermission(worksetsCollection, everyone, ResourceActionPermission.GET.toString(), PermissionType.ALLOW.toString());
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in setupRegistryPermissions method while invoking admin service due to session timeout.", e);
        resourceAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        setupRegistryPermissions(userHome, worksetsCollection, userName);
        return;
      }
      throw new RuntimeException(e);
    }
  }

  private String createJobsColleciton(String userName, WSRegistryServiceClient registry) {
    String regUserJobs = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_JOBS), userName);

    Collection filesCollection = registry.newCollection();
    String extra = userName.endsWith("s") ? "'" : "'s";
    filesCollection.setDescription(userName + extra + " file space");
    try {
      return registry.put(regUserJobs, filesCollection);
    } catch (Exception e) {
      log.error("Create Job collection error: ", e);
      throw new RuntimeException(e);
    }
  }

  private String createWorksetsCollection(String userName, WSRegistryServiceClient registry) {
    String regUserWorksets = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_WORKSETS),
        userName);

    Collection filesCollection = registry.newCollection();
    String extra = userName.endsWith("s") ? "'" : "'s";
    filesCollection.setDescription(userName + extra + " file space");
    try {
      return registry.put(regUserWorksets, filesCollection);
    } catch (Exception e) {
      log.error("Create Workset collection error: ", e);
      throw new RuntimeException(e);
    }
  }

  private String createFilesCollection(String userName, WSRegistryServiceClient registry) {
    String regUserFiles = String.format(configProperties.getProperty(PortalConstants.UR_CONFIG_HTRC_USER_FILES),
        userName);

    Collection filesCollection = registry.newCollection();
    String extra = userName.endsWith("s") ? "'" : "'s";
    filesCollection.setDescription(userName + extra + " file space");
    try {
      return registry.put(regUserFiles, filesCollection);
    } catch (Exception e) {
      log.error("Create File collection error: ", e);
      throw new RuntimeException(e);
    }
  }

  private void addUser(String userName, String password, ClaimValue[] claimValues) {
    try {
      userAdmin.addUser(userName, password, null, claimValues, "default");
      userAdmin.cleanup();
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in addUser method while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        addUser(userName, password, claimValues);
        return;
      }
      throw new RuntimeException(e);
    }
  }

  /* Check whether the user is already exists
  * @param userId*/
  // TODO: Fix this
  public boolean isUserExists(String userName) {
    try {
      String[] users = userAdmin.listUsers(userName, Integer.MAX_VALUE);
      return users != null && users.length > 0;
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in isUserExists method while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        return isUserExists(userName);
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the list of required user claims (expected to be supplied as part of the createUser request)
   *
   * @return The array of required user claims
   * @throws org.wso2.carbon.user.mgt.common.UserAdminException Thrown if an error occurred
   */
  public String[] getRequiredUserClaims() throws UserAdminException {
    return null;

  }

  /**
   * Get the list of available role permissions
   *
   * @return A Map<String,String> of available role permissions,
   * where the key represents the permission key, and the value provides a
   * human readable name for the permission
   * @throws org.wso2.carbon.user.mgt.common.UserAdminException Thrown if an error occurred
   */
  public Map<String, String> getAvailablePermissions() throws UserAdminException {
    return null;

  }

  /**
   * Add role to user
   *
   * @param roleName    Role name of the user
   * @param userName    User's username
   * @param permissions List of permissions
   * @throws java.rmi.RemoteException Thrown if an error occurred
   */

  public void addRole(String roleName, String userName, String[] permissions) {
    try {
      userAdmin.addRole(roleName, new String[]{userName}, permissions, false);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in addRole method while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        addRole(roleName, userName, permissions);
        return;
      }
      throw new RuntimeException(e);
    }

    if (log.isDebugEnabled()) {
      log.debug(String.format("Created role: %s with permissions: %s", userName,
          Arrays.toString(permissions)));
    }
  }

  public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getUserRole(String userName) {
    try {
      return userAdmin.getRolesOfUser(userName, userName, 10);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in getUserRole method while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        return getUserRole(userName);
      }
      throw new RuntimeException(e);
    }
  }

  public boolean roleNameExists(String roleName) {
    FlaggedName[] roles;
    try {
      roles = userAdmin.getAllRolesNames(roleName, 10);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in roleNameExists method  while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        return roleNameExists(roleName);
      }
      throw new RuntimeException(e);
    }

    for (FlaggedName role : roles) {
      if (role.getItemName().equals(roleName)) {
        log.info("Role name " + roleName + " already exists");
        return true;
      }
    }
    return false;
  }

  /**
   * Change User isPassword
   *
   * @param userName    The User's username
   * @param newPassword The User's new isPassword
   * @throws ChangePasswordUserAdminExceptionException
   */
  public void changePassword(String userName, String newPassword) throws ChangePasswordUserAdminExceptionException {
    try {
      userAdmin.changePassword(userName, newPassword);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in changePassword method while invoking admin service due to session timeout.", e);
        userAdmin._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        changePassword(userName, newPassword);
        return;
      }
      throw new ChangePasswordUserAdminExceptionException("Cannot change isPassword for userId: " + userName, e);
    }
  }

  /**
   * Get User's email address
   *
   * @param userId = User name
   * @throws RemoteException
   * @retun user Email
   */
  public String getEmail(String userId) {
    if (isUserExists(userId)) {
      try {
        return extendedUserAdminStub.getUserEmailFromUserId(userId); // TODO: Fix this
      } catch (Exception e) {
        if (e.getMessage().contains("401 Error: Unauthorized")) {
          log.warn("Unauthorized error in getEmail method while invoking admin service due to session timeout.", e);
          extendedUserAdminStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
              authenticateWithWSO2Server(isURL, isUserName, isPassword));
          return getEmail(userId);
        }

        throw new RuntimeException(e);
      }
    }
    return null;
  }

  /**
   * Retrieve User's Id from the email address
   *
   * @param userEmail = User Email
   * @throws RemoteException
   * @retun user Id
   */
  public List<String> getUserIdsFromEmail(String userEmail) throws RemoteException {
    String[] userIds = new String[0];
    try {
      userIds = extendedUserAdminStub.getUserIdsFromEmail(userEmail);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error in getUserIdsFromEmail method while invoking admin service due to session timeout.", e);
        extendedUserAdminStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        return getUserIdsFromEmail(userEmail);
      }

      throw e;
    }
    if (userIds == null) {
      log.info("User Ids are null.");
      return Collections.EMPTY_LIST;
    }

    List<String> userIdList = Arrays.asList(userIds);
    log.info("User IDs: " + Arrays.toString(userIds));
    return userIdList;
  }

  public UserInfoResponse getUserInformation(UserInfoRequest userInfoRequest) throws RemoteException {
    UserInfoResponse userInfoResponse = null;
    try {
      userInfoResponse = extendedUserAdminStub.getUserInformation(userInfoRequest);
    } catch (Exception e) {
      if (e.getMessage().contains("401 Error: Unauthorized")) {
        log.warn("Unauthorized error while invoking admin service due to session timeout.", e);
        extendedUserAdminStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING,
            authenticateWithWSO2Server(isURL, isUserName, isPassword));
        return getUserInformation(userInfoRequest);
      }

      throw e;
    }
    if (userInfoResponse.getError()) {
      log.info("Cannot retrieve user Information.");
      return null;
    }
    log.info("User information retrieved successfully. User Name: " + userInfoResponse.getAuthorizedUser() + ", User Email: " + userInfoResponse.getUserEmail());
    return userInfoResponse;
  }

}
