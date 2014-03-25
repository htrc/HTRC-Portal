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
import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import play.Logger;

import javax.xml.bind.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HTRCPersistenceAPIClient {
    private static Logger.ALogger log = play.Logger.of("application");

    private String accessToken;
    private String registryEPR;
    private String refreshToken;
    private Map<String, Object> session;
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

    public HTRCPersistenceAPIClient(String accessToken, String registryEPR) {
        this.accessToken = accessToken;
        this.registryEPR = registryEPR;

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
    public static String renewToken(String refreshToken)
            throws Exception {
        OAuthClientRequest refreshTokenRequest = OAuthClientRequest
                .tokenLocation(PlayConfWrapper.tokenEndpoint())
                .setGrantType(GrantType.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .setClientId(PlayConfWrapper.oauthClientID())
                .setClientSecret(PlayConfWrapper.oauthClientSecrete())
                .buildBodyMessage();
        OAuthClient refreshTokenClient = new OAuthClient(new URLConnectionClient());
        OAuthClientResponse refreshTokenResponse = refreshTokenClient
                .accessToken(refreshTokenRequest);
        String refreshAccessToken = refreshTokenResponse.getParam("access_token");
        log.info("Access token has been renewed to " + refreshAccessToken);

        return refreshAccessToken;
    }





    public List<Workset> getPublicWorksets() throws IOException,
            JAXBException {

        String worksetUrl = registryEPR + "/worksets" + "?public=" + true;
        log.debug("getPublicWorksets Url: " + worksetUrl);

        GetMethod get = new GetMethod(worksetUrl);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");

        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            String xmlStr = get.getResponseBodyAsString();
            Worksets worksets = (Worksets) parseXML(xmlStr);
            return worksets.getWorkset();
        } else {
            return null;
        }
    }


    public List<Volume> getWorksetVolumes(String worksetName, String worksetAuthor) throws IOException, JAXBException {
        String worksetUrl = registryEPR + "/worksets/" + worksetName + "?author=" + worksetAuthor;
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
            if (worksetContent != null) {
                return worksetContent.getVolumes().getVolume();
            }
            else {
                log.info("Workset content null.");
                return null;

            }
        }else{
            log.debug("Response Code: " + response);
            return null;
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
        if (nameReg != null)
            url += "?name=" + nameReg + "&";
        if (typeReg != null)
            url += "?type=" + typeReg + "&";
        if (url.lastIndexOf("&") == url.length() - 1)
            url += "public=" + String.valueOf(shared);
        else
            url += "?public=" + String.valueOf(shared);
        log.debug("getFilesAsString url: " + url);

        GetMethod get = new GetMethod(url);
        get.addRequestHeader("Authorization", "Bearer " + accessToken);
        int response = client.executeMethod(get);
        this.responseCode = response;
        if (response == 200) {
            renew = 0;
            return get.getResponseBodyAsString();
        } else if (response == 404) {
            return ""; // empty string
        } else if (response == 401 && (renew < MAX_RENEW)) {
            try {
                accessToken = renewToken(refreshToken);
                session.put(PortalConstants.SESSION_TOKEN, accessToken);
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




}
