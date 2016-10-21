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

package edu.indiana.d2i.htrc.portal;

import edu.illinois.i3.htrc.registry.entities.workset.Volume;
import edu.illinois.i3.htrc.registry.entities.workset.Workset;
import edu.illinois.i3.htrc.registry.entities.workset.WorksetContent;
import edu.illinois.i3.htrc.registry.entities.workset.Worksets;
import edu.indiana.d2i.htrc.portal.bean.AlgorithmDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.VolumeDetailsBean;
import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import play.Logger;
import play.mvc.Http;


import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

public class HTRCPersistenceAPIClient {
    private static Logger.ALogger log = play.Logger.of("application");

    private String accessToken;
    private String refreshToken;
    private static Http.Session session;
    private HttpClient client = new HttpClient();

    public int responseCode;

    /**
     * xml parsing for algorithm info
     */
    private XMLInputFactory factory = XMLInputFactory.newInstance();


    /**
     * access token renew time
     */
    private int renew = 0;
    private final int MAX_RENEW = 1;

    public HTRCPersistenceAPIClient(Http.Session session) {
        HTRCPersistenceAPIClient.session = session;
        accessToken = session.get(PortalConstants.SESSION_TOKEN);
        refreshToken = session.get(PortalConstants.SESSION_REFRESH_TOKEN);
    }


