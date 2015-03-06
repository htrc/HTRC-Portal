var userIdCorrect = false;
var passwordCorrect = false;
var validEmailDomain = false;
var confirmPasswordMatch = false;
var confirmEmailMatch = false;


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
    acknowledgeButtonVisibility();
    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var hasSuccess = function (element, iconElement) {
    acknowledgeButtonVisibility();
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

var checkEmailDomainValidity = function (email, onInvalidDomain, onValidDomain) {
   var request = $.ajax({
        url: "/isvalidemaildomain?email=" + email,
        type: "GET",
        success: function (result) {
            if (!result.valid) {
                onInvalidDomain();
            } else {
                onValidDomain();
            }
        }
    });
};

var validEmail = function (email) {
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
};

var registerEmailValidityChecker = function () {
    var emailControlGroup = $('#email-control-group');
    var emailFeedback = $('#email-feedback');
    var email = $('#email');
    var unrecognizedDomainWarning = $('#unrecognized-domain-warn');

    email.bind("change keyup" , function () {
        if (validEmail(email.val())) {
            checkEmailDomainValidity(email.val(),
                function () {
                    validEmailDomain = false;
                    hasError(emailControlGroup, emailFeedback);
                    unrecognizedDomainWarning.html('Your email domain is not recognized by our system, ' +
                        'please <a href="/accountrequest" class="btn btn-xs btn-default"> request an ' +
                        'account</a> with your current email address.<br/>');

                },
                function () {
                    validEmailDomain = true;
                    hasSuccess(emailControlGroup, emailFeedback);
                    unrecognizedDomainWarning.html('');
                    });
        } else {
            hasError(emailControlGroup, emailFeedback);
            unrecognizedDomainWarning.html('Please enter a valid email.');
        }
    });
};

var registerEmailMatcher = function () {
    var emailControlGroup = $('#confirm-email-control-group');
    var emailFeedback = $('#confirm-email-feedback');
    var confirmEmail = $('#confirmEmail');
    var email = $('#email');

    email.bind("change keyup" ,function (){
        if(confirmEmail.val() == email.val()){
            confirmEmailMatch = true;
            hasSuccess(emailControlGroup, emailFeedback);
        } else {
            confirmEmailMatch = false;
            hasError(emailControlGroup, emailFeedback);
        }
    });
    confirmEmail.bind("change keyup" ,function (){
        if(confirmEmail.val() == email.val()){
            confirmEmailMatch = true;
            hasSuccess(emailControlGroup, emailFeedback);
        } else {
            confirmEmailMatch = false;
            hasError(emailControlGroup, emailFeedback);
        }
    });
};

var registerAcknowledgementCheck = function () {
    var acknowledgementCheckBox = $('#acknowledgement');
    var submitButton = $('#submit');

    // Un-check the acknowledgement checkbox at start
    acknowledgementCheckBox.prop('checked', false);

    acknowledgementCheckBox.change(function () {
        submitButton.prop("disabled", !acknowledgementCheckBox.prop('checked'));
    });
};

var userIdInputKeyUp = function () {
    var userId = $(this).val();
    var userIdControlGroup = $('#username-control-group');
    var userIdWarnBlock = $('#userid-warn-block');
    var userIdFeedback = $('#user-id-feedback');

    if (userId.indexOf(' ') >= 0 || userId.match('[!,@@,#,$,%,\\,\/,^,&,*,?,~,(,),-]')) {
        userIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID contains a space or a special character!');
    } else if(userId.length == 0){
        userIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID cannot be empty!');
    }else if(userId.length < 3){
        userIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID must be at least 5 characters long!');
    }else {
        userIdCorrect = true;
        hasSuccess(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('');
    }
};

var registerUserIDValidation = function () {
    $('#userId').bind("change keyup" ,userIdInputKeyUp);
};

var acknowledgeButtonVisibility = function() {
    var acknowledgementCheckBox = $('#acknowledgement');
    if (userIdCorrect && passwordCorrect && validEmailDomain && confirmPasswordMatch && confirmEmailMatch) {
        acknowledgementCheckBox.prop("disabled",false);
    }else{
        acknowledgementCheckBox.prop("disabled",true);
    }
};


$(document).ready(function () {
    registerUserIDValidation();
    registerAcknowledgementCheck();
    registerEmailValidityChecker();
    registerEmailMatcher();
    acknowledgeButtonVisibility();
    registerPasswordMatcher();
    registerPasswordStrengthChecker();
});