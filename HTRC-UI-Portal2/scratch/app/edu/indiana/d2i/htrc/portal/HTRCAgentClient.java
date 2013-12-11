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

import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import edu.indiana.d2i.htrc.portal.bean.JobSubmitBean;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import play.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HTRCAgentClient {
    private String accessToken = null;
    private static Logger.ALogger log = play.Logger.of("application");
    private HttpClient client = new HttpClient();
    private XMLInputFactory factory = XMLInputFactory.newInstance();


    public int responseCode;

    public HTRCAgentClient(String accessToken){
        this.accessToken = accessToken;
    }

    private Map<String, JobDetailsBean> parseJobDetailBeans(InputStream stream) throws XMLStreamException{
        Map<String, JobDetailsBean> res = new TreeMap<String, JobDetailsBean>();
        XMLStreamReader parser = factory.createXMLStreamReader(stream);
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.hasName()) {
                    if (parser.getLocalName().equals("job_status")) {
                        // one job status
                        JobDetailsBean detail = new JobDetailsBean();
                        Map<String, String> jobParams = new HashMap<String, String>();
                        Map<String, String> results = new HashMap<String, String>();
                        int innerEvent = parser.next();
                        while (true) {
                            if (innerEvent == XMLStreamConstants.END_ELEMENT &&
                                    parser.getLocalName().equals("job_status")) {
                                break;
                            }

                            if (innerEvent == XMLStreamConstants.START_ELEMENT &&
                                    parser.hasName()) {
                                // single tag
                                if (parser.getLocalName().equals("job_name")) {
                                    detail.setJobTitle(parser.getElementText());
                                } else if (parser.getLocalName().equals("user")) {
                                    detail.setUserName(parser.getElementText());
                                } else if (parser.getLocalName().equals("algorithm")) {
                                    detail.setAlgorithmName(parser.getElementText());
                                } else if (parser.getLocalName().equals("job_id")) {
                                    detail.setJobId(parser.getElementText());
                                } else if (parser.getLocalName().equals("date")) {
                                    detail.setLastUpdatedDate(parser.getElementText());
                                }

                                // parameters
                                if (parser.hasName() &&
                                        parser.getLocalName().equals(JobDetailsBean.ONEPARAM)) {
                                    String name, value;
                                    name = value = "";
                                    for (int i = 0; i < 3; i++) {
                                        if (parser.getAttributeName(i).toString().equals("name"))
                                            name = parser.getAttributeValue(i);
                                        if (parser.getAttributeName(i).toString().equals("value"))
                                            value = parser.getAttributeValue(i);
                                    }
                                    jobParams.put(name, value);
                                }

                                // status
                                if (parser.hasName() &&
                                        parser.getLocalName().equals(JobDetailsBean.STATUS)) {
                                    String status = parser.getAttributeValue(0);
                                    detail.setJobStatus(status);
                                }

                                // results
                                if (parser.hasName() &&
                                        parser.getLocalName().equals(JobDetailsBean.RESULT)) {
                                    String name = parser.getAttributeValue(0);
                                    String value = parser.getElementText();
                                    results.put(name, value);
                                }

                                // message
                                if (parser.hasName() &&
                                        parser.getLocalName().equals(JobDetailsBean.MESSAGE)) {
                                    detail.setMessage(parser.getElementText());
                                }

                                // saved or unsaved
                                if (parser.hasName() &&
                                        parser.getLocalName().equals(JobDetailsBean.SAVEDORNOT)) {
                                    detail.setJobSavedStr(parser.getElementText());
                                }
                            }
                            innerEvent = parser.next();
                        }
                        detail.setJobParams(jobParams);
                        detail.setResults(results);
                        res.put(detail.getJobId(), detail);
                    }
                }
            }
        }
        return res;
    }

    private JobDetailsBean parseJobSubmit(InputStream stream) throws XMLStreamException {
        JobDetailsBean res = new JobDetailsBean();
        XMLStreamReader parser = factory.createXMLStreamReader(stream);
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.hasName()) {
                    // only parse several attributes
                    if (parser.getLocalName().equals(JobDetailsBean.STATUS)) {
                        res.setJobStatus(parser.getAttributeValue(0));
                    } else if (parser.getLocalName().equals(JobDetailsBean.JOBID)) {
                        res.setJobId(parser.getElementText());
                    } else if (parser.getLocalName().equals(JobDetailsBean.JOBNAME)) {
                        res.setJobTitle(parser.getElementText());
                    } else if (parser.getLocalName().equals(JobDetailsBean.DATE)) {
                        res.setLastUpdatedDate(parser.getElementText());
                    } else if (parser.getLocalName().equals(JobDetailsBean.USER)) {
                        res.setUserName(parser.getElementText());
                    }
                }
            }
        }
        return res;
    }

    private boolean parseJobDeleteResponse(InputStream stream) throws XMLStreamException {
        XMLStreamReader parser = factory.createXMLStreamReader(stream);
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.hasName()) {
                    String localName = parser.getLocalName();
                    if (localName.equals("success")) return true;
                    else {
                        log.error(parser.getElementText());
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public JobDetailsBean submitJob(JobSubmitBean jobSubmitBean){
        PutMethod putMethod = null;
        JobDetailsBean jobSubmitResponse = null;
        try{
            String submitJobUrl = PlayConfWrapper.agentEndpoint() + PlayConfWrapper.jobSubmitURL()+ "/" + jobSubmitBean.getAlgorithmName();
            putMethod = new PutMethod(submitJobUrl);

            StringRequestEntity requestEntity = new StringRequestEntity(jobSubmitBean.toXML());
            if (log.isDebugEnabled()) {
                log.debug("submitURL " + submitJobUrl);
                log.debug("request body " + jobSubmitBean.toXML());
            }
            putMethod.setRequestEntity(requestEntity);
            putMethod.setRequestHeader("Authorization", "Bearer " + accessToken);
            putMethod.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");


            int respondecode = client.executeMethod(putMethod);
            System.out.println(respondecode);
            this.responseCode = respondecode;
            if (respondecode == 200) {
                jobSubmitResponse = parseJobSubmit(putMethod.getResponseBodyAsStream());
                System.out.println(putMethod.getResponseBodyAsString());
            }else{
                this.responseCode = respondecode;
                throw new IOException("Response code " + respondecode + " for " + submitJobUrl + " message: \n " + putMethod.getResponseBodyAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return jobSubmitResponse;

    }

    public boolean deleteJobs(List<String> jobIds) {
        boolean res = true;
        for (String id : jobIds) {
            DeleteMethod deleteJobId = null;
            try {
                String jobdeleteURL = String.format(PlayConfWrapper.agentEndpoint()+PlayConfWrapper.jobDeleteURLTemplate(), id);
                deleteJobId = new DeleteMethod(jobdeleteURL);
                deleteJobId.setRequestHeader("Authorization", "Bearer " + accessToken);

                client.getHttpConnectionManager().getParams().setConnectionTimeout(PlayConfWrapper.agentConnectTimeout());
                client.getHttpConnectionManager().getParams().setSoTimeout(PlayConfWrapper.agentWaitTimeout());

                int response = client.executeMethod(deleteJobId);
                this.responseCode = response;
                if (response == 200) {
                    boolean success = parseJobDeleteResponse(deleteJobId.getResponseBodyAsStream());
                    if (!success) {
                        log.error("Error occurs while deleting job " + id);
                        res = success;
                    }
                } else{
                    log.error(String.format("Unable to delete job %s from agent. Response code %d",
                            response));
                    res = false;
                }
            } catch (Exception e) {
                log.error(String.valueOf(e));
            } finally {
                if (deleteJobId != null) {
                    deleteJobId.releaseConnection();
                    deleteJobId = null;
                }
            }
        }
        return res;
    }

    public boolean saveJobs(List<String> jobIds) {
        boolean res = true;
        for (String id : jobIds) {
            PutMethod saveJobId = null;
            try {
                String jobsaveURL = String.format(PlayConfWrapper.agentEndpoint()+PlayConfWrapper.jobSaveURLTemplate(), id);
                saveJobId = new PutMethod(jobsaveURL);
                saveJobId.setRequestHeader("Authorization", "Bearer "+ accessToken);
                log.info("Save job url: " + jobsaveURL);

                client.getHttpConnectionManager().getParams().setConnectionTimeout(PlayConfWrapper.agentConnectTimeout());
                client.getHttpConnectionManager().getParams().setSoTimeout(PlayConfWrapper.agentWaitTimeout());

                int response = client.executeMethod(saveJobId);
                this.responseCode = response;
                if (response == 200) {
                    System.out.println("Saved Job ID :  " + id);

                }else {
                    log.error(String.format("Unable to save job %s from agent. Response code %d",
                            response));
                    res = false;
                }
            } catch (Exception e) {
                log.error(String.valueOf(e));
            } finally {
                if (saveJobId != null) {
                    saveJobId.releaseConnection();
                    saveJobId = null;
                }
            }
        }
        return res;
    }

    public Map<String, JobDetailsBean> getAllJobsDetails() {
        GetMethod getJobDetailsList = null;
        Map<String, JobDetailsBean> jobdetailBeans = new TreeMap<String, JobDetailsBean>();
        try {
            String jobDetailsURL = PlayConfWrapper.agentEndpoint() + PlayConfWrapper.jobdetailsURL();
            getJobDetailsList = new GetMethod(jobDetailsURL);
            getJobDetailsList.setRequestHeader("Authorization", "Bearer "
                    + accessToken);

            client.getHttpConnectionManager().getParams().setConnectionTimeout(
                    PlayConfWrapper.agentConnectTimeout());
            client.getHttpConnectionManager().getParams().setSoTimeout(
                    PlayConfWrapper.agentWaitTimeout());

            int response = client.executeMethod(getJobDetailsList);
            this.responseCode = response;
            if (response == 200) {
                jobdetailBeans = parseJobDetailBeans(getJobDetailsList
                        .getResponseBodyAsStream());
            } else {
                log.error("Unable to get job list from agent. url " + jobDetailsURL +
                        "Response code " + response);
                jobdetailBeans = null;
            }
        } catch (Exception e) {
            log.error(String.valueOf(e));
            jobdetailBeans = null;
        } finally {
            if (getJobDetailsList != null) {
                getJobDetailsList.releaseConnection();
                getJobDetailsList = null;
            }
        }

        return jobdetailBeans;
    }

    public Map<String, JobDetailsBean> getJobsDetails(List<String> jobIds) {
        Map<String, JobDetailsBean> res = new HashMap<String, JobDetailsBean>();
        Map<String, JobDetailsBean> jobsDetails = getAllJobsDetails();
        if (jobsDetails == null) return res; // ugly!

        for (String id : jobIds) {
            if (jobsDetails.containsKey(id)) {
                res.put(id, jobsDetails.get(id));
            } else {
                log.error(String.format("Cannot find job Id %s from agent", id));
            }
        }
        return res;
    }
}
