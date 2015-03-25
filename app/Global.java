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
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import filters.LoggingFilter;
import models.User;
import org.pac4j.core.client.Clients;
import org.pac4j.play.Config;
import org.pac4j.saml.client.Saml2Client;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.api.mvc.EssentialFilter;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;

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
        User loggedInUser = User.findByUserID(session(PortalConstants.SESSION_USERNAME));

        if(loggedInUser != null) {
            log.error("Internal server error. Logged In UserId: " + loggedInUser.userId + " User Email: " + loggedInUser.email, throwable);
            UserManagement.sendMail(PlayConfWrapper.supportEmail(),"Exception","Internal server error. Logged In UserId: " + loggedInUser.userId + " User Email: " + loggedInUser.email + "Error: " + throwable.getMessage());
        } else {
            log.error("Internal server error.", throwable);
            UserManagement.sendMail(PlayConfWrapper.supportEmail(),"Exception",throwable.getMessage());
        }

        return F.Promise.<SimpleResult>pure(internalServerError(
                views.html.error500.render(throwable, loggedInUser)
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


        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath(PlayConfWrapper.saml2KeyStorePath());
        saml2Client.setKeystorePassword(PlayConfWrapper.saml2KeyStorePassword());
        saml2Client.setPrivateKeyPassword(PlayConfWrapper.saml2PrivateKeyPassword());
        saml2Client.setIdpMetadataPath(PlayConfWrapper.idpMetadataPath());

        // Enable SAML2 Assertion to OAuth2 access token exchange
        saml2Client.setOauth2ExchangeEnabled(true);
        saml2Client.setOauth2ClientID(PlayConfWrapper.oauthClientID());
        saml2Client.setOauth2ClientSecret(PlayConfWrapper.oauthClientSecrete());
        saml2Client.setOauth2TokenEndpoint(PlayConfWrapper.tokenEndpoint());
        saml2Client.setDevMode(true);

        final Clients clients = new Clients(PlayConfWrapper.portalUrl() + "/callback", saml2Client);
        Config.setClients(clients);

        super.onStart(app);
    }
}