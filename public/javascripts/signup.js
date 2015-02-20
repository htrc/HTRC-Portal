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
    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var hasSuccess = function (element, iconElement) {
    element.addClass('has-success');
    element.removeClass('has-error');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-ok');
    iconElement.removeClass('glyphicon-remove');
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
                    hasError(emailControlGroup, emailFeedback);
                    unrecognizedDomainWarning.html('Your email is not recognized by our system, ' +
                        'please <a href="/accountrequest" class="btn btn-xs btn-default"> request an ' +
                        'account</a> with your current email address.<br/>');

                },
                function () {
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
        if(confirmEmail.val() === email.val()){
            hasSuccess(emailControlGroup, emailFeedback);
        } else {
            hasError(emailControlGroup, emailFeedback);
        }
    });
    confirmEmail.bind("change keyup" ,function (){
        if(confirmEmail.val() === email.val()){
            hasSuccess(emailControlGroup, emailFeedback);
        } else {
            hasError(emailControlGroup, emailFeedback);
        }
    });
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
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Weak password!<br/>');
        } else if (passwordStrength == "SPACE_IN_PASSWORD") {
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('Spaces are not allowed in the password!<br/>');
        } else if (passwordStrength == "SIMILAR_TO_UID") {
            hasError(passwdControlGroup, passwordFeedback);
            warnBlock.html('User name is not allowed in the password!<br/>');
        } else {
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
            hasError(confirmPasswdControlGroup, confirmPasswdFeedback);
        } else {
            hasSuccess(confirmPasswdControlGroup, confirmPasswdFeedback);
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
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID contains a space or a special character!');
    } else {
        hasSuccess(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('');
    }
};

var registerUserIDValidation = function () {
    $('#userId').keyup(userIdInputKeyUp);
};

$(document).ready(function () {
    registerUserIDValidation();
    registerAcknowledgementCheck();
    registerPasswordMatcher();
    registerPasswordStrengthChecker();
    registerEmailValidityChecker();
    registerEmailMatcher();
});