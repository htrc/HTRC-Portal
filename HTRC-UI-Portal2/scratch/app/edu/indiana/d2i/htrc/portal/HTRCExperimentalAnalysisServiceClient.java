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
import models.User;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HTRCExperimentalAnalysisServiceClient {

    private HttpClient client = new HttpClient();

    public int responseCode;

    public String createVM(String imageName, String loginuUerName, String loginPassword, String memory, String vcpu, User loggedIn) throws IOException {
        String createVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/createvm";

        PostMethod post = new PostMethod(createVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("imagename", imageName);
        post.setParameter("loginusername", loginuUerName);
        post.setParameter("loginpassword", loginPassword);
        post.setParameter("memory", memory);
        post.setParameter("vcpu", vcpu);
//        post.setRequestBody((org.apache.commons.httpclient.NameValuePair[]) urlParameters);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = post.getResponseBodyAsString();
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                return ((String) jsonObject.get("vmid"));

            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }else{
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + createVMUrl + " message: \n " + post.getResponseBodyAsString());
        }
         return null;

    }

    public List<VMImageDetails> listVMImages(User loggedIn) throws IOException, GeneralSecurityException {
        String listVMImageUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/listimage";
        List<VMImageDetails> vmDetailsList = new ArrayList<VMImageDetails>();
        Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);

        GetMethod getMethod = new GetMethod(listVMImageUrl);
        getMethod.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        getMethod.addRequestHeader("htrc-remote-user", loggedIn.userId);

        int response = client.executeMethod(getMethod);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = getMethod.getResponseBodyAsString();
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
                    if(!vmDetailsList.contains(vmDetails)){
                        vmDetailsList.add(vmDetails);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return vmDetailsList;
//            System.out.println(jsonStr);
        } else {
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + listVMImageUrl + " message: \n " + getMethod.getResponseBodyAsString());
        }

    }

    public void listVMs(User loggedIn) throws GeneralSecurityException, IOException {
        String listVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/listvm";
        Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);

        PostMethod post = new PostMethod(listVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            System.out.println("LIST VM ACTION");
        } else {
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + listVMUrl + " message: \n " + post.getResponseBodyAsString());
        }

    }

    public VMStatus showVM(String vmId, User loggedIn) throws IOException {
        String showVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/show";
        VMStatus vmStatus = new VMStatus();
        PostMethod post = new PostMethod(showVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            String jsonStr = post.getResponseBodyAsString();
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(jsonStr);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray jsonArray = (JSONArray) jsonObject.get("status");
                System.out.println(jsonArray);

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
                    }

            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return vmStatus;
//            System.out.println(jsonStr);

        }else{
            this.responseCode = response;
            throw new IOException("Response code " + response + " for " + showVMUrl + " message: \n " + post.getResponseBodyAsString());
        }

    }

    public void switchVMMode(String vmId, String mode, User loggedIn) throws IOException {
        String switchVMModeUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/switchvm";
        PostMethod post = new PostMethod(switchVMModeUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("vmid", vmId);
        post.setParameter("mode", mode);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            System.out.println("Deleted VM Id: ");
        }
    }

    public void startVM(String vmId, User loggedIn) throws IOException {
        String launchVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/launchvm";
        PostMethod post = new PostMethod(launchVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            System.out.println("Deleted VM Id: ");
        }
    }

    public void stopVM(String vmId, User loggedIn) throws IOException {
        String stopVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/stopvm";
        PostMethod post = new PostMethod(stopVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            System.out.println("Deleted VM Id: ");
        }
    }

    public void deleteVM(String vmId, User loggedIn) throws IOException {
        String deleteVMUrl = "https://thatchpalm.pti.indiana.edu:8080/sloan-ws-1.0-SNAPSHOT/deletevm";
        PostMethod post = new PostMethod(deleteVMUrl);
        post.addRequestHeader("Authorization", "Bearer " + loggedIn.accessToken);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addRequestHeader("htrc-remote-user", loggedIn.userId);
        post.setParameter("vmid", vmId);

        int response = client.executeMethod(post);
        this.responseCode = response;
        if (response == 200) {
            System.out.println("Deleted VM Id: ");
        }

    }

}
