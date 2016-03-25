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
import edu.indiana.d2i.htrc.portal.config.SSOSPConfiguration;
import play.Configuration;

import java.util.Map;

import static com.avaje.ebean.config.GlobalProperties.get;

public class WSO2ISConfiguration extends Configuration {
    private static final String CONF_BACKEND_URL = "backend.url";
    private static final String CONF_ADMIN_USER_NAME = "admin.user";
    private static final String CONF_ADMIN_USER_PASSWORD = "admin.password";
    private static final String SSO_SUBSET_PREFIX = "sso";

    public WSO2ISConfiguration(Config conf) {
        super(conf);
    }

    public WSO2ISConfiguration(play.api.Configuration conf) {
        super(conf);
    }

    public String getBackendURL() {
        return getString(CONF_BACKEND_URL, "https://localhost:9443");
    }

    public String getAdminUser() {
        return getString(CONF_ADMIN_USER_NAME, "admin");
    }

    public String getAdminUserPassword() {
        return getString(CONF_ADMIN_USER_PASSWORD, "admin");
    }

    public SSOSPConfiguration getSSOSPConfiguration() {
        return new SSOSPConfiguration(getConfig(SSO_SUBSET_PREFIX).underlying());
    }
}
