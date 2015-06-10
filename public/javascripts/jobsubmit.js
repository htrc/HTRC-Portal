var jobNameNotNull = false;

var jobSubmitHasError = function (element, iconElement) {
    jobSubmitButtonVisibility();
    element.addClass('has-error');
    element.removeClass('has-success');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-remove');
    iconElement.removeClass('glyphicon-ok');
};

var jobSubmitHasSuccess = function (element, iconElement) {
    jobSubmitButtonVisibility();
    element.addClass('has-success');
    element.removeClass('has-error');
    iconElement.addClass('glyphicon');
    iconElement.addClass('glyphicon-ok');
    iconElement.removeClass('glyphicon-remove');
};

var jobSubmitButtonVisibility = function () {
    var jobSubmitButton = $('#job-submit');

    // disable submit button at start
    jobSubmitButton.prop("disabled",true);

    if (jobNameNotNull) {
        jobSubmitButton.prop("disabled",false);
    }else{
        jobSubmitButton.prop("disabled",true);
    }
};

var jobNameInputKeyUp = function () {
    var jobName = $(this).val();
    var jobNameControlGroup = $('#jobname-control-group');
    var jobNameWarnBlock = $('#jobname-warn-block');
    var jobNameFeedback = $('#jobname-feedback');

    if(jobName.length == 0) {
        jobNameNotNull = false;
        jobSubmitHasError(jobNameControlGroup, jobNameFeedback);
        jobNameWarnBlock.html('Job name cannot be empty!');
    }else{
        jobNameNotNull = true;
        jobSubmitHasSuccess(jobNameControlGroup, jobNameFeedback);
        jobNameWarnBlock.html(' ');
    }
};

var jobNameValidation = function () {
    var jobName = $('#jobName');
    if(jobName.val().length > 0){
        $('#job-submit').prop("disabled",false);
    }
    jobName.bind("change keyup" ,jobNameInputKeyUp);

};

$(document).ready(function () {
    jobSubmitButtonVisibility();
    jobNameValidation();
});

