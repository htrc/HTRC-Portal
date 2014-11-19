jQuery(document).ready(function () {
    var options = {
        onLoad: function () {
            $('#messages').text('Start typing password');
        },
        onKeyUp: function (evt) {
            $(evt.target).pwstrength("changeScore");
        }

    };
    $('#password').pwstrength(options);
});

function showalert(message) {

    $('#alert_placeholder').append('<div id="alertdiv" class="alert alert-danger">' +
        '<a class="close" data-dismiss="alert">×</a>' +
        '<span>'+message+'</span>' +
        '</div>');

    setTimeout(function() { // this will automatically close the alert and remove this if the users doesnt close it in 5 secs


        $("#alertdiv").remove();

    }, 5000);
}

/*
 jQuery(document).ready(function () {
 "use strict";
 var $password = $(':password').pwstrength(),
 common_words = ["password", "god", "123456"];

 $password.pwstrength("addRule", "notEmail", function (options, word, score) {
 return word.match(/^([\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+\.)*[\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+@((((([a-z0-9]{1}[a-z0-9\-]{0,62}[a-z0-9]{1})|[a-z])\.)+[a-z]{2,6})|(\d{1,3}\.){3}\d{1,3}(\:\d{1,5})?)$/i) && score;
 }, -100, true);

 $password.pwstrength("addRule", "commonWords", function (options, word, score) {
 var result = false;
 $.each(common_words, function (i, item) {
 var re = new RegExp(item, "gi");
 if (word.match(re)) {
 result = score;
 }
 });
 return result;
 }, -500, true);
 });

 */




/*jslint vars: false, browser: true, nomen: true, regexp: true */
/*global jQuery */

/*
 * jQuery Password Strength plugin for Twitter Bootstrap
 *
 * Copyright (c) 2008-2013 Tane Piper
 * Copyright (c) 2013 Alejandro Blanco
 * Dual licensed under the MIT and GPL licenses.
 *
 */

