package edu.indiana.d2i.htrc.portal;

import play.Play;

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

public class PlayConfWrapper {

    // agent properties
    private static String agentEndpoint = null;
    private static String jobdetailsURL = null;
    private static String submitURL = null;
    private static String jobdeleteURLTemplate = null;
    private static String jobsaveURLTemplate = null;
    private static int agentConnectTimeout = Integer.MIN_VALUE;
    private static int agentWaitTimeout = Integer.MIN_VALUE;

    // oauth2 properties
    private static String oauthEndpoint = null;
    private static String tokenEndpoint = null;
    private static String userinfoEndpoint = null;
    private static String clientID = null;
    private static String clientSecrete = null;
    private static String oauthScope = null;
    private static String oauthType = null;
    private static String callbackurl = null;
    private static String adminServiceEndpoint = null;
    private static String oauthBackendUrl = null;

    //Solr query URL
    private static String solrQueryUrl = null;


    // blacklight url
    private static String blacklighturl = null;

    // password reset
    private static String passwordResetLinkUrl = null;

    // registry properties
    private static String registryEPR = null;
    private static String registryAlgFolder = null;

    // workset properties
    private static  int worksetsPerPage = 0;

    // algorithm properties
    private static  int algorithmsPerPage = 0;

    // user register
    private static String userRegUrl = null;
    private static String userRegUser = null;
    private static String userRegPwd = null;
    private static String userRegTruststore = null;
    private static String userRegTruststorePwd = null;
    private static String userRegTruststoreType = null;

    private static String jobDetailsTimeOut = null;

    public static String agentEndpoint() {
        if(agentEndpoint == null){
            agentEndpoint = Play.application().configuration().getString(PortalConstants.PROPERTY_AGENT_EPR);
        }
        return agentEndpoint;
    }

    public static String jobdetailsURL() {
        if(jobdetailsURL == null){
            jobdetailsURL = Play.application().configuration().getString(PortalConstants.PROPERTY_AGENT_JOBLIST_PART);
        }
        return jobdetailsURL;
    }

    public static String jobSubmitURL() {
        if(submitURL == null){
            submitURL = Play.application().configuration().getString(PortalConstants.PROPERTY_AGENT_JOBSUBMIT_PART);
        }
        return submitURL;
    }

    public static String jobDeleteURLTemplate() {
        if(jobdeleteURLTemplate == null){
            jobdeleteURLTemplate = Play.application().configuration().getString(PortalConstants.PROPERTY_AGENT_JOB_DELETE);
        }
        return jobdeleteURLTemplate;
    }

    public static String jobSaveURLTemplate() {
        if(jobsaveURLTemplate == null){
            jobsaveURLTemplate = Play.application().configuration().getString(PortalConstants.PROPERTY_AGENT_JOB_SAVE);
        }
        return jobsaveURLTemplate;
    }

    public static int agentConnectTimeout() {
        if(agentConnectTimeout == 0){
            agentConnectTimeout = Play.application().configuration().getInt(PortalConstants.PROPERTY_AGENT_CONNECT_TIMEOUT);
        }
        return agentConnectTimeout;
    }

    public static int agentWaitTimeout() {
        if(agentWaitTimeout == 0){
            agentWaitTimeout = Play.application().configuration().getInt(PortalConstants.PROPERTY_AGENT_WAIT_TIMEOUT);
        }
        return agentWaitTimeout;
    }

    public static String oauthEndpoint() {
        if(oauthEndpoint == null){
            oauthEndpoint = Play.application().configuration().getString(PortalConstants.OAUTH2_AUTHZ_ENDPOINT);
        }
        return oauthEndpoint;
    }

    public static String tokenEndpoint() {
        if(tokenEndpoint == null){
            tokenEndpoint = Play.application().configuration().getString(PortalConstants.OAUTH2_TOKEN_ENDPOINT);
        }
        return tokenEndpoint;
    }

    public static String userInfoEndpoint() {
        if(userinfoEndpoint == null){
            userinfoEndpoint = Play.application().configuration().getString(PortalConstants.OAUTH2_USERINFO_ENDPOINT);
        }
        return userinfoEndpoint;
    }

    public static String oauthClientID() {
        if(clientID == null){
            clientID = Play.application().configuration().getString(PortalConstants.OAUTH2_CLIENT_ID);
        }
        return clientID;
    }

    public static String oauthClientSecrete() {
        if(clientSecrete == null){
            clientSecrete = Play.application().configuration().getString(PortalConstants.OAUTH2_CLIENT_SECRETE);
        }
        return clientSecrete;
    }

