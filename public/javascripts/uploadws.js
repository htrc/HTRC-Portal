var wsNameCorrect = false;
var inputFileCorrect = false;

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
    wsSubmitButton.prop("disabled", true);
    var inputFileValue = $('#inputCSV').val();
    if (wsNameCorrect && inputFileCorrect) {
        wsSubmitButton.prop("disabled", false);
    } else {
        wsSubmitButton.prop("disabled", true);
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

    if (wsName.indexOf(' ') >= 0 || wsName.indexOf('\'') >= 0 || wsName.match('[&?~!@#;%^*+={}|<>,"]') || wsName.match(/\\$/) || wsName.indexOf('\[') >= 0 || wsName.indexOf('\]') >= 0) {
        wsNameCorrect = false;
        uploadHasError(wsNameControlGroup, wsNameFeedback);
        wsNameWarnBlock.html('Workset name contains illegal characters &?~!@#;%^*+={}|<>,\'"\\ or spaces.');
    } else if (wsName.length == 0) {
        wsNameCorrect = false;
        uploadHasError(wsNameControlGroup, wsNameFeedback);
        wsNameWarnBlock.html('Workset name cannot be empty!');
    } else {
        checkWsNameValidity(wsName, function () {
            wsNameCorrect = false;
            uploadHasError(wsNameControlGroup, wsNameFeedback);
            wsNameWarnBlock.html('You already have a workset with this name. Please choose another name.');
        }, function () {
            wsNameCorrect = true;
            uploadHasSuccess(wsNameControlGroup, wsNameFeedback);
            wsNameWarnBlock.html('');
        });
    }
};

var inputFileChange = function () {
    var fileName = $(this).val();
    var validExtns = [".csv",".txt"];

    var inputFileControlGroup = $('#inputfile-control-group');
    var inputFileWarnBlock = $('#inputfile-warn-block');
    var inputFileFeedback = $('#inputfile-feedback');

    if (fileName.length != 0) {
        if (validExtns.indexOf(fileName.substr(fileName.lastIndexOf('.'))) > -1){
            inputFileCorrect = true;
            uploadHasSuccess(inputFileControlGroup, inputFileFeedback);
            inputFileWarnBlock.html('');
        } else {
            inputFileCorrect = false;
            uploadHasError(inputFileControlGroup, inputFileFeedback);
            inputFileWarnBlock.html('Workset should be uploaded as a CSV or TXT file.');
        }
    }else {
        inputFileCorrect = false;
        uploadHasError(inputFileControlGroup, inputFileFeedback);
        inputFileWarnBlock.html('Please upload a file.');
    }
};

var worksetNameValidation = function () {
    $('#uploadWorksetName').bind("change keyup", wsNameInputKeyUp);
};

var inputFileValidation = function () {
    $('#inputCSV').bind("change", inputFileChange);
};

$(document).ready(function () {
    worksetNameValidation();
    inputFileValidation();
    wsSubmitButtonVisibility();
});


