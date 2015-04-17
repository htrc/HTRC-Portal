/**
 * Created by shliyana on 4/16/15.
 */
var passwordCorrect = false;
var confirmPasswordMatch = false;

var isPasswordSimilarToUserName = function (password, userId) {
    return userId && password.toLowerCase().match(userId.toLowerCase());
};

var checkPasswordStrength = function (password, userId, minPasswordLength, maxPasswordLength) {
    var matchedRules = 0;

    if (password.match(/[a-z]/)) {
        matchedRules++;
    }

    if (password.match(/[A-Z]/)) {
        matchedRules++;
    }

    if (password.match(/\d+/)) {
        matchedRules++;
    }

    if (password.match(/.[!,@,#,$,%,\^,&,*,?,_,~]/)) {
        matchedRules++;
    }
    /*
     Check whether password contains any of the following characters
     0080-00FF	Latin-1 Supplement,0100-017F	Latin Extended-A,0180-024F	Latin Extended-B,0370-03FF	Greek and Coptic,0400-04FF	Cyrillic,2E80-2EFF	CJK Radicals Supplement,2C60-2C7F	Latin Extended-C,1760-177F	Tagbanwa
     1780-17FF	Khmer,1800-18AF	Mongolian,1900-194F	Limbu,1950-197F	Tai Le,1700-171F	Tagalog,1720-173F	Hanunoo,13A0-13FF	Cherokee,1400-167F	Unified Canadian Aboriginal Syllabics,0E00-0E7F	Thai,0E80-0EFF	Lao,0F00-0FFF	Tibetan
     1000-109F	Myanmar,10A0-10FF	Georgian,1100-11FF	Hangul Jamo,1200-137F	Ethiopic,0370-03FF	Greek and Coptic,0400-04FF	Cyrillic,0500-052F	Cyrillic Supplement,0530-058F	Armenian,0590-05FF	Hebrew,0600-06FF	Arabic
     0700-074F	Syriac,0750-077F	Arabic Supplement,0780-07BF	Thaana,07C0-07FF	NKo,0900-097F	Devanagari,0980-09FF	Bengali,0A00-0A7F	Gurmukhi,0A80-0AFF	Gujarati,0B00-0B7F	Oriya,0B80-0BFF	Tamil,0C00-0C7F	Telugu
     0C80-0CFF	Kannada,0D00-0D7F	Malayalam,0080-00FF	Latin-1 Supplement,0100-017F	Latin Extended-A,0180-024F	Latin Extended-B
     */
    if (password.match(/[\u0080-\u00FF\u0100-\u017F\u0180-\u024F\u0370-\u03FF\u0400-\u04FF\u0500-\u052F\u0530-\u058F\u0590-\u05FF\u0600-\u06FF\u0700-\u074F\u0750-\u077F\u0780-\u07BF\u07C0-\u07FF\u0900-\u097F\u0980-\u09FF\u0A00-\u0A7F\u0A80-\u0AFF\u0B00-\u0B7F\u0B80-\u0BFF\u0C00-\u0C7F\u0C80-\u0CFF\u0D00-\u0D7F\u0E00-\u0E7F\u0E80-\u0EFF\u0F00-\u0FFF\u1000-\u109F\u10A0-\u10FF\u1100-\u11FF\u1200-\u137F\u13A0-\u13FF\u1400-\u167F\u1700-\u171F\u1720-\u173F\u1760-\u177F\u1780-\u17FF\u1800-\u18AF\u1900-\u194F\u1950-\u197F\u2C60-\u2C7F\u2E80-\u2EFF]/)) {
        matchedRules++;
    }

    if (password.match(/[A-Z]/)) {
        matchedRules++;
    }

    if (isPasswordSimilarToUserName(password, userId)) {
        return "SIMILAR_TO_UID";
    }

    if (password.indexOf(' ') >= 0) {
        return "SPACE_IN_PASSWORD";
    }

    if (password.length > maxPasswordLength){
        return "PASSWORD_LENGTH_EXCEED_MAX";
    }

    if (matchedRules >= 2 && password.length >= minPasswordLength && password.length <= maxPasswordLength) {
        return "STRONG_PASSWORD";
    }

    return "WEAK_PASSWORD";
};

var registerPasswordStrengthChecker = function () {
    var passwd = $('#password');
    var passwdControlGroup = $('#password-control-group');
    var userId = $('#userId');
    var warnBlock = $('#password-warn-block');
    var passwordFeedback = $('#password-feedback');

    passwd.keyup(function () {
        var passwordStrength = checkPasswordStrength(passwd.val(), userId.val(), 15, 30);
        if (passwordStrength == "WEAK_PASSWORD") {
            passwordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Weak password!<br/>');
        } else if (passwordStrength == "SPACE_IN_PASSWORD") {
            passwordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Spaces are not allowed in the password!<br/>');
        } else if (passwordStrength == "SIMILAR_TO_UID") {
            passwordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('User name is not allowed in the password!<br/>');
        } else if (passwordStrength == "PASSWORD_LENGTH_EXCEED_MAX") {
            passwordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Password must not be more than 30 characters long!<br/>');
        } else {
            passwordCorrect = true;
            hasSuccess(passwdControlGroup, passwordFeedback);
            warnBlock.html('');
        }
    });
};

var registerPasswordMatcher = function () {
    var passwd = $('#password');
    var confirmPasswd = $('#confirmPassword');
    var confirmPasswdControlGroup = $('#confirm-password-control-group');
    var confirmPasswdFeedback = $('#confirm-password-feedback');

    confirmPasswd.keyup(function () {
        if (passwd.val() !== confirmPasswd.val()) {
            confirmPasswordMatch = false;
            hasError(confirmPasswdControlGroup, confirmPasswdFeedback);
        } else {
            confirmPasswordMatch = true;
            hasSuccess(confirmPasswdControlGroup, confirmPasswdFeedback);
        }
    });
};
