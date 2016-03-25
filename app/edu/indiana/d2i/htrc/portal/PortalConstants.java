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
    public static String SOLR_META_QUERY_URL = "solr.meta.query.url";
    public static String SOLR_OCR_QUERY_URL = "solr.ocr.query.url";

    // registry entries
    public static String REGISTRY_ENDPOINT = "reg.epr";
    public static String REGISTRY_ALGO_FOLDER = "reg.alg.folder";
    public static String WORKSETS_URL = "/worksets";
    public static String FILES_URL = "/files";
    public static String PUBLIC_WORKSET = "?public=true";
    public static String VOLUME_DETAILS_QUERY_SOLR_URL = "?q=id:";

    // workset entries
    public static String WORKSETS_PER_PAGE = "worksets.per.page";

    // algorithm entries
    public static String ALGORITHMS_PER_PAGE = "algorithms.per.page";

    // user register
    public static String USER_REG_URL = "user.reg.url";
    public static String USER_REG_USER = "user.reg.user";
    public static String USER_REG_PASSWORD = "user.reg.pwd";
    public static String USER_REG_TRUSTSTORE = "user.reg.truststore.store";
    public static String USER_REG_TRUSTSTORE_PWD = "user.reg.truststore.pwd";
    public static String USER_REG_TRUSTSTORE_TYPE = "user.reg.truststore.type";

    // trust store
    public static String CLIENT_TRUST_STORE = "client.truststore.path";
    public static String CLIENT_TRUST_STORE_PASSWORD = "client.truststore.pwd";


    // error message
    public static String CANNOT_GETDATA_FROM_AGENT = "Unable to get data from agent.";
    public static String CANNOT_GETDATA_FROM_REGISTRY = "Unable to get data from registry.";
    public static String CANNOT_GETDATA_FROM_SERVER = "Unable to get data from server.";

    public static final String PORTAL_CACHED_JOB_DETAILS = "portal.job.details";
    public static final String PORTAL_JOB_DETAILS_CACHED_TIME = "portal.job.details.cached.time";
    public static final String PORTAL_JOB_DETAILS_TIMEOUT = "portal.job.details.timeout";

    // session entry
    public static String SESSION_USERNAME = "session.username";              //This key is using in org.pac4j.play.CallbackController
    public static String SESSION_TOKEN = "session.token";                   //This key is using in org.pac4j.play.CallbackController
    public static String SESSION_EMAIL = "session.email";
    public static String SESSION_TOKEN_EXPIRE_SEC = "session.token.expire";
    public static String SESSION_REFRESH_TOKEN = "session.refresh.token";    //This key is using in org.pac4j.play.CallbackController
    public static String SESSION_AGENT_INSTANCE = "session.agent.instance";
    public static String SESSION_LAST_ACTION = "session.lastaction";
    public static String SESSION_EXIST_BEFORE = "session.timeout";
    public static String SESSION_OAUTH_CLIENT_ID = "session.oauth.client.id";
    public static String SESSION_OAUTH_CLIENT_SECRET = "session.oauth.client.secret";

    // Sloan Web Service entries
    public static String SLOAN_WS_EPR = "sloanws.epr";
    public static String SLOAN_WS_CREATEVM = "sloanws.createvm";
    public static String SLOAN_WS_LISTVMIMAGES = "sloanws.listimages";
    public static String SLOAN_WS_SHOWVM = "sloanws.showvm";
    public static String SLOAN_WS_DELETEVM = "sloanws.deletevm";
    public static String SLOAN_WS_STARTVM = "sloanws.startvm";
    public static String SLOAN_WS_STOPVM = "sloanws.stopvm";
    public static String SLOAN_WS_SWITCHVM = "sloanws.switchvm";

    //Portal URL
    public static  String PORTAL_URL = "portal.url";


    // User Manager Constants
    public static final String UR_CONFIG_HTRC_USER_HOME = "user.home";
    public static final String UR_CONFIG_HTRC_USER_FILES = "user.files";
    public static final String UR_CONFIG_HTRC_USER_WORKSETS = "user.worksets";
    public static final String UR_CONFIG_HTRC_USER_JOBS = "user.jobs";

    public static final String UR_CONFIG_WSO2IS_URL = "ur.config.wso2is.url";
    public static final String UR_CONFIG_WSO2GREG_URL = "ur.config.wso2greg.url";

    //HTRC email parameters
    public static String ERROR_HANDLING_EMAIL = "error.handling.email";
    public static String SUPPORT_EMAIL = "support.email";
    public static String HTRC_EMAIL = "htrc.email.address";
    public static String HTRC_EMAIL_USERNAME = "htrc.email.username";
    public static String HTRC_EMAIL_PASSWORD = "htrc.email.password";

    public static String HTRC_VALID_DOMAIN_FIRST_CSV = "htrc.valid.domains.csv.first";
    public static String HTRC_VALID_DOMAIN_SECOND_CSV = "htrc.valid.domains.csv.second";
    public static String HTRC_VALID_DOMAIN_THIRD_CSV = "htrc.valid.domains.csv.third";
    public static String HTRC_APPROVED_EMAILS = "htrc.approved.emails";

    //SAML2 Credentials
    public static String SAML2_KEYSTORE_PATH = "saml2.keystore.path";
    public static String SAML2_KEYSTORE_PASSWORD = "saml2.keystore.password";
    public static String SAML2_PRIVATEKEY_PASSWORD = "saml2.privatekey.password";
    public static String IDP_METADATA_PATH = "idp.metadata.path";
    public static String SAML_SSO_CALLBACK_URL = "saml.sso.callback";
    public static String CERTIFICATE_ALIAS = "certificate.alias";

    //Service provider
    public static String SERVICE_PROVIDER_NAME = "service.provider.name";
    public static String SERVICE_PROVIDER_DESCRIPTION = "service.provider.description";

    //Documentations
    public static String RELEASE_DOCUMENT = "release.document";
    public static String ANNOUNCEMENTS_DOCUMENT = "announcements.document";

    // Custom CSS
    public static String CUSTOM_CSS_THEME = "custom.css.theme";

    // Feature Activations
    public static String IS_DATA_CAPSULE_ENABLE = "datacapsule.enable";

    // Google Analytics code
    public static String GA_CODE = "google.analytics.code";

    // Features page
    public static String FEATURES_PAGE = "features.page";

    // Fiction dataset page
    public static String FICTION_PAGE = "fiction.page";

}
