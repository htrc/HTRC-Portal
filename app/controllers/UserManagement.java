package controllers;


import edu.indiana.d2i.htrc.portal.CSVReader;
import edu.indiana.d2i.htrc.portal.HTRCUserManagerUtility;
import edu.indiana.d2i.htrc.portal.PasswordChecker;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.exception.ChangePasswordUserAdminExceptionException;
import edu.indiana.d2i.htrc.portal.exception.UserAlreadyExistsException;
import models.Token;
import models.User;
import org.pac4j.play.java.JavaController;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static play.data.Form.form;

public class UserManagement extends JavaController {
    private static Logger.ALogger log = play.Logger.of("application");

    public static Result createSignUpForm() {
        return ok(signup.render(Form.form(SignUp.class), null));
    }

    public static Result signUp() {
        Form<SignUp> signUpForm = form(SignUp.class).bindFromRequest();

        if (signUpForm.hasErrors()) {
            if(!SignUp.isInstitutionalEmailDomain(signUpForm.data().get("email"))){
                return badRequest(accountrequest.render(Form.form(AccountRequest.class),null,signUpForm.data().get("firstName"),signUpForm.data().get("lastName"),signUpForm.data().get("email"), "Your email is not recognized as an institutional email by our system. Please fill this form and request an account for your existing email."));
            }
            return badRequest(signup.render(signUpForm, null));
        }
        log.info("User "+signUpForm.get().userId+" signed up successfully.");
        return ok(gotopage.render("Welcome to HTRC! You account activation link was sent to " + signUpForm.get().email, null, null,null));
    }

    public static Result activateAccount(String token) {
        Token token1 = Token.findByToken(token);
        if(token1 != null){
            String[] permissions = {"/permission/admin/login"};
            String userName = token1.userId;
//            Token.deleteToken(token);
            HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
            try {
                userManager.addRole(userName,userName,permissions);
            } catch (Exception e) {
                log.error("Cannot add role to the user: " + userName);
            }
            return ok(gotopage.render("Your account is activated successfully. Click on the login link to begin:", "login", "Login", null));
        }
        return ok(gotopage.render("Error on your activation link.",null,null,null));
    }

    public static Result createAccountRequestForm(){
        return ok(accountrequest.render(Form.form(AccountRequest.class), null, null, null, null, null));
    }

    public static Result accountRequest() {
        Form<AccountRequest> accountRequestForm = form(AccountRequest.class).bindFromRequest();
        if (accountRequestForm.hasErrors()) {
            return badRequest(accountrequest.render(accountRequestForm, null, accountRequestForm.data().get("firstName"), accountRequestForm.data().get("lastName"), accountRequestForm.data().get("email"), null));
        }
        log.info(accountRequestForm.toString());
        return ok(gotopage.render("Your account request was sent to HTRC support team. One of our team member will get back to you within 24 hours. Please visit our home page for more information about HTRC. ", "login" , "Home",null));
    }



    public static Result createPasswordResetMailForm() {
        return ok(passwordresetmail.render(Form.form(PasswordResetMail.class), null));
    }

    public static Result passwordResetMail() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Form<PasswordResetMail> passwordResetMailForm = form(PasswordResetMail.class).bindFromRequest();
        if (passwordResetMailForm.hasErrors()) {
            return badRequest(passwordresetmail.render(passwordResetMailForm, null));
        }

        String userId = passwordResetMailForm.get().userId;
        String userEmail;
        String userFirstName = userId; // User's name
        if(User.findByUserID(userId)!= null){
            User user = User.findByUserID(userId);
            userEmail= user.email;
//            userFirstName = user.userFirstName;
        }else{
            HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
            userEmail = userManager.getEmail(userId);

        }

