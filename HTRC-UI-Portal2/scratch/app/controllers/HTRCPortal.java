package controllers;

import models.User;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.login;

import static play.data.Form.form;


public class HTRCPortal extends Controller {

    @Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(index.render(User.find.byId(request().username())));
    }

    public static Result login(){
        return ok(login.render(form(Login.class), null));
    }

    public static Result logout(){
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.HTRCPortal.login());
    }

    public static Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm,null));
        } else {
            session().clear();
            session("userId", loginForm.get().userId);
            return redirect(
                    routes.HTRCPortal.index()
            );
        }
    }

    public static class Login{
        public String userId;
        public String password;

        public String validate() throws OAuthProblemException, OAuthSystemException {
            System.out.println(userId);
            System.out.println(password);
            if (User.authenticate(userId, password) == null) {
                return "Invalid user or password";
            }
            return null;
        }
    }
  
}
