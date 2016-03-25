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

package edu.indiana.d2i.htrc.portal.config;

import com.typesafe.config.Config;
import play.Configuration;

public class SSOSPConfiguration extends Configuration {
    private static final String SP_ID = "service.provider.id";
    private static final String SAML_ISSUER_URL = "saml.issuer.url";
    private static final String SAML_ASSERTION_URL = "saml.assertion.url";
    private static final String OAUTH2_CLIENT_ID = "oauth2.client.id";
    private static final String OAUTH2_CLIENT_SECRET = "oauth2.client.secret";
    private static final String OAUTH2_TOKEN_ENDPOINT = "oauth2.token.endpoint";
    private static final String CONF_OAUTH2_CALLBACK_URL = "oauth2.callback.url";
    private static final String SP_CERTIFICATE_ALIAS = "certificate.alias";

    public SSOSPConfiguration(Config conf) {
        super(conf);
    }

    public SSOSPConfiguration(play.api.Configuration conf) {
        super(conf);
    }

    public String getSPIdentifier() {
        return getString(SP_ID, "htrc-portal");
    }

    public String getSAMLIssuerURL() {
        return getString(SAML_ISSUER_URL);
    }

    public String getSAMLAssertionConsumerURL() {
        return getString(SAML_ASSERTION_URL);
    }

    public String getOAuth2ClientId() {
        return getString(OAUTH2_CLIENT_ID);
    }

    public String getOauth2ClientSecret() {
        return getString(OAUTH2_CLIENT_SECRET);
    }

    public String getSpCertificateAlias() {
        return getString(SP_CERTIFICATE_ALIAS, getSPIdentifier());
    }

    public String getOAuth2TokenEndpoint() {
        return getString(OAUTH2_TOKEN_ENDPOINT);
    }

    public String getOAuth2CallbackURL() {
        return getString(CONF_OAUTH2_CALLBACK_URL, "https://localhost:9000");
    }
}
