var wsNameCorrect = false;

var uploadHasError = function (element, iconElement) {
    wsSubmitButtonVisibility();
    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var uploadHasSuccess = function (element, iconElement) {
    wsSubmitButtonVisibility();
    element.addClass('has-success');
    element.removeClass('has-error');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-ok');
    iconElement.removeClass('glyphicon-remove');
};

var wsSubmitButtonVisibility = function () {
    var wsSubmitButton = $('#ws-submit');

    // disable submit button at start
    wsSubmitButton.prop("disabled",true);

    if (wsNameCorrect) {
        wsSubmitButton.prop("disabled",false);
    }else{
        wsSubmitButton.prop("disabled",true);
    }
};

var checkWsNameValidity = function (wsname, onInvalidWSName, onValidWSName) {
    var request = $.ajax({
        url: "/iswsnamevalid?wsName=" + wsname,
        type: "GET",
        success: function (result) {
            if (!result.valid) {
                onInvalidWSName();
            } else {
                onValidWSName();
            }
        }
    });
};

var wsNameInputKeyUp = function () {
    var wsName = $(this).val();
    var wsNameControlGroup = $('#wsname-control-group');
    var wsNameWarnBlock = $('#wsname-warn-block');
    var wsNameFeedback = $('#wsname-feedback');

    if (wsName.indexOf(' ') >= 0 || wsName.match('[!,@@,#,$,%,\\,\/,^,&,*,?,~,(,),-]')) {
        wsNameCorrect = false;
        uploadHasError(wsNameControlGroup, wsNameFeedback);
        wsNameWarnBlock.html('Workset name contains a space or a special character!');
    } else if(wsName.length == 0) {
        wsNameCorrect = false;
        uploadHasError(wsNameControlGroup, wsNameFeedback);
        wsNameWarnBlock.html('Workset name cannot be empty!');
    }else{
        checkWsNameValidity(wsName,function () {
            wsNameCorrect = false;
            uploadHasError(wsNameControlGroup, wsNameFeedback);
            wsNameWarnBlock.html('You already have a workset with this name. Please choose an another name.');
        },function(){
        wsNameCorrect = true;
        uploadHasSuccess(wsNameControlGroup, wsNameFeedback);
        wsNameWarnBlock.html('');});
    }
};

var worksetNameValidation = function () {
   $('#uploadWorksetName').bind("change keyup" ,wsNameInputKeyUp);
};

$(document).ready(function () {
    worksetNameValidation();
    wsSubmitButtonVisibility();
});


