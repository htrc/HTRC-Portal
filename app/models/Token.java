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

package models;

import play.Logger;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Entity
public class Token extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String userId;
    public String token;
    public Long createdTime;
    public String isTokenUsed = "NO";

    private static Logger.ALogger log = play.Logger.of("application");
    private static long validityPeriod = 3600; // in seconds

    public Token(String userId, String token, Long createdTime) {
        this.userId = userId;
        this.token = token;
        this.createdTime = createdTime;
    }

    public static Finder<Long, Token> find = new Finder<Long, Token>(Long.class, Token.class);

    public static Token findByToken(String token){
        log.info("Looking for token: "+ token);
        return find.where().eq("token", token).findUnique();
    }

    public static String generateToken(String userId, String userEmail) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

        MessageDigest messageDigestInstance = MessageDigest.getInstance("SHA-256");

        byte[] hash = messageDigestInstance.digest((new Date().toString() + userId + userEmail).getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
            sb.append(HEX_CHARS[b & 0x0F]);
        }

        String newToken = sb.toString();
        if(Token.findByToken(newToken) != null){
            Token alreadyExistToken = Token.findByToken(newToken);
            log.info("Token: "+newToken+" is already exist.");
            if(alreadyExistToken.userId.equals(userId)){
                alreadyExistToken.createdTime = new Date().getTime();
                alreadyExistToken.update();
            }else{
                log.error("Error in saving tokens.");
            }

        }else {
            Token token = new Token(userId,newToken,new Date().getTime());
            token.save();
            log.info("Token :"+ Token.findByToken(newToken).token + " is saved successfully.");
        }
        return newToken;
    }

    public static void deleteToken(String token){
       Token token1 = Token.findByToken(token);
        token1.delete();
    }

    public static void replaceToken(Token token1){
        token1.update();
    }

    public static void deleteAllTokens(){
        List<Token> tokenList = find.all();
        for(Token token1 : tokenList){
             token1.delete();
        }

    }

    public static boolean isTokenExpired(Token token){
        return validityPeriod <= (new Date().getTime() - token.createdTime);
    }



}