(function ($) {
    "use strict";



    var options = {
            errors: [],
            // Options
            minChar: 15,
            errorMessages: {
                password_to_short: "The Password is too short",
                same_as_username: "Your password cannot be included your username",
                white_spaces_included: "Your password cannot be included any white spaces"
            },
            scores: [17, 26, 40, 50],
            verdicts: ["Weak", "Normal", "Medium", "Strong", "Very Strong"],
            showVerdicts: true,
            raisePower: 1.4,
            usernameField: "#userId",
//            emailField: "#email",
            onLoad: undefined,
            onKeyUp: undefined,
            viewports: {
                progress: undefined,
                verdict: undefined,
                errors: undefined
            },
            // Rules stuff
            ruleScores: {
                wordNotEmail: -100,
                wordLength: -100,
                wordSimilarToUsername: -100,
                wordLowercase: 1,
                wordUppercase: 1,
                wordOneNumber: 1,
                wordThreeNumbers: 1,
                wordOneSpecialChar: 1,
                wordTwoSpecialChar: 1,
                wordUpperLowerCombo: 2,
                wordLetterNumberCombo: 2,
                wordLetterNumberCharCombo: 2
            },
            rules: {
                wordNotEmail: true,
                wordLength: true,
                wordSimilarToUsername: true,
                wordLowercase: true,
                wordUppercase: true,
                wordOneNumber: true,
                wordThreeNumbers: false,
                wordOneSpecialChar: true,
                wordTwoSpecialChar: false,
                wordUpperLowerCombo: false,
                wordLetterNumberCombo: false,
                wordLetterNumberCharCombo: false
            },
            validationRules: {
                wordNotEmail: function (options, word, score) {
                    return word.match(/^([\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+\.)*[\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+@((((([a-z0-9]{1}[a-z0-9\-]{0,62}[a-z0-9]{1})|[a-z])\.)+[a-z]{2,6})|(\d{1,3}\.){3}\d{1,3}(\:\d{1,5})?)$/i) && score;
                },
//                wordSimilarToEmail: function (options, word, score) {
//                    var email = $(options.emailField).val();
//                    if (email && word.toLowerCase().match(email.toLowerCase())) {
//                        return score;
//                    }
//                    return score+1;
//                },
                wordLength: function (options, word, score) {
                    var wordlen = word.length,
                        lenScore = Math.pow(wordlen, options.raisePower);
                    if (wordlen < options.minChar) {
                        lenScore = (lenScore + score);
                        alert(options.errorMessages.password_to_short);
                        options.errors.push(options.errorMessages.password_to_short);
                    }
                    return lenScore;
                },
                wordSimilarToUsername: function (options, word, score) {
                    var username = $(options.usernameField).val();
                    if (username && word.toLowerCase().match(username.toLowerCase())) {
//                        options.errors.push(options.errorMessages.same_as_username);
                       showalert(options.errorMessages.same_as_username);
                       return score;
                    }
                    return score+1;
                },

                wordHasWhiteSpace: function(options,word,score){
                    if(word.indexOf(' ')>= 0){
                        showalert(options.errorMessages.white_spaces_included);
                        return score;
                    }
                    return score+1;
                },

                wordLowercase: function (options, word, score) {
                    return word.match(/[a-z]/) && score;
                },
                wordUppercase: function (options, word, score) {
                    return word.match(/[A-Z]/) && score;
                },
                wordOneNumber : function (options, word, score) {
                    return word.match(/\d+/) && score;
                },
                wordThreeNumbers : function (options, word, score) {
                    return word.match(/(.*[0-9].*[0-9].*[0-9])/) && score;
                },
                wordOneSpecialChar : function (options, word, score) {
                    return word.match(/.[!,@,#,$,%,\^,&,*,?,_,~]/) && score;
                },
                wordTwoSpecialChar : function (options, word, score) {
                    return word.match(/(.*[!,@,#,$,%,\^,&,*,?,_,~].*[!,@,#,$,%,\^,&,*,?,_,~])/) && score;
                },
                wordUpperLowerCombo : function (options, word, score) {
                    return word.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/) && score;
                },
                wordLetterNumberCombo : function (options, word, score) {
                    return word.match(/([a-zA-Z])/) && word.match(/([0-9])/) && score;
                },
                wordLetterNumberCharCombo : function (options, word, score) {
                    return word.match(/([a-zA-Z0-9].*[!,@,#,$,%,\^,&,*,?,_,~])|([!,@,#,$,%,\^,&,*,?,_,~].*[a-zA-Z0-9])/) && score;
                }
            }
        },

        setProgressBar = function ($el, score) {
            var options = $el.data("pwstrength"),
                progressbar = options.progressbar,
                $verdict;

            if (options.showVerdicts) {
                if (options.viewports.verdict) {
                    $verdict = $(options.viewports.verdict).find(".password-verdict");
                } else {
                    $verdict = $el.parent().find(".password-verdict");
                    if ($verdict.length === 0) {
                        $verdict = $('<span class="password-verdict"></span>');
                        // Hack to get verdict in to progress bar
                        //$verdict.insertAfter($el);
                        //$(".bar").appendChild($verdict);
                    }
                }
            }

            if (score < options.scores[0]) {
                progressbar.addClass("progress-bar-danger").removeClass("progress-bar-warning").removeClass("progress-bar-success");
                progressbar.find(".progress-bar").css("width", "5%");
                if (options.showVerdicts) {
                    $verdict.text(options.verdicts[0]);
                }
            } else if (score >= options.scores[0] && score < options.scores[1]) {
                progressbar.addClass("progress-bar-danger").removeClass("progress-bar-warning").removeClass("progress-bar-success");
                progressbar.find(".progress-bar").css("width", "25%");
                if (options.showVerdicts) {
                    $verdict.text(options.verdicts[1]);
                }
            } else if (score >= options.scores[1] && score < options.scores[2]) {
                progressbar.addClass("progress-bar-warning").removeClass("progress-bar-danger").removeClass("progress-bar-success");
                progressbar.find(".progress-bar").css("width", "50%");
                if (options.showVerdicts) {
                    $verdict.text(options.verdicts[2]);
                }
            } else if (score >= options.scores[2] && score < options.scores[3]) {
                progressbar.addClass("progress-bar-warning").removeClass("progress-bar-danger").removeClass("progress-bar-success");
                progressbar.find(".progress-bar").css("width", "75%");
                if (options.showVerdicts) {
                    $verdict.text(options.verdicts[3]);
                }
            } else if (score >= options.scores[3]) {
                progressbar.addClass("progress-bar-success").removeClass("progress-bar-warning").removeClass("progress-bar-danger");
                progressbar.find(".progress-bar").css("width", "100%");
                if (options.showVerdicts) {
                    $verdict.text(options.verdicts[4]);
                }
            }
        },

        calculateScore = function ($el) {
            var self = this,
                word = $el.val(),
                totalScore = 0,
                options = $el.data("pwstrength");

//            $.each(options.rules, function (rule, active) {
//                if (active === true) {
//                    var score = options.ruleScores[rule],
//                        result = options.validationRules[rule](options, word, score);
//                    if (result) {
//                        totalScore += result;
//                    }
//                }
//            });
            var rulesMatched = [0,0,0,0];

            rulesMatched[0] = options.validationRules.wordLowercase(options, word, 1);
            rulesMatched[1] = options.validationRules.wordUppercase(options, word, 1);
            rulesMatched[2] = options.validationRules.wordOneNumber(options, word, 1);
            rulesMatched[3] = options.validationRules.wordOneSpecialChar(options, word, 1);

            var compulsoryRules = [0,0];



            compulsoryRules[0] = options.validationRules.wordHasWhiteSpace(options,word,0);
            compulsoryRules[1] = options.validationRules.wordSimilarToUsername(options,word,0);


            // Now we have number of rules matched.
            var noOfRulesMatched = 0;
            for(var i= 0; i < rulesMatched.length; i++){
                console.log('' + i + ': ' + rulesMatched[i]);
                noOfRulesMatched = noOfRulesMatched + rulesMatched[i];
            }

            var noOfCompRulesMatched = 0;
            for(var j= 0; j < compulsoryRules.length; j++){
                console.log('' + j + ': ' + compulsoryRules[j]);
                noOfCompRulesMatched = noOfCompRulesMatched + compulsoryRules[j];
            }


//            if(noOfCompRulesMatched<3){
//                alert("It looks like your password has white spaces.");
//            }


            // Check the length
            var length = word.length;

            // Calculate the score
            if(noOfCompRulesMatched == 2){
                if (length < 8){
                    totalScore = 16;
                } else if (length > 8 && length < 15 && noOfRulesMatched > 0 ){
                    totalScore = 20;
                }  else if (length == 15 && noOfRulesMatched == 1 ){
                    totalScore = 20;
                } else if (length > 15 && noOfRulesMatched == 1 ){
                    totalScore = 20;
                } else if (length == 15 && noOfRulesMatched == 2 ){
                    totalScore = 30;
                }else if (length > 15 && noOfRulesMatched == 2 ){
                    totalScore = 30;
                } else if (length == 15 && noOfRulesMatched == 3 ){
                    totalScore = 45;
                }else if (length > 15 && noOfRulesMatched == 3 ){
                    totalScore = 55;
                } else if (length == 15 && noOfRulesMatched == 4 ){
                    totalScore = 55;
                } else if (length > 15 && noOfRulesMatched == 4 ){
                    totalScore = 55;
                }
            }else{
                totalScore = 16;
            }


//            if(noOfCompRulesMatched<3){
//                alert("It looks like your password include either of following things. \n\n -White space " +
//                    "\n -Username \n -Email \n\n Please consider you can't include any of above things in your password. " +
//                    "\n\n Thank you!");
//
//            }

            console.log("Rules Matched: " + noOfRulesMatched + " Word Length: " + length);
            setProgressBar($el, totalScore);
            return totalScore;
        },

        progressWidget = function () {
            return '<div class="progress col-sm-12" style="margin-top: 5px;float:none;margin-left: 0;"><div class="progress-bar"><span class="password-verdict"></span></div></div>';
        },

        methods = {
            init: function (settings) {
                var self = this,
                    allOptions = $.extend(options, settings);

                return this.each(function (idx, el) {
                    var $el = $(el),
                        progressbar,
                        verdict;

                    $el.data("pwstrength", allOptions);

                    $el.on("keyup", function (event) {
                        var options = $el.data("pwstrength");
                        options.errors = [];
                        calculateScore.call(self, $el);
                        if ($.isFunction(options.onKeyUp)) {
                            options.onKeyUp(event);
                        }
                    });

                    progressbar = $(progressWidget());
                    if (allOptions.viewports.progress) {
                        $(allOptions.viewports.progress).append(progressbar);
                    } else {
                        progressbar.insertAfter($el);
                    }
                    progressbar.find(".bar").css("width", "0%");
                    $el.data("pwstrength").progressbar = progressbar;

                    if (allOptions.showVerdicts) {
                        verdict = $('<span class="password-verdict">' + allOptions.verdicts[0] + '</span>');
                        if (allOptions.viewports.verdict) {
                            $(allOptions.viewports.verdict).append(verdict);
                        } else {
                            // Hack to get verdict in to progress bar
                            //verdict.insertAfter($el);
                            //$(".bar").appendChild(verdict);
                        }
                    }

                    if ($.isFunction(allOptions.onLoad)) {
                        allOptions.onLoad();
                    }
                });
            },

            destroy: function () {
                this.each(function (idx, el) {
                    var $el = $(el);
                    $el.parent().find("span.password-verdict").remove();
                    $el.parent().find("div.progress").remove();
                    $el.parent().find("ul.error-list").remove();
                    $el.removeData("pwstrength");
                });
            },

            forceUpdate: function () {
                var self = this;
                this.each(function (idx, el) {
                    var $el = $(el),
                        options = $el.data("pwstrength");
                    options.errors = [];
                    calculateScore.call(self, $el);
                });
            },

            outputErrorList: function () {
                this.each(function (idx, el) {
                    var output = '<ul class="error-list">',
                        $el = $(el),
                        errors = $el.data("pwstrength").errors,
                        viewports = $el.data("pwstrength").viewports,
                        verdict;
                    $el.parent().find("ul.error-list").remove();

                    if (errors.length > 0) {
                        $.each(errors, function (i, item) {
                            output += '<li>' + item + '</li>';
                        });
                        output += '</ul>';
                        if (viewports.errors) {
                            $(viewports.errors).html(output);
                        } else {
                            output = $(output);
                            verdict = $el.parent().find("span.password-verdict");
                            if (verdict.length > 0) {
                                el = verdict;
                            }
                            output.insertAfter(el);
                        }
                    }
                });
            },

            addRule: function (name, method, score, active) {
                this.each(function (idx, el) {
                    var options = $(el).data("pwstrength");
                    options.rules[name] = active;
                    options.ruleScores[name] = score;
                    options.validationRules[name] = method;
                });
            },

            changeScore: function (rule, score) {
                this.each(function (idx, el) {
                    $(el).data("pwstrength").ruleScores[rule] = score;
                });
            },

            ruleActive: function (rule, active) {
                this.each(function (idx, el) {
                    $(el).data("pwstrength").rules[rule] = active;
                });
            }
        };

    $.fn.pwstrength = function (method) {
        var result;
        if (methods[method]) {
            result = methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === "object" || !method) {
            result = methods.init.apply(this, arguments);
        } else {
            $.error("Method " +  method + " does not exist on jQuery.pwstrength");
        }
        return result;
    };
}(jQuery));