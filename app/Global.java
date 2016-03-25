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

import controllers.UserManagement;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.Utils;
import edu.indiana.d2i.htrc.portal.api.SSOConfigurator;
import edu.indiana.d2i.htrc.portal.config.PortalConfiguration;
import edu.indiana.d2i.htrc.portal.config.SSOSPConfiguration;
import edu.indiana.d2i.htrc.portal.wso2is.WSO2ISSSOConfiguratorFactory;
import filters.LoggingFilter;
import org.pac4j.core.client.Clients;
import org.pac4j.play.Config;
import org.pac4j.saml.client.Saml2Client;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static play.mvc.Controller.session;
import static play.mvc.Results.internalServerError;


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

        if (userId != null) {
            log.error("Internal server error. Logged In UserId: " + userId + " User Email: " + userEmail, throwable);
            Utils.sendMail(PlayConfWrapper.errorHandlingEmail(), "Exception in " + PlayConfWrapper.portalUrl(),
                    "Internal server error in " + PlayConfWrapper.portalUrl() + "\n Date and time in US/ET: "
                            + dateFormat.format(date) + " \n Logged In UserId: " + userId + " \n User Email: "
                            + userEmail + "\n Error: " + throwable.getCause());
        } else {
            log.error("Internal server error.", throwable);
            Utils.sendMail(PlayConfWrapper.errorHandlingEmail(), "Exception in " + PlayConfWrapper.portalUrl(),
                    "Internal server error in " + PlayConfWrapper.portalUrl() + "\n Date and time in US/ET: "
                            + dateFormat.format(date) + " \n Error: " + throwable.getCause());
        }

        return F.Promise.<SimpleResult>pure(internalServerError(
                views.html.error500.render(throwable, userId)
        ));
    }

    @Override
    public void onStart(Application app) {
        PortalConfiguration portalConfiguration = new PortalConfiguration(app.configuration().underlying());

        UserManagement.SignUp.emailValidationConfiguration = portalConfiguration.getEmailValidationConfiguration();


        // TODO: Why we need to setup commons logging.
        setupCommonsLogging();
        setupTrustStore();

        SSOConfigurator wso2isSSOConfigurator = new WSO2ISSSOConfiguratorFactory()
                .getSSOConfigurator(portalConfiguration);
        SSOSPConfiguration spConfig = wso2isSSOConfigurator.registerPortalSP();

        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath(PlayConfWrapper.saml2KeyStorePath());
        saml2Client.setKeystorePassword(PlayConfWrapper.saml2KeyStorePassword());
        saml2Client.setPrivateKeyPassword(PlayConfWrapper.saml2PrivateKeyPassword());
        saml2Client.setIdpMetadataPath(PlayConfWrapper.idpMetadataPath());

        // Enable SAML2 Assertion to OAuth2 access token exchange
        saml2Client.setOauth2ExchangeEnabled(true);
        saml2Client.setOauth2ClientID(spConfig.getOAuth2ClientId());
        saml2Client.setOauth2ClientSecret(spConfig.getOauth2ClientSecret());
        saml2Client.setOauth2TokenEndpoint(spConfig.getOAuth2TokenEndpoint());
        saml2Client.setDevMode(true);

        final Clients clients = new Clients(portalConfiguration.getURL() + "/callback", saml2Client);
        Config.setClients(clients);

        super.onStart(app);
    }

    private void setupCommonsLogging() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
    }

    private void setupTrustStore() {
        String trustStore = PlayConfWrapper.getClientTrustStorePath();
        String trustStorePass = PlayConfWrapper.getClientTrustStorePassword();

        // Password less trust store
        trustStorePass = trustStorePass == null ? "" : trustStorePass;

        if (trustStore != null && !trustStore.isEmpty()) {
            System.setProperty("javax.net.ssl.trustStore", PlayConfWrapper.getClientTrustStorePath());
            System.setProperty("javax.net.ssl.trustStorePassword", PlayConfWrapper.getClientTrustStorePassword());
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        }
    }
}