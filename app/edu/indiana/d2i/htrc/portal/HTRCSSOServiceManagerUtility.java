package edu.indiana.d2i.htrc.portal;


import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceStub;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.utils.NetworkUtils;
import play.Logger;

import java.rmi.RemoteException;

public class HTRCSSOServiceManagerUtility {
    private static Logger.ALogger log = play.Logger.of("application");
    private IdentityApplicationManagementServiceStub identityApplicationManagementServiceStub;
    private IdentitySAMLSSOConfigServiceStub identitySAMLSSOConfigServiceStub;
    private OAuthAdminServiceStub oauthAdminServiceStub;


    public static HTRCSSOServiceManagerUtility getInstanceWithDefaultProperties() {
        return new HTRCSSOServiceManagerUtility(
                PlayConfWrapper.oauthBackendUrl() + "/services/",
                PlayConfWrapper.userRegUser(),
                PlayConfWrapper.userRegPwd());
    }

    public HTRCSSOServiceManagerUtility(String isURL, String userName, String password) {
        if (!isURL.endsWith("/")) isURL += "/";

        String identityApplicationManagementEPR = isURL + "IdentityApplicationManagementService";
        String oauthAdminEPR = isURL + "OAuthAdminService";
        String samlSSOConfigEPR = isURL + "IdentitySAMLSSOConfigService";

        try {
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            identityApplicationManagementServiceStub = new IdentityApplicationManagementServiceStub(configContext, identityApplicationManagementEPR);
            Options option = identityApplicationManagementServiceStub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
                    userName, password));


