var userIdCorrect = false;

var validEmailDomain = false;

var confirmEmailMatch = false;

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

    email.bind("change keyup paste" , function () {
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

    email.bind("change keyup paste" ,function (){
        if(confirmEmail.val() == email.val()){
            confirmEmailMatch = true;
            hasSuccess(emailControlGroup, emailFeedback);
        } else {
            confirmEmailMatch = false;
            hasError(emailControlGroup, emailFeedback);
        }
    });
    confirmEmail.bind("change keyup paste" ,function (){
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

    if (userId.indexOf(' ') >= 0 || userId.match('[^a-zA-Z0-9]')) {
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
        userIdWarnBlock.html('User ID must be at least 3 characters long!');
    }else if(userId.match(/[A-Z]/)) {
        userIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID contains a uppercase character.');
    }else if(userId.length > 30){
        userIdCorrect = false;
        hasError(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('User ID must not be more than 30 characters long!');
    }else{
        userIdCorrect = true;
        hasSuccess(userIdControlGroup, userIdFeedback);
        userIdWarnBlock.html('');
    }
};

var registerUserIDValidation = function () {
    var userId = $("#userId");
    var value = "";
    userId.bind("change keyup paste" ,userIdInputKeyUp);
};

var acknowledgeButtonVisibility = function() {
    var acknowledgementCheckBox = $('#acknowledgement');
    if (userIdCorrect && passwordCorrect && validEmailDomain && confirmPasswordMatch && confirmEmailMatch) {
        acknowledgementCheckBox.prop("disabled",false);
    }else{
        acknowledgementCheckBox.prop("disabled",true);
    }
};

var triggerAlreadyFilledFields = setTimeout(function() {
    $('input').each(function() {
        var elem = $(this);
        if (elem.val()) elem.change();
    })
}, 250);


$(document).ready(function () {
    registerUserIDValidation();
    registerAcknowledgementCheck();
    registerEmailValidityChecker();
    registerEmailMatcher();
    acknowledgeButtonVisibility();
    registerPasswordMatcher();
    registerPasswordStrengthChecker();
    triggerAlreadyFilledFields();
});