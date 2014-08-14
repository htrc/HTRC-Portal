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

import org.junit.Before;
import org.junit.Test;
import play.test.WithApplication;

import java.util.List;

import static org.junit.Assert.*;
import static play.test.Helpers.*;


public class ModelsTest extends WithApplication {
    @Before
    public void setUp(){
        start(fakeApplication(inMemoryDatabase()));
    }

    @Test
    public void createAndRetrieveUser() {
        new User("mpathira", "bob@gmail.com", "secret").save();
        User bob = User.find.where().eq("userId", "mpathira").findUnique();
        assertNotNull(bob);
        assertEquals("bob@gmail.com", bob.email);
    }

    @Test
    public void tryAuthenticateUser() {
        new User("mpathira", "bob@gmail.com", "secret").save();

        assertNotNull(User.authenticate("mpathira", "secret"));
        assertNull(User.authenticate("mpathira", "badpassword"));
        assertNull(User.authenticate("tom", "secret"));
    }


}
