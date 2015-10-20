/*
  Copyright 2012 - 2014 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play;

import org.pac4j.core.client.Clients;
import java.util.regex.Pattern;

/**
 * This class gathers all the configuration.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class Config {
    
    private final static String DEFAULT_URL = "/";
    
    // just relative urls
    private final static String DEFAULT_LOGOUT_URL_PATTERN = "/.*";

    private static String defaultSuccessUrl = DEFAULT_URL;
    
    private static String defaultLogoutUrl = DEFAULT_URL;
    
    private static Pattern logoutUrlPattern = Pattern.compile(DEFAULT_LOGOUT_URL_PATTERN);

    // 1 hour = 3600 seconds
    private static int profileTimeout = 3600;
    
    // 1 minute = 60 second
    private static int sessionTimeout = 60;
    
    // all the clients
    private static Clients clients;
    
    private static String errorPage401 = "authentication required";
    
    private static String errorPage403 = "forbidden";

    private static String cacheKeyPrefix = "";
    
    public static String getDefaultSuccessUrl() {
        return defaultSuccessUrl;
    }
    
    public static void setDefaultSuccessUrl(final String defaultSuccessUrl) {
        Config.defaultSuccessUrl = defaultSuccessUrl;
    }
    
    public static String getDefaultLogoutUrl() {
        return defaultLogoutUrl;
    }
    
    public static void setDefaultLogoutUrl(final String defaultLogoutUrl) {
        Config.defaultLogoutUrl = defaultLogoutUrl;
    }
    
    public static int getProfileTimeout() {
        return profileTimeout;
    }
    
    public static void setProfileTimeout(final int profileTimeout) {
        Config.profileTimeout = profileTimeout;
    }
    
    public static int getSessionTimeout() {
        return sessionTimeout;
    }
    
    public static void setSessionTimeout(final int sessionTimeout) {
        Config.sessionTimeout = sessionTimeout;
    }
    
    public static Clients getClients() {
        return clients;
    }
    
    public static void setClients(final Clients clients) {
        Config.clients = clients;
    }
    
    public static String getErrorPage401() {
        return errorPage401;
    }
    
    public static void setErrorPage401(final String errorPage401) {
        Config.errorPage401 = errorPage401;
    }
    
    public static String getErrorPage403() {
        return errorPage403;
    }
    
    public static void setErrorPage403(final String errorPage403) {
        Config.errorPage403 = errorPage403;
    }

    public static Pattern getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public static void setLogoutUrlPattern(String logoutUrlPattern) {
        Config.logoutUrlPattern = Pattern.compile(logoutUrlPattern);
    }

    /**
     * Gets the prefix used for all cache operations
     *
     * @return the prefix
     * @since 1.1.2
     */
    public static String getCacheKeyPrefix() {
        return cacheKeyPrefix;
    }

    /**
     * Sets the prefix to use for all cache operations
     *
     * @param cacheKeyPrefix
     * @since 1.1.2
     */
    public static void setCacheKeyPrefix(String cacheKeyPrefix) {
        Config.cacheKeyPrefix = cacheKeyPrefix;
    }
}
