package edu.indiana.d2i.htrc.portal;


import play.Logger;


/**
 * Created by shliyana on 6/6/14.
 */
public class PasswordChecker {
    private static Logger.ALogger log = play.Logger.of("application");

    int successCount = 0;
    int numberOfRulesToSatisfy = 2;


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

        if(atLeastOneUppercaseCharacter(password)){
            successCount++;
        }
        if(atLeastOneLowercaseCharacter(password)){
            successCount++;
        }
        if(atLeastOneDigit(password)){
            successCount++;
        }
        if(atLeastOneNonAlphaNumericCharacter(password)){
            successCount++;
        }
        if(atLeastOneUniCodeCharacter(password)){
            successCount++;
        }

        if(successCount >= numberOfRulesToSatisfy){
            log.info("Password satisfies "+successCount+ " rules out of 5 rules.");
        }
        return successCount >= 2;
    }

    public boolean atLeastOneUniCodeCharacter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
                log.info("Password include an Unicode");
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
                    log.info("Password include a NonAlphaNumeric character");
                    return true;
                }
            }
        }
        log.info("Password doesn't include a NonAlphaNumeric character");
        return false;
    }

    public boolean atLeastOneUppercaseCharacter(String password) {

        for (char c : password.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN && Character.isUpperCase(c)) {
                log.info("Password include an uppercase character");
                return true;
            }
        }
        log.info("Password doesn't include an uppercase character");
        return false;
    }

    public boolean atLeastOneLowercaseCharacter(String password) {

        for (char c : password.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN && Character.isLowerCase(c)) {
                log.info("Password include a lowercase character");
                return true;
            }
        }
        log.info("Password doesn't include a lowercase character");
        return false;
    }

    public boolean atLeastOneDigit(String password) {

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                log.info("Password include a digit.");
                return true;
            }
        }
        log.info("Password doesn't include a digit.");
        return false;
    }

}
