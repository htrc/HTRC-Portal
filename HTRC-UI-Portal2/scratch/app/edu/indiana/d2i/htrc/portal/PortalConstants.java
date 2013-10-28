package edu.indiana.d2i.htrc.portal;

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

public class  PortalConstants {

    // property file entry
    public static String PROPERTY_AGENT_EPR = "agent.epr";
    public static String PROPERTY_AGENT_JOBLIST_PART = "agent.joblist";
    public static String PROPERTY_AGENT_JOB_DELETE = "agent.jobdelete";
    public static String PROPERTY_AGENT_JOB_SAVE = "agent.jobsave";
    public static String PROPERTY_AGENT_JOBSUBMIT_PART = "agent.submit";
    public static String PROPERTY_AGENT_CONNECT_TIMEOUT = "agent.connecttimeout";
    public static String PROPERTY_AGENT_WAIT_TIMEOUT = "agent.waittimeout";

    // blacklight url
    public static String BLACKLIGHT_URL = "blacklight.url";

    // oauth2 entries
    public static String OAUTH2_AUTHZ_ENDPOINT = "oauth2.auth.endpoint";
    public static String OAUTH2_TOKEN_ENDPOINT = "oauth2.token.endpoint";
    public static String OAUTH2_USERINFO_ENDPOINT = "oauth2.userinfo.endpoint";
    //public static String OAUTH2_CLIENT_ID = "oauth2.client.id";
    public static String OAUTH2_CLIENT_SECRETE = "oauth2.client.secrete";
    public static String OAUTH2_GRANT_TYPE = "oauth2.grant";
    public static String OAUTH2_SCOPE = "oauth2.scope";
    public static String OAUTH2_CALLBACK_URL = "oauth2.callbackurl";
    public static String OAUTH2_ADMIN_SERVICE = "oauth2.admin.service";
    public static String OAUTH2_HOST = "oauth2.host";
    public static String OAUTH2_BACKEND_URL = "oauth2.backend.url";


    // oauth2 constants
    public static String OAUTH2_ACCESS_TOKEN = "access_token";
    public static String OAUTH2_REFRESH_TOKEN = "refresh_token";
    public static String OAUTH2_EXPIRE = "expires_in";

    //
    public static String OAUTH2_REDIRECT_URL = "oauth2.redirect";

    public static String OAUTH2_CLIENT_ID = "oauth2.client.id";

    //Solr query URL
    public static String SOLR_QUERY_URL = "solr.query.url";

    // registry entries
    public static String REGISTRY_ENDPOINT = "reg.epr";
    public static String REGISTRY_ALGO_FOLDER = "reg.alg.folder";
    public static String WORKSETS_URL = "/worksets";
    public static String FILES_URL = "/files";
    public static String PUBLIC_WORKSET = "?public=true";
    public static String VOLUME_DETAILS_QUERY_SOLR_URL = "?q=id:";

    // user register
    public static String USER_REG_URL = "user.reg.url";
    public static String USER_REG_USER = "user.reg.user";
    public static String USER_REG_PASSWORD = "user.reg.pwd";
    public static String USER_REG_TRUSTSTORE = "user.reg.truststore.store";
    public static String USER_REG_TRUSTSTORE_PWD = "user.reg.truststore.pwd";
    public static String USER_REG_TRUSTSTORE_TYPE = "user.reg.truststore.type";

    public static String PASSWORD_RESET_LINK_URL = "password.reset.link.url";

    // error message
    public static String CANNOT_GETDATA_FROM_AGENT = "Unable to get data from agent.";
    public static String CANNOT_GETDATA_FROM_REGISTRY = "Unable to get data from registry.";

    public static final String PORTAL_CACHED_JOB_DETAILS = "portal.job.details";
    public static final String PORTAL_JOB_DETAILS_CACHED_TIME = "portal.job.details.cached.time";
    public static final String PORTAL_JOB_DETAILS_TIMEOUT = "portal.job.details.timeout";
}
