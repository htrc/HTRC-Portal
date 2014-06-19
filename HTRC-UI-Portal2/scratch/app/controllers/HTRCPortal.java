package controllers;

import edu.indiana.d2i.htrc.portal.HTRCUserManagerUtility;
import models.User;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.about;
import views.html.index;
import views.html.login;

import static play.data.Form.form;

public class HTRCPortal extends  Controller {

    private static Logger.ALogger log = play.Logger.of("application");


    @Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(index.render(User.findByUserID(request().username())));
    }

    public static Result login() {
        return ok(login.render(form(Login.class), null));
    }

    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.HTRCPortal.login());
    }

    public static Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm, null));
        } else {
            session().clear();
            session("userId", loginForm.get().userId);
            return redirect(routes.HTRCPortal.index());
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result about() {
        return ok(about.render(User.findByUserID(request().username())));
    }

    public static class Login {
        @Constraints.Required
        public String userId;

        @Constraints.Required
        public String password;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        public String validate() throws Exception {
            if (userId.isEmpty() || password.isEmpty()) {
                return "Please fill username and password.";
            }
            if (User.authenticate(userId, password) == null) {
                return "Invalid user or password";
            }
            if(!userManager.roleNameExists(userId)){
                return String.format("Please activate your account. Activation link has been sent to %s",userManager.getEmail(userId));
            }
            return null;
        }
    }




}
