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
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.vmcreate;
import views.html.vmlist;
import views.html.vmstatus;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HTRCExperimentalAnalysis extends Controller {

    @Security.Authenticated(Secured.class)
    public static Result listVMs() throws IOException {
        User loggedInUser = User.find.byId(request().username());
        updateVMList(loggedInUser);
        List<VirtualMachine> vmList = VirtualMachine.all();
        return ok(vmlist.render(loggedInUser, vmList));
    }

    @Security.Authenticated(Secured.class)
    public static Result createVMForm(){
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        List<VMImageDetails> vmDetailsList = new ArrayList<VMImageDetails>();
        try {
            vmDetailsList = serviceClient.listVMImages(loggedInUser);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ok(vmcreate.render(loggedInUser, Form.form(CreateVM.class),vmDetailsList));
    }

    @Security.Authenticated(Secured.class)
    public static Result createVM() throws IOException {
//        User loggedInUser = User.find.byId(request().username());
//        List<VirtualMachine> virtualMachinesList = VirtualMachine.all();
        Form<CreateVM> createVMForm = Form.form(CreateVM.class).bindFromRequest();
        if(createVMForm.hasErrors()){
            return badRequest();
        }
        return redirect(routes.HTRCExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result showVMStatus(String vmId) throws IOException {
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        VMStatus vmStatus = serviceClient.showVM(vmId,loggedInUser);
        return ok(vmstatus.render(loggedInUser,vmStatus));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteVM(String vmId) throws IOException {
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.deleteVM(vmId,loggedInUser);
        updateVMList(loggedInUser);
        return redirect(routes.HTRCExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result startVM(String vmId) throws IOException {
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.startVM(vmId, loggedInUser);
        return redirect(routes.HTRCExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result stopVM(String vmId) throws IOException{
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.stopVM(vmId, loggedInUser);
        return redirect(routes.HTRCExperimentalAnalysis.listVMs());
    }

    @Security.Authenticated(Secured.class)
    public static Result switchVMMode(String vmId, String mode) throws IOException {
        User loggedInUser = User.find.byId(request().username());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        serviceClient.switchVMMode(vmId, mode, loggedInUser);
        return redirect(routes.HTRCExperimentalAnalysis.listVMs());
    }

    public static void updateVMList(User loggedInUser){
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        try {
            List<VMStatus> vmList = serviceClient.listVMs(loggedInUser);
            for(VMStatus v:vmList){
                VirtualMachine alreadyExist = VirtualMachine.findVM(v.getVmId());
                if(alreadyExist != null){
                    VirtualMachine.deleteVM(alreadyExist);
                }
                VirtualMachine virtualMachine = new VirtualMachine(v.getVmId(),v.getState(),v.getMode());
                VirtualMachine.createVM(virtualMachine);
            }

        } catch (GeneralSecurityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public static class CreateVM{
        public String vmImageName;
        public String userName;
        public String password;
        public String confirmPassword;
        public int numberOfVCPUs;
        public int memory;

        public String validate(){
            if(!password.equals(confirmPassword)) {
                return "Passwords doesn't match.";
            } else{
                User loggedInUser = User.find.byId(request().username());
                HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
                try {
                    String vmId = serviceClient.createVM(vmImageName, userName, password, String.valueOf(memory), String.valueOf(numberOfVCPUs), loggedInUser);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return null;
                }
            }


            // TODO: Validate for other errors like empty name, wrong vm sizes, etc.

            return null;
        }
    }
}
