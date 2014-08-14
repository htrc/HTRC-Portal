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

package controllers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import play.mvc.*;
import play.libs.*;
import play.test.*;
import static play.test.Helpers.*;
import com.avaje.ebean.Ebean;
import com.google.common.collect.ImmutableMap;

public class LoginTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        Ebean.save((List) Yaml.load("test-data.yml"));
    }

    @Test
    public void authenticateSuccess() {
        Result result = callAction(
                routes.ref.HTRCPortal.authenticate(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "userId", "mpathira",
                        "password", "secret"))
        );
        assertEquals(303, status(result));
        assertEquals("bob@example.com", session(result).get("email"));
    }

    @Test
    public void authenticateFailure() {
        Result result = callAction(
                routes.ref.HTRCPortal.authenticate(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "userId", "mpathira",
                        "password", "badpassword"))
        );
        assertEquals(400, status(result));
        assertNull(session(result).get("email"));
    }

    @Test
    public void authenticated() {
        Result result = callAction(
                routes.ref.HTRCPortal.index(),
                fakeRequest().withSession("userId", "mpathira")
        );
        assertEquals(200, status(result));
    }

    @Test
    public void notAuthenticated() {
        Result result = callAction(
                routes.ref.HTRCPortal.index(),
                fakeRequest()
        );
        assertEquals(303, status(result));
        assertEquals("/login", header("Location", result));
    }

}