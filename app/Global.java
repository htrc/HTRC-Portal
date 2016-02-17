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

import ch.ethz.ssh2.Session;
import controllers.UserManagement;
import edu.indiana.d2i.htrc.portal.CSVReader;
import edu.indiana.d2i.htrc.portal.HTRCSSOServiceManagerUtility;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import filters.LoggingFilter;
import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.Config;
import org.pac4j.saml.client.Saml2Client;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.api.mvc.EssentialFilter;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static play.mvc.Results.*;
import static play.mvc.Controller.*;


public class Global extends GlobalSettings {
    private static Logger.ALogger log = play.Logger.of("global");

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{LoggingFilter.class};
    }

    @Override
    public F.Promise<SimpleResult> onError(Http.RequestHeader requestHeader, Throwable throwable) {
        String userId = session(PortalConstants.SESSION_USERNAME);
        String userEmail = session(PortalConstants.SESSION_EMAIL);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        dateFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        log.debug("this is a test.");

        if(userId != null) {
            log.error("Internal server error. Logged In UserId: " + userId + " User Email: " + userEmail, throwable);
            UserManagement.sendMail(PlayConfWrapper.errorHandlingEmail(),"Exception in "+ PlayConfWrapper.portalUrl(),"Internal server error in "+ PlayConfWrapper.portalUrl() + "\n Date and time in US/ET: " + dateFormat.format(date) + " \n Logged In UserId: " + userId + " \n User Email: " + userEmail + "\n Error: " + throwable.getCause());
        } else {
            log.error("Internal server error.", throwable);
            UserManagement.sendMail(PlayConfWrapper.errorHandlingEmail(),"Exception in "+ PlayConfWrapper.portalUrl(),"Internal server error in "+ PlayConfWrapper.portalUrl() +"\n Date and time in US/ET: " + dateFormat.format(date) +" \n Error: "+ throwable.getCause());
        }

        return F.Promise.<SimpleResult>pure(internalServerError(
                views.html.error500.render(throwable, userId)
        ));
    }

    @Override
    public void onStart(Application app) {
        UserManagement.SignUp.instDomains = CSVReader.readAndSaveInstDomains(PlayConfWrapper.validDomainsFirstCSV());
        UserManagement.SignUp.instDomains.putAll(CSVReader.readAndSaveInstDomains(PlayConfWrapper.validDomainsSecondCSV()));
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        System.setProperty("javax.net.ssl.trustStore", Play.application().configuration().getString(PortalConstants.USER_REG_TRUSTSTORE));
        System.setProperty("javax.net.ssl.trustStorePassword", Play.application().configuration().getString(PortalConstants.USER_REG_TRUSTSTORE_PWD));
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        play.Logger.info("Java Trust Store", System.getProperty("javax.net.ssl.trustStore"));


         // Register portal as a service provider in Identity Server


        String backEndUrl = PlayConfWrapper.oauthBackendUrl();
        String appName = PlayConfWrapper.serviceProviderName();
        String samlIssuer = PlayConfWrapper.samlSSOCallbackURL();
        String samlAssertionUrl = PlayConfWrapper.samlSSOCallbackURL();
        String userNameToRegisterOAUTHApp = PlayConfWrapper.userRegUser();
        boolean isAppAlreadyRegistered = false;

        HTRCSSOServiceManagerUtility ssoServiceManager = HTRCSSOServiceManagerUtility.getInstanceWithDefaultProperties();


        try {
            // Check whether service provider is already registered
            ApplicationBasicInfo[] basicInfos = ssoServiceManager.listApplications();
            if(basicInfos != null){
               for(ApplicationBasicInfo basicInfo : basicInfos){
                   if(basicInfo.getApplicationName().equals(appName)){
                       isAppAlreadyRegistered = true;
                       log.debug(appName + " is already registered as a service provider.");

                       String[] oAuthCredentials = ssoServiceManager.getOAuthAppData(appName);              // Register OAUTH2 Client
                       if(oAuthCredentials != null){
                           PlayConfWrapper.setOauthClientID(oAuthCredentials[0]);
                           PlayConfWrapper.setOauthClientSecrete(oAuthCredentials[1]);
                       }else{
                           log.error("OAUTH credentials are null.");
                       }
                   }
               }
            } else{
                log.debug("There are no service providers.");
            }
            if(!isAppAlreadyRegistered){
                log.debug(appName + " is not registered as a service provider.");
                if(ssoServiceManager.registerSAMLClient(samlIssuer,samlAssertionUrl)){                                                                               // Register SAML client
                    String[] oAuthCredentials = ssoServiceManager.registerOauthApp(PlayConfWrapper.portalUrl(), appName, userNameToRegisterOAUTHApp);              // Register OAUTH2 Client
                    if(oAuthCredentials != null){
                        PlayConfWrapper.setOauthClientID(oAuthCredentials[0]);
                        PlayConfWrapper.setOauthClientSecrete(oAuthCredentials[1]);
                        int serviceProviderId = ssoServiceManager.registerServiceProvider(appName);                                                                  //Register Service provider
                        if(serviceProviderId != 0){
                            ssoServiceManager.updateServiceProvider(backEndUrl, serviceProviderId, appName, samlIssuer, oAuthCredentials[0],oAuthCredentials[1]);   //Update service provider with SAML and OAUTH credentials
                            log.info("Service provider " + appName + " is registered successfully!!");
                        }else{
                            log.error("Service provider ID = " + serviceProviderId);
                        }
                    }else{
                        log.error("OAUTH credentials are null.");
                    }
                }else{
                    log.error(samlIssuer + " is already registered as a SAML client. Please use different name as a SAML issuer.");
                }
            }
        } catch (RemoteException e) {
            log.error("Error when retrieving application information.", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Error when retrieving OAuth client information.", e);
            throw new RuntimeException(e);
        }

        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath(PlayConfWrapper.saml2KeyStorePath());
        saml2Client.setKeystorePassword(PlayConfWrapper.saml2KeyStorePassword());
        saml2Client.setPrivateKeyPassword(PlayConfWrapper.saml2PrivateKeyPassword());
        saml2Client.setIdpMetadataPath(PlayConfWrapper.idpMetadataPath());

        // Enable SAML2 Assertion to OAuth2 access token exchange

        if(PlayConfWrapper.getOauthClientID() != null && PlayConfWrapper.getOauthClientSecrete() != null){
            saml2Client.setOauth2ExchangeEnabled(true);
            saml2Client.setOauth2ClientID(PlayConfWrapper.getOauthClientID());
            saml2Client.setOauth2ClientSecret(PlayConfWrapper.getOauthClientSecrete());
            saml2Client.setOauth2TokenEndpoint(PlayConfWrapper.tokenEndpoint());
            saml2Client.setDevMode(true);

            final Clients clients = new Clients(PlayConfWrapper.portalUrl() + "/callback", saml2Client);
            Config.setClients(clients);

        }else{
            log.error(String.format("OAuth Client ID and secret are null. OAuth Client ID: %s", PlayConfWrapper.getOauthClientID()));
        }
        super.onStart(app);
    }
}