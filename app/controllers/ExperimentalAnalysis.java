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

import edu.indiana.d2i.htrc.portal.HTRCExperimentalAnalysisServiceClient;
import edu.indiana.d2i.htrc.sloan.bean.VMImageDetails;
import edu.indiana.d2i.htrc.sloan.bean.VMStatus;
import models.User;
import models.VirtualMachine;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.vmcreate;
import views.html.vmlist;
import views.html.vmstatus;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentalAnalysis extends Controller {
    private static Logger.ALogger log = play.Logger.of("application");


    @Security.Authenticated(Secured.class)
    public static Result listVMs() throws IOException, GeneralSecurityException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        List<VMStatus> vmList = serviceClient.listVMs(loggedInUser, session());
        return ok(vmlist.render(loggedInUser, vmList));
    }

    @Security.Authenticated(Secured.class)
    public static Result createVMForm() {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        List<VMImageDetails> vmImageDetailsList = new ArrayList<VMImageDetails>();
        try {
            vmImageDetailsList = serviceClient.listVMImages(loggedInUser, session());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ok(vmcreate.render(loggedInUser, Form.form(CreateVM.class), vmImageDetailsList));
    }

    @Security.Authenticated(Secured.class)
    public static Result createVM() throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        List<VMImageDetails> vmImageDetailsList = new ArrayList<VMImageDetails>();
        try {
            vmImageDetailsList = serviceClient.listVMImages(loggedInUser, session());
        } catch (GeneralSecurityException e) {
            log.error("Security exception while getting vm image details.", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("IO exception during vm image details retrieval.", e);
            throw new RuntimeException(e);
        }

        log.debug("Inside create vm action.");
        Form<CreateVM> createVMForm = Form.form(CreateVM.class).bindFromRequest();
        if (createVMForm.hasErrors()) {
            log.debug("Create VM form has errors." + createVMForm.errorsAsJson());
            return ok(vmcreate.render(loggedInUser, createVMForm, vmImageDetailsList));
        }
        return Results.redirect(controllers.routes.ExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result showVMStatus(String vmId) throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        VMStatus vmStatus = serviceClient.showVM(vmId, loggedInUser, session());
        return ok(vmstatus.render(loggedInUser, vmStatus));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteVM(String vmId) throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.deleteVM(vmId, loggedInUser, session());
        return Results.redirect(controllers.routes.ExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result startVM(String vmId) throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.startVM(vmId, loggedInUser, session());
        return Results.redirect(controllers.routes.ExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result stopVM(String vmId) throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.stopVM(vmId, loggedInUser, session());
        return Results.redirect(controllers.routes.ExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result switchVMMode(String vmId, String mode) throws IOException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.switchVMMode(vmId, mode, loggedInUser, session());
        return Results.redirect(controllers.routes.ExperimentalAnalysis.listVMs());
    }

    public static void updateVMList(User loggedInUser) {
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        try {
            List<VMStatus> vmList = serviceClient.listVMs(loggedInUser, session());
            for (VMStatus v : vmList) {
                VirtualMachine alreadyExist = VirtualMachine.findVM(v.getVmId());
                if (alreadyExist != null) {
                    VirtualMachine.deleteVM(alreadyExist);
                }
                VirtualMachine virtualMachine = new VirtualMachine(v.getVmId(), v.getState(), v.getMode());
                VirtualMachine.createVM(virtualMachine);
            }

        } catch (Exception e) {
            log.error("Update VM List Failed.", e);
        }
    }

    public static class CreateVM {
        @Constraints.Required
        public String vmImageName;

        @Constraints.Required
        public String userName;

        @Constraints.Required
        public String password;

        @Constraints.Required
        public String confirmPassword;

        @Constraints.Required
        public int numberOfVCPUs;

        @Constraints.Required
        public String memory;

        public String validate() {
            int mem = 0;

            if (!password.equals(confirmPassword)) {
                return "The Passwords do not match.";
            } if(password.length()>= 8){
                return "Password should be less than 8 characters.";
            }

            try {
                mem = Integer.parseInt(memory);
            } catch (Exception e) {
                log.error("Error parsing memory value.", e);
                return "Memory should be an integer between 1024 and 10240.";
            }

            if (mem < 1024 || mem > 4096) {
                return "Memory should be between 1024MB - 4096MB";
            } else {
                User loggedInUser = User.findByUserID(request().username());
                HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
                try {
                    String vmId = serviceClient.createVM(vmImageName, userName, password, String.valueOf(memory), String.valueOf(numberOfVCPUs), loggedInUser, session());
                } catch (Exception e) {
                    log.error("Error calling createVM in data capsule API.", e);
                    return "VM Creation failed. Internal Error occurred!!";
                }
            }

            return null;
        }
    }
}
