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
import models.User;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import play.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class HTRCAgentClient {
    private static Logger.ALogger log = play.Logger.of("application");
    private HttpClient client = new HttpClient();
    private XMLInputFactory factory = XMLInputFactory.newInstance();
    private JobDetailsBean jobSubmitResponse = null;

    public int responseCode;

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

    public JobDetailsBean submitJob(JobSubmitBean jobSubmitBean, User loggedInUser){
        PutMethod putMethod = null;
        try{
            String submitJobUrl = PlayConfWrapper.agentEndpoint() + PlayConfWrapper.jobSubmitURL()+ "/" + jobSubmitBean.getAlgorithmName();
            putMethod = new PutMethod(submitJobUrl);

            StringRequestEntity requestEntity = new StringRequestEntity(jobSubmitBean.toXML());
            if (log.isDebugEnabled()) {
                log.debug("submitURL " + submitJobUrl);
                log.debug("request body " + jobSubmitBean.toXML());
            }
            putMethod.setRequestEntity(requestEntity);
            putMethod.setRequestHeader("Authorization", "Bearer " + loggedInUser.accessToken);
            putMethod.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");


            int respondecode = client.executeMethod(putMethod);
            System.out.println(respondecode);
            this.responseCode = respondecode;
            if (respondecode == 200) {
                jobSubmitResponse = parseJobSubmit(putMethod.getResponseBodyAsStream());
                System.out.println(jobSubmitResponse.getJobStatus());
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
}
