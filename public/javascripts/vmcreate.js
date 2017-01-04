var confirmVNCPasswordMatch = false;
var vncPasswordCorrect = false;
var vncUserIdCorrect = false;

var hasError = function (element, iconElement) {
    createCapsuleButtonVisibility();

    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var hasSuccess = function (element, iconElement) {
    createCapsuleButtonVisibility();

    element.addClass('has-success');
    element.removeClass('has-error');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-ok');
    iconElement.removeClass('glyphicon-remove');
};

var vncUserIdInputKeyUp = function () {
    var userId = $(this).val();
    var userIdControlGroup = $('#username-control-group');
    var userIdWarnBlock = $('#userid-warn-block');
    var userIdFeedback = $('#user-id-feedback');

    if (userId.indexOf(' ') >= 0) {
        vncUserIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID cannot contain white spaces!');
    } else if(userId.length == 0){
        vncUserIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID cannot be empty!');
    }else if(userId.length < 3){
        vncUserIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID must be at least 3 characters long!');
    }else if(userId.length > 7){
        vncUserIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID must be less than 8 characters long!');
    }else{
        vncUserIdCorrect = true;
        hasSuccess(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('');
    }
};

var vncUserIDValidation = function () {
    var userId = $("#userName");
    var value = "";
    userId.bind("change keyup paste" ,vncUserIdInputKeyUp);
};


var checkVNCPasswordStrength = function (password, minPasswordLength, maxPasswordLength) {
    if (password.indexOf(' ') >= 0) {
        return "SPACE_IN_PASSWORD";
    }

    if (password.length > maxPasswordLength){
        return "PASSWORD_LENGTH_EXCEED_MAX";
    }

    if (password.length < minPasswordLength){
        return "PASSWORD_LENGTH_LESS_THAN_MIN";
    }

    if (password.length >= minPasswordLength && password.length <= maxPasswordLength) {
        return "CORRECT_PASSWORD";
    }
};

var matchPasswords = function () {
    var passwd = $('#password');
    var confirmPasswd = $('#confirmPassword');
    var confirmPasswdControlGroup = $('#confirm-password-control-group');
    var confirmPasswdFeedback = $('#confirm-password-feedback');

    if (passwd.val() !== confirmPasswd.val()) {
        confirmVNCPasswordMatch = false;
        hasError(confirmPasswdControlGroup, confirmPasswdFeedback);
    } else {
        confirmVNCPasswordMatch = true;
        hasSuccess(confirmPasswdControlGroup, confirmPasswdFeedback);
    }
};

var vncPasswordChecker = function () {
    var passwd = $('#password');
    var confirmPasswd = $('#confirmPassword');
    var passwdControlGroup = $('#password-control-group');
    var warnBlock = $('#password-warn-block');
    var passwordFeedback = $('#password-feedback');
    var confirmPasswdControlGroup = $('#confirm-password-control-group');
    var confirmPasswdFeedback = $('#confirm-password-feedback');

    passwd.keyup(function () {

        var passwordStrength = checkVNCPasswordStrength(passwd.val(),3,7);
        if (passwordStrength == "SPACE_IN_PASSWORD") {
            vncPasswordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Spaces are not allowed in the password!<br/>');
        } else if (passwordStrength == "PASSWORD_LENGTH_EXCEED_MAX") {
            vncPasswordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Password must be less than 8 characters long!<br/>');
        } else if (passwordStrength == "PASSWORD_LENGTH_LESS_THAN_MIN") {
            vncPasswordCorrect = false;
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Password must be at least 3 characters long!<br/>');
        } else {
            vncPasswordCorrect = true;
            hasSuccess(passwdControlGroup, passwordFeedback);
            warnBlock.html('');
        }
        matchPasswords();
    });
};


var vncPasswordMatcher = function () {
    var passwd = $('#password');
    var confirmPasswd = $('#confirmPassword');
    var confirmPasswdControlGroup = $('#confirm-password-control-group');
    var confirmPasswdFeedback = $('#confirm-password-feedback');

    confirmPasswd.keyup(matchPasswords);
};

var createCapsuleButtonVisibility = function () {
    var createCapsuleButton = $('#createCapsule');

    // disable submit button at start
    createCapsuleButton.prop("disabled",true);

    if (confirmVNCPasswordMatch && vncPasswordCorrect && vncUserIdCorrect) {
        createCapsuleButton.prop("disabled",false);
    }else{
        createCapsuleButton.prop("disabled",true);
    }
};

var triggerAlreadyFilledFields = function() {
    $('input').each(function() {
        var elem = $(this);
        if (elem.val()) elem.change();
    })
};

$(document).ready(function(){
    createCapsuleButtonVisibility();
    vncUserIDValidation();
    vncPasswordChecker();
    vncPasswordMatcher();
    setTimeout(triggerAlreadyFilledFields, 250);
});
