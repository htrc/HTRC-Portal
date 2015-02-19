var passwd = $("#password-field");
var confirmPasswd = $("#confirmPassword-field");

var checkPasswordValidity= function(){
    if(passwd.val() !== confirmPasswd.val()) {
        confirmPasswd.each( function() {this.setCustomValidity("Passwords Don't Match");});
        console.log("Passwords "+ passwd.val() + " and " + confirmPasswd.val() +" don't match" );
    } else {
        confirmPasswd.each(function(){this.setCustomValidity('');});
    }
};

passwd.change(checkPasswordValidity);
confirmPasswd.keyup(checkPasswordValidity);