    private static class RSLValidationEventHandler implements
            ValidationEventHandler {
        public boolean handleEvent(ValidationEvent ve) {
            if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
                    || ve.getSeverity() == ValidationEvent.ERROR)
                return false;
            return true;
        }
    }

    private Object parseXML(String xmlStr) throws JAXBException {
//        if (log.isDebugEnabled()) {
//            Logger.debug("Workset Response: \n" + xmlStr);
//        }

        JAXBContext jaxbContext = JAXBContext
                .newInstance("edu.illinois.i3.htrc.registry.entities.workset");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new RSLValidationEventHandler());
        JAXBElement<Object> worksets = (JAXBElement<Object>) unmarshaller
                .unmarshal(new ByteArrayInputStream(xmlStr.getBytes()));
        return worksets.getValue();
    }


    /**
     * This method renew the access token given a refresh token.
     *
     * @param refreshToken
     * @return access token
     * @throws IOException
     * @throws org.apache.amber.oauth2.common.exception.OAuthSystemException
     * @throws org.apache.amber.oauth2.common.exception.OAuthProblemException
     * @throws Exception
     */
    public static String renewToken(String refreshToken)throws Exception {
        String oauthClientId = PlayConfWrapper.getOauthClientID();
        String oauthClientSecret = PlayConfWrapper.getOauthClientSecrete();
        String tokenLocation = PlayConfWrapper.tokenEndpoint();

        if(oauthClientId != null && oauthClientSecret != null && tokenLocation != null && refreshToken != null){
            OAuthClientRequest refreshTokenRequest = OAuthClientRequest
                    .tokenLocation(tokenLocation)
                    .setGrantType(GrantType.REFRESH_TOKEN)
                    .setRefreshToken(refreshToken)
                    .setClientId(oauthClientId)
                    .setClientSecret(oauthClientSecret)
                    .buildBodyMessage();
            OAuthClient refreshTokenClient = new OAuthClient(new URLConnectionClient());
            OAuthClientResponse refreshTokenResponse = refreshTokenClient
                    .accessToken(refreshTokenRequest);
            String refreshedAccessToken = refreshTokenResponse.getParam("access_token");
            session.put(PortalConstants.SESSION_TOKEN, refreshedAccessToken);
            session.put(PortalConstants.SESSION_REFRESH_TOKEN, refreshTokenResponse.getParam("refresh_token"));
            log.info("Access token has been renewed to " + refreshedAccessToken);

            return refreshedAccessToken;
        }else{
            log.error("One or more variables are null.");
            return null;
        }

    }


    public List<Workset> getAllWorksets() throws IOException,
            JAXBException {

        String worksetUrl = PlayConfWrapper.registryEPR() + "/worksets" + "?public=" + true;
        log.debug("getAllWorksets Url: " + worksetUrl);

        GetMethod get = new GetMethod(worksetUrl);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");

        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            String xmlStr = get.getResponseBodyAsString();
            Worksets worksets = (Worksets) parseXML(xmlStr);
            renew = 0;
            return worksets.getWorkset();
        } else if (response == 401 && (renew < MAX_RENEW)) {
            try {
                accessToken = renewToken(refreshToken);
                renew++;
                return getAllWorksets();
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (response == 404) {
            log.info("No workset is found for request " + worksetUrl);
            return Collections.emptyList();
        } else {
            log.error("Response code is " + response + "\n" + get.getResponseBodyAsString());
            throw new IOException("Response code is " + response + " for request "
                    + worksetUrl);
        }
    }

    public List<Workset> getUserWorksets() throws IOException,
            JAXBException {

        String worksetUrl = PlayConfWrapper.registryEPR() + "/worksets";
        log.debug("getUserWorksets Url: " + worksetUrl);

        GetMethod get = new GetMethod(worksetUrl);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");

        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            String xmlStr = get.getResponseBodyAsString();
            Worksets worksets = (Worksets) parseXML(xmlStr);
            renew = 0;
            return worksets.getWorkset();
        } else if (response == 401 && (renew < MAX_RENEW)) {
            try {
                accessToken = renewToken(refreshToken);
                renew++;
                return getUserWorksets();
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (response == 404) {
            log.info("No workset is found for request " + worksetUrl);
            return Collections.emptyList();
        } else {
            log.error("Response code is " + response + "\n" + get.getResponseBodyAsString());
            throw new IOException("Response code is " + response + " for request "
                    + worksetUrl);
        }
    }
/** Get all the other allWorksets other than user's allWorksets
 *
 */

    public List<Workset> getPublicWorksets() throws IOException, JAXBException {
        List<Workset> allWorksets = getAllWorksets();
        allWorksets.removeAll(getUserWorksets());
        return allWorksets;
    }

    public Workset getWorkset(String worksetId, String worksetAuthor) throws IOException, JAXBException {
        String worksetUrl = PlayConfWrapper.registryEPR() + "/worksets/" + worksetId + "?author=" + worksetAuthor;
        log.debug("getWorkset Url: " + worksetUrl);

        GetMethod get = new GetMethod(worksetUrl);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");

        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            String xmlStr = get.getResponseBodyAsString();
            Workset workset = (Workset) parseXML(xmlStr);
            renew = 0;
            return workset;
        } else if (response == 401 && (renew < MAX_RENEW)) {
            try {
                accessToken = renewToken(refreshToken);
                renew++;
                return getWorkset(worksetId, worksetAuthor);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (response == 404) {
            log.info("No workset is found for request " + worksetUrl);
            return null;
        } else {
            log.error("Response code is " + response + "\n" + get.getResponseBodyAsString());
            throw new IOException("Response code is " + response + " for request "
                    + worksetUrl);
        }
    }


    public List<Volume> getWorksetVolumes(String worksetName, String worksetAuthor) throws IOException, JAXBException {
        String worksetUrl = PlayConfWrapper.registryEPR() + "/worksets/" + worksetName + "?author=" + worksetAuthor;
        log.debug("getWorkset Url: " + worksetUrl);

        GetMethod get = new GetMethod(worksetUrl);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");

        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            String xmlStr = get.getResponseBodyAsString();
            Workset workset = (Workset) parseXML(xmlStr);
            WorksetContent worksetContent = workset.getContent();
            renew = 0;
            if (worksetContent != null) {
                return worksetContent.getVolumes().getVolume();
            } else {
                log.info("Workset content null.");
                return null;

            }
        } else if (response == 401 && (renew < MAX_RENEW)) {
            try {
                accessToken = renewToken(refreshToken);
                renew++;
                return getWorksetVolumes(worksetName, worksetAuthor);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (response == 404) {
            log.info("No workset is found for request " + worksetUrl);
            return null;
        } else {
            log.error("Response code is " + response + "\n" + get.getResponseBodyAsString());
            throw new IOException("Response code is " + response + " for request "
                    + worksetUrl);
        }
    }

    /**
     * Return all algorithm details.
     *
     * @return a hash map with algorithm id as key and algorithm detail as value
     * @throws IOException
     * @throws IllegalStateException
     * @throws JAXBException
     * @throws javax.xml.stream.XMLStreamException
     */
    public Map<String, AlgorithmDetailsBean> getAllAlgorithmDetails()
            throws IOException, IllegalStateException,
            JAXBException, XMLStreamException {
        Map<String, AlgorithmDetailsBean> res = new TreeMap<String, AlgorithmDetailsBean>();

        String algoFolder = PlayConfWrapper.registryAlgFolder();
        log.debug("setting repo path to algofolder");
        String str = getFilesAsString(algoFolder, ".*.xml", null, true);


        while (true) {
            int start = str.indexOf("<algorithm>");
            int end = str.indexOf("</algorithm>");
            if (start == -1 || end == -1)
                break;
            String sub = str.substring(start, end + "</algorithm>".length());
            AlgorithmDetailsBean algoDetail = parseAlgorithmDetailBean(
                    new ByteArrayInputStream(sub.getBytes("UTF-8")));
            res.put(algoDetail.getName(), algoDetail);
            str = str.substring(end + "</algorithm>".length());
        }
        return res;
    }

    public String getFilesAsString(String repoPath, String nameReg,
                                   String typeReg, boolean shared)
            throws HttpException, IOException {
        String url = PlayConfWrapper.registryEPR() + "/files" + repoPath;

        if (nameReg != null) {
            url += "?name=" + nameReg + "&";
        }

        if (typeReg != null) {
            url += "?type=" + typeReg + "&";
        }

        if (url.lastIndexOf("&") == url.length() - 1) {
            url += "public=" + String.valueOf(shared);
        } else {
            url += "?public=" + String.valueOf(shared);
        }

        Logger.info("getFilesAsString url: " + url);

        GetMethod get = new GetMethod(url);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            renew = 0;
            return get.getResponseBodyAsString();
        } else if (response == 404) {
            play.Logger.error("getFilesAsString failed: \n" + get.getResponseBodyAsString());
            return ""; // empty string
        } else if (response == 401 && (renew < MAX_RENEW)) {
            play.Logger.info("getFilesAsString got access token expired error.");
            try {
                accessToken = renewToken(refreshToken);
                renew++;
                return getFilesAsString(repoPath, nameReg, typeReg, shared);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + url);
        }
    }

    private AlgorithmDetailsBean parseAlgorithmDetailBean(InputStream stream) throws XMLStreamException {
        AlgorithmDetailsBean res = new AlgorithmDetailsBean();
        XMLStreamReader parser = factory.createXMLStreamReader(stream);

        List<AlgorithmDetailsBean.Parameter> parameters =
                new ArrayList<AlgorithmDetailsBean.Parameter>();
        List<String> authors = new ArrayList<String>();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.hasName()) {
                    // only parse the info tag!
                    if (parser.getLocalName().equals(AlgorithmDetailsBean.NAME)) {
                        res.setName(parser.getElementText());
                    } else if (parser.getLocalName().equals(AlgorithmDetailsBean.VERSION)) {
                        res.setVersion(parser.getElementText());
                    } else if (parser.getLocalName().equals(AlgorithmDetailsBean.DESCRIPTION)) {
                        res.setDescription(parser.getElementText());
                    } else if (parser.getLocalName().equals(AlgorithmDetailsBean.SUPPORTURL)) {
                        res.setSupportUrl(parser.getElementText());
                    } else if (parser.getLocalName().equals(AlgorithmDetailsBean.PARAMETER)) {
                        AlgorithmDetailsBean.Parameter parameter = new AlgorithmDetailsBean.Parameter();
                        int count = parser.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            if (parser.getAttributeLocalName(i).equals("required"))
                                parameter.setRequired(Boolean.valueOf(parser.getAttributeValue(i)));
                            if (parser.getAttributeLocalName(i).equals("type"))
                                parameter.setType(parser.getAttributeValue(i));
                            if (parser.getAttributeLocalName(i).equals("name"))
                                parameter.setName(parser.getAttributeValue(i));
                            if (parser.getAttributeLocalName(i).equals("defaultValue"))
                                parameter.setDefaultValue(parser.getAttributeValue(i));
                            if (parser.getAttributeLocalName(i).equals("validation"))
                                parameter.setValidation(parser.getAttributeValue(i));
                            if (parser.getAttributeLocalName(i).equals("validationError"))
                                parameter.setValidationError(parser.getAttributeValue(i));
                            if (parser.getAttributeLocalName(i).equals("readOnly"))
                                parameter.setReadOnly(Boolean.parseBoolean(parser.getAttributeValue(i)));
                        }
                        parser.nextTag();
                        if (parser.getLocalName().equals("label"))
                            parameter.setLabel(parser.getElementText());
                        parser.nextTag();
                        if (parser.getLocalName().equals("description"))
                            parameter.setDescription(parser.getElementText());
                        parameters.add(parameter);
                    } else if (parser.getLocalName().equals(AlgorithmDetailsBean.AUTHOR)) {
                        int count = parser.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            if (parser.getAttributeLocalName(i).equals("name"))
                                authors.add(parser.getAttributeValue(i));
                        }
                    }
                }
            }
        }
        res.setParameters(parameters);
        res.setAuthors(authors);
        return res;
    }

    /**
     * Create workset
     *
     * @param worksetContent , Give workset content as a String
     * @param isPublic
     */

    public void createWorkset(String worksetContent, boolean isPublic) throws IOException {
        String url;

        if (isPublic) {
            url = PlayConfWrapper.registryEPR() + "/worksets" + "?public=true";
        } else {
            url = PlayConfWrapper.registryEPR() + "/worksets";
        }
        PostMethod post = new PostMethod(url);
        post.addRequestHeader("Authorization", "Bearer " + accessToken);
        post.setRequestEntity(new StringRequestEntity(worksetContent,
                "application/vnd.htrc-workset+xml", "UTF-8"));

        int response = client.executeMethod(post);
        if (response == 201) {
            // nothing
        } else {
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + url + " message: \n " + post.getResponseBodyAsString());
        }
    }

    /**
     * Get Volum details from solr meta data instance. Here it adds '\' character before the ':'.
     * @param volId
     * @return VolumeDetailsBean
     * @throws IOException
     */
    public VolumeDetailsBean getVolumeDetails(String volId) throws IOException {
        String volumeId;
        if(volId.contains(":")){
            volumeId = volId.substring(0,volId.indexOf(":")) + "\\" + volId.substring(volId.indexOf(":"));
        }else{
            volumeId = volId;
        }
        String volumeDetailsQueryUrl = PlayConfWrapper.solrMetaQueryUrl() + "id:" + URLEncoder.encode(volumeId, "UTF-8") + "&fl=title,author,htrc_genderMale,htrc_genderFemale,htrc_genderUnknown,htrc_pageCount,htrc_wordCount";
        VolumeDetailsBean volDetails = new VolumeDetailsBean();

        if(log.isDebugEnabled()) {
            log.debug(volumeDetailsQueryUrl);
        }

        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(volumeDetailsQueryUrl);
        method.setFollowRedirects(true);

        try {
            httpClient.executeMethod(method);
            volDetails.setVolumeId(volId);

            if (method.getStatusCode() == 200 && !method.getResponseBodyAsString().contains("<warn>RESPONSE CODE: 400</warn>")) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

                Document dom = documentBuilder.parse(method.getResponseBodyAsStream());

                NodeList result = dom.getElementsByTagName("result");
                NodeList arrays = ((org.w3c.dom.Element) result.item(0)).getElementsByTagName("arr");
                NodeList integers = ((org.w3c.dom.Element) result.item(0)).getElementsByTagName("int");
                NodeList longIntegers = ((org.w3c.dom.Element) result.item(0)).getElementsByTagName("long");


                for (int i = 0; i < arrays.getLength(); i++) {
                    org.w3c.dom.Element arr = (org.w3c.dom.Element) arrays.item(i);

                    if (arr.hasAttribute("name") && arr.getAttribute("name").equals("title")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        volDetails.setTitle((strElements.item(0)).getTextContent());
                    } else if (arr.hasAttribute("name") && arr.getAttribute("name").equals("htrc_genderMale")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        String maleAuthor = "";

                        for (int j = 0; j < strElements.getLength(); j++) {
                            org.w3c.dom.Element str = (org.w3c.dom.Element) strElements.item(j);
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
                            org.w3c.dom.Element str = (org.w3c.dom.Element) strElements.item(j);
                            if (j != strElements.getLength() - 1) {
                                femaleAuthor += str.getTextContent();
                            } else {
                                femaleAuthor += str.getTextContent();
                            }
                        }

                        volDetails.setFemaleAuthor(femaleAuthor);


                    } else if (arr.hasAttribute("name") && arr.getAttribute("name").equals("htrc_genderUnknown")) {
                        NodeList strElements = arr.getElementsByTagName("str");
                        String genderUnknownAuthor = "";

                        for (int j = 0; j < strElements.getLength(); j++) {
                            org.w3c.dom.Element str = (org.w3c.dom.Element) strElements.item(j);
                            if (j != strElements.getLength() - 1) {
                                genderUnknownAuthor += str.getTextContent();
                            } else {
                                genderUnknownAuthor += str.getTextContent();
                            }
                        }

                        volDetails.setGenderUnkownAuthor(genderUnknownAuthor);


                    }
                }

                for (int i = 0; i < integers.getLength(); i++) {
                    org.w3c.dom.Element integer = (org.w3c.dom.Element) integers.item(i);
                    if (integer.hasAttribute("name") && integer.getAttribute("name").equals("htrc_pageCount")) {
                        String pageCount = integer.getTextContent();
                        volDetails.setPageCount(pageCount);
                    }
                }
                for (int i = 0; i < longIntegers.getLength(); i++) {
                    org.w3c.dom.Element longInteger = (org.w3c.dom.Element) longIntegers.item(0);
                    if (longInteger.hasAttribute("name") && longInteger.getAttribute("name").equals("htrc_wordCount")) {
                        String wordCount = longInteger.getTextContent();
                        volDetails.setWordCount(wordCount);
                    }
                }

            } else {
                volDetails.setTitle("Cannot retrieve volume details.");
                log.warn("Cannot retrieve details for volume id: " + volId + " Response body: \n" + method.getResponseBodyAsString());
            }

        } catch (SAXParseException e) {
            log.error("Error while parsing volume details for volume: " + volId + " query url: " + volumeDetailsQueryUrl + " status code: " + method.getStatusCode(), e);
            volDetails.setTitle("Cannot parse volume details.");
        } catch (ParserConfigurationException e) {
            log.error("Unrecoverable error while parsing volume details.", e);
            log.error("Error while parsing volume details for volume: " + volId + " query url: " + volumeDetailsQueryUrl + " status code: " + method.getStatusCode(), e);
            throw new RuntimeException("Unrecoverable error while parsing volume details.", e);
        } catch (SAXException e) {
            log.error("Error while parsing volume details for volume: " + volId + " query url: " + volumeDetailsQueryUrl + " status code: " + method.getStatusCode(), e);
            volDetails.setTitle("Cannot parse volume details.");
        }

        return volDetails;
    }

}
