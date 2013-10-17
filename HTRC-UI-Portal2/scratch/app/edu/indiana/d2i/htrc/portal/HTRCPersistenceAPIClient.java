///**
// * Copyright 2013 The Trustees of Indiana University
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package edu.indiana.d2i.htrc.portal;
//
//import models.Algorithm;
//import models.Volume;
//import models.Workset;
//import org.apache.amber.oauth2.client.OAuthClient;
//import org.apache.amber.oauth2.client.URLConnectionClient;
//import org.apache.amber.oauth2.client.request.OAuthClientRequest;
//import org.apache.amber.oauth2.client.response.OAuthClientResponse;
//import org.apache.amber.oauth2.common.message.types.GrantType;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.methods.*;
//import play.Logger;
//import play.Play;
//
//import javax.xml.bind.*;
//import javax.xml.stream.XMLStreamException;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
//
//public class HTRCPersistenceAPIClient {
//    private static Logger.ALogger log = play.Logger.of("application");
//
//    private String accessToken;
//    private String refreshToken;
//    private Map<String, Object> session;
//    private HttpClient client = new HttpClient();
//
//    public int responseCode;
//
//    /**
//     * different service urls
//     */
//    private final String WORKSETS_URL = "/worksets";
//    private final String FILES_URL = "/files";
//    private final String PUBLIC_WORKSET = "?public=true";
//    private final String VOLUME_DETAILS_QUERY_SOLR_URL = "?q=id:";
//
//
//    private static class RSLValidationEventHandler implements
//            ValidationEventHandler {
//        public boolean handleEvent(ValidationEvent ve) {
//            if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
//                    || ve.getSeverity() == ValidationEvent.ERROR)
//                return false;
//            return true;
//        }
//    }
//
//    private Object parseXML(String xmlStr) throws JAXBException {
//        JAXBContext jaxbContext = JAXBContext
//                .newInstance("edu.illinois.i3.htrc.registry.entities.workset");
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        unmarshaller.setEventHandler(new RSLValidationEventHandler());
//        JAXBElement<Object> worksets = (JAXBElement<Object>) unmarshaller
//                .unmarshal(new ByteArrayInputStream(xmlStr.getBytes()));
//        return worksets.getValue();
//    }
//
//    private Algorithm parseAlgorithmDetailBean(InputStream stream){
//        Algorithm algorithm = new Algorithm();
//        return algorithm;
//    }
//
//    private Workset createWorksetBean(Workset workset, int page){
//        Workset workset1 = new Workset();
//        return workset1;
//    }
//
//    private Volume getVolumeDetails(String volid){
//        Volume volume = new Volume();
//        return volume;
//    }
//
//    public static String renewToken(String refreshToken)
//            throws Exception {
//        OAuthClientRequest refreshTokenRequest = OAuthClientRequest
//                .tokenLocation(Play.application().configuration().getString("oauth2.token.endpoint"))
//                .setGrantType(GrantType.PASSWORD)
//                .setClientId(Play.application().configuration().getString("oauth2.client.id"))
//                .setClientSecret(Play.application().configuration().getString("oauth2.client.secrete"))
//                .buildBodyMessage();
//        OAuthClient refreshTokenClient = new OAuthClient(new URLConnectionClient());
//        OAuthClientResponse refreshTokenResponse = refreshTokenClient
//                .accessToken(refreshTokenRequest);
//        String refreshAccessToken = refreshTokenResponse.getParam("access_token");
//        log.info("Access token has been renewed to " + refreshAccessToken);
//
//        return refreshAccessToken;
//    }
//
//    public HTRCPersistenceAPIClient (String accessToken, String refreshToken,
//                                     Map<String, Object> session) {
//        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
//        this.session = session;
//    }
//
//    /**
//     * Return all workset details.
//     *
//     * @return a hash map with unique id as key and collection detail as value,
//     *         format of unique id is "workset_name@workset_author"
//     * @throws java.io.IOException
//     * @throws JAXBException
//     */
//    public Map<String, Workset> getAllWorksets() throws IOException,
//            JAXBException {
//        String url = PortalConfiguration.getRegistryEPR() + WORKSETS_URL +
//                PUBLIC_WORKSET;
//        logger.debug("getAllWorksets url: " + url);
//
//        GetMethod get = new GetMethod(url);
//        get.addRequestHeader("Authorization", "Bearer " + accessToken);
//        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");
//
//        int response = client.executeMethod(get);
//        this.responseCode = response;
//        if (response == 200) {
//            String userName = (String) session.get(HTRCPortalConstants.SESSION_USERNAME);
//
//            String xmlStr = get.getResponseBodyAsString();
//            // System.out.println(xmlStr);
//            Worksets worksets = (Worksets) parseXML(xmlStr);
//            Map<String, WorksetDetailBean> userWorksets = new TreeMap<String, WorksetDetailBean>();
//            Map<String, WorksetDetailBean> otherWorksets = new TreeMap<String, WorksetDetailBean>();
//            for (Workset workset : worksets.getWorkset()) {
//                //WorksetMeta metadata = workset.getMetadata();
//                WorksetDetailBean bean = createWorksetBean(workset, 0);
//                if (bean.getAuthor().equalsIgnoreCase(userName)) {
//                    userWorksets.put(bean.getUniqueIdentity(), bean);
//                } else {
//                    otherWorksets.put(bean.getUniqueIdentity(), bean);
//                }
//            }
//
//            Map<String, WorksetDetailBean> res = new LinkedHashMap<String, WorksetDetailBean>();
//            for (Map.Entry<String, WorksetDetailBean> entry : userWorksets.entrySet()) {
//                res.put(entry.getKey(), entry.getValue());
//            }
//            for (Map.Entry<String, WorksetDetailBean> entry : otherWorksets.entrySet()) {
//                res.put(entry.getKey(), entry.getValue());
//            }
//            renew = 0;
//            return res;
//        } else if (response == 401 && (renew < MAX_RENEW)) {
//            try {
//                accessToken = renewToken(refreshToken);
//                session.put(HTRCPortalConstants.SESSION_TOKEN, accessToken);
//                renew++;
//                return getAllWorksets();
//            } catch (Exception e) {
//                throw new IOException(e);
//            }
//        } else if (response == 404) {
//            logger.info("No workset is found for request " + url);
//            return new LinkedHashMap<String, WorksetDetailBean>();
//        } else {
//            logger.error("500 error \n" + get.getResponseBodyAsString());
//            throw new IOException("Response code is " + response + " for request "
//                    + url);
//        }
//    }
//
//    /**
//     * Return the detail info of workset based on workset name.
//     *
//     * @param worksetName
//     * @param page
//     * @return Workset detail wrapped in a bean
//     * @throws IOException
//     * @throws JAXBException
//     */
//    public WorksetDetailBean getOneWorkset(String worksetName,
//                                           String worksetAuthor, int page) throws IOException, JAXBException {
//        String url = PortalConfiguration.getRegistryEPR() + WORKSETS_URL + "/"
//                + worksetName + "?author=" + worksetAuthor;
//        logger.debug("getOneWorkset url: " + url);
//
//        GetMethod get = new GetMethod(url);
//        get.addRequestHeader("Authorization", "Bearer " + accessToken);
//        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");
//
//        int response = client.executeMethod(get);
//        this.responseCode = response;
//        if (response == 200) {
//            String xmlStr = get.getResponseBodyAsString();
//            Workset workset = (Workset) parseXML(xmlStr);
//            WorksetDetailBean bean = createWorksetBean(workset, page);
//            renew = 0;
//            return bean;
//        } else if (response == 401 && (renew < MAX_RENEW)) {
//            try {
//                accessToken = renewToken(refreshToken);
//                session.put(HTRCPortalConstants.SESSION_TOKEN, accessToken);
//                renew++;
//                return getOneWorkset(worksetName, worksetAuthor, page);
//            } catch (Exception e) {
//                throw new IOException(e);
//            }
//        } else if (response == 404) { // not found, return an empty one
//            renew = 0;
//            return new WorksetDetailBean();
//        } else {
//            this.responseCode = response;
//            throw new IOException("Response code is " + response + " for request "
//                    + url);
//        }
//    }
//
//    public List<VolumeDetailsBean> getVolumesOfWorkset(String worksetName, String author) throws IOException, JAXBException {
//        String url = PortalConfiguration.getRegistryEPR() + WORKSETS_URL + "/"
//                + worksetName + "?author=" + author;
//        logger.debug("getOneWorkset url: " + url);
//        //System.out.println(worksetName);
//        //System.out.println(author);
//        // TODO: Logging
//        GetMethod get = new GetMethod(url);
//        get.addRequestHeader("Authorization", "Bearer " + accessToken);
//        get.addRequestHeader("Accept", "application/vnd.htrc-workset+xml");
//
//        int response = client.executeMethod(get);
//        this.responseCode = response;
//        if (response == 200) {
//            String xmlStr = get.getResponseBodyAsString();
//            // System.out.println(xmlStr);
//            Workset workset = (Workset) parseXML(xmlStr);
//            WorksetContent worksetContent = workset.getContent();
//            List<VolumeDetailsBean> volumeDetailsBeanList = new ArrayList<VolumeDetailsBean>();
//
//            if (worksetContent != null) {
//                List<Volume> volumes = worksetContent.getVolumes().getVolume();
//
//                for (Volume vol : volumes) {
//                    VolumeDetailsBean volumeBean = getVolumeDetails(vol.getId());
//                    if (vol.getProperties() != null &&
//                            vol.getProperties().getProperty() != null &&
//                            vol.getProperties().getProperty().size() > 0) {
//                        for (Property prop : vol.getProperties().getProperty()) {
//                            volumeBean.addProperty(new VolumeProperty(prop.getName(), prop.getValue()));
//                        }
//                    }
//                    volumeDetailsBeanList.add(volumeBean);
//                }
//            } else {
//                logger.info("Workset content null.");
//            }
//            renew = 0;
//            return volumeDetailsBeanList;
//
//        } else if (response == 401 && (renew < MAX_RENEW)) {
//            try {
//                accessToken = renewToken(refreshToken);
//                session.put(HTRCPortalConstants.SESSION_TOKEN, accessToken);
//                renew++;
//                return getVolumesOfWorkset(worksetName, author);
//            } catch (Exception e) {
//                throw new IOException(e);
//            }
//        } else if (response == 404) { // not found, return an empty one
//            renew = 0;
//            return new ArrayList<VolumeDetailsBean>();
//
//        } else {
//            this.responseCode = response;
//            throw new IOException("Response code is " + response + " for request "
//                    + url);
//        }
//    }
//
//
//    /**
//     * Return all algorithm details.
//     *
//     * @return a hash map with algorithm id as key and algorithm detail as value
//     * @throws IOException
//     * @throws IllegalStateException
//     * @throws JAXBException
//     * @throws javax.xml.stream.XMLStreamException
//     */
//    public Map<String, AlgorithmDetailBean> getAllAlgorithmDetailBeans()
//            throws IOException, IllegalStateException,
//            JAXBException, XMLStreamException {
////		Map<String, AlgorithmDetailBean> res = new HashMap<String, AlgorithmDetailBean>();
//        Map<String, AlgorithmDetailBean> res = new TreeMap<String, AlgorithmDetailBean>();
//
//        String algoFolder = PortalConfiguration.getRegistryAlgoFolder();
//        String str = getFilesAsString(algoFolder, ".*.xml", null, true);
//
//        while (true) {
//            int start = str.indexOf("<algorithm>");
//            int end = str.indexOf("</algorithm>");
//            if (start == -1 || end == -1)
//                break;
//            String sub = str.substring(start, end + "</algorithm>".length());
//            AlgorithmDetailBean algoDetail = parseAlgorithmDetailBean(
//                    new ByteArrayInputStream(sub.getBytes("UTF-8")));
//            res.put(algoDetail.getName(), algoDetail);
//            str = str.substring(end + "</algorithm>".length());
//        }
//        return res;
//    }
//
//    public String getFilesAsString(String repoPath, String nameReg,
//                                   String typeReg, boolean shared)
//            throws HttpException, IOException {
//        String url = PortalConfiguration.getRegistryEPR() + FILES_URL + repoPath;
//        if (nameReg != null)
//            url += "?name=" + nameReg + "&";
//        if (typeReg != null)
//            url += "?type=" + typeReg + "&";
//        if (url.lastIndexOf("&") == url.length() - 1)
//            url += "public=" + String.valueOf(shared);
//        else
//            url += "?public=" + String.valueOf(shared);
//        logger.debug("getFilesAsString url: " + url);
//
//        GetMethod get = new GetMethod(url);
//        get.addRequestHeader("Authorization", "Bearer " + accessToken);
//        int response = client.executeMethod(get);
//        this.responseCode = response;
//        if (response == 200) {
//            renew = 0;
//            return get.getResponseBodyAsString();
//        } else if (response == 404) {
//            return ""; // empty string
//        } else if (response == 401 && (renew < MAX_RENEW)) {
//            try {
//                accessToken = renewToken(refreshToken);
//                session.put(HTRCPortalConstants.SESSION_TOKEN, accessToken);
//                renew++;
//                return getFilesAsString(repoPath, nameReg, typeReg, shared);
//            } catch (Exception e) {
//                throw new IOException(e);
//            }
//        } else {
//            this.responseCode = response;
//            throw new IOException("Response code " + response + " for " + url);
//        }
//    }
//
//    /**
//     * unit test purposes
//     */
//    public void postFile(String repoPath, InputStream in, String mediaType,
//                         boolean shared) throws HttpException, IOException {
//        String url = PortalConfiguration.getRegistryEPR() + FILES_URL
//                + repoPath + "?public=" + String.valueOf(shared);
//        logger.debug("postFile url: " + url);
//
//        PutMethod put = new PutMethod(url);
//        put.addRequestHeader("Content-Type", mediaType);
//        put.addRequestHeader("Authorization", "Bearer " + accessToken);
//        put.setRequestEntity(new InputStreamRequestEntity(in));
//
//        int response = client.executeMethod(put);
//        if (response == 204) {
//            System.out.println("success!!");
//        } else {
//            this.responseCode = response;
//            throw new IOException("Response code " + response + " for " + url);
//        }
//    }
//
//    /**
//     * unit test purposes
//     */
//    public void createWorkset(String worksetContent, boolean isPublic) throws IOException {
//        String url;
//
//        if(isPublic) {
//            url = PortalConfiguration.getRegistryEPR() + WORKSETS_URL + "?public=true";
//        }else {
//            url = PortalConfiguration.getRegistryEPR() + WORKSETS_URL;
//        }
//        PostMethod post = new PostMethod(url);
//        post.addRequestHeader("Authorization", "Bearer " + accessToken);
//        post.setRequestEntity(new StringRequestEntity(worksetContent,
//                "application/vnd.htrc-workset+xml", "UTF-8"));
//
//        int response = client.executeMethod(post);
//        if (response == 201) {
//            // nothing
//        } else {
//            this.responseCode = response;
//            throw new IOException("Response code " + response + " for " + url + " message: \n " + post.getResponseBodyAsString());
//        }
//    }
//
//}
