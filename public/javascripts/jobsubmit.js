var jobNameNotNull = true;
var worksetSelected = false;
var parametersNotNull = true;

var checkSelectedMyWorkset = function (myworksets) {
    var myWorksetDefault;

    if(myworksets[0].defaultSelected){
        alert(myworksets.value);
        myWorksetDefault = myworksets[0];
        if(myworksets.value != myWorksetDefault){
            worksetSelected = true;
        }
    }
};

var checkSelectedAllWorkset = function (allworksets) {
    var allWorksetDefault;

    if(allworksets[0].defaultSelected){
        allWorksetDefault = myworksets[0];
        if(allworksets.value != allWorksetDefault){
            worksetSelected = true;
        }
    }
};

var selectMyWorksets = function (myworksets,allworksets) {
    myworksets.show();
    allworksets.hide();
    //checkSelectedMyWorkset(myworksets)
};

function selectAllWorksets(myworksets,allworksets) {
    allworksets.show();
    myworksets.hide();
}

var selectTypeOfWorksets = function (){
    var myWorksetRadio = $('#myWorksetsCollection');
    var allWorksetRadio = $('#allWorksetsCollection');
    var myWorksetsMenu = $('#myWorksetsMenu');
    var allWorksetsMenu = $('#allWorksetsMenu');
    if(myWorksetRadio.prop("checked", true)){
        selectMyWorksets(myWorksetsMenu,allWorksetsMenu);
    }
    if(allWorksetRadio.prop("checked", true)){
        selectAllWorksets(myWorksetsMenu, allWorksetsMenu);
    }
};

var jobSubmitButtonVisibility = function () {
    var jobSubmitButton = $('#job-submit');

    // disable submit button at start
    jobSubmitButton.prop("disabled",true);

    if (worksetSelected && jobNameNotNull && parametersNotNull) {
        jobSubmitButton.prop("disabled",false);
    }else{
        jobSubmitButton.prop("disabled",true);
    }
};

//var jobNameInputKeyUp = function () {
//    var jobName = $(this).val();
//    var jobNameControlGroup = $('#jobname-control-group');
//    var jobNameWarnBlock = $('#jobname-warn-block');
//    var jobNameFeedback = $('#jobname-feedback');
//
//    if(jobName.length == 0) {
//        jobNameNotNull = false;
//        uploadHasError(jobNameControlGroup, jobNameFeedback);
//        jobNameWarnBlock.html('Job name cannot be empty!');
//    }else{
//        jobNameNotNull = true;
//        uploadHasError(jobNameControlGroup, jobNameFeedback);
//        jobNameWarnBlock.html(' ');
//    }
//};
//
//var jobNameValidation = function () {
//    $('#jobName').bind("change keyup" ,jobNameInputKeyUp);
//};

$(document).ready(function () {
    selectTypeOfWorksets();
    jobSubmitButtonVisibility();
    //jobNameValidation();
});

