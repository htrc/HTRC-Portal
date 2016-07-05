package controllers;


import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.illinois.i3.htrc.registry.entities.workset.Property;
import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.Workset;
import edu.indiana.d2i.htrc.portal.CSV2WorksetXMLConverter;
import edu.indiana.d2i.htrc.portal.HTRCPersistenceAPIClient;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetailsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorksetManagement extends JavaController {
    private static Logger.ALogger log = play.Logger.of("application");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");


    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result allWorksets() throws IOException, JAXBException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> worksetList = persistenceAPIClient.getAllWorksets();
        return ok(allworksetstable.render(userId, worksetList));
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result userWorksets() throws IOException, JAXBException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        List<edu.illinois.i3.htrc.registry.entities.workset.Workset> worksetList = persistenceAPIClient.getUserWorksets();
        return ok(userworksetstable.render(userId, worksetList));
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result viewWorkset(String worksetName, String worksetAuthor) throws IOException, JAXBException{
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        edu.illinois.i3.htrc.registry.entities.workset.Workset ws = persistenceAPIClient.getWorkset(worksetName, worksetAuthor);
        List<Volume> volumeList = new ArrayList<>();
        if (!worksetName.contains(" ")) {
            volumeList = persistenceAPIClient.getWorksetVolumes(worksetName, worksetAuthor);
            int totalNoOfVolumes = volumeList.size();
            if(totalNoOfVolumes > 200){
                List<Volume> first200VolumeList = new ArrayList<>();
                List<VolumeDetailsBean> first200VolumeDetailsList = new ArrayList<>();
                for (int i = 0; i <= 199; i++) {
                    String volumeId = volumeList.get(i).getId();
                    log.debug("Volume Id: "+volumeId);
                    if(volumeId.isEmpty()){
                        log.error("Volume Id is empty.");
                    }else{
                        models.Volume alreadyExistVolume = models.Volume.findByVolumeID(volumeId);
                        if(alreadyExistVolume != null){
                            VolumeDetailsBean volumeDetailsBean = new VolumeDetailsBean();
                            volumeDetailsBean.setVolumeId(alreadyExistVolume.volumeId);
                            volumeDetailsBean.setTitle(alreadyExistVolume.title);
                            volumeDetailsBean.setMaleAuthor(alreadyExistVolume.maleAuthor);
                            volumeDetailsBean.setFemaleAuthor(alreadyExistVolume.femaleAuthor);
                            volumeDetailsBean.setGenderUnkownAuthor(alreadyExistVolume.genderUnkownAuthor);
                            volumeDetailsBean.setPageCount(alreadyExistVolume.pageCount);
                            volumeDetailsBean.setWordCount(alreadyExistVolume.wordCount);
                            first200VolumeDetailsList.add(volumeDetailsBean);
                            log.info("Volume Id: "+ volumeId + " is already exists in Volume object in portal.");
                        }else{
                            VolumeDetailsBean volumeDetailsBean = new VolumeDetailsBean();
                            volumeDetailsBean = persistenceAPIClient.getVolumeDetails(volumeList.get(i).getId());
                            first200VolumeDetailsList.add(volumeDetailsBean);
                            models.Volume nVolume = new models.Volume();
                            nVolume.volumeId = volumeDetailsBean.getVolumeId();
                            nVolume.title = volumeDetailsBean.getTitle();
                            nVolume.maleAuthor = volumeDetailsBean.getMaleAuthor();
                            nVolume.femaleAuthor = volumeDetailsBean.getFemaleAuthor();
                            nVolume.genderUnkownAuthor = volumeDetailsBean.getGenderUnkownAuthor();
                            nVolume.pageCount = volumeDetailsBean.getPageCount();
                            nVolume.wordCount = volumeDetailsBean.getWordCount();
                            nVolume.save();
                            log.info("Volume Id: "+ volumeId + " details retrieved from Solr and saved to Volume object.");
                        }
                    }
                }
                log.debug("Workset: " + ws);
                log.debug("Volumes: " + totalNoOfVolumes);
                log.info("Workset" + worksetName + " has more than 200 volumes.");
                 return ok(workset.render(userId, ws, first200VolumeDetailsList));
            }else{
                List<VolumeDetailsBean> volumeDetailsList = new ArrayList<>();
                for (int i = 0; i <= volumeList.size() - 1; i++) {
                    String volumeId = volumeList.get(i).getId();
                    log.debug("Volume Id: " + volumeId);
                    if(volumeId.isEmpty()){
                        log.error("Volume Id is null.");
                    }else{
                        volumeDetailsList.add(persistenceAPIClient.getVolumeDetails(volumeId));
                    }

                }

                log.debug("Workset: " + ws);
                log.debug("Volumes: " + totalNoOfVolumes);
                log.info("Workset" + worksetName + " has lesser than 200 volumes.");
                return ok(workset.render(userId, ws, volumeDetailsList));

            }
        }
        return ok(gotopage.render("Error occurred retrieving "+ worksetName +" workset. Please try again later.", "routes.WorksetManagement.allWorksets()","Go to Workset List page.", userId));
        

    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result uploadWorksetForm() {
        String userId = session(PortalConstants.SESSION_USERNAME);
        return ok(worksetupload.render(userId));
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result uploadWorkset() throws ParserConfigurationException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart csv = body.getFile("csv");
        String[] worksetName = body.asFormUrlEncoded().get("uploadWorksetName");
        String[] description = body.asFormUrlEncoded().get("uploadWSdescription");
        boolean isPrivateWorkset = body.asFormUrlEncoded().containsKey("privateWorkset");
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());

        if (csv != null) {
            String wsName;
            String wsDescription;
            String csvFileName = csv.getFilename();
            String contentType = csv.getContentType();
            File csvFile = csv.getFile();
            log.info("CSV file name: " + csvFileName + " content type: " + contentType +
                    " is private: " + isPrivateWorkset + " workset name: " + worksetName[0]);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
            Document worksetDoc = domBuilder.newDocument();

            try {
                Document volumesDoc = CSV2WorksetXMLConverter.convert(csvFile);
                if(worksetName[0].length() <= 0){
                    wsName = FilenameUtils.removeExtension(csvFileName);
                } else {
                    wsName = worksetName[0];
                }
                Element worksetEle = worksetDoc.createElement("workset");
                worksetEle.setAttribute("xmlns", "http://registry.htrc.i3.illinois.edu/entities/workset");
                worksetDoc.appendChild(worksetEle);

                Element metadataEle = worksetDoc.createElement("metadata");

                Element name = worksetDoc.createElement("name");
                name.setTextContent(wsName);
                metadataEle.appendChild(name);
                if(description == null || description[0].length() <= 0){
                    wsDescription = null;
                } else {
                    wsDescription = description[0];
                }

                Element descriptionEle = worksetDoc.createElement("description");
                descriptionEle.setTextContent(wsDescription);
                metadataEle.appendChild(descriptionEle);

                Element authorEle = worksetDoc.createElement("author");
                authorEle.setTextContent(userId);
                metadataEle.appendChild(authorEle);

                Element ratingEle = worksetDoc.createElement("rating");
                ratingEle.setTextContent("0");
                metadataEle.appendChild(ratingEle);

                Element avgRatingEle = worksetDoc.createElement("avgRating");
                avgRatingEle.setTextContent("0.0");
                metadataEle.appendChild(avgRatingEle);

                Element lastModifiedEle = worksetDoc.createElement("lastModified");
                lastModifiedEle.setTextContent(format(new Date()));
                metadataEle.appendChild(lastModifiedEle);

                worksetEle.appendChild(metadataEle);

                Node volumes = worksetDoc.adoptNode(volumesDoc.getDocumentElement());
                Element content = worksetDoc.createElement("content");
                content.appendChild(volumes);
                worksetEle.appendChild(content);




            } catch (Exception e) {
                log.error("Error on converting to xml file.");
                throw new RuntimeException("Error on converting to xml file.",e);
            }
            try {
                persistenceAPIClient.createWorkset(domToString(worksetDoc),!isPrivateWorkset);
            } catch (Exception e) {
                log.error("Error when uploading workset in to registry");
                throw new RuntimeException("Error when uploading workset in to registry");
            }
            log.info("Workset :" + wsName + " uploaded successfully. ");
            return redirect(routes.WorksetManagement.viewWorkset(wsName,userId));
        } else {
            flash("error", "Missing file");
            return ok(warnings.render("Please upload a CSV file",null,null,userId));
        }
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result downloadWorkset(String worksetName, String worksetAuthor) throws IOException, JAXBException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        List<Volume> volumes = persistenceAPIClient.getWorksetVolumes(worksetName, worksetAuthor);

        File csv = File.createTempFile(worksetName + "-" + worksetAuthor, ".csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(csv));

        if(volumes == null || volumes.isEmpty()){
            return notFound("Workset " + worksetName + " from " + worksetAuthor + " not found.");
        }

        Set<String> headers = new HashSet<String>();
        Map<String, Map<String, String>> volumesMap = new HashMap<String, Map<String,
                String>>();

        for (Volume volume : volumes) {
            Map<String, String> props = new HashMap<String, String>();

            if(volume.getProperties() != null){
                if (volume.getProperties().getProperty().size() > 0) {
                    for (Property volumeProperty : volume.getProperties().getProperty()) {
                        headers.add(volumeProperty.getName());
                        props.put(volumeProperty.getName(), volumeProperty.getValue());
                    }
                }

            }

            volumesMap.put(volume.getId(), props);
        }

        String[] headersArray = headers.toArray(new String[headers.size()]);
        String[] volID = {"volume_id"};
        String[] headersWithVolumeIdField = (String[]) ArrayUtils.addAll(volID, headersArray);

        csvWriter.writeNext(headersWithVolumeIdField);

        for(Map.Entry<String, Map<String, String>> entry : volumesMap.entrySet()){
            String[] csvRow = new String[headersWithVolumeIdField.length];

            // volume id
            csvRow[0] = entry.getKey();

            Map<String, String> propsMap = entry.getValue();

            for(int i = 0; i < headersArray.length; i++){
                csvRow[i + 1] = propsMap.get(headersArray[i]);
            }

            csvWriter.writeNext(csvRow);
        }

        csvWriter.close();

        response().setContentType("text/csv");
        response().setHeader("Content-Disposition", "attachment;filename=" + worksetName + "-" + worksetAuthor +".csv");
        response().setHeader("Cache-control", "private");

        return ok(csv);
    }

    /**
     * This is to check whether user already has a workset in with the given name
     * @param wsName Workset's name
     * @return
     */
    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result validateWSName(String wsName) throws IOException, JAXBException {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        Workset ws = persistenceAPIClient.getWorkset(wsName, userId);
        // Validate the Workset Name and set isValid.
        ObjectNode result = Json.newObject();
        result.put("valid", ws == null);
        return ok(result);
    }


    private static String domToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    public static synchronized String format(Date xmlDateTime) throws IllegalFormatException {
        String s =  simpleDateFormat.format(xmlDateTime);
        StringBuilder sb = new StringBuilder(s);
        sb.insert(22, ':');
        return sb.toString();
    }


    public static String getWorksetWithAuthor(String workset, String author){
        return workset + '@' + author;
    }

    public static String convertTimeToGMT(String timeString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(timeString);
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        gmtFormatter.setTimeZone(gmtTimeZone);
        return gmtFormatter.format(date) + " " + gmtTimeZone.getID();
    }

}
