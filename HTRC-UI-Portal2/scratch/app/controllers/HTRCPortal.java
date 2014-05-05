package controllers;

import com.avaje.ebean.PagingList;
import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.WorksetMeta;
import edu.indiana.d2i.htrc.portal.*;
import edu.indiana.d2i.htrc.portal.bean.AlgorithmDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobSubmitBean;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetailsBean;
import edu.indiana.d2i.htrc.portal.exception.ChangePasswordUserAdminExceptionException;
import edu.indiana.d2i.htrc.portal.exception.UserAlreadyExistsException;
import models.*;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Constraint;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static play.data.Form.form;

//import models.Workset;


public class HTRCPortal extends Controller {

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
            return redirect(
                    routes.HTRCPortal.index()
            );
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result about() {
        return ok(about.render(User.findByUserID(request().username())));
    }

    @Security.Authenticated(Secured.class)
    public static Result listWorkset(int sharedPage, int ownerPage) throws IOException, JAXBException {
        User loggedInUser = User.findByUserID(request().username());
        updateWorksets(loggedInUser.accessToken, PlayConfWrapper.registryEPR());
        PagingList<Workset> shared = Workset.shared();
        PagingList<Workset> owned = Workset.owned(loggedInUser);
        List<Workset> allShared = Workset.listAllShared();
        List<Workset> allOwned = Workset.listAllOwned(loggedInUser.userId);

        return ok(worksets.render(loggedInUser,
                shared.getPage(sharedPage - 1).getList(),
                owned.getPage(ownerPage - 1).getList(),
                sharedPage,
                ownerPage,
                shared.getTotalPageCount(),
                owned.getTotalPageCount(),
                allShared.size(),
                allOwned.size()));
    }

    @Security.Authenticated(Secured.class)
    public static Result viewWorkset(String worksetName, String worksetAuthor) throws IOException, JAXBException {
        User loggedInUser = User.findByUserID(request().username());
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(loggedInUser.accessToken, PlayConfWrapper.registryEPR());
        Workset ws = Workset.findWorkset(worksetName);
        List<Volume> volumeList = new ArrayList<>();
        if (!worksetName.contains(" ")) {
            volumeList = persistenceAPIClient.getWorksetVolumes(worksetName, worksetAuthor);
        }
        List<VolumeDetailsBean> volumeDetailsList = new ArrayList<>();
        for (int i = 0; i <= volumeList.size() - 1; i++) {
            volumeDetailsList.add(getVolumeDetails(volumeList.get(i).getId()));
        }
        return ok(workset.render(loggedInUser, ws, volumeDetailsList));
    }

    @Security.Authenticated(Secured.class)
    public static Result listAlgorithms(int page) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.findByUserID(request().username());
        updateWorksets(loggedInUser.accessToken, PlayConfWrapper.registryEPR());
        updateAlgorithms(loggedInUser.accessToken, PlayConfWrapper.registryEPR());
        PagingList<Algorithm> algorithmsPL = Algorithm.algorithmPagingList();
        List<Algorithm> algorithms = algorithmsPL.getPage(page - 1).getList();

