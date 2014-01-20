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

package edu.indiana.d2i.htrc.portal;

import play.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static Logger.ALogger logger = play.Logger.of("application");

    private static Map<String, PasswordResetEntry> tokensStore = new ConcurrentHashMap<String, PasswordResetEntry>();

    public static String generatePasswordResetURL(String userId, String userEmail) throws IOException, NoSuchAlgorithmException {
        // Generate token
        String token = generateToken(userId, userEmail);
        // create password reset entry
        PasswordResetEntry passwordResetEntry = new PasswordResetEntry(token, userId, userEmail);
        // add to map against token
        tokensStore.put(token, passwordResetEntry);
        // build the url and return
        return PlayConfWrapper.passwordResetLinkUrl() + token;
    }

    public static PasswordResetEntry isValidPasswordResetAttempt(String token) {
        // Check whether token is there in the map
        long validityPeriod = 3600;
        if (!tokensStore.containsKey(token)) {
            return null;
        } else if (tokensStore.get(token).isExpired(validityPeriod)) {
            return tokensStore.remove(token);
        }
        // return true or false
        return tokensStore.remove(token);
    }

    public static String generateToken(String userId, String userEmail) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

        MessageDigest messageDigestInstance = MessageDigest.getInstance("SHA-256");
        messageDigestInstance.digest((new Date().toString() + userId + userEmail).getBytes("UTF-8"));

        byte[] hash = messageDigestInstance.digest();
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
            sb.append(HEX_CHARS[b & 0x0F]);
        }
        return sb.toString();
    }

    static class PasswordResetEntry {
        private String token;
        private String userId;
        private String userEmail;
        private long createdAt;
        private boolean valid = true;


        public PasswordResetEntry(String token_1, String userId_1, String userEmail_1) {
            token = token_1;
            userId = userId_1;
            userEmail = userEmail_1;
            createdAt = new Date().getTime();
        }


        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isExpired(long validityPeriod) {
            long currentMills = new Date().getTime();
            long timeGap = currentMills - createdAt;
            if (validityPeriod*1000 <= timeGap) {
                valid = false;
                return true;
            }

            return false;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
