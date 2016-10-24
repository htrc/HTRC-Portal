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

import edu.indiana.d2i.htrc.sloan.bean.VMImageDetails;
import edu.indiana.d2i.htrc.sloan.bean.VMStatus;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import play.Logger;
import play.mvc.Http;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTRCExperimentalAnalysisServiceClient {
    private static Logger.ALogger log = play.Logger.of("application");

    private HttpClient client = new HttpClient();

    /**
     * access token renew time
     */
    private int renew = 0;
    private final int MAX_RENEW = 1;

    public int responseCode;

    public String createVM(String imageName, String loginUerName, String loginPassword, String memory, String vcpu, Http.Session session) throws IOException {
        String createVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.createVMUrl();

        PostMethod post = new PostMethod(createVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("imagename", imageName);
        post.setParameter("loginusername", loginUerName);
        post.setParameter("loginpassword", loginPassword);
        post.setParameter("memory", memory);
        post.setParameter("vcpu", vcpu);
//        post.setRequestBody((org.apache.commons.httpclient.NameValuePair[]) urlParameters);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = post.getResponseBodyAsString();
            log.info(Arrays.toString(post.getRequestHeaders()));
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                return ((String) jsonObject.get("vmid"));

            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            this.responseCode = response;
            String jsonStr = post.getResponseBodyAsString();
            JSONParser parser = new JSONParser();
            log.error(post.getResponseBodyAsString());
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                String description = ((String) jsonObject.get("description"));
                throw new IOException("Staus code is  " + response + "\n  Error message: " + description);

            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        return null;

    }

    public List<VMImageDetails> listVMImages(Http.Session session) throws IOException, GeneralSecurityException {
        String listVMImageUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.listVMImagesUrl();
        List<VMImageDetails> vmDetailsList = new ArrayList<VMImageDetails>();
        Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);

        GetMethod getMethod = new GetMethod(listVMImageUrl);
        getMethod.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        getMethod.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        getMethod.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));

        int response = client.executeMethod(getMethod);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = getMethod.getResponseBodyAsString();
            log.info(Arrays.toString(getMethod.getRequestHeaders()));
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray jsonArray = (JSONArray) jsonObject.get("imageInfo");

                for (Object aJsonArray : jsonArray) {
                    JSONObject infoObject = (JSONObject) aJsonArray;
                    String name = (String) infoObject.get("imageName");
                    String description = (String) infoObject.get("imageDescription");
                    VMImageDetails vmDetails = new VMImageDetails(name, description);
                    if (!vmDetailsList.contains(vmDetails)) {
                        vmDetailsList.add(vmDetails);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return vmDetailsList;
        } else {
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + listVMImageUrl + " message: \n " + getMethod.getResponseBodyAsString());
        }

    }

    public List<VMStatus> listVMs(Http.Session session) throws GeneralSecurityException, IOException {
        String listVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.showVMUrl();
        Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
        List<VMStatus> vmList = new ArrayList<VMStatus>();
        PostMethod post = new PostMethod(listVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = post.getResponseBodyAsString();
            JSONParser parser = new JSONParser();
            log.info(Arrays.toString(post.getRequestHeaders()));
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray jsonArray = (JSONArray) jsonObject.get("status");

                for (Object aJsonArray : jsonArray) {
                    VMStatus vmStatus = new VMStatus();
                    JSONObject infoObject = (JSONObject) aJsonArray;
                    vmStatus.setVmId((String) infoObject.get("vmid"));
                    vmStatus.setMode((String) infoObject.get("mode"));
                    vmStatus.setState((String) infoObject.get("state"));
                    vmStatus.setVncPort((Long) infoObject.get("vncport"));
                    vmStatus.setSshPort((Long) infoObject.get("sshport"));
                    vmStatus.setPublicIp((String) infoObject.get("publicip"));
                    vmStatus.setVcpu((Long) infoObject.get("vcpus"));
                    vmStatus.setMemory((Long) infoObject.get("memSize"));
                    vmStatus.setVolumeSize((Long) infoObject.get("volumeSize"));
                    vmStatus.setImageName((String) infoObject.get("imageName"));
                    vmStatus.setVmIntialLogingId((String) infoObject.get("vmInitialLoginId"));
                    vmStatus.setVmInitialLogingPassword((String) infoObject.get("vmInitialLoginPassword"));
                    vmList.add(vmStatus);
                }
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        } else {
            this.responseCode = response;
            log.error("Response code " + response + " for " + listVMUrl + " message: \n " + post.getResponseBodyAsString());
            vmList = null;
//            throw new IOException("Response code " + response + " for " + listVMUrl + " message: \n " + post.getResponseBodyAsString());
        }
        return vmList;

    }

    public VMStatus showVM(String vmId, Http.Session session) throws IOException {
        String showVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.showVMUrl();
        VMStatus vmStatus = new VMStatus();
        PostMethod post = new PostMethod(showVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = post.getResponseBodyAsString();
            log.info(Arrays.toString(post.getRequestHeaders()));
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray jsonArray = (JSONArray) jsonObject.get("status");
                log.info(String.valueOf(jsonArray));

                for (Object aJsonArray : jsonArray) {
                    JSONObject infoObject = (JSONObject) aJsonArray;

                    vmStatus.setVmId((String) infoObject.get("vmid"));
                    vmStatus.setMode((String) infoObject.get("mode"));
                    vmStatus.setState((String) infoObject.get("state"));
                    vmStatus.setVncPort((Long) infoObject.get("vncport"));
                    vmStatus.setSshPort((Long) infoObject.get("sshport"));
                    vmStatus.setPublicIp((String) infoObject.get("publicip"));
                    vmStatus.setVcpu((Long) infoObject.get("vcpus"));
                    vmStatus.setMemory((Long) infoObject.get("memSize"));
                    vmStatus.setVolumeSize((Long) infoObject.get("volumeSize"));
                    vmStatus.setImageName((String) infoObject.get("imageName"));
                    vmStatus.setVmIntialLogingId((String) infoObject.get("vmInitialLoginId"));
                    vmStatus.setVmInitialLogingPassword((String) infoObject.get("vmInitialLoginPassword"));
                }

            } catch (ParseException e) {

                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return vmStatus;

        } else {
            this.responseCode = response;
            log.error(post.getResponseBodyAsString());
            throw new IOException("Response code " + response + " for " + showVMUrl + " message: \n " + post.getResponseBodyAsString());
        }

    }

    public void switchVMMode(String vmId, String mode, Http.Session session) throws IOException {
        String switchVMModeUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.switchVMUrl();
        PostMethod post = new PostMethod(switchVMModeUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("vmid", vmId);
        post.setParameter("mode", mode);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            log.info("Switch to " + mode + " mode.");
            log.info(Arrays.toString(post.getRequestHeaders()));
        } else {
            log.error(post.getResponseBodyAsString());
        }
    }

    public void startVM(String vmId, Http.Session session) throws IOException {
        String launchVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.startVMUrl();
        PostMethod post = new PostMethod(launchVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            log.info("Started VM Id: " + vmId);
            log.info(Arrays.toString(post.getRequestHeaders()));
        } else {
            log.error(post.getResponseBodyAsString());
        }
    }

    public void stopVM(String vmId, Http.Session session) throws IOException {
        String stopVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.stopVMUrl();
        PostMethod post = new PostMethod(stopVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            log.info("Stoped VM Id: " + vmId);
            log.info(Arrays.toString(post.getRequestHeaders()));
        } else {
            log.error(post.getResponseBodyAsString());
        }
    }

    public void deleteVM(String vmId,Http.Session session) throws IOException {
        String deleteVMUrl = PlayConfWrapper.sloanWsEndpoint() + PlayConfWrapper.deleteVMUrl();
        PostMethod post = new PostMethod(deleteVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + session.get(PortalConstants.SESSION_TOKEN));
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", session.get(PortalConstants.SESSION_USERNAME));
        post.addRequestHeader("htrc-remote-user-email", session.get(PortalConstants.SESSION_EMAIL));
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            log.info("Deleted VM Id: " + vmId);
            log.info(Arrays.toString(post.getRequestHeaders()));
        } else {
            log.error(post.getResponseBodyAsString());
        }

    }

    public List<String>  getVolumesInHtrc(JSONObject volumes) throws IOException
    {
        //String volumesUrl = "http://localhost:8087/SGA_REST_DataIngestor/sga/dataingestor"  ;
        String volumesUrl = PlayConfWrapper.getVolumesUrl();
        StringRequestEntity entity = new StringRequestEntity(volumes.toJSONString(),"application/json","UTF-8");
        PostMethod post = new PostMethod(volumesUrl);
        post.setRequestEntity(entity);
        post.addRequestHeader("Content-Type", "application/json");

        NameValuePair levelparam = new NameValuePair("level", URIUtil.encodeQuery("3"));
        NameValuePair[] params = new NameValuePair[] {levelparam};
        post.setQueryString(params);
        int response = client.executeMethod(post);
        if(response ==200)
        {
            String jsonStr = post.getResponseBodyAsString();
            log.info(Arrays.toString(post.getRequestHeaders()));
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                return (List<String>)jsonObject.get("volumeIdsList");
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            this.responseCode = response;
            log.error(post.getResponseBodyAsString());
            throw new IOException("Response code " + response + " for " + volumesUrl + " message: \n " + post.getResponseBodyAsString());
        }
        return null;
    }


}
