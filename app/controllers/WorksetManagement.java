package controllers;


import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.illinois.i3.htrc.registry.entities.workset.Property;
import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.Workset;
import edu.indiana.d2i.htrc.portal.*;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetailsBean;
import models.Validation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.json.simple.JSONObject;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.data.Form;
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
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    public static Result validateWorkset() throws ParserConfigurationException,FileNotFoundException,IOException,Exception {
        String userId = session(PortalConstants.SESSION_USERNAME);
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart csv = body.getFile("csv");
        String[] worksetName = body.asFormUrlEncoded().get("uploadWorksetName");
        String[] description = body.asFormUrlEncoded().get("uploadWSdescription");
        boolean isPrivateWorkset = body.asFormUrlEncoded().containsKey("privateWorkset");
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        HTRCExperimentalAnalysisServiceClient serviceClient = new HTRCExperimentalAnalysisServiceClient();
        if (csv != null) {
            int totalVolumes=0;
            int copyRightVolumeCount =0;
            int missingVolumeCount =0;
            int titleIndex = -1;
            List<String[]> rows;
            List<Validation> rowsList = new ArrayList<Validation>();
            List<Validation> rowsNotInRepositoryList = new ArrayList<Validation>();
            List<Validation> rowsInRepositoryList = new ArrayList<Validation>();
            List<String[]> rowsNotInRepository=new ArrayList<String[]>();
            List<String[]> rowsInRepository=new ArrayList<String[]>();
            String[] headers;
            String wsName;
            String wsDescription;
            String csvFileName = csv.getFilename();
            String contentType = csv.getContentType();
            File csvFile = csv.getFile();
            File modifiedFile;
            File missingVolumesmodifiedFile;
            String volumeFilePath = PlayConfWrapper.htrcvolumesdata();
            log.info("CSV file name: " + csvFileName + " content type: " + contentType +
                    " is private: " + isPrivateWorkset + " workset name: " + worksetName[0]);
            try {
                if(worksetName == null || worksetName[0].length() <= 0){
                    wsName = FilenameUtils.removeExtension(csvFileName);
                } else {
                    wsName = worksetName[0];
                }
                if(description == null || description[0].length() <= 0){
                    wsDescription = null;
                } else {
                    wsDescription = description[0];
                }


                File volumeFile = new File(volumeFilePath);
                List<String> volumesList = Files.readAllLines(volumeFile.toPath(), StandardCharsets.UTF_8);

                BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
                String readCSVData = bufferedReader.readLine();
                String[] tabData = readCSVData.split("\t");
                String[] commaData = readCSVData.split(",");
                au.com.bytecode.opencsv.CSVReader csvReader = null;
                if (tabData.length >= commaData.length) {
                    csvReader = new au.com.bytecode.opencsv.CSVReader(new FileReader(csvFile), '\t');
                } else {
                    csvReader = new au.com.bytecode.opencsv.CSVReader(new FileReader(csvFile));
                }
                //au.com.bytecode.opencsv.CSVReader csvReader= new au.com.bytecode.opencsv.CSVReader(new FileReader(csv));
                rows = csvReader.readAll();
                if (rows.size() <= 0) {
                    log.warn("Empty Workset CSV.");
                    throw new Exception("Empty Workset CSV.");
                }
                headers = rows.get(0);
                if (headers[0].startsWith("volume") || headers[0].contains("volume") || headers[0].contains("id")) {
                    rows.remove(0);
                }  else {
                    headers = new String[headers.length];
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = "header-" + i;
                    }
                    log.warn("No headers for the workset");
                }

                for(int i =0; i<headers.length;i++)
                {
                    if(headers[i].contains("title") ||headers[i].startsWith("title"))
                    {
                        titleIndex = i;
                    }
                }
                List<String> volumeList = new ArrayList<String>();
                for (String[]data :rows)
                {
                    volumeList.add(data[0]);
                }
                JSONObject jsonVolumeList =new JSONObject();
                jsonVolumeList.put("volumeIdsList",volumeList);
                List<String> volumesInHtrc = serviceClient.getVolumesInHtrc(jsonVolumeList);
                totalVolumes =rows.size();
                for(String[]row : rows)
                {
                    String title_row;
                    String volume = row[0];
                    if(titleIndex >0)
                    { title_row = row[titleIndex];}
                    else
                    {title_row = null;}
                    Validation totalRows = new Validation(volume,title_row);
                    rowsList.add(totalRows);
                    if(volumesInHtrc.contains(volume))
                    {
                        copyRightVolumeCount +=1;
                        Validation copyrightRows = new Validation(volume,title_row);
                        rowsInRepositoryList.add(copyrightRows);
                        rowsInRepository.add(row);
                    }
                    else
                    {
                        Validation missingRows = new Validation(volume,title_row);
                        rowsNotInRepositoryList.add(missingRows);
                        missingVolumeCount +=1;
                        rowsNotInRepository.add(row);
                    }
                }
                modifiedFile=File.createTempFile(wsName + "-" +wsDescription, ".csv");
                CSVWriter csvmodified = new CSVWriter(new FileWriter(modifiedFile));
                for(String[] str:rowsInRepository)
                {
                    csvmodified.writeNext(str);
                }
                csvmodified.close();

                missingVolumesmodifiedFile = File.createTempFile(wsName +"-"+wsDescription+"-missing",".csv");
                CSVWriter csvMissingVolumes = new CSVWriter(new FileWriter(missingVolumesmodifiedFile));
                for(String [] str:rowsNotInRepository)
                {
                    csvMissingVolumes.writeNext(str);
                }
                csvMissingVolumes.close();
            }catch (Exception e) {
                log.error("Error while uploading CSV file.");
                throw new RuntimeException("Error while uploading CSV file.",e);
            }

            if(totalVolumes <5000) {
                log.debug("CSV File: " + csvFile.getPath() + " , Modified File: " + modifiedFile.getPath());
                return ok(worksetvalidate.render(userId, totalVolumes, copyRightVolumeCount, rows, Arrays.asList(headers), Form.form(UploadWorkset.class), wsName, wsDescription, isPrivateWorkset, csvFile.getPath(),
                        modifiedFile.getPath(), missingVolumeCount, rowsList, rowsInRepositoryList, rowsNotInRepositoryList));
            }
            else
            {
                return ok(worksetValidateLarge.render(userId, totalVolumes, copyRightVolumeCount, rows, Arrays.asList(headers), Form.form(UploadWorkset.class), wsName, wsDescription, isPrivateWorkset, csvFile.getPath(),
                        modifiedFile.getPath(), missingVolumeCount, rowsList, rowsInRepositoryList, rowsNotInRepositoryList,
                        missingVolumesmodifiedFile.getPath()));
            }
        }else {
            flash("error", "Missing file");
            return ok(warnings.render("Please upload a CSV file", null, null, userId));
        }
    }


    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result uploadWorkset() throws ParserConfigurationException {
        Form<UploadWorkset> uploadWorksetForm = Form.form(UploadWorkset.class).bindFromRequest();
        HTRCPersistenceAPIClient persistenceAPIClient = new HTRCPersistenceAPIClient(session());
        String userId = session(PortalConstants.SESSION_USERNAME);

        if (uploadWorksetForm.hasErrors()) {
            return badRequest(gotopage.render("Some error has happened.", null, null, userId));
        }
        String filePath = uploadWorksetForm.get().wsFilePath;

        if (filePath != null) {
            File worksetFile = new File(filePath);
            //File worksetFile = uploadWorksetForm.get().wsFile;
            log.debug("File : " + worksetFile);
            String worksetName = uploadWorksetForm.get().wsName;
            String worksetDescription = uploadWorksetForm.get().wsDescription;
            Boolean isPrivateWorkset = uploadWorksetForm.get().isPrivate;
            log.debug("Workset Name: " + worksetName + " is private: " + isPrivateWorkset);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
            Document worksetDoc = domBuilder.newDocument();

            try {

                Document volumesDoc = CSV2WorksetXMLConverter.convert(worksetFile);


                if (worksetName == null) {
                    worksetName = FilenameUtils.removeExtension(worksetFile.getName());
                }
                Element worksetEle = worksetDoc.createElement("workset");
                worksetEle.setAttribute("xmlns", "http://registry.htrc.i3.illinois.edu/entities/workset");
                worksetDoc.appendChild(worksetEle);

                Element metadataEle = worksetDoc.createElement("metadata");

                Element name = worksetDoc.createElement("name");
                name.setTextContent(worksetName);
                metadataEle.appendChild(name);

                Element descriptionEle = worksetDoc.createElement("description");
                descriptionEle.setTextContent(worksetDescription);
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
                throw new RuntimeException("Error on converting to xml file.", e);
            }
            try {
                log.debug("Workset content: " + domToString(worksetDoc));
                persistenceAPIClient.createWorkset(domToString(worksetDoc), !isPrivateWorkset);
            } catch (Exception e) {
                log.error("Error when uploading workset in to registry");
                throw new RuntimeException("Error when uploading workset in to registry");
            }
            log.info("Workset :" + worksetName + " uploaded successfully. ");
            return redirect(routes.WorksetManagement.viewWorkset(worksetName, userId));
        } else {
            flash("error", "Missing file");
            return ok(warnings.render("Please upload a CSV file", null, null, userId));
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

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result downloadValidateWorkset(String filePath,String worksetName) throws IOException, JAXBException {

        File file = new File(filePath);
        response().setContentType("text/csv");
        response().setHeader("Content-Disposition", "attachment;filename=" + worksetName+".csv");
        response().setHeader("Cache-control", "private");

        return ok(file);
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

    public static class UploadWorkset
    {
        public List<String[]> rows;
        public List headers;
        public String wsName;
        public String wsDescription;
        public boolean isPrivate;
        public String wsFilePath;
        //public File wsFile;

    }

}
