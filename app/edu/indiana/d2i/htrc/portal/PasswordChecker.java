package edu.indiana.d2i.htrc.portal;

import edu.vt.middleware.password.*;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shliyana on 6/6/14.
 */
public class PasswordChecker {
    private static Logger.ALogger log = play.Logger.of("application");
    /**
     * Check password strength.
     * Check whether password contain characters from three of the following five categories:
     * Uppercase characters of European languages (A through Z, with diacritic marks, Greek and Cyrillic characters)
     * Lowercase characters of European languages (a through z, sharp-s, with diacritic marks, Greek and Cyrillic characters)
     * Base 10 digits (0 through 9)
     * Nonalphanumeric characters: ~!@#$%^&*_-+=`|\(){}[]:;"'<>,.?/
     * Any Unicode character that is categorized as an alphabetic character but is not uppercase or lowercase. This includes Unicode characters from Asian languages.
     */

    public boolean isValidPassword(String password) {
        // password must be more than 15 chars long
        LengthRule lengthRule = new LengthRule(15, Integer.MAX_VALUE);

//        // don't allow whitespace
       WhitespaceRule whitespaceRule = new WhitespaceRule();

        // control allowed characters
        CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
        // require at least 1 digit in passwords
        charRule.getRules().add(new DigitCharacterRule(1));
        // require at least 1 non-alphanumeric char
//            charRule.getRules().add(new NonAlphanumericCharacterRule(1));
        // require at least 1 upper case char
        charRule.getRules().add(new UppercaseCharacterRule(1));
        // require at least 1 lower case char
        charRule.getRules().add(new LowercaseCharacterRule(1));

        // require at least 2 of the previous rules be met if there is an Unicode in the password
        // if there is no Unicode in the password, require at least 3 of previous rules.
        if (atLeastOneUniCodeCharacter(password) && atLeastOneNonAlphaNumericCharacter(password)) {
            charRule.setNumberOfCharacteristics(0);
        } else if (atLeastOneUniCodeCharacter(password) || atLeastOneNonAlphaNumericCharacter(password)) {
            charRule.setNumberOfCharacteristics(1);
        } else {
            charRule.setNumberOfCharacteristics(2);
        }


//            // don't allow alphabetical sequences
//            AlphabeticalSequenceRule alphaSeqRule = new AlphabeticalSequenceRule();
//
//            // don't allow numerical sequences of length 3
//            NumericalSequenceRule numSeqRule = new NumericalSequenceRule();
//
//            // don't allow qwerty sequences
//            QwertySequenceRule qwertySeqRule = new QwertySequenceRule();
//
//            // don't allow 4 repeat characters
//            RepeatCharacterRegexRule repeatRule = new RepeatCharacterRegexRule(4);

        // group all rules together in a List
        List<Rule> ruleList = new ArrayList<Rule>();
        ruleList.add(charRule);
        ruleList.add(lengthRule);
        ruleList.add(whitespaceRule);

        PasswordValidator validator = new PasswordValidator(ruleList);
        PasswordData passwordData = new PasswordData(new Password(password));

        RuleResult result = validator.validate(passwordData);
        if (result.isValid()) {
            for (String msg : validator.getMessages(result)) {
                log.info(msg);
            }
            return result.isValid();
        } else {
            for (String msg : validator.getMessages(result)) {
                log.warn(msg);
            }
            return result.isValid();
        }
    }

    public boolean atLeastOneUniCodeCharacter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
                log.info("Password include Unicodes");
                return true;
            }
        }
        log.info("Password doesn't include any Unicode");
        return false;
    }

    public boolean atLeastOneNonAlphaNumericCharacter(String password) {
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN) {
                    log.info("Password include NonAlphaNumeric character");
                    return true;
                }
            }
        }
        log.info("Password doesn't include NonAlphaNumeric character");
        return false;
    }

}