    public static String oauthScope() {
        if(oauthScope == null){
            oauthScope = Play.application().configuration().getString(PortalConstants.OAUTH2_SCOPE);
        }
        return oauthScope;
    }

    public static String oauthType() {
        if(oauthType == null){
            oauthType = Play.application().configuration().getString(PortalConstants.OAUTH2_GRANT_TYPE);
        }
        return oauthType;
    }

    public static String callBackUrl() {
        if(callbackurl == null){
            callbackurl = Play.application().configuration().getString(PortalConstants.OAUTH2_CALLBACK_URL);
        }
        return callbackurl;
    }

    public static String adminServiceEndpoint() {
        if(adminServiceEndpoint == null){
            adminServiceEndpoint = Play.application().configuration().getString(PortalConstants.OAUTH2_ADMIN_SERVICE);
        }
        return adminServiceEndpoint;
    }

    public static String oauthBackendUrl() {
        if(oauthBackendUrl == null){
            oauthBackendUrl = Play.application().configuration().getString(PortalConstants.OAUTH2_BACKEND_URL);
        }
        return oauthBackendUrl;
    }

    public static String solrQueryUrl() {
        if(solrQueryUrl == null){
            solrQueryUrl = Play.application().configuration().getString(PortalConstants.SOLR_QUERY_URL);
        }
        return solrQueryUrl;
    }

    public static String blacklightUrl() {
        if(blacklighturl == null){
            blacklighturl = Play.application().configuration().getString(PortalConstants.BLACKLIGHT_URL);
        }
        return blacklighturl;
    }

    public static String passwordResetLinkUrl() {
        if(passwordResetLinkUrl == null){
            passwordResetLinkUrl = Play.application().configuration().getString(PortalConstants.PASSWORD_RESET_LINK_URL);
        }
        return passwordResetLinkUrl;
    }

    public static String registryEPR() {
        if(registryEPR == null){
            registryEPR = Play.application().configuration().getString(PortalConstants.REGISTRY_ENDPOINT);
        }
        return registryEPR;
    }

    public static String registryAlgFolder() {
        if(registryAlgFolder == null){
            registryAlgFolder = Play.application().configuration().getString(PortalConstants.REGISTRY_ALGO_FOLDER);
        }
        return registryAlgFolder;
    }

    public static String userRegUrl() {
        if(userRegUrl == null){
            userRegUrl = Play.application().configuration().getString(PortalConstants.USER_REG_URL);
        }
        return userRegUrl;
    }

    public static String userRegUser() {
        if(userRegUser == null){
            userRegUser = Play.application().configuration().getString(PortalConstants.USER_REG_USER);
        }
        return userRegUser;
    }

    public static String userRegPwd() {
        if(userRegPwd == null){
            userRegPwd = Play.application().configuration().getString(PortalConstants.USER_REG_PASSWORD);
        }
        return userRegPwd;
    }

    public static String userRegTruststore() {
        if(userRegTruststore == null){
            userRegTruststore = Play.application().configuration().getString(PortalConstants.USER_REG_TRUSTSTORE);
        }
        return userRegTruststore;
    }

    public static String userRegTruststorePwd() {
        if(userRegTruststorePwd == null){
            userRegTruststorePwd = Play.application().configuration().getString(PortalConstants.USER_REG_TRUSTSTORE_PWD);
        }
        return userRegTruststorePwd;
    }

    public static String userRegTruststoreType() {
        if(userRegTruststoreType == null){
            userRegTruststoreType = Play.application().configuration().getString(PortalConstants.USER_REG_TRUSTSTORE_TYPE);
        }
        return userRegTruststoreType;
    }

    public static String jobDetailsTimeOut() {
        if(jobDetailsTimeOut == null){
            jobDetailsTimeOut = Play.application().configuration().getString(PortalConstants.PORTAL_JOB_DETAILS_TIMEOUT);
        }
        return jobDetailsTimeOut;
    }

    public static int worksetsPerPage(){
        if(worksetsPerPage == 0){
            worksetsPerPage = Play.application().configuration().getInt(PortalConstants.WORKSETS_PER_PAGE);
        }
        return worksetsPerPage;
    }

    public static int algorithmsPerPage(){
        if(algorithmsPerPage == 0){
            algorithmsPerPage = Play.application().configuration().getInt(PortalConstants.ALGORITHMS_PER_PAGE);
        }
        return algorithmsPerPage;
    }
}

