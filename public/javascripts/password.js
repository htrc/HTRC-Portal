var passwordCorrect = false;
var confirmPasswordMatch = false;

function isFunction(functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
}

var isPasswordSimilarToUserName = function (password, userId) {
    return userId && password.toLowerCase().match(userId.toLowerCase());
};

var checkPasswordStrength = function (password, userId, minPasswordLength) {
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

    if (isPasswordSimilarToUserName(password, userId)) {
        return "SIMILAR_TO_UID";
    }

    if (password.indexOf(' ') >= 0) {
        return "SPACE_IN_PASSWORD";
    }

    if (matchedRules >= 2 && password.length >= minPasswordLength) {
        return "STRONG_PASSWORD";
    }

    return "WEAK_PASSWORD";
};

var hasError = function (element, iconElement) {
    passwdSubmitButtonVisibility();

    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var hasSuccess = function (element, iconElement) {
    passwdSubmitButtonVisibility();

    element.addClass('has-success');
    element.removeClass('has-error');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-ok');
    iconElement.removeClass('glyphicon-remove');
};

var registerPasswordStrengthChecker = function () {
    var passwd = $('#password');
    var passwdControlGroup = $('#password-control-group');
    var userId = $('#userId');
    var warnBlock = $('#password-warn-block');
    var passwordFeedback = $('#password-feedback');

    passwd.keyup(function () {
        var passwordStrength = checkPasswordStrength(passwd.val(), userId.val(), 15);
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
        } else {
            passwordCorrect = true;
            hasSuccess(passwdControlGroup, passwordFeedback);
            warnBlock.html('');
        }
    });
};

var registerPasswordMatcher = function () {
    var passwd = $('#password');
    var confirmPasswd = $('#retypePassword');
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

var passwdSubmitButtonVisibility = function () {
    var passwdSubmitButton = $('#passwordReset');

    // disable submit button at start
    passwdSubmitButton.prop("disabled",true);

    if (confirmPasswordMatch && passwordCorrect) {
        passwdSubmitButton.prop("disabled",false);
    }else{
        passwdSubmitButton.prop("disabled",true);
    }
};

$(document).ready(function(){
    passwdSubmitButtonVisibility();
    registerPasswordMatcher();
    registerPasswordStrengthChecker();
});