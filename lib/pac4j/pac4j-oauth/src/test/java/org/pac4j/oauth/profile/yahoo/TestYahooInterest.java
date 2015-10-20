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
package org.pac4j.oauth.profile.yahoo;

import junit.framework.TestCase;

import org.pac4j.core.util.TestsConstants;
import org.pac4j.oauth.profile.JsonHelper;

/**
 * This class tests the {@link YahooInterest} class.
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public final class TestYahooInterest extends TestCase implements TestsConstants {
    
    private final static String DECLARED_INTERESTS = "declaredInterests";
    
    private final static String INTEREST_CATEGORY = "interestCategory";
    
    private static final String GOOD_JSON = "{\"declaredInterests\" : [\"" + DECLARED_INTERESTS
                                            + "\"], \"interestCategory\" : \"" + INTEREST_CATEGORY + "\"}";
    
    public void testNull() {
        final YahooInterest yahooInterest = new YahooInterest();
        yahooInterest.buildFrom(null);
        assertNull(yahooInterest.getDeclaredInterests());
        assertNull(yahooInterest.getInterestCategory());
    }
    
    public void testBadJson() {
        final YahooInterest yahooInterest = new YahooInterest();
        yahooInterest.buildFrom(JsonHelper.getFirstNode(BAD_JSON));
        assertNull(yahooInterest.getDeclaredInterests());
        assertNull(yahooInterest.getInterestCategory());
    }
    
    public void testGoodJson() {
        final YahooInterest yahooInterest = new YahooInterest();
        yahooInterest.buildFrom(JsonHelper.getFirstNode(GOOD_JSON));
        assertEquals(DECLARED_INTERESTS, yahooInterest.getDeclaredInterests().get(0));
        assertEquals(INTEREST_CATEGORY, yahooInterest.getInterestCategory());
    }
}
