package controllers;

import com.avaje.ebean.PagingList;
import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.WorksetMeta;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.AlgorithmDetails;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetails;
import models.Algorithm;
import models.User;
import models.Workset;
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
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;

//import models.Workset;


public class HTRCPortal extends Controller {

    private static Logger.ALogger log = play.Logger.of("application");
    private static Map<String, Object> session;


    @Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(index.render(User.find.byId(request().username())));
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
        return ok(about.render(User.find.byId(request().username())));
    }

    @Security.Authenticated(Secured.class)
    public static Result listWorkset(int sharedPage, int ownerPage) throws IOException, JAXBException {
        User loggedInUser = User.find.byId(request().username());
        updateWorksets(loggedInUser.accessToken, loggedInUser.userId, PlayConfWrapper.registryEPR(), true);
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
        User loggedInUser = User.find.byId(request().username());
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(loggedInUser.accessToken, PlayConfWrapper.registryEPR());
        Workset ws = Workset.findWorkset(worksetName);
        List<Volume> volumeList = new ArrayList<Volume>();
        if (!worksetName.contains(" ")) {
            volumeList = persistenceAPIClient.getWorksetVolumes(worksetName, worksetAuthor);
            System.out.println(worksetName + " : " + volumeList.size());
        }
        List<VolumeDetails> volumeDetailsList = new ArrayList<VolumeDetails>();
        System.out.println(volumeList.size());
        for (int i = 0; i <= volumeList.size() - 1; i++) {
            volumeDetailsList.add(getVolumeDetails(volumeList.get(i).getId()));
        }
        return ok(workset.render(loggedInUser, ws, volumeDetailsList));
    }

    @Security.Authenticated(Secured.class)
    public static Result listAlgorithms(int page) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.find.byId(request().username());
        updateAlgorithms(loggedInUser.accessToken,PlayConfWrapper.registryEPR());
        List<Algorithm> algorithms = Algorithm.algorithmPagingList().getPage(page -1).getList();

        return ok(views.html.algorithms.render(loggedInUser, algorithms, Algorithm.all().size()));
    }

    @Security.Authenticated(Secured.class)
    public static Result viewAlgorithm(String algorithmName) throws JAXBException, IOException, XMLStreamException {
        User loggedInUser = User.find.byId(request().username());
        AlgorithmDetails algorithmDetails = getAlgorithmDetails(loggedInUser.accessToken, algorithmName);
        List<AlgorithmDetails.Parameter> parameters = algorithmDetails.getParameters();
        List<Workset> worksetList = Workset.all();
        return ok(algorithm.render(loggedInUser,algorithmDetails,parameters,worksetList));
    }

    @Security.Authenticated(Secured.class)
    public static Result submitAlgorithm(){
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result listVMs(){
        User loggedInUser = User.find.byId(request().username());
        return ok(vmlist.render(loggedInUser));
    }

    @Security.Authenticated(Secured.class)
    public static Result createVMs(){
        User loggedInUser = User.find.byId(request().username());
        return ok(vmcreate.render(loggedInUser));
    }


    public static void updateWorksets(String accessToken, String userName, String registryUrl, Boolean isPublicWorkset) throws IOException, JAXBException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, registryUrl);

        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> worksetList = persistenceAPIClient.getWorksets(isPublicWorkset);
        for (edu.illinois.i3.htrc.registry.entities.workset.Workset w : worksetList) {
            WorksetMeta metadata = w.getMetadata();
            Workset alreadyExistWorkset = Workset.findWorkset(metadata.getName());
            if (alreadyExistWorkset != null) {
                Workset.delete(Workset.findWorkset(metadata.getName()));
            }
            List<Volume> volumeList = new ArrayList<Volume>();
            if (!metadata.getName().contains(" ")) {
                volumeList = persistenceAPIClient.getWorksetVolumes(metadata.getName(), metadata.getAuthor());
//                System.out.println(metadata.getName() + " : " + volumeList.size());
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

    public static VolumeDetails getVolumeDetails(String volid) throws IOException {
        String volumeDetailsQueryUrl = PlayConfWrapper.solrQueryUrl() + "?q=id:" + volid.replace(":", "%20") + "&fl=title,author,htrc_genderMale,htrc_genderFemale,htrc_genderUnknown,htrc_pageCount,htrc_wordCount";

        VolumeDetails volDetails = new VolumeDetails();

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
                        volDetails.setTitle(((Element) strElements.item(0)).getTextContent());
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
        Map<String, AlgorithmDetails> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY + " for user "
                    + session.get(PortalConstants.SESSION_USERNAME));
        }else{
            List<String> algorithms = new ArrayList<String>(allAlgorithms.keySet());
            List<AlgorithmDetails> algorithmDetailsList = new ArrayList<AlgorithmDetails>(allAlgorithms.values());

            for(AlgorithmDetails al:algorithmDetailsList ){
                if(Algorithm.findAlgoritm(al.getName()) != null){
                    Algorithm.delete(Algorithm.findAlgoritm(al.getName()));
                }
                String authors = StringUtils.collectionToCommaDelimitedString(al.getAuthors());
                Algorithm algorithm = new Algorithm(al.getName(),al.getDescription().substring(0,50),authors,al.getVersion());
                Algorithm.create(algorithm);
            }
        }

    }

    public static AlgorithmDetails getAlgorithmDetails(String accessToken, String algorithmName) throws JAXBException, IOException, XMLStreamException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, PlayConfWrapper.registryEPR());
        AlgorithmDetails algorithmDetails = new AlgorithmDetails();
        Map<String, AlgorithmDetails> allAlgorithms = persistenceAPIClient.getAllAlgorithmDetails();
        if (allAlgorithms == null) {
            log.error(PortalConstants.CANNOT_GETDATA_FROM_REGISTRY + " for user "
                    + session.get(PortalConstants.SESSION_USERNAME));
        }else{
            algorithmDetails = allAlgorithms.get(algorithmName);
        }
        return algorithmDetails;
    }

    public static class Login {
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
