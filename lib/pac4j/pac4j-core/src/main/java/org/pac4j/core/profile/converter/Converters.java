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
package org.pac4j.core.profile.converter;


/**
 * This class defines the default converters.
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public final class Converters {
    
    public final static LocaleConverter localeConverter = new LocaleConverter();
    
    public final static StringConverter stringConverter = new StringConverter();
    
    public final static BooleanConverter booleanConverter = new BooleanConverter();
    
    public final static IntegerConverter integerConverter = new IntegerConverter();
    
    public final static LongConverter longConverter = new LongConverter();
    
    public final static ColorConverter colorConverter = new ColorConverter();
    
    public final static GenderConverter genderConverter = new GenderConverter("male", "female");
    
    public final static FormattedDateConverter dateConverter = new FormattedDateConverter("yyyy-MM-dd'T'HH:mm:ssz");
    
    public final static StringReplaceConverter urlConverter = new StringReplaceConverter("\\/", "/");
}
