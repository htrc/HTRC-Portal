/**
 * Copyright 2016 The Trustees of Indiana University
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

package edu.indiana.d2i.htrc.portal.wso2is;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import edu.indiana.d2i.htrc.portal.PortalException;
import edu.indiana.d2i.htrc.portal.api.SSOConfigurator;
import edu.indiana.d2i.htrc.portal.config.SSOSPConfiguration;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
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

import java.net.URL;
import java.rmi.RemoteException;

public class WSO2ISSSOConfigurator implements SSOConfigurator {
    private static final Logger.ALogger log = play.Logger.of(WSO2ISSSOConfigurator.class);

    private static final String SERVICES_CTX = "/services/";
    private static final String AUTHENTICATION_ADMIN_SERVICE = SERVICES_CTX + "AuthenticationAdmin";
    private static final String APPMANAGEMENT_SERVICE = SERVICES_CTX + "IdentityApplicationManagementService";
    private static final String SAMLSSOCONFIG_SERVICE = SERVICES_CTX + "IdentitySAMLSSOConfigService";
    private static final String OAUTHADMIN_SERVICE = SERVICES_CTX + "OAuthAdminService";

    private final WSO2ISConfiguration configuration;
    private final IdentityApplicationManagementServiceStub appManagementStub;
    private final IdentitySAMLSSOConfigServiceStub samlSSOConfigStub;
    private final OAuthAdminServiceStub oauthAdminServiceStub;

    public WSO2ISSSOConfigurator(WSO2ISConfiguration configuration) {
        this.configuration = configuration;
        try {
            URL baseURL = new URL(configuration.getBackendURL());
            if(log.isDebugEnabled()) {
                log.debug("Initializing WSO2 IS configurator against WSO2 IS at " + baseURL);
            }

            log.info("Authenticating to WSO2 IS at " + baseURL);
            String authenticationCookie = authenticate();

            String appManagementSeriviceEPR =
                    new URL(baseURL, APPMANAGEMENT_SERVICE).toExternalForm();
            String samlSSOConfigServiceEPR =
                    new URL(baseURL, SAMLSSOCONFIG_SERVICE).toExternalForm();
            String oauthAdminServiceEPR = new URL(baseURL, OAUTHADMIN_SERVICE).toExternalForm();

            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);

            appManagementStub = new IdentityApplicationManagementServiceStub(configContext, appManagementSeriviceEPR);
            samlSSOConfigStub = new IdentitySAMLSSOConfigServiceStub(configContext, samlSSOConfigServiceEPR);
            oauthAdminServiceStub = new OAuthAdminServiceStub(configContext, oauthAdminServiceEPR);

            setAuthenticationCookie(appManagementStub, authenticationCookie);
            setAuthenticationCookie(samlSSOConfigStub, authenticationCookie);
            setAuthenticationCookie(oauthAdminServiceStub, authenticationCookie);
        } catch (Exception e) {
            log.error("Could not initialize WSO2ISSSOConfigurator.", e);
            throw new PortalException("Error occurred during configurator initialization.", e);
        }
    }

    /**
     * Register Portal as a service provider in WSO2 IS.
     * @return Service provider configuration
     * @throws PortalException in case something goes wrong during service provider registration.
     */
    @Override
    public SSOSPConfiguration registerPortalSP() throws PortalException {
        OAuth2AppCredentials oAuth2AppCredentials = null;
        ApplicationBasicInfo spInfo = getExistingSP();

        try {
            if (spInfo != null) {
                log.info("Portal is already registered as a service provider with id {}.", spInfo.getApplicationName());
                oAuth2AppCredentials = getOAuth2ClientCredentials(spInfo.getApplicationName());
            } else {
                if (registerSAMLSP()) {
                    log.info("Registered SAML service provider.");
                    oAuth2AppCredentials = registerOAuth2Client();
                    log.info("Registered OAuth2 client.");
                    int appId = createApplication();
                    log.info("Portal is registered as an applicaiton in WSO2 IS.");
                    updateApplication(appId, oAuth2AppCredentials);
                    log.info("Portal service provider registration completed.");
                } else {
                    throw new PortalException("Couldn't register SAML service provider " +
                            configuration.getSSOSPConfiguration().getSPIdentifier());
                }
            }
        } catch (Exception e) {
            String errMessage = "Couldn't register Portal in WSO2 IS.";
            log.error(errMessage, e);
            throw new PortalException("Couldn't register Portal in WSO2 IS.", e);
        }

        Config originalSPConfig = configuration.getSSOSPConfiguration().underlying();

        return new SSOSPConfiguration(originalSPConfig
                .withValue("oauth2.client.id", ConfigValueFactory.fromAnyRef(oAuth2AppCredentials.clientId))
                .withValue("oauth2.client.secret", ConfigValueFactory.fromAnyRef(oAuth2AppCredentials.clientSecret)));
    }

    private boolean registerSAMLSP() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        SSOSPConfiguration spConfig = configuration.getSSOSPConfiguration();
        SAMLSSOServiceProviderDTO serviceProviderDTO = new SAMLSSOServiceProviderDTO();

        serviceProviderDTO.setIssuer(spConfig.getSAMLIssuerURL());
        serviceProviderDTO.setAssertionConsumerUrl(spConfig.getSAMLAssertionConsumerURL());
        serviceProviderDTO.setDoSignResponse(false);
        serviceProviderDTO.setEnableAttributesByDefault(true);
        serviceProviderDTO.setDoSingleLogout(true);
        serviceProviderDTO.setUseFullyQualifiedUsername(true);
        serviceProviderDTO.setDoSignResponse(true);
        serviceProviderDTO.setDoSignAssertions(true); // TODO: Do we need this?
        serviceProviderDTO.setDoValidateSignatureInRequests(true);
        serviceProviderDTO.setCertAlias(spConfig.getSpCertificateAlias());
        serviceProviderDTO.setRequestedAudiences(new String[]{spConfig.getOAuth2TokenEndpoint()});
        serviceProviderDTO.setRequestedRecipients(new String[]{spConfig.getOAuth2TokenEndpoint()});


        return samlSSOConfigStub.addRPServiceProvider(serviceProviderDTO);
    }

    private OAuth2AppCredentials registerOAuth2Client() throws RemoteException, OAuthAdminServiceException {
        SSOSPConfiguration spConfig = configuration.getSSOSPConfiguration();
        OAuthConsumerAppDTO consumerDTO = new OAuthConsumerAppDTO();

        consumerDTO.setApplicationName(spConfig.getSPIdentifier());
        consumerDTO.setCallbackUrl(spConfig.getOAuth2CallbackURL());
        consumerDTO.setUsername(configuration.getAdminUser());
        consumerDTO.setGrantTypes(
                "password client_credentials refresh_token urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        consumerDTO.setOAuthVersion("OAuth-2.0");

        oauthAdminServiceStub.registerOAuthApplicationData(consumerDTO);

        return getOAuth2ClientCredentials(spConfig.getSPIdentifier());
    }

    private OAuth2AppCredentials getOAuth2ClientCredentials(String appName) throws RemoteException,
            OAuthAdminServiceException {
        OAuthConsumerAppDTO[] consumers = oauthAdminServiceStub.getAllOAuthApplicationData();
        for (OAuthConsumerAppDTO consumer : consumers) {
            if (consumer.getApplicationName().equals(appName)) {
                OAuth2AppCredentials appCredentials = new OAuth2AppCredentials();
                appCredentials.clientId = consumer.getOauthConsumerKey();
                appCredentials.clientSecret = consumer.getOauthConsumerSecret();

                return appCredentials;
            }
        }

        throw new PortalException("Couldn't retrieve OAuth2 client information for client: " + appName);
    }

    private int createApplication() throws RemoteException,
            IdentityApplicationManagementServiceIdentityApplicationManagementException {
        // We have to do this because the way WSO2 IS 5.0 handles service providers. Service provider registration API
        // is in a transition state from older API.
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(configuration.getSSOSPConfiguration().getSPIdentifier());

        // TODO: This returns an integer, but not sure what does the return value means.
        return appManagementStub.createApplication(serviceProvider);
    }

    private void updateApplication(int appId, OAuth2AppCredentials oAuth2AppCredentials) throws RemoteException,
            IdentityApplicationManagementServiceIdentityApplicationManagementException {
        SSOSPConfiguration spConfig = configuration.getSSOSPConfiguration();

        Property consumerSecret = new Property();
        consumerSecret.setConfidential(false);
        consumerSecret.setName("oauthConsumerSecret");
        consumerSecret.setRequired(false);
        consumerSecret.setValue(oAuth2AppCredentials.clientSecret);

        Property[] oauthProperties = {consumerSecret};


        InboundAuthenticationRequestConfig oauthRequestConfig = new InboundAuthenticationRequestConfig();
        oauthRequestConfig.setInboundAuthKey(oAuth2AppCredentials.clientId);
        oauthRequestConfig.setInboundAuthType("oauth2");
        oauthRequestConfig.setProperties(oauthProperties);

        InboundAuthenticationRequestConfig samlRequestConfig = new InboundAuthenticationRequestConfig();
        samlRequestConfig.setInboundAuthKey(spConfig.getSAMLIssuerURL());
        samlRequestConfig.setInboundAuthType("samlsso");

        InboundAuthenticationRequestConfig[] requestConfigurations = {oauthRequestConfig, samlRequestConfig};


        InboundAuthenticationConfig authenticationConfig = new InboundAuthenticationConfig();
        authenticationConfig.setInboundAuthenticationRequestConfigs(requestConfigurations);

        InboundProvisioningConfig provisioningConfig = new InboundProvisioningConfig();
        provisioningConfig.setProvisioningEnabled(false);
        provisioningConfig.setProvisioningUserStore("PRIMARY");

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(false);
        localAndOutboundAuthenticationConfig.setAuthenticationType("default");

        OutboundProvisioningConfig outboundProvisioningConfig = new OutboundProvisioningConfig();
        outboundProvisioningConfig.setProvisionByRoleList(null);

        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{spConfig.getSPIdentifier()});


        // TODO: Request e-mail as a claim
        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setAlwaysSendMappedLocalSubjectId(false);

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationID(appId);
        serviceProvider.setApplicationName(spConfig.getSPIdentifier());
        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setDescription("HTRC Portal");
        serviceProvider.setInboundAuthenticationConfig(authenticationConfig);
        serviceProvider.setInboundProvisioningConfig(provisioningConfig);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        serviceProvider.setOutboundProvisioningConfig(outboundProvisioningConfig);
        serviceProvider.setPermissionAndRoleConfig(permissionsAndRoleConfig);
        serviceProvider.setSaasApp(false);

        appManagementStub.updateApplication(serviceProvider);
    }

    private void setAuthenticationCookie(Stub stub, String cookie) {
        Options options = stub._getServiceClient().getOptions();
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    private ApplicationBasicInfo getExistingSP() {
        try {
            ApplicationBasicInfo[] apps = appManagementStub.getAllApplicationBasicInfo();
            for (ApplicationBasicInfo appBasicInfo : apps) {
                if (appBasicInfo.getApplicationName().equals(configuration.getSSOSPConfiguration().getSPIdentifier())) {
                    return appBasicInfo;
                }
            }
        } catch (Exception e) {
            throw new PortalException("Couldn't get existing service provider info.", e);
        }

        return null;
    }

    private String authenticate() {
        try {
            String authenticationAdminEPR =
                    new URL(new URL(configuration.getBackendURL()), AUTHENTICATION_ADMIN_SERVICE).toExternalForm();

            if(log.isDebugEnabled()) {
                log.debug("Authenticating to WSO2 IS using authentication admin at " + authenticationAdminEPR);
            }

            String remoteAddress = NetworkUtils.getLocalHostname();

            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            AuthenticationAdminStub adminStub = new AuthenticationAdminStub(configContext, authenticationAdminEPR);
            adminStub._getServiceClient().getOptions().setManageSession(true);
            adminStub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);
            if (adminStub.login(configuration.getAdminUser(), configuration.getAdminUser(), remoteAddress)) {
                return (String) adminStub._getServiceClient().getServiceContext().getProperty
                        (HTTPConstants.COOKIE_STRING);
            } else {
                log.error("Couldn't authenticate with WSO2 IS backend: " + configuration.getBackendURL());
                throw new PortalException("Authentication failed against server " +
                        configuration.getBackendURL() + ". This can be due to invalid username and/or password.");
            }
        } catch (Exception e) {
            throw new PortalException("Error occurred while authenticating.", e);
        }
    }

    private static class OAuth2AppCredentials {
        String clientId;
        String clientSecret;
    }
}
