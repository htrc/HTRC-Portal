
function isFunction(functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
}


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