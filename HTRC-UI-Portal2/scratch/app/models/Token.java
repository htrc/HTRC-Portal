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

@Entity
public class Token extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public User user;
    public String token;
    public Long createdTime;
    public Boolean isTokenUsed = false;

    private static Logger.ALogger log = play.Logger.of("application");

    public Token(User user, String token, Long createdTime) {
        this.user = user;
        this.token = token;
        this.createdTime = createdTime;
    }

    public static Finder<Long, Token> find = new Finder<Long, Token>(Long.class, Token.class);

    public static Token findByToken(String token){
        return find.where().eq("token", token).findUnique();
    }



}
