package controllers;

import com.avaje.ebean.PagingList;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import models.User;
import models.Workset;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.about;
import views.html.index;
import views.html.login;
import views.html.worksets;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

import static play.data.Form.form;


public class HTRCPortal extends Controller {

    private static Logger.ALogger log = play.Logger.of("application");


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

    @Security.Authenticated(Secured.class)
    public static Result about() {
        return ok(about.render(User.find.byId(request().username())));
    }

    @Security.Authenticated(Secured.class)
    public static Result listWorkset(int sharedPage, int ownerPage) throws IOException, JAXBException {
        User loggedInUser = User.find.byId(request().username());
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(loggedInUser.accessToken);
        List<Workset> publicWorksets = persistenceAPIClient.getAllWorksets();
        for(Workset w : publicWorksets) {
            Workset.create(w);
        }
        PagingList<Workset> shared = Workset.shared();
        PagingList<Workset> owned = Workset.owned(loggedInUser);
        System.out.println("shared: " + shared.getTotalPageCount());
        System.out.println(owned.getTotalPageCount());
        return ok(worksets.render(loggedInUser,
                shared.getPage(sharedPage).getList(),
                owned.getPage(ownerPage).getList(),
                sharedPage,
                ownerPage,
                shared.getTotalPageCount(),
                owned.getTotalPageCount()));
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
