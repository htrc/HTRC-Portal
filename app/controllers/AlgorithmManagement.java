package controllers;


import edu.indiana.d2i.htrc.portal.HTRCAgentClient;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.AlgorithmDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobSubmitBean;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;
import views.html.algorithm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static controllers.HTRCPortal.isAccountActivated;
import static play.data.Form.form;

public class AlgorithmManagement extends JavaController {
    private static Logger.ALogger log = play.Logger.of("application");


    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result listAlgorithms(int page) throws JAXBException, IOException, XMLStreamException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        String userEmail = session(PortalConstants.SESSION_EMAIL);
        if(userId == null){
            return redirect(routes.HTRCPortal.userIdNotFound());
        } else if (!isAccountActivated(userId)){
            return redirect(routes.HTRCPortal.accountNotActivated(userId, userEmail));
        }

        return ok(views.html.algorithms.render(userId, getAlgorithms()));
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result viewAlgorithm(String algorithmName, String message) throws JAXBException, IOException, XMLStreamException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        String userEmail = session(PortalConstants.SESSION_EMAIL);
        if(userId == null){
            return redirect(routes.HTRCPortal.userIdNotFound());
        } else if (!isAccountActivated(userId)){
            return redirect(routes.HTRCPortal.accountNotActivated(userId, userEmail));
        }
        AlgorithmDetailsBean algorithmDetails = getAlgorithmDetails(session().get(PortalConstants.SESSION_TOKEN), algorithmName);
        List<AlgorithmDetailsBean.Parameter> parameters = algorithmDetails.getParameters();
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> userWorksetList = persistenceAPIClient.getUserWorksets();
        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> allWorksetList = persistenceAPIClient.getAllWorksets();

        return ok(algorithm.render(userId, algorithmDetails, parameters, userWorksetList, allWorksetList, Form.form(SubmitJob.class),message));
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result submitAlgorithm() throws Exception {
        String userId = session(PortalConstants.SESSION_USERNAME);
        String userEmail = session(PortalConstants.SESSION_EMAIL);
        if(userId == null){
            return redirect(routes.HTRCPortal.userIdNotFound());
        } else if (!isAccountActivated(userId)){
            return redirect(routes.HTRCPortal.accountNotActivated(userId, userEmail));
        }
        Form<SubmitJob> jobSubmitForm = form(SubmitJob.class).bindFromRequest();
        JobSubmitBean jobSubmitBean = new JobSubmitBean();
        DynamicForm requestData = form().bindFromRequest();
        String jobName = requestData.get("jobName");
        String algorithmName = requestData.get("algorithmName");
        if(jobName.length() > 0){
            jobSubmitBean.setJobName(jobName);
        }else{
            return redirect(routes.AlgorithmManagement.viewAlgorithm(algorithmName,"Job name cannot be null."));
        }
        jobSubmitBean.setUserName(userId);
        jobSubmitBean.setAlgorithmName(algorithmName);
        AlgorithmDetailsBean algorithmDetails;
        try {
            algorithmDetails = getAlgorithmDetails(session().get(PortalConstants.SESSION_TOKEN), requestData.get("algorithmName"));
        } catch (Exception e) {
            Logger.error("Error occurred during algorithm details request.", e);
            throw e;
        }

        List<AlgorithmDetailsBean.Parameter> parameters = null;
        if (algorithmDetails != null) {
            parameters = algorithmDetails.getParameters();
        }
        if (parameters != null) {
            List<JobSubmitBean.Parameter> parameterList = new ArrayList<>();
            for (AlgorithmDetailsBean.Parameter pm : parameters) {
                int index = parameters.indexOf(pm) + 1;
                JobSubmitBean.Parameter parameter = new JobSubmitBean.Parameter();
                parameter.setParamName(requestData.field("parameters[" + index + "].paramName").value());
                parameter.setParamType(requestData.field("parameters[" + index + "].paramType").value());
                if(Objects.equals(parameter.getParamType(), "collection")){
                    String collectionType = requestData.field("worksetsCollection" + (index - 1)).value();
                    String myWSValue = requestData.field("myWorksetsMenu" + (index - 1)).value();
                    String allWSValue = requestData.field("allWorksetsMenu" + (index - 1)).value();
                    switch (collectionType) {
                        case "myworksets":
                            parameter.setParamValue(myWSValue);
                            log.info("Workset has selected from my worksets:" + myWSValue);
                            break;
                        case "allworksets":
                            parameter.setParamValue(allWSValue);
                            log.info("Workset has selected from all worksets:" + allWSValue);
                            break;
                        default:
                            log.error("Error on workset selection. My workset value: " + myWSValue + ". All workset value: " + allWSValue);
                            break;
                    }
                }else{
                    parameter.setParamValue(requestData.field("parameters[" + index + "].paramValue").value());
                }
                boolean isRequired = Boolean.parseBoolean(requestData.field("parameters[" + index + "].paramRequired").value());
                if(isRequired){
                    if(parameter.getParamValue().length() == 0){
                        return redirect(routes.AlgorithmManagement.viewAlgorithm(algorithmName, "Please fill all the required parameters."));
                    }
                }
                parameterList.add(parameter);
            }
            jobSubmitBean.setParameters(parameterList);
        }

        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        JobDetailsBean jobDetailsBean = agentClient.submitJob(jobSubmitBean);
        if (jobDetailsBean == null) {
            log.error(String.format("Unable to submit job %s for user %s to agent",
                    jobSubmitBean.getJobName(), jobSubmitBean.getUserName()));
        } else {
            log.info(String.format("Active Job (id: %s) is submitted, status is %s",
                    jobDetailsBean.getJobId(), jobDetailsBean.getJobStatus()));
        }


        return redirect(routes.JobManagement.listJobs());
    }

    public static List<AlgorithmDetailsBean> getAlgorithms() throws JAXBException, IOException, XMLStreamException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        Map<String, AlgorithmDetailsBean> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();

        return new ArrayList<AlgorithmDetailsBean>(allAlgorithms.values());
    }

    public static void updateAlgorithms(String accessToken, String registryUrl) throws JAXBException, IOException, XMLStreamException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        Map<String, AlgorithmDetailsBean> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.warn(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY); //TODO: fix log message
        } else {
            List<AlgorithmDetailsBean> algorithmDetailsList = new ArrayList<>(allAlgorithms.values());
        }

    }

    public static AlgorithmDetailsBean getAlgorithmDetails(String accessToken, String algorithmName) throws JAXBException, IOException, XMLStreamException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        AlgorithmDetailsBean algorithmDetails = new AlgorithmDetailsBean();
        Map<String, AlgorithmDetailsBean> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.warn(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY); //TODO: fix log message
        } else {
            algorithmDetails = allAlgorithms.get(algorithmName);
        }
        return algorithmDetails;
    }

    public static class SubmitJob {
        public String jobName;
        public String userName;
        public String algorithmName;
        public List<JobParameters> parameters;
    }

    public static class JobParameters {
        public String paramName;
        public String paramType;
        public String paramValue;
    }
}
