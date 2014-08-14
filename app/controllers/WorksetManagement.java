package controllers;


import com.avaje.ebean.PagingList;
import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.WorksetMeta;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetailsBean;
import models.User;
import models.Workset;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.workset;
import views.html.worksets;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorksetManagement extends Controller {
    private static Logger.ALogger log = play.Logger.of("application");

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
        Workset ws = Workset.findWorkset(worksetName, worksetAuthor);
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

    public static void updateWorksets(String accessToken, String registryUrl) throws IOException, JAXBException {
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(accessToken, registryUrl);

        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> worksetList = persistenceAPIClient.getPublicWorksets();
        log.debug("Workset count: " + worksetList.size());
        for (edu.illinois.i3.htrc.registry.entities.workset.Workset w : worksetList) {
            WorksetMeta metadata = w.getMetadata();
            Workset alreadyExistWorkset = Workset.findWorkset(metadata.getName(), metadata.getAuthor());
            if (alreadyExistWorkset != null) {
                Workset.delete(Workset.findWorkset(metadata.getName(), metadata.getAuthor()));
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
}