        String passwordResetToken = Token.generateToken(userId, userEmail);
        String url = PlayConfWrapper.portalUrl() + "/passwordreset" + "?" + "token=" + passwordResetToken;
        sendMail(userEmail, "Password Reset for HTRC Portal","Hi " +userFirstName+",\n" +"Looks like you'd like to change your HTRC Portal password.Please click the following link to do so: \n" + url +"\n Please disregard this e-mail if you did not request a password reset.\n \n Cheers, \n HTRC Team." );
        return ok(gotopage.render("Password reset link sent to " + userEmail.substring(0, 4) + "......" + userEmail.substring(userEmail.indexOf("@")),null,null,null));
    }

    public static Result createPasswordResetForm(String token) {
        return ok(passwordreset.render(Form.form(PasswordReset.class), null, token));
    }

    public static Result passwordReset() {
        Form<PasswordReset> passwordResetForm = form(PasswordReset.class).bindFromRequest();
        if (passwordResetForm.hasErrors()) {
            return badRequest(passwordreset.render(passwordResetForm, null, passwordResetForm.data().get("token")));
        }
        Token token1 = Token.findByToken(passwordResetForm.get().token);
        String userId = token1.userId;
        log.info("Password reset token for user ID " +userId+" : " + token1.token);
        log.info("Is token used: " + token1.isTokenUsed);
        log.info("Token created at: " + token1.createdTime);
        if(!Token.isTokenExpired(token1)) {
            if (token1.isTokenUsed.equals("NO")) {
                HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
                try {
                    userManager.changePassword(userId, passwordResetForm.get().password);
                } catch (ChangePasswordUserAdminExceptionException e) {
                    log.error("Cannot change user password due to error in User Admin");
                    throw new RuntimeException(e); // TODO: Review this.
                }

                token1.isTokenUsed = "YES";
                token1.update();
                return ok(gotopage.render("Password changed successfully. Click on the login link to begin:", "login", "Login", null));
            }
        }
        return ok(gotopage.render("We were unable to reset your password. Please check your email for a more recent password reset email, or request a ", "passwordresetmail", "new one.", null));



    }

    public static Result createUserIDRetrieveMailForm() {
        return ok(useridretrievemail.render(Form.form(UserIDRetrieveMail.class), null));
    }

    public static Result userIDRetrieveMail() throws RemoteException {
        Form<UserIDRetrieveMail> userIDRetrieveMailForm = form(UserIDRetrieveMail.class).bindFromRequest();
        if (userIDRetrieveMailForm.hasErrors()) {
            return badRequest(useridretrievemail.render(userIDRetrieveMailForm, null));
        }

        String userEmail = userIDRetrieveMailForm.get().userEmail;
        List<String> userIds = userIDRetrieveMailForm.get().userIDs;

        if(userIds.isEmpty()){
            return ok(gotopage.render("Cannot find user with email " + userEmail + " !", "login", "Login", null));
        }

        sendMail(userEmail,"Retrieve User ID.", "Your User ID: "+ userIds + ". To login please go to " + PlayConfWrapper.portalUrl() + "/login");
//        userIds.clear();
//        userIDRetrieveMailForm.get().userIDs.clear();
        log.info(userIDRetrieveMailForm.toString());
        return ok(gotopage.render("Your user ID is sent to " + userEmail + ". Click on the login link to begin:", "login", "Login",null));
    }


    public static class SignUp {
        public static Map<String,Integer> instDomains;
        public String userId;
        public String password;
        public String confirmPassword;
        public String firstName;
        public String lastName;
        public String email;
//        private final String[] permissions = {"/permission/admin/login"};
        private String status = null;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
        PasswordChecker passwordChecker = new PasswordChecker();

        public String validate() {
            if (userId.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty() || firstName.isEmpty()
                    || lastName.isEmpty() || email.isEmpty()) {

                return "Please fill all the fields.";
            }
            log.info("User ID: " + userId);
            if(userId.contains("@")){
                return "User ID must not contain '@' sign. ";
            }
            if (userManager.isUserExists(userId)) {
                return "Username already exists.";
            }

            if (userManager.roleNameExists(userId)) {
                return "There's a role name already exists with this name. Please use another username.";
            }

            if (passwordValidate(password,confirmPassword) != null){
                return passwordValidate(password,confirmPassword);
            }


            if (!isInstitutionalEmailDomain(email)) {
                return "Email is not an institutional email. Please enter your institutional email." +
                        "If you don't have an institutional email or if your email is not recognized by our system, " +
                        "please press 'Request Account' with your current email address ";
            } else {
                List<Map.Entry<String, String>> claims = new ArrayList<>();
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/givenname", firstName));
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/lastname", lastName));
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/emailaddress", email));
                try {
                    userManager.createUser(userId, password, claims);
                    sendUserRegistrationEmail(email, userId);
                    setStatus("Success");

                } catch (UserAlreadyExistsException e) {
                    log.warn(e.getMessage());
                    setStatus("Failed");
                } catch (Exception e) {
                    log.error("Unable to sign up user.", e);
                }

            }
            return null;
        }

        public void sendUserRegistrationEmail(String userEmail, String userId) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            String userRegistrationToken = Token.generateToken(userId, userEmail);
            String url = PlayConfWrapper.portalUrl() + "/activateaccount" + "?" + "token=" + userRegistrationToken;
            sendMail(userEmail, "User Registration for HTRC Portal", "Welcome to HTRC. Please click on following url to activate your account. \n" + url );

        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }



        public static boolean isInstitutionalEmailDomain(String email) {

            instDomains.putAll(CSVReader.readAndSaveInstDomains(PlayConfWrapper.validDomainsThirdCSV()));
            if (email.isEmpty()) {
                log.warn("Email is empty");
                return false;
            } else {
                String domainName = email.substring(email.indexOf("@") + 1);
                if(domainName.indexOf(".") != domainName.lastIndexOf(".")){
                    domainName = domainName.substring(domainName.indexOf(".") + 1);
                }
                if(instDomains.containsKey(domainName)){
                    log.info(domainName + " is an institutional email domain");
                    return true;
                }
                log.warn(domainName + " is not an institutional email domain");
                return false;
            }
        }
    }

    public static class AccountRequest{
        public String firstName;
        public String lastName;
        public String email;
        public String institution;
        public String motivation;

        public String validate() throws Exception {
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || institution.isEmpty()) {
                return "Please fill all the required fields.";
            }else{
                sendMail(PlayConfWrapper.supportEmail(), "Account request for HTRC Portal", "Following user has requested an account.\n" +
                        "User's name : " + firstName + " " + lastName + ";\n" +
                        "User's email : " + email + ";\n" +
                        "Institution/Employer : " + institution + ";\n" +
                        "Purpose/Motivation : " + motivation);
                return null;
            }
        }

    }

    public static class PasswordResetMail {
        public String userId;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        public String validate() {
            if (userId.isEmpty()) {
                return "User name is empty, Please enter username";
            }
            if (!userManager.isUserExists(userId)) {
                return "User Name Does Not Exist";
            } else {
                return null;
            }
        }
    }

    public static class PasswordReset {
        public String token;
        public String password;
        public String retypePassword;

        PasswordChecker passwordChecker = new PasswordChecker();


        public String validate() {
            log.debug("Token in the form: "+ token);
            if(passwordValidate(password,retypePassword) != null){
                return passwordValidate(password,retypePassword);
            }
            return null;
        }
    }

    public static class UserIDRetrieveMail {
        public String userEmail;
        List<String> userIDs;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        public String validate() throws RemoteException {
            if (userEmail.isEmpty()) {
                return "Email is empty, Please enter your email";
            }
            userIDs = userManager.getUserIdsFromEmail(userEmail);
            log.info("User ID List : " + userIDs);
            if (userIDs.isEmpty()) {
                return "User name does not exist for the email: " + userEmail;
            } else {
                return null;
            }
        }
    }

    public static void sendMail(String recipientEmail, String subject, String emailBody){
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        log.info("Email: " + PlayConfWrapper.htrcEmail() + " password: " + PlayConfWrapper.htrcEmailPassword());

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(PlayConfWrapper.htcEmailUserName(), PlayConfWrapper.htrcEmailPassword());
                    }
                }
        );

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(PlayConfWrapper.htrcEmail()));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(emailBody);
            Transport.send(message);

            log.info("Message with subject : " + subject + " is sent successfully.");


        } catch (MessagingException e) {
            throw new RuntimeException(e);  // TODO: Review exception handling logic.
        }

    }

    public static String passwordValidate(String password, String retypePassword){
        PasswordChecker passwordChecker = new PasswordChecker();

        if(password.isEmpty()){
            return "Password is empty, Please enter password.";
        }
        if(password.length()< 15){
            return "Password should be more than 15 characters long.";
        }if(password.contains(" ")){
            return "Password should not contain any white spaces.";}
        if (!passwordChecker.isValidPassword(password)) {
            return "Please use a strong password.";
        }if (!password.equals(retypePassword)) {
            return "The Passwords do not match.";
        }
        return null;
    }
}