            identitySAMLSSOConfigServiceStub = new IdentitySAMLSSOConfigServiceStub(configContext, samlSSOConfigEPR);
            Options option1 = identitySAMLSSOConfigServiceStub._getServiceClient().getOptions();
            option1.setManageSession(true);
            option1.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
                    userName, password));

            oauthAdminServiceStub = new OAuthAdminServiceStub(configContext, oauthAdminEPR);
            Options option2 = oauthAdminServiceStub._getServiceClient().getOptions();
            option2.setManageSession(true);
            option2.setProperty(HTTPConstants.COOKIE_STRING, authenticateWithWSO2Server(isURL,
                    userName, password));

        } catch (Exception e) {
            String errMessage = "Error occurred during HTRC SSOService Manager utility intialization " +
                    "with WSO2IS URL: " + isURL + ".";
            log.error(errMessage, e);
            throw new RuntimeException(e);
        }


    }

    private String authenticateWithWSO2Server(String wso2ServerURL, String userName,
                                              String password) {
        try {
            String authAdminEPR = wso2ServerURL + "AuthenticationAdmin";
            String remoteAddress = NetworkUtils.getLocalHostname();

            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            AuthenticationAdminStub adminStub = new AuthenticationAdminStub(configContext, authAdminEPR);
            adminStub._getServiceClient().getOptions().setManageSession(true);
            adminStub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);
            if (adminStub.login(userName, password, remoteAddress)) {
                return (String) adminStub._getServiceClient().getServiceContext().getProperty
                        (HTTPConstants.COOKIE_STRING);
            } else {
                throw new RuntimeException("Authentication failed against server " +
                        wso2ServerURL + ". This can be due to invalid user name and/or password.");
            }
        } catch (Exception e) {
            String errMessage = "Error occurred during authenticating with WSO2IS";
            log.error(errMessage, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * List all the registered applications in WSO2 Identity Server.
     *
     */

    public  ApplicationBasicInfo[] listApplications() throws RemoteException {
        if(identityApplicationManagementServiceStub != null){

            try {
                return identityApplicationManagementServiceStub.getAllApplicationBasicInfo();
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                log.error("Error when initializing Identity Application Management Stub." + e);
                throw new RemoteException("Error when initiating stub", e);
            }

        }else{
            log.error("Identity Application Management stub is null.");
            return null;
        }
    }

    /**
     * Register SAML client.
     */

    public boolean registerSAMLClient(String issuer, String assertionUrl) throws RemoteException, LoginAuthenticationExceptionException, LogoutAuthenticationExceptionException {

        SAMLSSOServiceProviderDTO serviceProviderDTO = new SAMLSSOServiceProviderDTO();
        serviceProviderDTO.setIssuer(issuer);
        serviceProviderDTO.setAssertionConsumerUrl(assertionUrl);
        serviceProviderDTO.setDoSignAssertions(false);
        serviceProviderDTO.setDoSignResponse(false);
        serviceProviderDTO.setEnableAttributesByDefault(true);
        serviceProviderDTO.setDoSingleLogout(true);
        serviceProviderDTO.setUseFullyQualifiedUsername(true);
        serviceProviderDTO.setDoSignResponse(true);
        serviceProviderDTO.setDoSignAssertions(true);
        serviceProviderDTO.setDoValidateSignatureInRequests(true);
        serviceProviderDTO.setCertAlias("portal.crt");
        serviceProviderDTO.setRequestedAudiences(new String[]{"https://localhost:9443/oauth2/token"});
        serviceProviderDTO.setRequestedRecipients(new String[]{"https://localhost:9443/oauth2/token"});

        if(identitySAMLSSOConfigServiceStub != null){
            try {
                return identitySAMLSSOConfigServiceStub.addRPServiceProvider(serviceProviderDTO);
            } catch (IdentitySAMLSSOConfigServiceIdentityException e) {
                log.error("Error when initializing Identity SAML SSO config service Stub." + e);
                throw new RemoteException("Error when initiating stub", e);
            }
        }else{
            log.error("Identity SAML SSO config service stub is null.");
            return false;
        }

    }

    /**
     * Register OAUTH application
     * @param callbackdUrl
     * @param appName
     * @param userNameToRegisterOauth
     * @return List of OAUTH credentials
     * @throws RemoteException
     * @throws LogoutAuthenticationExceptionException
     * @throws LoginAuthenticationExceptionException
     */

    public String[] registerOauthApp(String callbackdUrl, String appName, String userNameToRegisterOauth) throws RemoteException, LogoutAuthenticationExceptionException, LoginAuthenticationExceptionException {



        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

        oAuthConsumerAppDTO.setApplicationName(appName);
        oAuthConsumerAppDTO.setCallbackUrl(callbackdUrl);
        oAuthConsumerAppDTO.setUsername(userNameToRegisterOauth);
        oAuthConsumerAppDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        oAuthConsumerAppDTO.setOAuthVersion("OAuth-2.0");

        if(oauthAdminServiceStub != null){
            try {
                oauthAdminServiceStub.registerOAuthApplicationData(oAuthConsumerAppDTO);
                return getOAuthAppData(appName);

                } catch (Exception e) {
                log.error("Error when initializing OAUTH Admin service Stub." + e);
                throw new RemoteException("Error when initiating stub", e);
            }
        }else{
            log.error("OAUTH Admin service Stub is null.");
            return null;
        }

    }

    /**
     * Get all the OAUTH application data
     * @return List of All the OAUTH application data
     * @throws RemoteException
     */

    public OAuthConsumerAppDTO[] getAllOAuthAppData() throws RemoteException{
        if(oauthAdminServiceStub != null){
            try {
                return oauthAdminServiceStub.getAllOAuthApplicationData();
            } catch (OAuthAdminServiceException e) {
                log.error("Error when initializing OAUTH Admin service Stub." + e);
                throw new RemoteException("Error when initiating stub", e);
            }
        }else{
            log.error("OAUTH Admin service Stub is null.");
            return null;
        }
    }

    public String[] getOAuthAppData(String appName) throws Exception {
        String oAuthAppCounsumerKey = null;
        String oAuthAppConsumerSecret = null;

        OAuthConsumerAppDTO[] oAuthConsumerAppDTOList = new OAuthConsumerAppDTO[0];
        try {
            oAuthConsumerAppDTOList = getAllOAuthAppData();
            if(oAuthConsumerAppDTOList == null || oAuthConsumerAppDTOList.length == 0){
                log.error("There are no OAUTH apps or error when retrieving OAUTH consumer app information");
                return null;
            }else {
                for (OAuthConsumerAppDTO appDTO : oAuthConsumerAppDTOList) {
                    if (appDTO.getApplicationName().equals(appName)) {
                        oAuthAppCounsumerKey = appDTO.getOauthConsumerKey();
                        oAuthAppConsumerSecret = appDTO.getOauthConsumerSecret();
                    }
                }
                return new String[]{oAuthAppCounsumerKey, oAuthAppConsumerSecret};
            }
        } catch (RemoteException e) {
            log.error("Error when retrieving all auth app data" + e);
            throw new Exception("Error when retrieving all auth app data" ,e);
        }

    }

    public int registerServiceProvider(String appName) throws RemoteException, LoginAuthenticationExceptionException {
        ServiceProvider serviceProvider = new ServiceProvider();

        serviceProvider.setApplicationName(appName);

        if(identityApplicationManagementServiceStub != null){

            try {
                return identityApplicationManagementServiceStub.createApplication(serviceProvider);
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                log.error("Error when initializing Identity Application Management service Stub." + e);
                throw new RemoteException("Error when initiating Identity Application Management service stub", e);
            }

        }else{
            log.error("Identity Application Management service Stub is null.");
            return 0;
        }
    }

    public void updateServiceProvider(String backEndUrl, int appId, String appName, String samlIssuer, String oAuthAppCounsumerKey, String oAuthAppConsumerSecret) throws RemoteException, LoginAuthenticationExceptionException, LogoutAuthenticationExceptionException {

        // Setting Property list for InboundAuthenticationRequestConfig
        Property oauthProperty = new Property();
        oauthProperty.setConfidential(false);
        oauthProperty.setName("oauthConsumerSecret");
        oauthProperty.setRequired(false);
        oauthProperty.setValue(oAuthAppConsumerSecret);

        Property[] oauthPropertyList = {oauthProperty};


        InboundAuthenticationRequestConfig oautRequestConfig = new InboundAuthenticationRequestConfig();
        oautRequestConfig.setInboundAuthKey(oAuthAppCounsumerKey);
        oautRequestConfig.setInboundAuthType("oauth2");
        oautRequestConfig.setProperties(oauthPropertyList);

        InboundAuthenticationRequestConfig samlRequestConfig = new InboundAuthenticationRequestConfig();
        samlRequestConfig.setInboundAuthKey(samlIssuer);
        samlRequestConfig.setInboundAuthType("samlsso");

        InboundAuthenticationRequestConfig[] requestConfigList = {oautRequestConfig,samlRequestConfig};


        InboundAuthenticationConfig config = new InboundAuthenticationConfig();
        config.setInboundAuthenticationRequestConfigs(requestConfigList);

        InboundProvisioningConfig provisioningConfig = new InboundProvisioningConfig();
        provisioningConfig.setProvisioningEnabled(false);
        provisioningConfig.setProvisioningUserStore("PRIMARY");

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(false);
        localAndOutboundAuthenticationConfig.setAuthenticationType("default");

        OutboundProvisioningConfig outboundProvisioningConfig = new OutboundProvisioningConfig();
        outboundProvisioningConfig.setProvisionByRoleList(null);

        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{appName});


        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setAlwaysSendMappedLocalSubjectId(false);

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationID(appId);
        serviceProvider.setApplicationName(appName);
        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setDescription(PlayConfWrapper.serviceProviderDescription());
        serviceProvider.setInboundAuthenticationConfig(config);
        serviceProvider.setInboundProvisioningConfig(provisioningConfig);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        serviceProvider.setOutboundProvisioningConfig(outboundProvisioningConfig);
        serviceProvider.setPermissionAndRoleConfig(permissionsAndRoleConfig);
        serviceProvider.setSaasApp(false);

        if(identityApplicationManagementServiceStub != null){

            try {
                identityApplicationManagementServiceStub.updateApplication(serviceProvider);
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                log.error("Error when initializing Identity Application Management service Stub." + e);
                throw new RemoteException("Error when initiating stub", e);
            }

        }else{
            log.error("Identity Application Management service Stub is null.");
        }
    }
}
