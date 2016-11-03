/**
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package controllers;

import edu.indiana.d2i.htrc.portal.PortalConstants;
import play.Play;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.about;

import java.util.Date;

public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context ctx) {
        // see if user is logged in
        if (ctx.session().get(PortalConstants.SESSION_USERNAME) == null)
        return null;

        // see if the session is expired
        String previousTick = ctx.session().get("userTime");
        if (previousTick != null && !previousTick.equals("")) {
            long previousT = Long.valueOf(previousTick);
            long currentT = new Date().getTime();
            long timeout = Long.valueOf(Play.application().configuration().getString("sessionTimeout")) * 1000 * 60;
            if ((currentT - previousT) > timeout) {
                // session expired
                ctx.session().clear();
                return null;
            }
        }

        // update time in session
        String tickString = Long.toString(new Date().getTime());
        ctx.session().put("userTime", tickString);

        return ctx.session().get("userId");
//        return ctx.session().get("userId");
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        if(ctx.request().path().equals("/about")){
            return ok(about.render(null));
        }
        if(ctx.request().path().equals("/datasets")){
            return ok(about.render(null));
        }

        if(ctx.request().path().equals("/robort.txt")){
            return ok("User-agent: *\nDisallow: /blacklight/");
        }
        return redirect(routes.HTRCPortal.login());
    }
}
