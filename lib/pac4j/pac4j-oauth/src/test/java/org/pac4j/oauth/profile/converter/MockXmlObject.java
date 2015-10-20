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
package org.pac4j.oauth.profile.converter;

import org.pac4j.oauth.profile.XmlObject;

/**
 * This class is a mock for XmlObject.
 * 
 * @author Jerome Leleu
 * @since 1.4.1
 */
public final class MockXmlObject extends XmlObject {
    
    private static final long serialVersionUID = 8482186401170683300L;
    
    private String value;
    
    @Override
    protected void buildFromXml(final String xml) {
        this.value = xml;
    }
    
    public String getValue() {
        return this.value;
    }
}
