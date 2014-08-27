package controllers;


import com.avaje.ebean.PagingList;
import edu.indiana.d2i.htrc.portal.HTRCAgentClient;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.AlgorithmDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobSubmitBean;
import models.ActiveJob;
import models.Algorithm;
import models.User;
import models.Workset;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.algorithm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static play.data.Form.form;

public class AlgorithmManagement extends Controller{
    private static Logger.ALogger log = play.Logger.of("application");


    @Security.Authenticated(Secured.class)
    public static Result listAlgorithms(int page) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.findByUserID(request().username());
//        WorksetManagement.updateWorksets(session().get(PortalConstants.SESSION_TOKEN), PlayConfWrapper.registryEPR());
//        updateAlgorithms(session().get(PortalConstants.SESSION_TOKEN), PlayConfWrapper.registryEPR());
//        PagingList<Algorithm> algorithmsPL = Algorithm.algorithmPagingList();
//        List<Algorithm> algorithms = algorithmsPL.getPage(page - 1).getList();

        return ok(views.html.algorithms.render(loggedInUser, getAlgorithms()));
    }

    @Security.Authenticated(Secured.class)
    public static Result viewAlgorithm(String algorithmName) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.findByUserID(request().username());
        AlgorithmDetailsBean algorithmDetails = getAlgorithmDetails(session().get(PortalConstants.SESSION_TOKEN), algorithmName);
        List<AlgorithmDetailsBean.Parameter> parameters = algorithmDetails.getParameters();
        List<Workset> worksetList = Workset.all();
        return ok(algorithm.render(loggedInUser, algorithmDetails, parameters, worksetList, Form.form(SubmitJob.class)));
    }

    @Security.Authenticated(Secured.class)
    public static Result submitAlgorithm() throws Exception {
        AtomicReference<User> loggedInUser = new AtomicReference<>(User.findByUserID(request().username()));
        JobSubmitBean jobSubmitBean = new JobSubmitBean();
        DynamicForm requestData = form().bindFromRequest();
        jobSubmitBean.setJobName(requestData.get("jobName"));
        jobSubmitBean.setUserName(loggedInUser.get().userId);
        jobSubmitBean.setAlgorithmName(requestData.get("algorithmName"));
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
                parameter.setParamValue(requestData.field("parameters[" + index + "].paramValue").value());
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
            log.info(String.format("ActiveJob (id: %s) is submitted, status is %s",
                    jobDetailsBean.getJobId(), jobDetailsBean.getJobStatus()));
            ActiveJob job = new ActiveJob(jobDetailsBean.getJobId(), jobDetailsBean.getJobTitle(),
                    jobDetailsBean.getLastUpdatedDate(), jobDetailsBean.getJobStatus());
            job.save();
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

            for (AlgorithmDetailsBean al : algorithmDetailsList) {
                if (Algorithm.findAlgoritm(al.getName()) != null) {
                    Algorithm.delete(Algorithm.findAlgoritm(al.getName()));
                }
                String authors = StringUtils.collectionToCommaDelimitedString(al.getAuthors());
                Algorithm algorithm = new Algorithm(al.getName(), al.getDescription().substring(0, 50), authors, al.getVersion());
                Algorithm.create(algorithm);
            }
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