        return ok(views.html.algorithms.render(loggedInUser, algorithms, Algorithm.all().size(), page, algorithmsPL.getTotalPageCount()));
    }

    @Security.Authenticated(Secured.class)
    public static Result viewAlgorithm(String algorithmName) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.findByUserID(request().username());
        AlgorithmDetailsBean algorithmDetails = getAlgorithmDetails(loggedInUser.accessToken, algorithmName);
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
            algorithmDetails = getAlgorithmDetails(loggedInUser.get().accessToken, requestData.get("algorithmName"));
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

        HTRCAgentClient agentClient = new HTRCAgentClient(loggedInUser.get().accessToken);
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


        return redirect(routes.HTRCPortal.listJobs());
    }

    @Security.Authenticated(Secured.class)
    public static Result listJobs() {
        User loggedInUser = User.findByUserID(request().username());
        updateJobList(loggedInUser);
        return ok(joblist.render(loggedInUser, ActiveJob.all(), CompletedJob.all()));

    }

    @Security.Authenticated(Secured.class)
    public static Result cancelJobs() {
        List<String> activeJobIds = new ArrayList<>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();

        for (String key : keys) {
            if (form.get(key).length > 0) {
                activeJobIds.add(form.get(key)[0]);
            }
        }
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(loggedInUser.accessToken);
        boolean response = agentClient.cancelJobs(activeJobIds);
        if (response) {
            for (String jonId : activeJobIds) {
                ActiveJob.delete(ActiveJob.findByJobID(jonId));
            }
            log.info("Following ActiveJob Ids are canceled successfully :" + activeJobIds);
        } else {
            log.error("Error occured during ActiveJob cancellation process");
        }
        return redirect(routes.HTRCPortal.listJobs());
    }

    @Security.Authenticated(Secured.class)
    public static Result updateJobs() {
        List<String> completedJobIds = new ArrayList<>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();
        String actionType = form.get("update-type")[0].trim();
        for (String key : keys) {
            if (form.get(key).length > 0 && !key.equals("update-type")) {
                completedJobIds.add(form.get(key)[0]);
            }
        }
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(loggedInUser.accessToken);
        boolean response;
        if (actionType.equals("delete")) {
            log.info("Deleting jobs: " + completedJobIds);
            response = agentClient.deleteJobs(completedJobIds);
            if (response) {
                for (String jobId : completedJobIds) {
                    CompletedJob.delete(CompletedJob.findByJobID(jobId));
                }
                log.info("Following Completed Job Ids are deleted successfully :" + completedJobIds);
            } else {
                log.error("Error occured during Completed Job deletion process");
            }
        }
        if (actionType.equals("save")) {
            log.info("Saving jobs: " + completedJobIds);
            response = agentClient.saveJobs(completedJobIds);
            if (response) {
                log.info("Following Completed Job Ids are saved successfully :" + completedJobIds);
            } else {
                log.error("Error occured during Completed Job saving process");
            }
        }

        return redirect(routes.HTRCPortal.listJobs());
    }

    @Security.Authenticated(Secured.class)
    public static Result viewJobDetails(String jobId) {
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(loggedInUser.accessToken);
        List<String> jobIds = new ArrayList<>();
        jobIds.add(jobId);
        Map<String, JobDetailsBean> jobsDetails = agentClient.getJobsDetails(jobIds);
        return ok(jobdetails.render(loggedInUser, jobsDetails.values()));
    }

    public static Result createSignUpForm() {
        return ok(signup.render(Form.form(SignUp.class), null));
    }

    public static Result signUp() {
        Form<SignUp> signUpForm = form(SignUp.class).bindFromRequest();
        if (signUpForm.hasErrors()) {
            return badRequest(signup.render(signUpForm, null));
        }
        log.info(signUpForm.toString());
        return redirect(routes.HTRCPortal.login());
    }


    public static Result createPasswordResetMailForm() {
        return ok(passwordresetmail.render(Form.form(PasswordResetMail.class), null));
    }

    public static Result passwordResetMail() {
        Form<PasswordResetMail> passwordResetMailForm = form(PasswordResetMail.class).bindFromRequest();
        if (passwordResetMailForm.hasErrors()) {
            return badRequest(passwordresetmail.render(passwordResetMailForm, null));
        }
        log.info(passwordResetMailForm.toString());
        return ok("Password reset link sent to " + passwordResetMailForm.get().userEmail.substring(0, 4) + "...." + passwordResetMailForm.get().userEmail.substring(15));
    }

    public static Result createPasswordResetForm(String token) {
        return ok(passwordreset.render(Form.form(PasswordReset.class), null, token));
    }

    public static Result passwordReset() {
        Form<PasswordReset> passwordResetForm = form(PasswordReset.class).bindFromRequest();
        if (passwordResetForm.hasErrors()) {
            return badRequest(passwordreset.render(passwordResetForm, null, passwordResetForm.get().token));
        }
        log.info(passwordResetForm.toString());
        return ok("Password changed successfully. Click on the login link to begin: " + redirect(routes.HTRCPortal.login()));
    }


    public static void updateWorksets(String accessToken, String registryUrl) throws IOException, JAXBException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, registryUrl);

        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> worksetList = persistenceAPIClient.getPublicWorksets();
        for (edu.illinois.i3.htrc.registry.entities.workset.Workset w : worksetList) {
            WorksetMeta metadata = w.getMetadata();
            Workset alreadyExistWorkset = Workset.findWorkset(metadata.getName());
            if (alreadyExistWorkset != null) {
                Workset.delete(Workset.findWorkset(metadata.getName()));
            }
            List<Volume> volumeList = new ArrayList<>();
            if (!metadata.getName().contains(" ")) {
                volumeList = persistenceAPIClient.getWorksetVolumes(metadata.getName(), metadata.getAuthor());
            }

            Calendar calendar = metadata.getLastModified().toGregorianCalendar();
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm");
            formatter.setTimeZone(calendar.getTimeZone());
            String dateString = formatter.format(calendar.getTime());

            Workset ws = new Workset(
                    metadata.getName(),
                    metadata.getDescription(),
                    metadata.getAuthor(),
                    metadata.getLastModifiedBy(),
                    dateString,
                    volumeList.size(),
                    metadata.isPublic());
            Workset.create(ws);

        }

    }

    public static VolumeDetailsBean getVolumeDetails(String volid) throws IOException {
        String volumeDetailsQueryUrl = PlayConfWrapper.solrMetaQueryUrl() + "id:" + volid.replace(":", "%20") + "&fl=title,author,htrc_genderMale,htrc_genderFemale,htrc_genderUnknown,htrc_pageCount,htrc_wordCount";

        VolumeDetailsBean volDetails = new VolumeDetailsBean();

        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(volumeDetailsQueryUrl);
        method.setFollowRedirects(true);

        try {
            httpClient.executeMethod(method);
            volDetails.setVolumeId(volid);

            if (method.getStatusCode() == 200) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

                Document dom = documentBuilder.parse(method.getResponseBodyAsStream());

                NodeList result = dom.getElementsByTagName("result");
                NodeList arrays = ((Element) result.item(0)).getElementsByTagName("arr");
                NodeList integers = ((Element) result.item(0)).getElementsByTagName("int");
                NodeList longIntegers = ((Element) result.item(0)).getElementsByTagName("long");


                for (int i = 0; i < arrays.getLength(); i++) {
                    Element arr = (Element) arrays.item(i);

                    if (arr.hasAttribute("name") && arr.getAttribute("name").equals("title")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        volDetails.setTitle((strElements.item(0)).getTextContent());
                    } else if (arr.hasAttribute("name") && arr.getAttribute("name").equals("htrc_genderMale")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        String maleAuthor = "";

                        for (int j = 0; j < strElements.getLength(); j++) {
                            Element str = (Element) strElements.item(j);
                            if (j != strElements.getLength() - 1) {
                                maleAuthor += str.getTextContent();
                            } else {
                                maleAuthor += str.getTextContent();
                            }
                        }

                        volDetails.setMaleAuthor(maleAuthor);


                    } else if (arr.hasAttribute("name") && arr.getAttribute("name").equals("htrc_genderFemale")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        String femaleAuthor = "";

                        for (int j = 0; j < strElements.getLength(); j++) {
                            Element str = (Element) strElements.item(j);
                            if (j != strElements.getLength() - 1) {
                                femaleAuthor += str.getTextContent();
                            } else {
                                femaleAuthor += str.getTextContent();
                            }
                        }

                        volDetails.setFemaleAuthor(femaleAuthor);


                    } else if (arr.hasAttribute("name") && arr.getAttribute("name").equals("htrc_genderUnknown")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        String genderUnkownAuthor = "";

                        for (int j = 0; j < strElements.getLength(); j++) {
                            Element str = (Element) strElements.item(j);
                            if (j != strElements.getLength() - 1) {
                                genderUnkownAuthor += str.getTextContent();
                            } else {
                                genderUnkownAuthor += str.getTextContent();
                            }
                        }

                        volDetails.setGenderUnkownAuthor(genderUnkownAuthor);


                    }
                }

                for (int i = 0; i < integers.getLength(); i++) {
                    Element integer = (Element) integers.item(i);
                    if (integer.hasAttribute("name") && integer.getAttribute("name").equals("htrc_pageCount")) {
                        String pageCount = integer.getTextContent();
                        volDetails.setPageCount(pageCount);
                    }
                }
                for (int i = 0; i < longIntegers.getLength(); i++) {
                    Element longInteger = (Element) longIntegers.item(0);
                    if (longInteger.hasAttribute("name") && longInteger.getAttribute("name").equals("htrc_wordCount")) {
                        String wordCount = longInteger.getTextContent();
                        volDetails.setWordCount(wordCount);
                    }
                }

            } else {
                volDetails.setTitle("Error while querying solr.");
                volDetails.setMaleAuthor("Error while querying solr.");
                volDetails.setFemaleAuthor("Error while querying solr.");
                volDetails.setGenderUnkownAuthor("Error while querying solr.");
                volDetails.setWordCount("Error while querying solr.");
                volDetails.setPageCount("Error while querying solr.");
            }

            return volDetails;
        } catch (Exception e) {
            log.error("Error while getting volume details for volume: " + volid + " query url: " + volumeDetailsQueryUrl, e);
            throw new RuntimeException("Error while getting volume details for volume: " + volid + " response: \n" + method.getResponseBodyAsString(), e);
        }
    }

    public static void updateAlgorithms(String accessToken, String registryUrl) throws JAXBException, IOException, XMLStreamException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, PlayConfWrapper.registryEPR());
        Map<String, AlgorithmDetailsBean> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY);
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
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, PlayConfWrapper.registryEPR());
        AlgorithmDetailsBean algorithmDetails = new AlgorithmDetailsBean();
        Map<String, AlgorithmDetailsBean> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY);
        } else {
            algorithmDetails = allAlgorithms.get(algorithmName);
        }
        return algorithmDetails;
    }

    public static void updateJobList(User loggedInUser) {
        HTRCAgentClient agentClient = new HTRCAgentClient(loggedInUser.accessToken);
        Map<String, JobDetailsBean> activeJobs = agentClient.getActiveJobsDetails();
        Map<String, JobDetailsBean> completedJobs = agentClient.getCompletedJobsDetails();
        if (completedJobs == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_AGENT + " for user "
                    + loggedInUser.userId);
        } else {
            List<JobDetailsBean> completedJobsList = new ArrayList<>(completedJobs.values());
            for (JobDetailsBean job : completedJobsList) {
                if (ActiveJob.findByJobID(job.getJobId()) != null) {
                    ActiveJob.delete(ActiveJob.findByJobID(job.getJobId()));
                } else if (CompletedJob.findByJobID(job.getJobId()) == null) {
                    CompletedJob completedJob = new CompletedJob(job.getJobId(), job.getJobTitle(), job.getLastUpdatedDate(), job.getJobStatus(), job.getJobSavedStr());
                    completedJob.save();
                }
            }
        }
        if (activeJobs == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_AGENT + " for user "
                    + loggedInUser.userId);
        } else {
            List<JobDetailsBean> activeJobsList = new ArrayList<>(activeJobs.values());
            for (JobDetailsBean job : activeJobsList) {
                if (ActiveJob.findByJobID(job.getJobId()) != null) {
                    ActiveJob.delete(ActiveJob.findByJobID(job.getJobId()));
                }
                ActiveJob activeJob = new ActiveJob(job.getJobId(), job.getJobTitle(), job.getLastUpdatedDate(), job.getJobStatus());
                activeJob.save();
            }
        }

    }

    public static class Login {
        @Constraints.Required
        public String userId;

        @Constraints.Required
        public String password;

        public String validate() throws OAuthProblemException, OAuthSystemException {
            if (userId.isEmpty() || password.isEmpty()) {
                return "Please fill username and password.";
            }
            if (User.authenticate(userId, password) == null) {
                return "Invalid user or password";
            }
            return null;
        }
    }

    public static class SignUp {
        public String userId;
        public String password;
        public String confirmPassword;
        public String firstName;
        public String lastName;
        public String email;
        private final String[] permissions = {"/permission/admin/login"};
        private String status = null;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        public String validate() {
            if (userId.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty() || firstName.isEmpty()
                    || lastName.isEmpty() || email.isEmpty()) {

                return "Please fill all the fields.";
            }
            if (!password.equals(confirmPassword)) {
                return "Passwords do not match.";

            }
            if (userManager.isUserExists(userId)) {
                return "Username already exists.";
            } else {
                List<Map.Entry<String, String>> claims = new ArrayList<>();
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/givenname", firstName));
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/lastname", lastName));
                claims.add(new AbstractMap.SimpleEntry<>(
                        "http://wso2.org/claims/emailaddress", email));
                try {
                    userManager.createUser(userId, password, claims, permissions);
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class PasswordResetMail {
        public String userId;
        public String userEmail;

        HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();

        public String validate() {
            if (userId.isEmpty()) {
                return "User name is empty, Please enter username";
            }
            if (!userManager.isUserExists(userId)) {
                return "User Name Does Not Exist";
            } else {
                try {
                    userEmail = userManager.getEmail(userId);
                    Properties props = new Properties();

                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class",
                            "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");

                    Session session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication("htrc.portal", "anep0rtal");
                                }
                            }
                    );

                    try {

                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress("htrc.portal@gmail.com"));
                        message.setRecipients(Message.RecipientType.TO,
                                InternetAddress.parse(userEmail));
                        message.setSubject("Password Reset for HTRC Portal");
//                        User user = User.findByUserID(userId);
//                        Token.deleteAllTokens();
                        String passwordResetToken = Token.generateToken(userId, userEmail);
                        String url = PlayConfWrapper.portalUrl() + "/passwordreset" + "?" + "token=" + passwordResetToken;

                        // Now set the actual message
                        message.setText("Please click on following url to reset your password. \n" + url);

                        Transport.send(message);

                        log.info("Sent message successfully....");


                    } catch (MessagingException | NoSuchAlgorithmException | IOException e) {
                        throw new RuntimeException(e);  // TODO: Review exception handling logic.
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        }
    }

    public static class PasswordReset {
        public String token;
        public String newPassword;
        public String retypePassword;

        public String validate() {
            if (!newPassword.equals(retypePassword)) {
                return "The Passwords do not match.";

            } else {
                Token token1 = Token.findByToken(token);
                String userId = token1.userId;
                HTRCUserManagerUtility userManager = HTRCUserManagerUtility.getInstanceWithDefaultProperties();
                try {
                    userManager.changePassword(userId, newPassword);
                    token1.isTokenUsed = true;
                    token1.update();
                } catch (ChangePasswordUserAdminExceptionException e) {
                    throw new RuntimeException(e); // TODO: Review this.
                }
            }

            return null;
        }
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
