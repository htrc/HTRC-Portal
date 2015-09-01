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
    private static String solrMetaQueryUrl = null;
    private static String solrOcrQueryUrl = null;


    // blacklight url
    private static String blacklighturl = null;

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

    // Sloan Web Service
    private static String sloanWsEndpoint = null;
    private static String createVMUrl = null;
    private static String showVMUrl = null;
    private static String listVMImagesUrl = null;
    private static String deleteVMUrl = null;
    private static String startVMUrl = null;
    private static String stopVMUrl = null;
    private static String switchVMUrl = null;

    //Portal URL
    private static String portalUrl = null;

    //HTRC email parameters
    private static String errorHandlingEmail = null;
    private static String supportEmail = null;
    private static String htrcEmail = null;
    private static String htrcEmailUserName = null;
    private static String htrcEmailPassword = null;


    // Valid domain files
    private static String validDomainsFirstCSV = null;
    private static String validDomainsSecondCSV = null;
    private static String validDomainsThirdCSV = null;

    // Approved email
    private static String approvedEmailsCSV = null;

    // SAML2 Credentials
    private static String saml2KeyStorePath = null;
    private static String saml2KeyStorePassword = null;
    private static String saml2PrivateKeyPassword = null;
    private static String idpMetadataPath = null;

    //Documentations
    private static String releaseDocument = null;
    private static String announcementsDocument = null;

    // Custom CSS
    private static String customCSSTheme = null;

    // Released Version
    private static String releasedVersion = null;

    // Google Analytics Code
    private static String googleAnalyticsCode = null;

    // Features page
    private static String featurespage = null;

    // Fiction dataset page
    private static String fictionpage = null;


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

    public static String solrMetaQueryUrl() {
        if(solrMetaQueryUrl == null){
            solrMetaQueryUrl = Play.application().configuration().getString(PortalConstants.SOLR_META_QUERY_URL);
        }
        return solrMetaQueryUrl;
    }

    public static String solrOcrQueryUrl() {
        if(solrOcrQueryUrl == null){
            solrOcrQueryUrl = Play.application().configuration().getString(PortalConstants.SOLR_OCR_QUERY_URL);
        }
        return solrOcrQueryUrl;
    }

    public static String blacklightUrl() {
        if(blacklighturl == null){
            blacklighturl = Play.application().configuration().getString(PortalConstants.BLACKLIGHT_URL);
        }
        return blacklighturl;
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

    public static String sloanWsEndpoint() {
        if(sloanWsEndpoint == null){
            sloanWsEndpoint = Play.application().configuration().getString(PortalConstants.SLOAN_WS_EPR);
        }
        return sloanWsEndpoint;
    }

    public static String createVMUrl() {
        if(createVMUrl == null){
            createVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_CREATEVM);
        }
        return createVMUrl;
    }

    public static String showVMUrl() {
        if(showVMUrl == null){
            showVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_SHOWVM);
        }
        return showVMUrl;
    }

    public static String listVMImagesUrl() {
        if(listVMImagesUrl == null){
            listVMImagesUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_LISTVMIMAGES);
        }
        return listVMImagesUrl;
    }

    public static String deleteVMUrl() {
        if(deleteVMUrl == null){
            deleteVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_DELETEVM);
        }
        return deleteVMUrl;
    }

    public static String startVMUrl() {
        if(startVMUrl == null){
            startVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_STARTVM);
        }
        return startVMUrl;
    }

    public static String stopVMUrl() {
        if(stopVMUrl == null){
            stopVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_STOPVM);
        }
        return stopVMUrl;
    }

    public static String switchVMUrl() {
        if(switchVMUrl == null){
            switchVMUrl = Play.application().configuration().getString(PortalConstants.SLOAN_WS_SWITCHVM);
        }
        return switchVMUrl;
    }

    public static String portalUrl(){
        if(portalUrl == null){
            portalUrl = Play.application().configuration().getString(PortalConstants.PORTAL_URL);
        }
        return portalUrl;
    }

    public static String errorHandlingEmail(){
        if(errorHandlingEmail == null){
            errorHandlingEmail = Play.application().configuration().getString(PortalConstants.ERROR_HANDLING_EMAIL);
        }
        return errorHandlingEmail;
    }

    public static String supportEmail(){
        if(supportEmail == null){
            supportEmail = Play.application().configuration().getString(PortalConstants.SUPPORT_EMAIL);
        }
        return supportEmail;
    }

    public static String htcEmailUserName(){
        if(htrcEmailUserName == null){
            htrcEmailUserName = Play.application().configuration().getString(PortalConstants.HTRC_EMAIL_USERNAME);
        }
        return htrcEmailUserName;
    }

    public static String htrcEmailPassword(){
        if(htrcEmailPassword == null){
            htrcEmailPassword = Play.application().configuration().getString(PortalConstants.HTRC_EMAIL_PASSWORD);
        }
        return htrcEmailPassword;
    }

    public static String htrcEmail(){
        if(htrcEmail == null){
            htrcEmail = Play.application().configuration().getString(PortalConstants.HTRC_EMAIL);
        }
        return htrcEmail;
    }

    public static String validDomainsFirstCSV(){
        if(validDomainsFirstCSV == null){
            validDomainsFirstCSV = Play.application().configuration().getString(PortalConstants.HTRC_VALID_DOMAIN_FIRST_CSV);
        }
        return validDomainsFirstCSV;
    }

    public static String validDomainsSecondCSV(){
        if(validDomainsSecondCSV == null){
            validDomainsSecondCSV = Play.application().configuration().getString(PortalConstants.HTRC_VALID_DOMAIN_SECOND_CSV);
        }
        return validDomainsSecondCSV;
    }

    public static String validDomainsThirdCSV(){
        if(validDomainsThirdCSV == null){
            validDomainsThirdCSV = Play.application().configuration().getString(PortalConstants.HTRC_VALID_DOMAIN_THIRD_CSV);
        }
        return validDomainsThirdCSV;
    }

    public static String approvedEmailsCSV(){
        if(approvedEmailsCSV == null){
            approvedEmailsCSV = Play.application().configuration().getString(PortalConstants.HTRC_APPROVED_EMAILS);
        }
        return approvedEmailsCSV;
    }

    public static String saml2KeyStorePath(){
        if(saml2KeyStorePath == null){
            saml2KeyStorePath = Play.application().configuration().getString(PortalConstants.SAML2_KEYSTORE_PATH);
        }
        return saml2KeyStorePath;
    }

    public static String saml2KeyStorePassword(){
        if(saml2KeyStorePassword == null){
            saml2KeyStorePassword = Play.application().configuration().getString(PortalConstants.SAML2_KEYSTORE_PASSWORD);
        }
        return saml2KeyStorePassword;
    }

    public static String saml2PrivateKeyPassword(){
        if(saml2PrivateKeyPassword == null){
            saml2PrivateKeyPassword = Play.application().configuration().getString(PortalConstants.SAML2_PRIVATEKEY_PASSWORD);
        }
        return saml2PrivateKeyPassword;
    }

    public static String idpMetadataPath(){
        if(idpMetadataPath == null){
            idpMetadataPath = Play.application().configuration().getString(PortalConstants.IDP_METADATA_PATH);
        }
        return idpMetadataPath;
    }

    public static String releaseDocument(){
        if(releaseDocument == null){
            releaseDocument = Play.application().configuration().getString(PortalConstants.RELEASE_DOCUMENT);
        }
        return releaseDocument;
    }

    public static String announcementDocument(){
        if(announcementsDocument == null){
            announcementsDocument = Play.application().configuration().getString(PortalConstants.ANNOUNCEMENTS_DOCUMENT);
        }
        return announcementsDocument;
    }

    public static String customCSSTheme(){
        if(customCSSTheme == null){
            customCSSTheme = Play.application().configuration().getString(PortalConstants.CUSTOM_CSS_THEME);
        }
        return customCSSTheme;
    }

    public static boolean isDataCapsuleEnable(){
        return Play.application().configuration().getBoolean(PortalConstants.IS_DATA_CAPSULE_ENABLE);
    }

    public static String releasedVersion(){
        if(releasedVersion == null){
            releasedVersion= Play.application().configuration().getString(PortalConstants.RELESED_VERSION);
        }
        return releasedVersion;
    }

    public static String googleAnalyticsCode(){
        if(googleAnalyticsCode == null){
            googleAnalyticsCode= Play.application().configuration().getString(PortalConstants.GA_CODE);
        }
        return googleAnalyticsCode;
    }

    public static String featuresPage(){
        if(featurespage == null){
            featurespage= Play.application().configuration().getString(PortalConstants.FEATURES_PAGE);
        }
        return featurespage;
    }

    public static String fictionPage(){
        if(fictionpage == null){
            fictionpage= Play.application().configuration().getString(PortalConstants.FICTION_PAGE);
        }
        return fictionpage;
    }
}

